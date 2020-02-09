package top.xcphoenix.delayqueue.threads;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import top.xcphoenix.delayqueue.constant.RedisDataStruct;
import top.xcphoenix.delayqueue.exception.CallbackException;
import top.xcphoenix.delayqueue.pojo.BaseTask;
import top.xcphoenix.delayqueue.pojo.Task;
import top.xcphoenix.delayqueue.service.CallbackService;
import top.xcphoenix.delayqueue.service.DelayQueueService;
import top.xcphoenix.delayqueue.utils.BeanUtil;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author xuanc
 * @version 1.0
 * @date 2020/2/7 上午11:21
 */
@Slf4j
public class ConsumeMonitorThread implements Runnable {

    /**
     * 关注的 group
     */
    private String attendGroup;
    /**
     * 关注 group 下的 topic
     */
    private String attendTopic;
    /**
     * redis 操作(BLPOP)
     */
    private StringRedisTemplate redisTemplate = BeanUtil.getBean(StringRedisTemplate.class);
    /**
     * redis 操作
     */
    private DelayQueueService delayQueueService = BeanUtil.getBean(DelayQueueService.class);
    /**
     * 回调操作
     */
    private CallbackService callbackService = BeanUtil.getBean(CallbackService.class);

    public ConsumeMonitorThread(String attendGroup, String attendTopic) {
        this.attendGroup = attendGroup;
        this.attendTopic = attendTopic;
    }

    @Override
    public synchronized void run() {
        log.info("start monitor for consuming list, group: " + attendGroup + ", topic: " + attendTopic);
        String listKey = RedisDataStruct.consumingKey(BaseTask.of(attendGroup, attendTopic));

        while (!Thread.currentThread().isInterrupted()) {
            // TODO 获取回调线程池可用线程数
            log.info(format("callback executor available thread number: "));

            List<Task> taskList = delayQueueService.consumeTasksInList(attendGroup, attendTopic, 5);
            if (taskList != null) {
                log.info(format("get task number: " + taskList.size()));
                log.info(format("push tasks to callback..."));
                // 放入回调线程池
                for (Task task : taskList) {
                    try {
                        callbackService.call(task);
                    } catch (CallbackException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                log.info(format("no task, listen key: " + listKey));
                Objects.requireNonNull(redisTemplate.getConnectionFactory());
                String taskId = redisTemplate.boundListOps(listKey).leftPop(0, TimeUnit.MILLISECONDS);
                log.info(format("block end"));
                if (taskId != null) {
                    redisTemplate.opsForList().leftPush(listKey, taskId);
                }
                log.info(format("there are new task: " + taskId));
            }
        }
    }

    private String format(String msg) {
        return attendGroup + "," + attendTopic + " -> " + msg;
    }

}
