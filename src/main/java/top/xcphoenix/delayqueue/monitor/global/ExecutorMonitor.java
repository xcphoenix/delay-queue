package top.xcphoenix.delayqueue.monitor.global;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author      xuanc
 * @date        2020/2/10 上午11:09
 * @version     1.0
 */
@Slf4j
@Component
public class ExecutorMonitor {

    private volatile int callTmpOccupy = 0;

    private ThreadPoolExecutor pushExecutor;
    private ThreadPoolExecutor consumeExecutor;
    private ThreadPoolExecutor callbackExecutor;

    public ExecutorMonitor(@Qualifier("pushThreadPool") ThreadPoolExecutor pushExecutor,
                           @Qualifier("consumeThreadPool") ThreadPoolExecutor consumeExecutor,
                           @Qualifier("callbackThreadPool") ThreadPoolExecutor callbackExecutor) {
        this.pushExecutor = pushExecutor;
        this.consumeExecutor = consumeExecutor;
        this.callbackExecutor = callbackExecutor;
    }

    /**
     * 获取当前回调线程池大小
     *
     * @return 线程池可用线程
     */
    public synchronized int getCallbackAvailableThreads() {
        return callbackExecutor.getMaximumPoolSize() - callbackExecutor.getActiveCount();
    }

}
