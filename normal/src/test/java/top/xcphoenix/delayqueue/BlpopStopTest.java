package top.xcphoenix.delayqueue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import top.xcphoenix.delayqueue.exception.RegisterException;
import top.xcphoenix.delayqueue.monitor.ServiceRegister;
import top.xcphoenix.delayqueue.monitor.global.TopicMonitor;

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
    private ServiceRegister register;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void testBlpop() throws RegisterException, InterruptedException {
        String group = "delay-queue";
        System.out.println("注册");
        register.registerTopic(group, "testE");
        Thread.sleep(10000);
        System.out.println("移除");
        register.cancelTopic(group, "testE");
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
