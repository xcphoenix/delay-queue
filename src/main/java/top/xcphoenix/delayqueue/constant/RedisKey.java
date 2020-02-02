package top.xcphoenix.delayqueue.constant;

import org.apache.commons.lang3.StringUtils;
import top.xcphoenix.delayqueue.pojo.Task;

/**
 * @author      xuanc
 * @date        2020/2/2 下午5:34
 * @version     1.0
 */ 
public class RedisKey {

    private static final String DETAIL = "DETAIL";
    private static final String WAITING = "WAITING";
    private static final String CONSUMING = "CONSUMING";

    private RedisKey() {}

    public static String taskKey(Task task) {
        return join(wrap(task.getGroup()), DETAIL);
    }

    public static String waitingKey(Task task) {
        return join(wrap(task.getGroup()), WAITING);
    }

    public static String consumingKey(Task task) {
        return join(wrap(task.getGroup()), CONSUMING, wrap(task.getTopic()));
    }

    private static String wrap(String val) {
        return String.format("{%s}", val);
    }

    private static String join(String ... values) {
        return StringUtils.join(values, ":");
    }

}
