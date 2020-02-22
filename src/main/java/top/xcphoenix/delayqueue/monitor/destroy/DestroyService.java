package top.xcphoenix.delayqueue.monitor.destroy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 关闭线程池
 *
 * @author      xuanc
 * @date        2020/2/13 上午10:33
 * @version     1.0
 */
@Slf4j
@Service
public class DestroyService implements DisposableBean {

    private ThreadPoolExecutor pushExecutor;
    private ThreadPoolExecutor consumeExecutor;
    private ThreadPoolExecutor callbackExecutor;

    private long timeout = 20L;
    private TimeUnit timeUnit = TimeUnit.SECONDS;

    public DestroyService(@Qualifier("pushThreadPool") ThreadPoolExecutor pushExecutor,
                          @Qualifier("consumeThreadPool") ThreadPoolExecutor consumeExecutor,
                          @Qualifier("callbackThreadPool") ThreadPoolExecutor callbackExecutor) {
        this.pushExecutor = pushExecutor;
        this.consumeExecutor = consumeExecutor;
        this.callbackExecutor = callbackExecutor;
    }

    @Override
    public void destroy() throws Exception {
        log.info("Destroy service...");
        closeExecutor(consumeExecutor, "consumeExecutor");
        closeExecutor(pushExecutor, "pushExecutor");
        closeExecutor(callbackExecutor, "callbackExecutor");
        log.info("Destroy end");
    }

    private void closeExecutor(ThreadPoolExecutor executor, String executorName) {
        log.info("Begin terminal executor: " + executorName);
        // 阻止新的任务进入
        executor.shutdown();
        // 超时则强制关闭
        if (!executor.isTerminated()) {
            try {
                if (!executor.awaitTermination(timeout, timeUnit)) {
                    log.warn("terminal executor: " + executorName + " timeout(" + timeout + timeUnit.name() + "), force stop!");
                    List<Runnable> canceledJobs =  executor.shutdownNow();
                    log.warn("force stop end, there are " + canceledJobs.size() + " threads be canceled");
                }
            } catch (InterruptedException e) {
                log.error("Executor: "+ executorName + " be interrupted!", e);
            }
        }
        log.info("Terminal executor success");
    }

}
