package top.xcphoenix.delayqueue.service.impl;

import top.xcphoenix.delayqueue.pojo.Task;
import top.xcphoenix.delayqueue.service.DelayQueueService;

import java.util.List;

/**
 * @author      xuanc
 * @date        2019/12/30 下午9:55
 * @version     1.0
 */ 
public class RedisDelayQueueServiceImpl implements DelayQueueService {

    @Override
    public void addTask() {

    }

    @Override
    public Task removeTask() {
        return null;
    }

    @Override
    public List<Task> getTaskByTopic(String topic, long offset, int limit) {
        return null;
    }

}
