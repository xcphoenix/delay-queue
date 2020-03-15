package top.xcphoenix.delayqueue.service.threads;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import top.xcphoenix.delayqueue.constant.RedisDataStruct;
import top.xcphoenix.delayqueue.exception.CallbackException;
import top.xcphoenix.delayqueue.monitor.global.ExecutorMonitor;
import top.xcphoenix.delayqueue.pojo.BaseTask;
import top.xcphoenix.delayqueue.pojo.Task;
import top.xcphoenix.delayqueue.service.core.CallbackService;
import top.xcphoenix.delayqueue.service.core.DelayQueueService;
import top.xcphoenix.delayqueue.utils.BeanUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author xuanc
 * @version 1.0
 * @date 2020/2/7 上午11:21
 */
@Slf4j
public class ConsumeMonitorThread extends Thread {

    /**
     * 关注的 group
     */
    private String attendGroup;
    /**
     * 关注 group 下的 topic
     */
    private String attendTopic;
    /**
     * 控制线程结束
     */
    private volatile boolean stop = false;
    /**
     * 线程池监控
     */
    private final ExecutorMonitor executorMonitor = BeanUtil.getBean(ExecutorMonitor.class);
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
    public void run() {
        log.info("start monitor for consuming list, group: " + attendGroup + ", topic: " + attendTopic);
        String listKey = RedisDataStruct.consumingKey(BaseTask.of(attendGroup, attendTopic));

        while (!stop) {
            List<Task> taskList;
            // 防止数据不一致
            synchronized (executorMonitor) {
                int availableThreads = executorMonitor.getCallbackAvailableThreads();
                // == 0 will get all data
                if (availableThreads == 0) {
                    continue;
                }
                log.info(format("callback executor available thread number: " + availableThreads));
                taskList = delayQueueService.consumeTasksInList(attendGroup, attendTopic, availableThreads);
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
                }
            }

            if (taskList == null) {
                log.info(format("no task, listen key: " + listKey));
                String taskId = redisTemplate.boundListOps(listKey).leftPop(0, TimeUnit.SECONDS);
                if (taskId != null && !"".equals(taskId)) {
                    redisTemplate.opsForList().leftPush(listKey, taskId);
                    log.info(format("there are new task: " + taskId));
                }
                log.info(format("block end"));
            }
        }

        log.info(format("listen thread end"));
    }

    @Override
    public void interrupt() {
        log.info("terminal thread");
        close();
        super.interrupt();
    }

    private void close() {
        this.stop = true;
        // add "" string for end blpop
        // also filter for lua
        redisTemplate.opsForList().leftPush(RedisDataStruct.consumingKey(BaseTask.of(attendGroup, attendTopic)), "");
    }

    private String format(String msg) {
        return attendGroup + "," + attendTopic + " -> " + msg;
    }

}
