package top.xcphoenix.delayqueue;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import top.xcphoenix.delayqueue.manager.IdGenerator;
import top.xcphoenix.delayqueue.manager.impl.SnowFlakeIdGenerator;

/**
 * @author      xuanc
 * @date        2020/2/4 下午3:28
 * @version     1.0
 */
@Slf4j
@SpringBootTest
public class SnowFlakeTest {

    @Test
    void testID() {
        long maxId = 10;
        IdGenerator idGenerator = new SnowFlakeIdGenerator();
        // Set<Long> idSet = new HashSet<>();
        for (int i = 0; i < maxId; i++) {
            // long id = SnowFlakeIDUtil.nextId();
            long id = idGenerator.getId();
            log.info(">> " + id);
        }
        // Assertions.assertEquals(idSet.size(), maxId);
    }

}
