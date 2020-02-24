package top.xcphoenix.delayqueue.service.core;

import top.xcphoenix.delayqueue.pojo.BaseTask;
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
    Task removeTask(BaseTask task);

    /**
     * 推送任务至待消费队列
     *
     * @param group 推送操作的组
     * @param maxScore 最大分数
     * @param minScore 最小分数
     * @return 等待队列中最高分数
     */
    Long pushTask(String group, long maxScore, long minScore);

    /**
     * 获取并删除指定 topic 的任务队列数据
     *
     * @param group topic 所在的组
     * @param topic topic
     * @param limit 条数
     *              <code>if limit == 0</code> 获取所有数据
     * @return 任务信息
     * @throws IllegalArgumentException limit < 0
     */
    List<Task> consumeTasksInList(String group, String topic, int limit) throws IllegalArgumentException;

}
