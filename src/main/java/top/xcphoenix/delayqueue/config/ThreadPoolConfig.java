package top.xcphoenix.delayqueue.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * @author xuanc
 * @version 1.0
 * @date 2020/2/6 上午10:26
 */
@Configuration
public class ThreadPoolConfig {

    /**
     * 推送任务至可消费队列
     *
     * @return 线程池
     */
    @Bean(name = "pushThreadPool")
    public ThreadPoolExecutor pushThreadPool() {
        int corePoolSize = 5;
        int maxPoolSize = 10;
        long keepAliveTime = 0L;
        ThreadFactory pushThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("PushPool-%d")
                .build();
        TimeUnit unit = TimeUnit.MILLISECONDS;
        BlockingDeque<Runnable> workQueue = new LinkedBlockingDeque<>();

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
    @Bean(name = "consumeThreadPool")
    public ThreadPoolExecutor consumeThreadPool() {
        int corePoolSize = 10;
        int maxPoolSize = 30;
        long keepAliveTime = 0L;
        ThreadFactory pushThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("ConsumePool-%d")
                .build();
        TimeUnit unit = TimeUnit.MILLISECONDS;
        BlockingDeque<Runnable> workQueue = new LinkedBlockingDeque<>();

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
    @Bean(name = "callbackThreadPool")
    public ThreadPoolExecutor callbackThreadPool() {
        int corePoolSize = 50;
        int maxPoolSize = 100;
        long keepAliveTime = 0L;
        ThreadFactory pushThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("CallbackPool-%d")
                .build();
        TimeUnit unit = TimeUnit.MILLISECONDS;
        BlockingDeque<Runnable> workQueue = new LinkedBlockingDeque<>();

        // 创建线程池
        return new ThreadPoolExecutor(
                corePoolSize, maxPoolSize, keepAliveTime,
                unit, workQueue, pushThreadFactory, new ThreadPoolExecutor.AbortPolicy());
    }

}
