package top.xcphoenix.delayqueue;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;
import top.xcphoenix.delayqueue.monitor.ServiceRegister;
import top.xcphoenix.delayqueue.monitor.destroy.DestroyService;
import top.xcphoenix.delayqueue.monitor.global.ExecutorMonitor;
import top.xcphoenix.delayqueue.monitor.global.GroupMonitor;
import top.xcphoenix.delayqueue.monitor.global.TopicMonitor;
import top.xcphoenix.delayqueue.monitor.init.impl.GroupInitScanImpl;
import top.xcphoenix.delayqueue.monitor.init.impl.TopicInitScanImpl;
import top.xcphoenix.delayqueue.properties.ClusterConfigurationProperties;
import top.xcphoenix.delayqueue.properties.JedisPoolProperties;
import top.xcphoenix.delayqueue.service.core.CallbackService;
import top.xcphoenix.delayqueue.service.core.DelayQueueService;
import top.xcphoenix.delayqueue.service.core.impl.CallbackServiceImpl;
import top.xcphoenix.delayqueue.service.core.impl.RedisDelayQueueServiceImpl;
import top.xcphoenix.delayqueue.utils.BeanUtil;

import java.util.concurrent.*;

/**
 * @author xuanc
 */
@Configuration
@ConditionalOnProperty(prefix = "delayqueue", value = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties({ClusterConfigurationProperties.class, JedisPoolProperties.class})
public class DelayqueueAutoConfigure {

    @Bean("delayqueue-connectionfactory")
    public RedisConnectionFactory connectionFactory(ClusterConfigurationProperties clusterProp,
                                                    JedisPoolProperties jedisPoolProp) {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(jedisPoolProp.getMaxTotal());
        jedisPoolConfig.setMaxWaitMillis(jedisPoolProp.getMaxWait());
        jedisPoolConfig.setMaxIdle(jedisPoolProp.getMaxIdle());
        jedisPoolConfig.setMinIdle(jedisPoolProp.getMinIdle());
        jedisPoolConfig.setNumTestsPerEvictionRun(jedisPoolProp.getNumTestsPerEvictionRun());
        jedisPoolConfig.setTimeBetweenEvictionRunsMillis(jedisPoolProp.getTimeBetweenEvictionRunsMills());
        jedisPoolConfig.setMinEvictableIdleTimeMillis(jedisPoolProp.getMinEvictableIdleTimeMillis());
        jedisPoolConfig.setSoftMinEvictableIdleTimeMillis(jedisPoolProp.getSoftMinEvictableIdleTimeMillis());
        jedisPoolConfig.setTestOnBorrow(jedisPoolProp.isTestOnBorrow());
        jedisPoolConfig.setTestWhileIdle(jedisPoolProp.isTestWhileIdle());

        return new JedisConnectionFactory(
                new RedisClusterConfiguration(
                        clusterProp.getNodes()
                ),
                jedisPoolConfig
        );
    }

    @Bean("delayqueue-template")
    public StringRedisTemplate redisTemplate(@Qualifier("delayqueue-connectionfactory") RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate redisTemplate = new StringRedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        GenericFastJsonRedisSerializer fastJsonRedisSerializer = new GenericFastJsonRedisSerializer();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(fastJsonRedisSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /* Thread Pool */

    /**
     * 推送任务至可消费队列
     *
     * @return 线程池
     */
    @Bean("pushThreadPool")
    public ThreadPoolExecutor pushThreadPool() {
        int corePoolSize = 5;
        int maxPoolSize = 10;
        long keepAliveTime = 0L;
        ThreadFactory pushThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("PushPool-%d")
                .build();
        TimeUnit unit = TimeUnit.MILLISECONDS;
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(maxPoolSize);

        // 创建线程池
        return new ThreadPoolExecutor(
                corePoolSize, maxPoolSize, keepAliveTime,
                unit, workQueue, pushThreadFactory, new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * 从消费队列中取出task放入回调线程池
     *
     * @return 线程池
     */
    @Bean("consumeThreadPool")
    public ThreadPoolExecutor consumeThreadPool() {
        int corePoolSize = 10;
        int maxPoolSize = 30;
        long keepAliveTime = 0L;
        ThreadFactory pushThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("ConsumePool-%d")
                .build();
        TimeUnit unit = TimeUnit.MILLISECONDS;
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(maxPoolSize);

        // 创建线程池
        return new ThreadPoolExecutor(
                corePoolSize, maxPoolSize, keepAliveTime,
                unit, workQueue, pushThreadFactory, new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * 反序列化 task，执行回调任务
     *
     * @return 线程池
     */
    @Bean("callbackThreadPool")
    public ThreadPoolExecutor callbackThreadPool() {
        int corePoolSize = 50;
        int maxPoolSize = 250;
        long keepAliveTime = 0L;
        ThreadFactory pushThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("CallbackPool-%d")
                .build();
        TimeUnit unit = TimeUnit.MILLISECONDS;
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(maxPoolSize);

        // 创建线程池
        return new ThreadPoolExecutor(
                corePoolSize, maxPoolSize, keepAliveTime,
                unit, workQueue, pushThreadFactory, new ThreadPoolExecutor.AbortPolicy());
    }

    /* Monitor */

    @Bean
    @ConditionalOnMissingBean
    public GroupMonitor groupMonitor(@Qualifier("pushThreadPool") ThreadPoolExecutor executor) {
        return new GroupMonitor(executor);
    }

    @Bean
    @ConditionalOnMissingBean
    public TopicMonitor topicMonitor(GroupMonitor groupMonitor,
                                     @Qualifier("consumeThreadPool") ThreadPoolExecutor executor) {
        return new TopicMonitor(groupMonitor, executor);
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceRegister serviceRegister(GroupMonitor groupMonitor,
                                           TopicMonitor topicMonitor,
                                           @Qualifier("delayqueue-template") StringRedisTemplate redisTemplate) {
        return new ServiceRegister(groupMonitor, topicMonitor, redisTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public ExecutorMonitor executorMonitor(@Qualifier("pushThreadPool") ThreadPoolExecutor push,
                                           @Qualifier("consumeThreadPool") ThreadPoolExecutor consume,
                                           @Qualifier("callbackThreadPool") ThreadPoolExecutor callback) {
        return new ExecutorMonitor(push, consume, callback);
    }

    /* Lifecycle */

    @Bean
    @ConditionalOnMissingBean
    public DestroyService destroyService(@Qualifier("pushThreadPool") ThreadPoolExecutor push,
                                         @Qualifier("consumeThreadPool") ThreadPoolExecutor consume,
                                         @Qualifier("callbackThreadPool") ThreadPoolExecutor callback) {
        return new DestroyService(push, consume, callback);
    }

    @Bean
    @Qualifier("scan-group")
    @ConditionalOnMissingBean
    @Order(1)
    public GroupInitScanImpl groupInitScan(GroupMonitor groupMonitor,
                                           @Qualifier("delayqueue-template") StringRedisTemplate redisTemplate) {
        return new GroupInitScanImpl(groupMonitor, redisTemplate);
    }

    @Bean
    @Qualifier("scan-topic")
    @ConditionalOnMissingBean
    @Order(2)
    public TopicInitScanImpl topicInitScan(GroupMonitor groupMonitor,
                                           TopicMonitor topicMonitor,
                                           @Qualifier("delayqueue-template") StringRedisTemplate redisTemplate) {
        return new TopicInitScanImpl(groupMonitor, topicMonitor, redisTemplate);
    }

    /* Other */

    @Bean
    @ConditionalOnMissingBean
    public CallbackService callbackService() {
        return new CallbackServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public DelayQueueService delayQueueService(GroupMonitor groupMonitor, @Qualifier("delayqueue-template") StringRedisTemplate redisTemplate) {
        return new RedisDelayQueueServiceImpl(groupMonitor, redisTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public BeanUtil beanUtil() {
        return new BeanUtil();
    }

}
