package top.xcphoenix.delayqueue.service;

import top.xcphoenix.delayqueue.pojo.Task;

import java.util.List;

/**
 * @author      xuanc
 * @date        2019/12/30 下午9:49
 * @version     1.0
 */ 
public interface DelayQueueService {

    /**
     * 初始化
     *
     * @throws Exception 异常
     */
    void init() throws Exception;

    /**
     * 添加任务
     */
    void addTask();

    /**
     * 移除任务
     *
     * @return 移除任务的信息
     */
    Task removeTask();

    /**
     * 获取指定 topic 的任务信息
     *
     * @param topic 主题
     * @param offset 偏移量<br />
     *               if <code>offset < 0</code> return all task
     * @param limit 条数
     * @return 任务信息
     */
    List<Task> getTaskByTopic(String topic, long offset, int limit);


}
