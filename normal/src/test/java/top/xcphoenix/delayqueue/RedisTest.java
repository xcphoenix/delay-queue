package top.xcphoenix.delayqueue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author      xuanc
 * @date        2019/12/22 下午3:51
 * @version     1.0
 */
@ExtendWith({SpringExtension.class})
@SpringBootTest
public class RedisTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void redisIocTest() {
        redisTemplate.opsForValue().set("xuanc", "xuanc");
    }

}
