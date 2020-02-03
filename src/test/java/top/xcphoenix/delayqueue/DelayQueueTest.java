package top.xcphoenix.delayqueue;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.xcphoenix.delayqueue.pojo.AbstractTask;
import top.xcphoenix.delayqueue.pojo.Task;
import top.xcphoenix.delayqueue.service.DelayQueueService;

import java.sql.Timestamp;
import java.util.Random;

/**
 * @author      xuanc
 * @date        2020/2/3 下午1:28
 * @version     1.0
 */
@Slf4j
@SpringBootTest
public class DelayQueueTest {

    @Autowired
    private DelayQueueService delayQueueService;

    @Test
    void testAddTask() {
        for (int i = 0; i < 10; i++) {
            Task task = Task.newTask("test",
                    new Timestamp(System.currentTimeMillis() + new Random().nextLong()));
            delayQueueService.addTask(task);
        }
    }

    @Test
    void testRemoveTask() {
        AbstractTask abstractTask = AbstractTask.of(360595456L, "test");
        Task task = delayQueueService.removeTask(abstractTask);
        log.info(JSON.toJSONString(task, SerializerFeature.PrettyFormat));
    }

}
