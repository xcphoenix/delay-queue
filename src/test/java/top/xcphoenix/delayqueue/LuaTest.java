package top.xcphoenix.delayqueue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.xcphoenix.delayqueue.service.DelayQueueService;

/**
 * @author      xuanc
 * @date        2020/1/31 下午9:48
 * @version     1.0
 */
@SpringBootTest
public class LuaTest {

    @Autowired
    private DelayQueueService delayQueueService;

    @Test
    void testLoadScripts() throws Exception {
        delayQueueService.init();
    }


}
