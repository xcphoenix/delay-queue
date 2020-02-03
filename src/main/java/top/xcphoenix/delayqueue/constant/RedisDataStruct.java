package top.xcphoenix.delayqueue.constant;

import org.apache.commons.lang3.StringUtils;
import top.xcphoenix.delayqueue.pojo.AbstractTask;

/**
 * @author      xuanc
 * @date        2020/2/2 下午5:34
 * @version     1.0
 */ 
public class RedisDataStruct {

    private static final String DETAIL = "DETAIL";
    private static final String WAITING = "WAITING";
    private static final String CONSUMING = "CONSUMING";

    private RedisDataStruct() {}

    public static String taskKey(AbstractTask task) {
        return join(wrap(task.getGroup()), DETAIL);
    }

    public static String waitingKey(AbstractTask task) {
        return join(wrap(task.getGroup()), WAITING);
    }

    public static String consumingKey(AbstractTask task) {
        return join(wrap(task.getGroup()), CONSUMING, wrap(task.getTopic()));
    }

    public static String taskField(AbstractTask task) {
        return String.valueOf(task.getId());
    }

    public static String waitingValue(AbstractTask task) {
        return join(task.getTopic(), task.getId());
    }

    public static String consumingValue(AbstractTask task) {
        return String.valueOf(task.getId());
    }


    private static String wrap(String val) {
        return String.format("{%s}", val);
    }

    private static String join(Object ... values) {
        return StringUtils.join(values, ":");
    }

}
