package top.xcphoenix.delayqueue.config;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author      xuanc
 * @date        2019/12/22 下午3:57
 * @version     1.0
 */
@Slf4j
@Configuration
public class RedisClusterConfig {

    private JedisPoolConfig jedisPoolConfig;
    private ClusterConfigurationProperties clusterConfigurationProperties;

    public RedisClusterConfig(JedisPoolConfig jedisPoolConfig, ClusterConfigurationProperties clusterConfigurationProperties) {
        this.jedisPoolConfig = jedisPoolConfig;
        this.clusterConfigurationProperties = clusterConfigurationProperties;
    }

    @Bean
    public RedisConnectionFactory connectionFactory() {
        log.info("RedisProps ==> " + JSON.toJSON(this.clusterConfigurationProperties).toString());

        return new JedisConnectionFactory(
                new RedisClusterConfiguration(
                        clusterConfigurationProperties.getNodes()
                ),
                jedisPoolConfig
        );
    }

}
