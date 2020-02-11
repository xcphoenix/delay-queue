package top.xcphoenix.delayqueue.demo;

import lombok.extern.slf4j.Slf4j;
import top.xcphoenix.delayqueue.pojo.Callback;

import java.util.Random;

/**
 * @author      xuanc
 * @date        2020/2/9 下午10:12
 * @version     1.0
 */
@Slf4j
public class CallbackDemo implements Callback {

    @Override
    public void call() throws InterruptedException {
        synchronized (this) {
            log.info("exec start");
            Thread.sleep(Math.abs(new Random().nextLong()) % 10000);
            log.info("exec end");
        }
    }

}
