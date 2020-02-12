package top.xcphoenix.delayqueue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.jedis.JedisClusterConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.clients.jedis.Client;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.util.JedisClusterCRC16;
import top.xcphoenix.delayqueue.monitor.global.TopicMonitor;

import java.io.IOException;
import java.util.Objects;

/**
 * @author xuanc
 * @version 1.0
 * @date 2020/2/11 下午6:14
 */
@SpringBootTest
public class BlpopStopTest {

    @Autowired
    private TopicMonitor topicMonitor;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void testBlpop() {
        String listKey = "{delay-queue}:CONSUMING:testE";

        class DemoThread extends Thread {
            private volatile Jedis jedis;

            public DemoThread(Jedis jedis) {
                this.jedis = jedis;
            }

            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        System.out.println("start");
                        jedis.blpop(0, listKey);
                    } catch (JedisConnectionException e) {
                        if (Thread.currentThread().isInterrupted()) {
                            System.out.println("Thread end");
                        } else {
                            e.printStackTrace();
                        }
                        return;
                    } finally {
                        jedis.close();
                    }
                    System.out.println("wait end");
                }
            }

            public void close() {
                if (jedis != null) {
                    try {
                        jedis.getClient().getSocket().close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        JedisClusterConnection clusterConnection = (JedisClusterConnection) Objects.requireNonNull(
                redisTemplate.getConnectionFactory()).getConnection();
        Client client = clusterConnection.getNativeConnection()
                .getConnectionFromSlot(JedisClusterCRC16.getSlot(listKey)).getClient();
        Jedis jedis = new Jedis(client.getHost(), client.getPort());

        DemoThread thread = new DemoThread(jedis);
        thread.start();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        thread.interrupt();
        thread.close();

        System.out.println("hhh");

        try {
            Thread.sleep(50000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testRemTopic() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        topicMonitor.remOldTopic("delay-queue", "testA");
        System.out.println();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
