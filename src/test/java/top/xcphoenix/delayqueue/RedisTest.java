package top.xcphoenix.delayqueue;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import top.xcphoenix.delayqueue.config.RedisSerializerConfig;

import javax.annotation.Resource;

/**
 * @author      xuanc
 * @date        2019/12/22 下午3:51
 * @version     1.0
 */
@ExtendWith({SpringExtension.class})
@SpringBootApplication
public class RedisTest {

    @Resource
    private RedisTemplate redisTemplate;

    @Test
    void testRedisIoc() {
        redisTemplate.opsForValue().set("xuanc", "xuanc");
    }

}
