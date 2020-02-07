package top.xcphoenix.delayqueue.init.impl;

import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import top.xcphoenix.delayqueue.constant.RedisDataStruct;
import top.xcphoenix.delayqueue.init.InitScanInterface;
import top.xcphoenix.delayqueue.monitor.global.GroupTopicMonitor;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 扫描 TOPIC
 *
 * @author      xuanc
 * @date        2020/2/6 下午10:31
 * @version     1.0
 */
@Component("scan-topic")
@Order(2)
public class TopicInitScanImpl implements InitScanInterface {

    private GroupTopicMonitor groupTopicMonitor;
    private StringRedisTemplate redisTemplate;

    public TopicInitScanImpl(GroupTopicMonitor groupTopicMonitor, StringRedisTemplate redisTemplate) {
        this.groupTopicMonitor = groupTopicMonitor;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        String redisKey = RedisDataStruct.PROJECT_MONITOR_KEY;
        Set<String> groups = groupTopicMonitor.getCurrGroups();

        for (String group : groups) {
            String topics = (String) redisTemplate.opsForHash().get(redisKey, group);
            String[] topicArr = topics == null ? new String[0]
                    : topics.split(RedisDataStruct.MONITOR_TOPIC_DELIMITER);
            Set<String> topicSet = new HashSet<>(Arrays.asList(topicArr));
            Set<String> atomicSet = Collections.synchronizedSet(topicSet);
            // TODO 添加topic，创建 topic 监听线程 +

        }

    }

}
