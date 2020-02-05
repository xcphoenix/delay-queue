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
    private static ConcurrentMap<String, RedisScript<String>> REDIS_SCRIPT = new ConcurrentHashMap<>();

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
    public Task removeTask(AbstractTask task) {
        List<String> keys = getRedisKeys(task, true, true);

        String taskField = RedisDataStruct.taskField(task);
        String waitingValue = RedisDataStruct.waitingValue(task);
        Object[] args = new String[]{
                taskField, waitingValue
        };

        log.info("Remove task:: task => " + JSON.toJSONString(task));

        String serializer = redisTemplate.execute(getRedisScript(LuaEnum.REMOVE_TASK), keys, args);

        return JSONObject.parseObject(serializer, Task.class);
    }

    @Override
    public void pushTask(String group, long maxScore, long minScore) {
        AbstractTask abstractTask = AbstractTask.of(group);
        List<String> keys = getRedisKeys(abstractTask, false, true, true);
        Object[] args = new Object[] {
                String.valueOf(maxScore), String.valueOf(minScore)
        };

        log.info("Push Task:: group => " + group + ", keys => " + keys.toString() + ", args => " + Arrays.toString(args));

        redisTemplate.execute(getRedisScript(LuaEnum.PUSH_TASK), keys, args);
    }

    @Override
    public List<Task> getTaskByTopic(String topic, long offset, int limit) {
        return null;
    }

    @PostConstruct
    @SuppressWarnings({"unchecked"})
    public void init() throws IOException {
        log.info("Init:: loading lua script");

        for (LuaEnum luaEnum : LuaEnum.values()) {
            log.debug("- load script: " + luaEnum.getFileName());

            REDIS_SCRIPT.put(luaEnum.toString(), RedisScript.of(luaEnum.getContent(), String.class));
        }

        log.info("Init:: load lua scripts success");
    }

    private RedisScript<String> getRedisScript(LuaEnum luaEnum) {
        return REDIS_SCRIPT.get(luaEnum.toString());
    }

    private List<String> getRedisKeys(AbstractTask task, boolean... bool) {
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
