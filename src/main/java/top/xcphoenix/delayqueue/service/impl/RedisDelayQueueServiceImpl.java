package top.xcphoenix.delayqueue.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import top.xcphoenix.delayqueue.constant.LuaEnum;
import top.xcphoenix.delayqueue.constant.RedisDataStruct;
import top.xcphoenix.delayqueue.pojo.AbstractTask;
import top.xcphoenix.delayqueue.pojo.Task;
import top.xcphoenix.delayqueue.service.DelayQueueService;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
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
    private static ConcurrentMap<String, RedisScript<String>> REDIS_SCRIPT = new ConcurrentHashMap<>();

    private StringRedisTemplate redisTemplate;

    public RedisDelayQueueServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void addTask(Task task) {
        List<String> keys = new ArrayList<>();
        String taskKey = RedisDataStruct.taskKey(task);
        String waitingKey = RedisDataStruct.waitingKey(task);
        keys.add(taskKey);
        keys.add(waitingKey);

        String taskField = RedisDataStruct.taskField(task);
        String taskSerializer = JSON.toJSONString(task);
        String waitingValue = RedisDataStruct.waitingValue(task);
        long execTime = task.getDelayExecTime().getTime();

        String[] args = new String[] {
                taskField,
                taskSerializer,
                waitingValue,
                String.valueOf(execTime)
        };

        log.info("Add Task:: task => " + taskSerializer);

        redisTemplate.execute(getRedisScript(LuaEnum.ADD_TASK), keys, (String[]) args);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Task removeTask(AbstractTask task) {
        List<String> keys = new ArrayList<>();
        String taskKey = RedisDataStruct.taskKey(task);
        String waitingKey = RedisDataStruct.waitingKey(task);
        keys.add(taskKey);
        keys.add(waitingKey);

        String taskField = RedisDataStruct.taskField(task);
        String waitingValue = RedisDataStruct.waitingValue(task);
        String[] args = new String[] {
                taskField, waitingValue
        };

        Object serializer = redisTemplate.execute(
                getRedisScript(LuaEnum.REMOVE_TASK),
                keys,
                (String[]) args);

        assert serializer != null;
        return JSONObject.parseObject(serializer.toString(), Task.class);
    }

    @Override
    public List<Task> getTaskByTopic(String topic, long offset, int limit) {
        return null;
    }

    @PostConstruct
    public void init() throws IOException {
        log.info("Init:: loading lua script");

        for (LuaEnum luaEnum : LuaEnum.values()) {
            log.debug("- load script: " + luaEnum.getFileName());

            REDIS_SCRIPT.put(luaEnum.toString(), RedisScript.of(luaEnum.getContent(), String.class));
        }

        log.info("Init:: load lua scripts success");
    }

    @SuppressWarnings({"rawtypes"})
    private RedisScript getRedisScript(LuaEnum luaEnum) {
        return REDIS_SCRIPT.get(luaEnum.toString());
    }

}
