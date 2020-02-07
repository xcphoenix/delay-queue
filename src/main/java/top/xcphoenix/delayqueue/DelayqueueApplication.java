package top.xcphoenix.delayqueue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author xuanc
 */
@EnableAsync
@SpringBootApplication
public class DelayqueueApplication {

    public static void main(String[] args) {
        SpringApplication.run(DelayqueueApplication.class, args);
    }

}
