package top.xcphoenix.delayqueue;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.xcphoenix.delayqueue.constant.ProjectConst;
import top.xcphoenix.delayqueue.demo.CallbackDemo;
import top.xcphoenix.delayqueue.pojo.BaseTask;
import top.xcphoenix.delayqueue.pojo.Task;
import top.xcphoenix.delayqueue.service.DelayQueueService;

import java.sql.Timestamp;
import java.util.List;
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
        String[] topics = new String[] {
                "testA",
                "testB",
                "testC",
                "testD"
        };
        for (int i = 0; i < 1000; i++) {
            Task task = Task.newTask(
                    "delay-queue",
                    topics[Math.abs(new Random().nextInt()) % 4],
                    new Timestamp(System.currentTimeMillis() + (new Random().nextLong() % (100 * 1000))));
            if (new Random().nextInt() % 2 == 0) {
                task.setCallback(CallbackDemo.class, null);
            }
            delayQueueService.addTask(task);
        }
    }

    @Test
    void testRemoveTask() {
        BaseTask abstractTask = BaseTask.of(293486592L, "delay-queue", "test");
        Task task = delayQueueService.removeTask(abstractTask);
        log.info(JSON.toJSONString(task, SerializerFeature.PrettyFormat));
    }

    @Test
    void testPushTask() {
        String group = ProjectConst.projectName;
        delayQueueService.pushTask(group, 9029740217268089000L, 0L);
    }

    @Test
    void testConsume() {
        List<Task> taskList = delayQueueService.consumeTasksInList("delay-queue", "testB", 5);
        log.info(JSON.toJSONString(taskList));
    }

}
