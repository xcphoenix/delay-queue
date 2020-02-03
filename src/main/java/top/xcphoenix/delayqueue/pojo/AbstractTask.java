package top.xcphoenix.delayqueue.pojo;

import lombok.Getter;
import top.xcphoenix.delayqueue.constant.ProjectConst;

/**
 * @author      xuanc
 * @date        2020/2/3 下午5:21
 * @version     1.0
 */
@Getter
public abstract class AbstractTask {

    protected AbstractTask() {}
    protected AbstractTask(long id, String topic, String group) {
        this.id = id;
        this.topic = topic;
        this.group = group;
    }

    /**
     * 任务所在组
     */
    private String group;
    /**
     * 任务主题
     */
    private String topic;
    /**
     * 任务id
     */
    private long id;

    public static AbstractTask of(long id, String topic) {
        String group = ProjectConst.projectName;
        return new AbstractTask(id, topic, group) {
        };
    }

    public static AbstractTask of(long id, String topic, String group) {
        return new AbstractTask(id, topic, group) {
        };
    }

}
