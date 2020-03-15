package top.xcphoenix.delayqueue.service.core.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import top.xcphoenix.delayqueue.constant.LuaEnum;
import top.xcphoenix.delayqueue.constant.RedisDataStruct;
import top.xcphoenix.delayqueue.monitor.global.GroupMonitor;
import top.xcphoenix.delayqueue.pojo.BaseTask;
import top.xcphoenix.delayqueue.pojo.Task;
import top.xcphoenix.delayqueue.service.core.DelayQueueService;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author xuanc
 * @version 1.0
 * @date 2019/12/30 下午9:55
 */
@Slf4j
@Service
public class RedisDelayQueueServiceImpl implements DelayQueueService {

    private GroupMonitor groupMonitor;

    private StringRedisTemplate redisTemplate;

    /**
     * lua 脚本
     */
    private static Map<String, RedisScript<List<String>>> REDIS_SCRIPT = new ConcurrentHashMap<>();

    public RedisDelayQueueServiceImpl(GroupMonitor groupMonitor, StringRedisTemplate redisTemplate) {
        this.groupMonitor = groupMonitor;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void addTask(Task task) {
        List<String> keys = getRedisKeys(task, true, true);

        String taskField = RedisDataStruct.taskIdField(task);
        String taskSerializer = JSON.toJSONString(task);
        String waitingValue = RedisDataStruct.waitingValue(task);
        long execTime = task.getDelayExecTime().getTime();

        Object[] args = new String[]{
                taskField,
                taskSerializer,
                waitingValue,
                String.valueOf(execTime)
        };

        log.info("Add task: " + taskSerializer);

        redisTemplate.execute(getRedisScript(LuaEnum.ADD_TASK), keys, args);
        groupMonitor.updateAndNotify(task.getGroup(), task.getDelayExecTime());
    }

    @Override
    public Task removeTask(BaseTask task) {
        List<String> keys = getRedisKeys(task, true, true);

        String taskField = RedisDataStruct.taskIdField(task);
        String waitingValue = RedisDataStruct.waitingValue(task);
        Object[] args = new String[]{
                taskField, waitingValue
        };

        log.info("Remove task: " + JSON.toJSONString(task));

        List<String> serializer = redisTemplate.execute(getRedisScript(LuaEnum.REMOVE_TASK), keys, args);
        if (serializer == null || serializer.size() == 0) {
            return null;
        }
        return JSONObject.parseObject(serializer.get(0), Task.class);
    }

    @Override
    public Long pushTask(String group, long maxScore, long minScore) {
        BaseTask taskTask = BaseTask.of(group);
        List<String> keys = getRedisKeys(taskTask, false, true, true);
        Object[] args = new Object[]{
                String.valueOf(maxScore), String.valueOf(minScore)
        };

        log.info("Push task -> group: " + group + ", keys: " + keys.toString() + ", args: " + Arrays.toString(args));

        redisTemplate.execute(getRedisScript(LuaEnum.PUSH_TASK), keys, args);

        Set<ZSetOperations.TypedTuple<String>> strScore = redisTemplate.opsForZSet().rangeWithScores(RedisDataStruct.waitingKey(taskTask), 0, 0);
        if (strScore == null || strScore.isEmpty()) {
            return null;
        }

        // 处理 lua 返回科学计数法
        return BigDecimal.valueOf(strScore.iterator().next().getScore()).longValue();
    }

    @Override
    public List<Task> consumeTasksInList(String group, String topic, int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("limit can't < 0");
        }

        BaseTask baseTask = BaseTask.of(group, topic);
        String consumingKey = RedisDataStruct.consumingKey(baseTask);
        String taskKey = RedisDataStruct.taskKey(baseTask);

        List<Task> taskList = new ArrayList<>();
        List<String> tasksStr = redisTemplate.execute(
                getRedisScript(LuaEnum.CONSUME_TASKS_IN_LIST),
                Arrays.asList(consumingKey, taskKey),
                String.valueOf(limit));

        if (tasksStr == null || tasksStr.size() == 0) {
            return null;
        }

        for (String taskStr : tasksStr) {
            Task task = JSON.parseObject(taskStr, Task.class);
            taskList.add(task);
        }

        return taskList;
    }

    /**
     * init lua scripts
     */
    @PostConstruct
    @SuppressWarnings({"unchecked"})
    public void init() throws IOException {
        log.info("Init -> loading lua script");

        for (LuaEnum luaEnum : LuaEnum.values()) {
            log.debug("~ load script: " + luaEnum.getFileName());

            REDIS_SCRIPT.put(luaEnum.toString(), RedisScript.of(luaEnum.getContent(), String[].class));
        }

        log.info("Init -> load lua scripts success");
    }

    private RedisScript<List<String>> getRedisScript(LuaEnum luaEnum) {
        return REDIS_SCRIPT.get(luaEnum.toString());
    }

    private List<String> getRedisKeys(BaseTask task, boolean... bool) {
        List<String> keys = new ArrayList<>();
        List<String> redisKeys = RedisDataStruct.getRedisKeys(task);
        for (int i = 0; i < redisKeys.size() && i < bool.length; i++) {
            if (bool[i]) {
                keys.add(redisKeys.get(i));
            }
        }
        return keys;
    }

}
