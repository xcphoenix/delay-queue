package top.xcphoenix.delayqueue.constant;

import org.apache.commons.lang3.StringUtils;
import top.xcphoenix.delayqueue.pojo.AbstractTask;

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

    private RedisDataStruct() {}

    /**
     * for key
     */

    public static String taskKey(AbstractTask task) {
        return join(wrap(task.getGroup()), DETAIL);
    }

    public static String waitingKey(AbstractTask task) {
        return join(wrap(task.getGroup()), WAITING);
    }

    /**
     * 获取任务所对应的消费队列key
     *
     * @param task 任务信息
     * @throws IllegalArgumentException topic is null
     * @return 消费队列 key
     */
    public static String consumingKey(AbstractTask task) {
        if (task.getTopic() == null) {
            throw new IllegalArgumentException("topic can't be null");
        }
        return join(wrap(task.getGroup()), CONSUMING, task.getTopic());
    }

    public static String consumingKeyPrefix(AbstractTask task) {
        return join(wrap(task.getGroup()), CONSUMING);
    }

    public static List<String> getRedisKeys(AbstractTask task) {
        return Arrays.asList(taskKey(task), waitingKey(task), consumingKeyPrefix(task));
    }

    /**
     * for field or value
     */

    public static String taskField(AbstractTask task) {
        return String.valueOf(task.getId());
    }

    public static String waitingValue(AbstractTask task) {
        return join(task.getTopic(), task.getId());
    }

    public static String consumingValue(AbstractTask task) {
        return String.valueOf(task.getId());
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
