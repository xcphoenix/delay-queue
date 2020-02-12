package top.xcphoenix.delayqueue.monitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import top.xcphoenix.delayqueue.constant.RedisDataStruct;
import top.xcphoenix.delayqueue.exception.RegisterException;
import top.xcphoenix.delayqueue.monitor.global.GroupMonitor;
import top.xcphoenix.delayqueue.monitor.global.TopicMonitor;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 服务注册与取消
 *
 * @author      xuanc
 * @date        2020/2/11 下午4:07
 * @version     1.0
 */
@Slf4j
@Component
public class ServiceRegister {

    private GroupMonitor groupMonitor;
    private TopicMonitor topicMonitor;
    private StringRedisTemplate redisTemplate;

    private static Pattern validatePattern = Pattern.compile("^[a-zA-Z]+[0-9a-zA-Z-]*$");
    private String summaryKey = RedisDataStruct.PROJECT_MONITOR_KEY;

    public ServiceRegister(GroupMonitor groupMonitor, TopicMonitor topicMonitor,
                           StringRedisTemplate redisTemplate) {
        this.groupMonitor = groupMonitor;
        this.topicMonitor = topicMonitor;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 注册组，若组存在则返回 false
     *
     * @param group 要注册的组
     * @return 是否注册成功
     */
    public synchronized boolean registerGroup(String group) throws RegisterException {
        log.info("register group: " + group);
        if (!validatePattern.matcher(group).matches()) {
            throw new RegisterException("group name is invalid");
        }
        if (groupMonitor.isGroupExist(group)) {
            return false;
        }
        redisTemplate.opsForHash().put(summaryKey, group, "");
        groupMonitor.pushNewGroup(group);
        topicMonitor.init(group);

        log.info("register success");
        return true;
    }

    /**
     * 注册 topic，topic 存在返回 false
     *
     * @param group topic 所在的组
     * @param topic 要注册的 topic
     * @return 是否注册成功
     */
    public synchronized boolean registerTopic(String group, String topic) throws RegisterException {
        log.info("register group: " + group + ", topic: " + topic);

        if (!groupMonitor.isGroupExist(group)) {
            throw new RegisterException("group: " + group + " registered");
        }
        if (!validatePattern.matcher(topic).matches()) {
            throw new RegisterException("topic name is invalid");
        }
        if (topicMonitor.isTopicExist(group, topic)) {
            return false;
        }

        String topics = (String) redisTemplate.opsForHash().get(summaryKey, group);
        if (topics == null || "".equals(topics.trim())) {
            topics = topic;
        } else {
            topics = topics + RedisDataStruct.MONITOR_TOPIC_DELIMITER + topic;
        }
        redisTemplate.opsForHash().put(summaryKey, group, topics);
        topicMonitor.pushNewTopic(group, topic);

        log.info("register success");
        return true;
    }

    /**
     * 组是否被注册
     *
     * @param group 组
     * @return 注册情况
     */
    public boolean isGroupRegistered(String group) {
        return groupMonitor.isGroupExist(group);
    }

    /**
     * topic 是否被注册
     *
     * @param group topic 所在的组
     * @param topic topic
     * @return 注册情况
     */
    public boolean isTopicRegistered(String group, String topic) {
        return topicMonitor.isTopicExist(group, topic);
    }

    /**
     * 结束注册
     *
     * @param group 要结束的组
     */
    public synchronized void cancelGroup(String group) {
        if (!isGroupRegistered(group)) {
            log.warn("group: " + group + " is not registered");
            return;
        }
        log.info("cancel topics...");
        topicMonitor.getCurrTopics(group).forEach(s -> cancelTopic(group, s));
        groupMonitor.remOldGroup(group);
        redisTemplate.opsForHash().delete(summaryKey, group);
        log.info("cancel group: " + group + " done");
    }

    /**
     * 取消 topic
     *
     * @param group topic 所在的组
     * @param topic topic
     */
    public synchronized void cancelTopic(String group, String topic) {
        if (!isTopicRegistered(group, topic)) {
            log.warn("topic: " + topic + " in group: " + group + " is not registered");
            return;
        }
        log.info("cancel topic: " + topic + " in group: " + group);

        topicMonitor.remOldTopic(group, topic);
        String topics = (String) redisTemplate.opsForHash().get(summaryKey, group);
        assert topics != null;
        String[] topicArr = topics.split(RedisDataStruct.MONITOR_TOPIC_DELIMITER);
        for (int i = 0; i < topicArr.length; i++) {
            if (topicArr[i].equals(topic)) {
                topicArr[i] = null;
                break;
            }
        }
        topics = Arrays.stream(topicArr).filter(Objects::nonNull)
                .collect(Collectors.joining(RedisDataStruct.MONITOR_TOPIC_DELIMITER));
        redisTemplate.opsForHash().put(summaryKey, group, topics);

        log.info("cancel topic: " + topic + " done");
    }


}
