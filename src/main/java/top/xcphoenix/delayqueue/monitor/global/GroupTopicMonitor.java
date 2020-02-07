package top.xcphoenix.delayqueue.monitor.global;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import top.xcphoenix.delayqueue.threads.WaitMonitorThread;

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
public class GroupTopicMonitor {

    /**
     * 线程池
     */
    private ThreadPoolExecutor pushExecutor;

    /**
     * 组与线程的映射
     * thread safe
     */
    private Map<String, Future<Void>> groupRunnable = new ConcurrentHashMap<>();

    /**
     * 组与topic的映射
     * set 也要注意线程安全诶..
     */
    private Map<String, Set<String>> groupTopic = new ConcurrentHashMap<>();

    public GroupTopicMonitor(@Qualifier("pushThreadPool") ThreadPoolExecutor pushExecutor) {
        this.pushExecutor = pushExecutor;
    }

    /*
     * group method
     */

    /**
     * Group 是否存在
     *
     * @param group 要判断的组名
     * @return 存在返回 true，否则返回 false
     */
    public boolean isGroupExist(String group) {
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

        WaitMonitorThread thread = new WaitMonitorThread(group);
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

    /*
     * topic method
     */

    /**
     * topic 是否存在
     *
     * @param group topic 所在的组
     * @param topic 要检查的 topic
     * @return 是否存在
     */
    public boolean isTopicExist(String group, String topic) {
        if (!isGroupExist(group)) {
            return false;
        }
        return groupTopic.get(group).contains(topic);
    }

    /**
     * 获取组内的 topic
     *
     * @param group 组
     * @return 组内的 topic 数据
     */
    public Set<String> getCurrTopics(String group) {
        return groupTopic.get(group);
    }

}
