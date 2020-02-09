package top.xcphoenix.delayqueue.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.xcphoenix.delayqueue.pojo.Task;
import top.xcphoenix.delayqueue.service.DelayQueueService;

/**
 * @author      xuanc
 * @date        2020/2/9 下午2:58
 * @version     1.0
 */
@RequestMapping("/test")
@RestController
public class TestController {

    private DelayQueueService delayQueueService;

    public TestController(DelayQueueService delayQueueService) {
        this.delayQueueService = delayQueueService;
    }

    @PostMapping("/task")
    public void addTask(@RequestBody Task task) {
        delayQueueService.addTask(task);
    }

}
