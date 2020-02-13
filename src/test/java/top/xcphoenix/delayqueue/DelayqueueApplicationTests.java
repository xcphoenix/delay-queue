package top.xcphoenix.delayqueue;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.xcphoenix.delayqueue.service.core.DelayQueueService;
import top.xcphoenix.delayqueue.service.core.impl.RedisDelayQueueServiceImpl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SpringBootTest
class DelayqueueApplicationTests {

    @Autowired
    private RedisDelayQueueServiceImpl redisDelayQueueService;

    @Test
    void contextLoads() {
    }

    @Test
    @Disabled
    void tmp() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<? extends DelayQueueService> delayQueueService = redisDelayQueueService.getClass();
        Method method = delayQueueService.getDeclaredMethod("init");
        method.setAccessible(true);
        method.invoke(redisDelayQueueService);
    }

}
