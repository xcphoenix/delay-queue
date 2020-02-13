package top.xcphoenix.delayqueue.service.threads;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import top.xcphoenix.delayqueue.service.core.DelayQueueService;
import top.xcphoenix.delayqueue.utils.BeanUtil;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author xuanc
 * @version 1.0
 * @date 2020/2/5 下午5:24
 */
@Slf4j
public class PushMonitorThread extends Thread {

    /**
     * 关注的 group
     */
    private String attentionGroup;
    /**
     * 下一次操作的时间
     */
    private AtomicLong nextTime = new AtomicLong(0);
    /**
     * get delayqueue for push
     */
    private DelayQueueService delayQueueService = BeanUtil.getBean(DelayQueueService.class);

    public PushMonitorThread(@NonNull String attentionGroup) {
        this.attentionGroup = attentionGroup;
    }

    @Override
    public void run() {
        synchronized (nextTime) {
            log.info("start monitor for waiting zset, attend group => " + attentionGroup);

            while (!Thread.currentThread().isInterrupted()) {
                log.info("loop scan...");
                long now = System.currentTimeMillis();

                if (nextTime.get() <= now) {
                    // push processing
                    Long newTime = delayQueueService.pushTask(attentionGroup, System.currentTimeMillis(), nextTime.get());
                    // update nextTime
                    nextTime.set(Objects.requireNonNullElse(newTime, Long.MAX_VALUE));
                    log.info("next exec time: " + nextTime.get());

                } else {
                    log.info("wait until time is go");
                    try {
                        // 阻塞
                        nextTime.wait(nextTime.get() - now);
                    } catch (InterruptedException e) {
                        log.warn("thread exit");
                        break;
                    }
                    log.info("wait end");
                }
            }

            log.info("PushTask -> end push task, attend group: " + attentionGroup);
        }
    }

    @Override
    public void interrupt() {
        log.info("terminal thread");
        super.interrupt();
    }

    public AtomicLong getNextTime() {
        return nextTime;
    }

    public void setNextTime(long newTime) {
        this.nextTime.set(newTime);
    }

}
