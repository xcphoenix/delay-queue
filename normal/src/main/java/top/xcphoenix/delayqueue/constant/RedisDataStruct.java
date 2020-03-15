package top.xcphoenix.delayqueue.constant;

import org.apache.commons.lang3.StringUtils;
import top.xcphoenix.delayqueue.pojo.BaseTask;

import java.util.Arrays;
import java.util.List;

/**
 * @author      xuanc
 * @date        2020/2/2 下午5:34
 * @version     1.0
 */ 
public class RedisDataStruct {

    /**
     * key prefix
     */
    private static final String DETAIL = "DETAIL";
    private static final String WAITING = "WAITING";
    private static final String CONSUMING = "CONSUMING";

    /**
     * [Hash] 监视 group、topic 数据
     */
    public static final String PROJECT_MONITOR_KEY = "MONITOR:SUMMARY";
    public static final String MONITOR_TOPIC_DELIMITER = ",";

    private RedisDataStruct() {}

    /**
     * 获取任务所在详情列表中的 key
     * @param task 任务信息
     * @return key
     */
    public static String taskKey(BaseTask task) {
        return join(wrap(task.getGroup()), DETAIL);
    }

    /**
     * 获取任务所在的等待的有序集合的 key
     * @param task 任务信息
     * @return key
     */
    public static String waitingKey(BaseTask task) {
        return join(wrap(task.getGroup()), WAITING);
    }

    /**
     * 获取任务所对应的消费队列key
     *
     * @param task 任务信息
     * @throws IllegalArgumentException topic is null
     * @return 消费队列 key
     */
    public static String consumingKey(BaseTask task) {
        if (task.getTopic() == null) {
            throw new IllegalArgumentException("topic can't be null");
        }
        return join(wrap(task.getGroup()), CONSUMING, task.getTopic());
    }

    /**
     * 获取任务所在的待消费队列中的 key 前缀
     * @param task 任务信息
     * @return key 前缀（无topic信息）
     */
    public static String consumingKeyPrefix(BaseTask task) {
        return join(wrap(task.getGroup()), CONSUMING);
    }

    /**
     * 获取任务所在的键
     * @param task 任务信息
     * @return 任务在 hash、zset、list 中的键
     */
    public static List<String> getRedisKeys(BaseTask task) {
        return Arrays.asList(taskKey(task), waitingKey(task), consumingKeyPrefix(task));
    }

    /**
     * for field or value
     */

    public static String taskIdField(BaseTask task) {
        if (task.getId() == null) {
            throw new IllegalArgumentException("id can't be null");
        }
        return String.valueOf(task.getId());
    }

    public static String waitingValue(BaseTask task) {
        if (task.getId() == null || task.getTopic() == null) {
            throw new IllegalArgumentException("id or topic can't be null");
        }
        return join(task.getTopic(), task.getId());
    }

    /**
     * private
     */

    private static String wrap(String val) {
        return String.format("{%s}", val);
    }

    private static String join(Object ... values) {
        return StringUtils.join(values, ":");
    }

}
