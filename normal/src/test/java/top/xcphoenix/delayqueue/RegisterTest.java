package top.xcphoenix.delayqueue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.xcphoenix.delayqueue.monitor.ServiceRegister;

/**
 * @author      xuanc
 * @date        2020/2/12 下午2:17
 * @version     1.0
 */
@SpringBootTest
public class RegisterTest {

    @Autowired
    private ServiceRegister register;

    @Test
    void testRem() throws InterruptedException {
        Thread.sleep(5000);
        register.cancelGroup("delay-queue");
        Thread.sleep(10 * 1000);
    }

}
