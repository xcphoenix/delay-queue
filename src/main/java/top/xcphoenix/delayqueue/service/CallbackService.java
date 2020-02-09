package top.xcphoenix.delayqueue.service;

import top.xcphoenix.delayqueue.exception.CallbackException;
import top.xcphoenix.delayqueue.pojo.Callback;
import top.xcphoenix.delayqueue.pojo.Task;

/**
 * 任务回调
 *
 * @author xuanc
 * @version 1.0
 * @date 2019/12/30 下午9:57
 */
public interface CallbackService {

    /**
     * 初始化回调接口、参数
     *
     * @param task 任务
     * @return callback 接口
     * @throws CallbackException class 无效、参数不合法等
     */
    Callback init(Task task) throws CallbackException;

    /**
     * 调用接口
     *
     * @param task 任务
     * @throws CallbackException 执行回调产生的异常
     */
    void call(Task task) throws CallbackException;

}
