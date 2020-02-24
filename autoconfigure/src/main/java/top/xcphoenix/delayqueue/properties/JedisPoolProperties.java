package top.xcphoenix.delayqueue.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author      xuanc
 * @date        2020/2/22 上午9:55
 * @version     1.0
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "delayqueue.jedis.pool")
public class JedisPoolProperties {

    private int maxTotal;
    private long maxWait;
    private int maxIdle;
    private int minIdle;
    private int numTestsPerEvictionRun;
    private long timeBetweenEvictionRunsMills;
    private long minEvictableIdleTimeMillis;
    private long softMinEvictableIdleTimeMillis;
    private boolean testOnBorrow;
    private boolean testWhileIdle;

}
