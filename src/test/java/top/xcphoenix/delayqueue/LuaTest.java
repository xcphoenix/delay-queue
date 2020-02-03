package top.xcphoenix.delayqueue;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.xcphoenix.delayqueue.service.DelayQueueService;

import java.io.File;
import java.io.IOException;

/**
 * @author      xuanc
 * @date        2020/1/31 下午9:48
 * @version     1.0
 */
@Slf4j
@SpringBootTest
public class LuaTest {

    @Autowired
    private DelayQueueService delayQueueService;

    @Test
    void testLoadScripts() throws IOException {
        // log.info(LuaEnum.ADD_TASK.getContent());
        File file = new File(String.valueOf(this.getClass().getClassLoader().getResource("./")));
        log.info(file.getAbsolutePath());
    }


}
