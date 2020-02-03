package top.xcphoenix.delayqueue.service;

import top.xcphoenix.delayqueue.pojo.AbstractTask;
import top.xcphoenix.delayqueue.pojo.Task;

import java.util.List;

/**
 * @author      xuanc
 * @date        2019/12/30 下午9:49
 * @version     1.0
 */ 
public interface DelayQueueService {

    /**
     * 添加任务
     *
     * @param task 要添加的任务
     */
    void addTask(Task task);

    /**
     * 移除任务
     *
     * @param task 要删除的任务
     *             必须具备以下字段：
     *             - id
     *             - group
     * @return 移除任务的信息
     */
    Task removeTask(AbstractTask task);

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
