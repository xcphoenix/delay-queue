package top.xcphoenix.delayqueue.watch.impl;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import top.xcphoenix.delayqueue.watch.WatchTimerService;

import java.util.concurrent.*;

/**
 * 搬运线程线程池
 * TODO 创建线程时，使用 Set 保存线程与 group 对应关系，如果有，且线程存活不处理，如果没有新建线程
 *  不过wait notified 机制怎么搞 ...
 *
 * @author      xuanc
 * @date        2020/2/5 下午4:35
 * @version     1.0
 */
// @Component("push")
// @Order(1)
public class PushWatchTimerImpl implements WatchTimerService {

    /**
     * 线程池参数
     */
    private int corePoolSize = 5;
    private int maxPoolSize = 10;
    private long keepAliveTime = 0L;
    private TimeUnit unit = TimeUnit.MILLISECONDS;
    private BlockingDeque<Runnable> workQueue = new LinkedBlockingDeque<>();
    private ThreadFactory pushThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("PushWatchTimer-%d")
            .build();

    @Override
    public void run(String... args) throws Exception {
        // 创建线程池
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize, maxPoolSize, keepAliveTime,
                unit, workQueue, pushThreadFactory, new ThreadPoolExecutor.AbortPolicy());
    }

}
