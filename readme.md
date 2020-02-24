# DelayQueue - 基于Redis的延迟队列

[TOC]

## 项目介绍

项目基于 Redis 实现了简单的延迟队列，在创建了需要关注的 ***Group*** 以及 ***Topic*** 后，可以创建任务，指定任务执行的时间、执行任务的接口以及需要的参数。当到达任务的执行时间时，会从 Redis 中取出数据并执行任务。

## 项目结构

```shell
./
├── autoconfigure			// springboot-autoconfigure
├── demo                    // demo演示
├── normal                  // 非springboot-starter代码
│   ├── pom.xml
│   └── src
│       ├── main　　　　　　　 // main/resources/redis下提供了集群搭建脚本以及Lua脚本
│       └── test
└── starter                 // springboot-starter
```

项目提供了普通的 SpringBoot 版本，以及对应的 SpringBoot Starter，通过引入starter包可以在另一个项目中使用延迟队列的api。

> autoconfigure 与 normal 中主体代码大致相同，区别只在于 autoconfigure、starter 提供了 springboot starter 以便于第三方项目使用，并移除了资源文件。

## 使用说明

0. 安装 Redis 或配置集群

1. 使用Idea等导入 autoconfigure、starter 目录

2. 在上面的两个项目中依次使用 `mvn install` 打包项目并放置在本地的 mvn 仓库中

3. 在自己的项目中添加依赖：

   ```xml
   <dependency>
       <groupId>top.xcphoenix</groupId>
       <artifactId>delayqueue-spring-boot-starter</artifactId>
       <version>0.0.1-SNAPSHOT</version>
   </dependency>
   ```

4. 在项目的 resource/redis/lua 下放置对应的 Lua 脚本

5. 配置集群信息以及JedisPool参数

   ```yml
   # Redis Config
   delayqueue:
     cluster:
       # 集群节点配置
       nodes:
         - 169.69.2.2:7001
         - 169.69.2.3:7002
         - 169.69.2.4:7003
         - 169.69.2.5:7004
         - 169.69.2.6:7005
         - 169.69.2.7:7006
       max-redirects: 3
     # jedis连接池配置
     jedis:
       pool:
         max-total: 300
         max-wait: 2000
         max-idle: 50
         min-idle: 10
         num-tests-per-eviction-run: 1024
         min-evictable-idle-time-millis: 1800000
         soft-min-evictable-idle-time-millis: 10000
         test-on-borrow: true
         test-while-idle: true
         time-between-eviction-runs-mills: 30000
   ```

6. 使用相应的 API 去完成某个功能，代码示例：

   ```java
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
   ```

   具体可参考demo示例：https://github.com/PhoenixXC/DelayQueue/tree/master/demo

   Lua 脚本可以自己编写或使用：https://github.com/PhoenixXC/DelayQueue/tree/master/normal/src/main/resources/redis/lua

除此也可以直接导入 normal 目录，在此项目基础上完成业务逻辑。



## 项目实现

### Group、Topic处理

为了区分项目和业务逻辑，使用 Group、和 Topic 来划分任务。Group 用于区分项目，Topic 用于区分项目中的某个业务。

在添加任务前，必须指定所在的 Group 以及 Topic，否则任务不会被监听线程扫描。

Group、Topic 数据以Hash存储在 Redis 中的键：***MONITOR:SUMMARY***

注册 Group、Topic 后，会将数据添加到此键中，防止发生意外事故导致数据丢失。

服务开启之后会启动两个线程去扫描 Redis 中保存的 Group、Topic 数据，并做一些初始化工作，初始化完成之后会为 Group、Topic 启动对应的监听线程。

### 集群处理

由于集群不允许操作在不同节点上的key，使用 hashtag 让同一 Group 的任务放在一台机子上。为了降低网络延迟使用了 Lua 脚本来进行操作，同时保证了任务执行的原子性。

### 数据组织

1. 任务队列

   **类型**： `HASH`

   **名字**： `{GROUP}:DETAIL`

   - `{id 任务id}:{使用 Json 序列化任务对象}`

2. 待消费队列

   **类型**： `ZSET`

   **名字**： `{GROUP}:WAITING`

   **值**：     

   - <kbd>value</kbd>`{topic}:{id}`
   - <kbd>score</kbd> `{execTime}`

   任务等待队列，使用任务执行时间作为分数

   定时启动、根据时间轮训判断是否有任务可以被消费

3. 消费队列

   放置要处理的任务，每次从队列中取出一定量的任务去处理，如果任务为空就阻塞。

   **类型**： `LIST`

   **名字**： `{GROUP}:CONSUMING:TOPIC`

   **值**：     `{id 任务id}`

### 数据监听

使用线程池去管理group推送线程、topic 消费线程以及最后的任务回调线程。

#### 推送线程

在添加任务后，会将任务的id以及执行时间存储到等待队列中（ZSET），项目会自动为每一个 Group 创建一个推送线程，去监听等待队列，并获取队列中的最早执行时间，与当前时间相比后，若晚于当前时间，说明有任务可以被消费，就取出可以消费的任务推送至对应的待消费队列，否则就 wait 等到下一个最早任务的执行时间。

#### 消费线程

服务会为每一个 Topic 创建线程，去监听所对应的待消费队列，使用 Redis 的 BLPOP 阻塞操作避免空轮训，当有任务可以被消费则根据任务回调线程池可用的线程数量取出合适的任务，交由任务回调线程会处理。

#### 回调线程

回调线程会根据任务的回调接口以及传递的参数，通过反射构建对象，之后会调用会调用回调接口中的方法。

### 服务关闭

通过实现 `DisposableBean` 接口再服务关闭前处理线程池等的关闭操作：

```java
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
```

阻止新的任务进入线程池，若线程池还未关闭，等待20s之后，若仍然有线程处理，强制关闭线程池。

## Demo演示

![](https://raw.githubusercontent.com/PhoenixXC/DelayQueue/master/note/demo-show.gif)

