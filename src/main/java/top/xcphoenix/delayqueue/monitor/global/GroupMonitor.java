package top.xcphoenix.delayqueue.monitor.global;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import top.xcphoenix.delayqueue.threads.PushTaskThread;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author      xuanc
 * @date        2020/2/5 下午8:10
 * @version     1.0
 */
@Slf4j
@Component
public class GroupMonitor {

    private long stopTimeout = 50;

    /**
     * 线程池
     */
    private ThreadPoolExecutor pushExecutor;

    /**
     * 组与线程的映射
     *
     * thread safe
     */
    private Map<String, Future<Void>> groupRunnable = new ConcurrentHashMap<>();

    public GroupMonitor(@Qualifier("pushThreadPool") ThreadPoolExecutor pushExecutor) {
        this.pushExecutor = pushExecutor;
    }

    /**
     * Group 是否存在
     *
     * @param group 要判断的组名
     * @return 存在返回 true，否则返回 false
     */
    public boolean isExist(String group) {
        return groupRunnable.containsKey(group);
    }

    /**
     * 获取当前的群组列表
     *
     * @return group list
     */
    public Set<String> getCurrGroups() {
        return groupRunnable.keySet();
    }

    /**
     * 添加新的组，若组存在则忽略，若不存在则创建新的线程监视加入的 group
     *
     * @param group 新的组
     */
    public void pushNewGroup(String group) {
        log.info("Monitor:: push new group => " + group);

        if (groupRunnable.containsKey(group)) {
            return;
        }

        PushTaskThread thread = new PushTaskThread(group);
        // 执行线程
        Future<Void> future = pushExecutor.submit(thread, null);
        groupRunnable.put(group, future);

        log.info("Monitor:: create new thread listen group => " + group);
    }

    /**
     * 当 group 全部处理完毕（taskKey、waitingKey 中没有对应数据）后，移除组以及监视线程
     *
     * @param group 要移除的 group
     */
    public void remOldGroup(String group) {
        Future<Void> future = groupRunnable.get(group);
        groupRunnable.remove(group);
        future.cancel(true);
    }

    /**
     * 重启后定时从 redis 中获取 group 列表同步信息，防止特殊情况下数据丢失
     *
     * @param currGroups 当前
     */
    public void syncGroup(Set<String> currGroups) {
        Set<String> oriGroups = groupRunnable.keySet();
        Collection<String> oldGroups = CollectionUtils.subtract(oriGroups, currGroups);
        Collection<String> newGroups = CollectionUtils.subtract(currGroups, oriGroups);

        oldGroups.forEach(this::remOldGroup);
        newGroups.forEach(this::pushNewGroup);
    }

}
