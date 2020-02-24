package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.xcphoenix.delayqueue.exception.RegisterException;
import top.xcphoenix.delayqueue.monitor.ServiceRegister;
import top.xcphoenix.delayqueue.monitor.global.TopicMonitor;
import top.xcphoenix.delayqueue.pojo.Task;
import top.xcphoenix.delayqueue.service.core.DelayQueueService;
import top.xcphoenix.delayqueue.utils.BeanUtil;

import java.sql.Timestamp;
import java.util.Random;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author xuanc
 * @version 1.0
 * @date 2020/2/23 下午4:43
 */
@RestController
public class DemoController {

    private ServiceRegister serviceRegister;
    private DelayQueueService delayQueueService;

    public DemoController(ServiceRegister serviceRegister, DelayQueueService delayQueueService) {
        this.serviceRegister = serviceRegister;
        this.delayQueueService = delayQueueService;
    }

    @GetMapping("/demo")
    public String demo(@RequestParam("num") int num) throws RegisterException {
        String group = "demo";
        String[] topics = new String[]{"t1", "t2"};
        serviceRegister.registerGroup(group);
        for (String topic : topics) {
            serviceRegister.registerTopic(group, topic);
        }

        for (int i = 0; i < num; i++) {
            Task task = Task.newTask(group,
                    topics[Math.abs(new Random().nextInt()) % topics.length],
                    new Timestamp(System.currentTimeMillis() + new Random().nextInt(30) * 1000)
            );
            delayQueueService.addTask(task);
        }

        return "add success";
    }

}
