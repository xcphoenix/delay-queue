package top.xcphoenix.delayqueue.monitor.global;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import top.xcphoenix.delayqueue.service.threads.PushMonitorThread;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author      xuanc
 * @date        2020/2/5 下午8:10
 * @version     1.0
 */
@Slf4j
public class GroupMonitor {

    /**
     * 线程池
     */
    private ThreadPoolExecutor pushExecutor;

    /**
     * 组与线程的映射
     * thread safe
     */
    private Map<String, PushMonitorThread> groupPushThread = new ConcurrentHashMap<>();

    public GroupMonitor(@Qualifier("pushThreadPool") ThreadPoolExecutor pushExecutor) {
        this.pushExecutor = pushExecutor;
    }

    /**
     * Group 是否存在
     *
     * @param group 要判断的组名
     * @return 存在返回 true，否则返回 false
     */
    public boolean isGroupExist(String group) {
        return groupPushThread.containsKey(group);
    }

    /**
     * 获取当前的群组列表
     *
     * @return group list
     */
    public Set<String> getCurrGroups() {
        return groupPushThread.keySet();
    }

    /**
     * 添加新的组，若组存在则忽略，若不存在则创建新的线程监视加入的 group
     *
     * @param group 新的组
     */
    public void pushNewGroup(String group) {
        log.info("Push new group: " + group);

        if (groupPushThread.containsKey(group)) {
            log.warn("Group: " + group + " exists");
            return;
        }

        PushMonitorThread thread = new PushMonitorThread(group);
        // 执行线程
        pushExecutor.execute(thread);
        groupPushThread.put(group, thread);

        log.info("Create new thread listen group => " + group);
    }

    /**
     * 当 group 全部处理完毕（taskKey、waitingKey 中没有对应数据）后，移除组以及监视线程
     *
     * @param group 要移除的 group
     */
    public void remOldGroup(String group) {
        PushMonitorThread thread = groupPushThread.get(group);
        groupPushThread.remove(group);
        // 中断线程
        thread.interrupt();
    }

    /**
     * 重启后定时从 redis 中获取 group 列表同步信息，防止特殊情况下数据丢失
     *
     * @param currGroups 当前
     */
    public void syncGroup(Set<String> currGroups) {
        Set<String> oriGroups = groupPushThread.keySet();
        Collection<String> oldGroups = CollectionUtils.subtract(oriGroups, currGroups);
        Collection<String> newGroups = CollectionUtils.subtract(currGroups, oriGroups);

        oldGroups.forEach(this::remOldGroup);
        newGroups.forEach(this::pushNewGroup);
    }

    /**
     * 与 push 线程的 nextTime 比较，若小于 nextTime 则更新值，并唤醒线程
     *
     * @param group 所在的组
     * @param time 要比较/更新的时间戳
     */
    public void updateAndNotify(String group, Timestamp time) {
        if (!isGroupExist(group)) {
            throw new RuntimeException("group not exists");
        }
        PushMonitorThread thread = groupPushThread.get(group);
        if (thread.getNextTime().get() > time.getTime()) {
            synchronized (thread.getNextTime()) {
                log.info("update next time in group: " + group + ", notify listen thread");

                thread.setNextTime(time.getTime());
                thread.getNextTime().notify();
            }
        }
    }

}
