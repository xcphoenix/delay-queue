package top.xcphoenix.delayqueue.threads;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import top.xcphoenix.delayqueue.constant.RedisDataStruct;
import top.xcphoenix.delayqueue.pojo.BaseTask;
import top.xcphoenix.delayqueue.pojo.Task;
import top.xcphoenix.delayqueue.service.DelayQueueService;
import top.xcphoenix.delayqueue.utils.BeanUtil;

import java.util.List;
import java.util.Objects;

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

    public ConsumeMonitorThread(String attendGroup, String attendTopic) {
        this.attendGroup = attendGroup;
        this.attendTopic = attendTopic;
    }

    @Override
    public void run() {
        log.info("start monitor for consuming list, group: " + attendGroup + ", topic: " + attendTopic);
        String listKey = RedisDataStruct.consumingKey(BaseTask.of(attendGroup, attendTopic));

        while (!Thread.currentThread().isInterrupted()) {
            // TODO 获取回调线程池可用线程数
            log.info("callback executor available thread number: ");

            List<Task> taskList = delayQueueService.getTasksInList(attendGroup, attendTopic, 0);
            if (taskList != null) {
                log.info("get task number: " + taskList.size());
                log.info("push tasks to callback...");
                // 放入回调线程池
            } else {
                log.info("no task, wait...");
                Objects.requireNonNull(redisTemplate.getConnectionFactory());
                redisTemplate.getConnectionFactory().getConnection().bLPop(0, listKey.getBytes());

                log.info("there are new tasks be found");
            }
        }
    }

}
