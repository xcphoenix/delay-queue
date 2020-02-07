package top.xcphoenix.delayqueue.pojo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import top.xcphoenix.delayqueue.manager.IdGenerator;
import top.xcphoenix.delayqueue.manager.impl.SnowFlakeIdGenerator;

import lombok.NonNull;
import java.sql.Timestamp;
import java.util.concurrent.Callable;

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
    private Object[] args;
    /**
     * 回调接口
     */
    @SuppressWarnings("rawtypes")
    private Class<? extends Callable> callBack;

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

    @SuppressWarnings("rawtypes")
    public Task setCallback(Class<? extends Callable> callBack, Object[] args) {
        this.callBack = callBack;
        this.args = args;
        return this;
    }

}
