package top.xcphoenix.delayqueue.service.threads;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.jedis.JedisClusterConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.clients.jedis.Client;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.util.JedisClusterCRC16;
import top.xcphoenix.delayqueue.constant.RedisDataStruct;
import top.xcphoenix.delayqueue.exception.CallbackException;
import top.xcphoenix.delayqueue.monitor.global.ExecutorMonitor;
import top.xcphoenix.delayqueue.pojo.BaseTask;
import top.xcphoenix.delayqueue.pojo.Task;
import top.xcphoenix.delayqueue.service.core.CallbackService;
import top.xcphoenix.delayqueue.service.core.DelayQueueService;
import top.xcphoenix.delayqueue.utils.BeanUtil;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

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
     * for stop blpop
     */
    private Jedis jedis;
    /**
     * 控制线程结束
     */
    private volatile boolean stop = false;
    /**
     * 线程池监控
     */
    private ExecutorMonitor executorMonitor = BeanUtil.getBean(ExecutorMonitor.class);
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
            int availableThreads = executorMonitor.getCallbackAvailableThreads();
            log.info(format("callback executor available thread number: " + availableThreads));

            List<Task> taskList = delayQueueService.consumeTasksInList(attendGroup, attendTopic,
                    Math.max(availableThreads, 5));
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
                List<String> results;
                this.jedis = getJedis(listKey);
                try {
                    results = jedis.blpop(0, listKey);
                } catch (JedisConnectionException ex) {
                    if (this.stop) {
                        log.info(format("Thread end"));
                    } else {
                        ex.printStackTrace();
                        // TODO 重试
                    }
                    break;
                } finally {
                    jedis.close();
                    jedis = null;
                }
                log.info(format("block end"));
                if (results != null && results.size() >= 2) {
                    String taskId = results.get(1);
                    redisTemplate.opsForList().leftPush(listKey, taskId);
                    log.info(format("there are new task: " + taskId));
                }
            }
        }

        log.info(format("listen thread end"));
    }

    @Override
    public void interrupt() {
        log.info("terminal thread");
        this.closeJedis();
        super.interrupt();
    }

    private void closeJedis() {
        this.stop = true;
        if (this.jedis != null) {
            try {
                jedis.getClient().getSocket().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Jedis getJedis(String key) {
        JedisClusterConnection clusterConnection = (JedisClusterConnection) Objects.requireNonNull(
                redisTemplate.getConnectionFactory()).getConnection();
        Client client = clusterConnection.getNativeConnection()
                .getConnectionFromSlot(JedisClusterCRC16.getSlot(key)).getClient();
        return new Jedis(client.getHost(), client.getPort());
    }

    private String format(String msg) {
        return attendGroup + "," + attendTopic + " -> " + msg;
    }

}
