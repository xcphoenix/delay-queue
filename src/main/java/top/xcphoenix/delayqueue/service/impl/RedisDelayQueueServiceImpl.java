package top.xcphoenix.delayqueue.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import top.xcphoenix.delayqueue.constant.LuaEnum;
import top.xcphoenix.delayqueue.constant.RedisDataStruct;
import top.xcphoenix.delayqueue.pojo.BaseTask;
import top.xcphoenix.delayqueue.pojo.Task;
import top.xcphoenix.delayqueue.service.DelayQueueService;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    /**
     * lua 脚本及其对应的 sha1 值
     */
    private static ConcurrentMap<String, RedisScript<List<String>>> REDIS_SCRIPT = new ConcurrentHashMap<>();

    private StringRedisTemplate redisTemplate;

    public RedisDelayQueueServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void addTask(Task task) {
        List<String> keys = getRedisKeys(task, true, true);

        String taskField = RedisDataStruct.taskField(task);
        String taskSerializer = JSON.toJSONString(task);
        String waitingValue = RedisDataStruct.waitingValue(task);
        long execTime = task.getDelayExecTime().getTime();

        Object[] args = new String[]{
                taskField,
                taskSerializer,
                waitingValue,
                String.valueOf(execTime)
        };

        log.info("Add Task:: task => " + taskSerializer);

        redisTemplate.execute(getRedisScript(LuaEnum.ADD_TASK), keys, args);
    }

    @Override
    public Task removeTask(BaseTask task) {
        List<String> keys = getRedisKeys(task, true, true);

        String taskField = RedisDataStruct.taskField(task);
        String waitingValue = RedisDataStruct.waitingValue(task);
        Object[] args = new String[]{
                taskField, waitingValue
        };

        log.info("Remove task:: task => " + JSON.toJSONString(task));

        List<String> serializer = redisTemplate.execute(getRedisScript(LuaEnum.REMOVE_TASK), keys, args);
        if (serializer == null || serializer.size() == 0) {
            return null;
        }
        return JSONObject.parseObject(serializer.get(0), Task.class);
    }

    @Override
    public Long pushTask(String group, long maxScore, long minScore) {
        BaseTask abstractTask = BaseTask.of(group);
        List<String> keys = getRedisKeys(abstractTask, false, true, true);
        Object[] args = new Object[]{
                String.valueOf(maxScore), String.valueOf(minScore)
        };

        log.info("Push Task:: group => " + group + ", keys => " + keys.toString() + ", args => " + Arrays.toString(args));

        List<String> strScore = redisTemplate.execute(getRedisScript(LuaEnum.PUSH_TASK), keys, args);
        if (strScore == null || strScore.size() == 0) {
            return null;
        }
        // 处理 lua 返回科学计数法
        return new BigDecimal(strScore.get(0)).longValue();
    }

    @Override
    public List<Task> getTasksInList(String group, String topic, int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("limit can't < 0");
        }

        BaseTask baseTask = BaseTask.of(group, topic);

        String consumingKey = RedisDataStruct.consumingKey(baseTask);
        String taskKey = RedisDataStruct.taskKey(baseTask);

        List<Task> taskList = new ArrayList<>();
        List<String> tasksStr = redisTemplate.execute(
                getRedisScript(LuaEnum.GET_TASKS_IN_LIST),
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

    @PostConstruct
    @SuppressWarnings({"unchecked"})
    public void init() throws IOException {
        log.info("Init:: loading lua script");

        for (LuaEnum luaEnum : LuaEnum.values()) {
            log.debug("- load script: " + luaEnum.getFileName());

            REDIS_SCRIPT.put(luaEnum.toString(), RedisScript.of(luaEnum.getContent(), String[].class));
        }

        log.info("Init:: load lua scripts success");
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
