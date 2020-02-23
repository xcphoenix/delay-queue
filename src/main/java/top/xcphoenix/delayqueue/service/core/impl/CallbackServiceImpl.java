package top.xcphoenix.delayqueue.service.core.impl;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import top.xcphoenix.delayqueue.exception.CallbackException;
import top.xcphoenix.delayqueue.pojo.Args;
import top.xcphoenix.delayqueue.pojo.Callback;
import top.xcphoenix.delayqueue.pojo.Task;
import top.xcphoenix.delayqueue.service.core.CallbackService;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

/**
 * @author xuanc
 * @version 1.0
 * @date 2020/2/9 下午8:50
 */
@Slf4j
public class CallbackServiceImpl implements CallbackService {

    @Override
    public Callback init(Task task) throws CallbackException {
        Class<? extends Callback> callbackClass = task.getCallback();
        if (callbackClass == null) {
            log.info("task has not callback, taskId: " + task.getId() +
                    ", group: " + task.getGroup() + ", topic: " + task.getTopic());
            return null;
        } else if (Modifier.isAbstract(callbackClass.getModifiers()) || callbackClass.isInterface()) {
            throw new CallbackException(task.toString() + ", callback is invalid class");
        }

        Args args = task.getArgs();
        log.info("callback: " + callbackClass + ", args: " + JSON.toJSONString(args));

        Callback callback;
        try {
            if (args == null) {
                callback = callbackClass.getConstructor().newInstance();
            } else {
                Constructor<? extends Callback> constructor = callbackClass.getConstructor(args.getClasses());
                callback = constructor.newInstance(args.getObjs());
            }
        } catch (InstantiationException | IllegalAccessException |
                InvocationTargetException | NoSuchMethodException ex) {
            throw new CallbackException("error in get class instance", ex);
        }

        log.info("get callback instance success");

        return callback;
    }

    @Async("callbackThreadPool")
    @Override
    public void call(Task task) throws CallbackException {
        if (task == null) {
            throw new IllegalArgumentException("task not be null");
        }

        Callback callback = init(task);
        if (callback == null) {
            return;
        }
        try {
            log.info("begin exec callback method..");
            callback.call();
        } catch (Exception ex) {
            log.warn("exec callback error", ex);
        }
        log.info("exec end");
    }

}
