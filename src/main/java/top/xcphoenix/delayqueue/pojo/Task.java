package top.xcphoenix.delayqueue.pojo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import top.xcphoenix.delayqueue.manager.IdGenerator;
import top.xcphoenix.delayqueue.manager.impl.SnowFlakeIdGenerator;

import java.sql.Timestamp;

/**
 * TODO 自定义序列化
 *   - 隐藏无参构造器
 *   - 隐藏setter方法
 *
 * @author      xuanc
 * @date        2019/12/22 下午4:20
 * @version     1.0
 */
@Setter
@Getter
@ToString
public class Task extends BaseTask {

    /**
     * 执行时间
     */
    private Timestamp delayExecTime;

    /* ----------- 可选 ----------- */
    /**
     * 任务描述
     */
    private String desc;
    /**
     * 回调接口构造器参数
     */
    private Args args;
    /**
     * 回调接口
     */
    private Class<? extends Callback> callback;

    @JSONField(deserialize = false, serialize = false)
    private IdGenerator idGenerator = new SnowFlakeIdGenerator();

    public Task() {}

    private Task(@NonNull String group, @NonNull String topic,
                 @NonNull Timestamp delayExecTime) {
        this.topic = topic;
        this.group = group;
        this.delayExecTime = delayExecTime;
        this.id = idGenerator.getId(group);
    }

    private Task(@NonNull String group, @NonNull String topic,
                 @NonNull Timestamp delayExecTime, @NonNull IdGenerator idGenerator) {
        this.topic = topic;
        this.group = group;
        this.delayExecTime = delayExecTime;
        this.idGenerator = idGenerator;
        this.id = idGenerator.getId(group);
    }

    /*
     * build task
     */

    public static Task newTask(String group, String topic, Timestamp delayExecTime) {
        return new Task(group, topic, delayExecTime);
    }

    public static Task newTask(String group, String topic, Timestamp delayExecTime, IdGenerator idGenerator) {
        return new Task(group, topic, delayExecTime, idGenerator);
    }

    /*
     * set attr
     */

    public Task setDesc(String desc) {
        this.desc = desc;
        return this;
    }

    public Task setIdGenerator(IdGenerator idGenerator) {
        this.id = idGenerator.getId(this.group);
        return this;
    }

    public Task setCallback(Class<? extends Callback> clazz, Args args) {
        this.callback = clazz;
        this.args = args;
        return this;
    }

}
