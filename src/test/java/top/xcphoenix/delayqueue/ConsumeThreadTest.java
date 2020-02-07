package top.xcphoenix.delayqueue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import top.xcphoenix.delayqueue.threads.ConsumeMonitorThread;

import java.util.concurrent.Executor;

/**
 * @author      xuanc
 * @date        2020/2/7 下午5:27
 * @version     1.0
 */
@SpringBootTest
public class ConsumeThreadTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    @Qualifier("consumeThreadPool")
    private Executor executor;

    @Test
    void testBlpop() {
        ConsumeMonitorThread thread = new ConsumeMonitorThread("delay-queue", "testA");
        executor.execute(thread);
        try {
            Thread.sleep(100 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
