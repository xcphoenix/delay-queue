package top.xcphoenix.delayqueue.monitor.init.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import top.xcphoenix.delayqueue.constant.RedisDataStruct;
import top.xcphoenix.delayqueue.monitor.global.GroupMonitor;
import top.xcphoenix.delayqueue.monitor.global.TopicMonitor;
import top.xcphoenix.delayqueue.monitor.init.InitScanInterface;

import java.util.Arrays;
import java.util.Set;

/**
 * 扫描 TOPIC
 *
 * @author      xuanc
 * @date        2020/2/6 下午10:31
 * @version     1.0
 */
@Slf4j
public class TopicInitScanImpl implements InitScanInterface {

    private GroupMonitor groupMonitor;
    private TopicMonitor topicMonitor;
    private RedisTemplate redisTemplate;

    public TopicInitScanImpl(GroupMonitor groupTopicMonitor, TopicMonitor topicMonitor, RedisTemplate redisTemplate) {
        this.groupMonitor = groupTopicMonitor;
        this.topicMonitor = topicMonitor;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void run(String... args) {
        log.info("Topic init...");

        String redisKey = RedisDataStruct.PROJECT_MONITOR_KEY;
        Set<String> groups = groupMonitor.getCurrGroups();

        for (String group : groups) {
            // get topics
            String topics = (String) redisTemplate.opsForHash().get(redisKey, group);
            String[] topicArr = topics == null ? new String[0]
                    : topics.split(RedisDataStruct.MONITOR_TOPIC_DELIMITER);

            log.info("Init group: " + group + ", topics: " + Arrays.toString(topicArr));

            // init
            topicMonitor.init(group);
            for (String topic : topicArr) {
                topicMonitor.pushNewTopic(group, topic);
            }
        }

    }

}
