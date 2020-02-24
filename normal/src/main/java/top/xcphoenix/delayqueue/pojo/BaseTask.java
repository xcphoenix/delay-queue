package top.xcphoenix.delayqueue.pojo;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * TODO
 * - topic、group名不能包含特殊字符（限制为数字+英文字母）
 *
 * @author xuanc
 * @version 1.0
 * @date 2020/2/3 下午5:21
 */
@Getter
@Setter
public class BaseTask {

    /**
     * 任务所在组
     */
    protected String group;
    /**
     * 任务主题
     */
    protected String topic;
    /**
     * 任务id
     */
    protected Long id;

    protected BaseTask() {
    }

    protected BaseTask(Long id, String group, String topic) {
        this.id = id;
        this.topic = topic;
        this.group = group;
    }

    public static BaseTask of(@NonNull String group) {
        return new BaseTask(null, group, null);
    }

    public static BaseTask of(@NonNull String group, String topic) {
        return new BaseTask(null, group, topic);
    }

    public static BaseTask of(long id, @NonNull String group, String topic) {
        return new BaseTask(id, group, topic);
    }

}
