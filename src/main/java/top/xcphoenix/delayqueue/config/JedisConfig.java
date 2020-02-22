package top.xcphoenix.delayqueue.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author      xuanc
 * @date        2020/2/22 上午9:55
 * @version     1.0
 */
@Configuration
public class JedisConfig {

    @Value("${spring.redis.jedis.pool.max-total}")
    private int maxTotal;

    @Value("${spring.redis.jedis.pool.max-wait}")
    private long maxWait;

    @Value("${spring.redis.jedis.pool.max-idle}")
    private int maxIdle;

    @Value("${spring.redis.jedis.pool.min-idle}")
    private int minIdle;

    @Value("${spring.redis.jedis.pool.num-tests-per-eviction-run}")
    private int numTestsPerEvictionRun;

    @Value("${spring.redis.jedis.pool.time-between-eviction-runs}")
    private long timeBetweenEvictionRunsMills;

    @Value("${spring.redis.jedis.pool.min-evictable-idle-time-millis}")
    private long minEvictableIdleTimeMillis;

    @Value("${spring.redis.jedis.pool.soft-min-evictable-idle-time-millis}")
    private long softMinEvictableIdleTimeMillis;

    @Value("${spring.redis.jedis.pool.test-on-borrow}")
    private boolean testOnBorrow;

    @Value("${spring.redis.jedis.pool.test-while-idle}")
    private boolean testWhileIdle;

    @Value("${spring.redis.jedis.pool.block-when-exhausted}")
    private boolean blockWhenExhausted;

    @Bean
    public JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(maxTotal);
        jedisPoolConfig.setMaxWaitMillis(maxWait);
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMinIdle(minIdle);
        jedisPoolConfig.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
        jedisPoolConfig.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMills);
        jedisPoolConfig.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        jedisPoolConfig.setSoftMinEvictableIdleTimeMillis(softMinEvictableIdleTimeMillis);
        jedisPoolConfig.setTestOnBorrow(testOnBorrow);
        jedisPoolConfig.setTestWhileIdle(testWhileIdle);
        // jedisPoolConfig.setBlockWhenExhausted(blockWhenExhausted);
        return jedisPoolConfig;
    }


}
