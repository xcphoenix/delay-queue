package top.xcphoenix.delayqueue.monitor.global;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import top.xcphoenix.delayqueue.service.threads.ConsumeMonitorThread;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author      xuanc
 * @date        2020/2/8 上午10:06
 * @version     1.0
 */
@Slf4j
public class TopicMonitor {

    private GroupMonitor groupMonitor;

    /**
     * 消费线程池
     */
    private ThreadPoolExecutor consumeExecutor;

    /**
     * 组与topic的映射
     * set 也要注意线程安全诶..
     */
    private Map<String, Topics> groupTopic = new ConcurrentHashMap<>();

    public TopicMonitor(GroupMonitor groupMonitor,
                        @Qualifier("consumeThreadPool") ThreadPoolExecutor consumeExecutor) {
        this.groupMonitor = groupMonitor;
        this.consumeExecutor = consumeExecutor;
    }

    /**
     * 初始化，添加前必须初始化
     *
     * @param group 组
     */
    public void init(String group) {
        groupTopic.put(group, new Topics());
    }

    /**
     * topic 是否存在
     *
     * @param group topic 所在的组
     * @param topic 要检查的 topic
     * @return 是否存在
     */
    public boolean isTopicExist(String group, String topic) {
        if (!groupMonitor.isGroupExist(group)) {
            return false;
        }
        return groupTopic.get(group).isExist(topic);
    }

    /**
     * 获取组内的 topic
     *
     * @param group 组
     * @return 组内的 topic 数据
     */
    public Set<String> getCurrTopics(String group) {
        return groupTopic.get(group).getTopics();
    }

    /**
     * 添加新的 topic
     *
     * @param group topic 所在的组
     * @param topic 要添加的 topic
     */
    public void pushNewTopic(String group, String topic) {
        if (!groupMonitor.isGroupExist(group)) {
            log.warn("invalid group: " + group + ", tried add topic: " + topic);
            return;
        }
        if (isTopicExist(group, topic)) {
            log.warn("topic: " + topic + " exists in group: " + group);
            return;
        }

        // start monitor thread
        ConsumeMonitorThread thread = new ConsumeMonitorThread(group, topic);
        consumeExecutor.execute(thread);
        groupTopic.get(group).setTopic(topic, thread);

        log.info("create new thread listen topic: " + topic + " in group: " + group);
    }

    /**
     * 移除 topic
     *
     * @param group topic 所在组
     * @param topic topic
     */
    public void remOldTopic(String group, String topic) {
        log.info("remove group: " + group + ", topic: " + topic);
        if (!isTopicExist(group, topic)) {
            log.warn("remove error, group: " + group + ", topic: " + topic + " not exists");
            return;
        }
        ConsumeMonitorThread thread = groupTopic.get(group).getThread(topic);
        thread.interrupt();
        groupTopic.get(group).removeTopic(topic);
        log.info("remove success");
    }

    private static class Topics {

        Map<String, ConsumeMonitorThread> topicConsumeThread = new ConcurrentHashMap<>();

        boolean isExist(String topic) {
            return topicConsumeThread.containsKey(topic);
        }

        ConsumeMonitorThread getThread(String topic) {
            return topicConsumeThread.get(topic);
        }

        void setTopic(String topic, ConsumeMonitorThread thread) {
            topicConsumeThread.put(topic, thread);
        }

        Set<String> getTopics() {
            return topicConsumeThread.keySet();
        }

        void removeTopic(String topic) {
            topicConsumeThread.remove(topic);
        }

    }

}