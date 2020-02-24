package top.xcphoenix.delayqueue;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import top.xcphoenix.delayqueue.service.core.impl.RedisDelayQueueServiceImpl;

import java.util.concurrent.FutureTask;

// @SpringBootTest
class DelayqueueApplicationTests {

    @Autowired
    private RedisDelayQueueServiceImpl redisDelayQueueService;

    @Test
    void contextLoads() {
    }

    @Test
    @Disabled
    void tmp() {
        FutureTask futureTask = new FutureTask<Void>(new Runnable() {
            @Override
            public void run() {
                while (true) {
                }
            }
        }, null);

        futureTask.run();
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start <= 10000) {
        }

        System.out.println("stop...");
        futureTask.cancel(true);

    }

}
