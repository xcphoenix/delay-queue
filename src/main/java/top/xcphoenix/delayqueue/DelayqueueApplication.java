package top.xcphoenix.delayqueue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author xuanc
 */
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class DelayqueueApplication {

    public static void main(String[] args) {
        SpringApplication.run(DelayqueueApplication.class, args);
    }

}
