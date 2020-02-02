package top.xcphoenix.delayqueue.pojo;

import lombok.Getter;
import top.xcphoenix.delayqueue.constant.ProjectConst;
import top.xcphoenix.delayqueue.manager.IdGenerator;
import top.xcphoenix.delayqueue.manager.impl.SnowFlakeIdGenerator;

import java.sql.Timestamp;
import java.util.concurrent.Callable;

/**
 * @author      xuanc
 * @date        2019/12/22 下午4:20
 * @version     1.0
 */
@Getter
public class Task {

    private static final String DEFAULT_GROUP = ProjectConst.projectName;

    /**
     * 任务所在组
     */
    private String group;
    /**
     * 任务主题
     */
    private String topic;
    /**
     * 使用雪花算法根据执行时间生成id
     */
    private long id;
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

    private IdGenerator idGenerator = new SnowFlakeIdGenerator();

    private Task(String topic, String group, Timestamp delayExecTime) {
        this.topic = topic;
        this.group = group;
        this.delayExecTime = delayExecTime;
        idGenerator.setSeed(group);
        this.id = idGenerator.getId();
    }

    private Task(String topic, String group, Timestamp delayExecTime, IdGenerator idGenerator) {
        this.topic = topic;
        this.group = group;
        this.delayExecTime = delayExecTime;
        this.idGenerator = idGenerator;
        idGenerator.setSeed(group);
        this.id = idGenerator.getId();
    }

    public static Task newTask(String topic, String group, Timestamp delayExecTime, IdGenerator idGenerator) {
        return new Task(topic, group, delayExecTime, idGenerator);
    }

    public static Task newTask(String topic, String group, Timestamp delayExecTime) {
        return new Task(topic, group, delayExecTime);
    }

    public static Task newTask(String topic, Timestamp delayExecTime, IdGenerator idGenerator) {
        return new Task(topic, DEFAULT_GROUP, delayExecTime, idGenerator);
    }

    public static Task newTask(String topic, Timestamp delayExecTime) {
        return new Task(topic, DEFAULT_GROUP, delayExecTime);
    }

    public Task setIdGenerator(IdGenerator idGenerator) {
        idGenerator.setSeed(this.group);
        this.id = idGenerator.getId();
        return this;
    }

    public Task setDesc(String desc) {
        this.desc = desc;
        return this;
    }

    @SuppressWarnings("rawtypes")
    public Task setCallback(Class<? extends Callable> callBack, Object[] args) {
        this.callBack = callBack;
        this.args = args;
        return this;
    }

}
