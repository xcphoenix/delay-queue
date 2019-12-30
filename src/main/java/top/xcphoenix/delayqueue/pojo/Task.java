package top.xcphoenix.delayqueue.pojo;

import lombok.Getter;
import top.xcphoenix.delayqueue.service.IdGeneratorService;
import top.xcphoenix.delayqueue.service.impl.SnowFlakeIdGeneratorServiceImpl;

import java.sql.Timestamp;
import java.util.concurrent.Callable;

/**
 * @author      xuanc
 * @date        2019/12/22 下午4:20
 * @version     1.0
 */
@Getter
public class Task {

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

    private IdGeneratorService idGenerator = new SnowFlakeIdGeneratorServiceImpl();

    private Task(String topic, Timestamp delayExecTime) {
        this.topic = topic;
        this.delayExecTime = delayExecTime;
        idGenerator.setSeed(delayExecTime.getTime());
        this.id = idGenerator.getId();
    }

    private Task(String topic, Timestamp delayExecTime, IdGeneratorService idGenerator) {
        this.topic = topic;
        this.delayExecTime = delayExecTime;
        this.idGenerator = idGenerator;
        idGenerator.setSeed(delayExecTime.getTime());
        this.id = idGenerator.getId();
    }

    public Task newTask(String topic, Timestamp delayExecTime) {
        return new Task(topic, delayExecTime);
    }

    public Task newTask(String topic, Timestamp delayExecTime, IdGeneratorService idGenerator) {
        return new Task(topic, delayExecTime, idGenerator);
    }

    public Task desc(String desc) {
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
