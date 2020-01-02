package top.xcphoenix.delayqueue.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import top.xcphoenix.delayqueue.constant.ProjectConst;
import top.xcphoenix.delayqueue.pojo.Task;
import top.xcphoenix.delayqueue.service.DelayQueueService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author xuanc
 * @version 1.0
 * @date 2019/12/30 下午9:55
 */
@Slf4j
@Service
public class RedisDelayQueueServiceImpl implements DelayQueueService {

    private static final String KEY_PREFIX = ProjectConst.projectName + ":";
    private static final String LUA_SCRIPT_RES = "redis/lua/";
    private static final List<String> LUA_FILE_LIST = Arrays.asList(
            "addTask.lua"
    );
    private static final Map<String, String> LUA_SHA1 = new HashMap<>();

    private StringRedisTemplate redisTemplate;

    public RedisDelayQueueServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void addTask() {

    }

    @Override
    public Task removeTask() {
        return null;
    }

    @Override
    public List<Task> getTaskByTopic(String topic, long offset, int limit) {
        return null;
    }

    @Override
    public void init() throws IOException {
        log.info("loading lua script");

        URL url = getClass().getClassLoader().getResource(LUA_SCRIPT_RES);
        File directory = new File(
                URLDecoder.decode(
                        Objects.requireNonNull(url).getFile(),
                        StandardCharsets.UTF_8
                )
        );

        for (String file : LUA_FILE_LIST) {
            loadLuaScript(directory, file);
        }

        log.info("load lua scripts success");
    }

    private void loadLuaScript(File directory, String luaFileName) throws IOException {
        File luaFile = new File(directory, luaFileName);
        String addTaskScriptContent = StreamUtils.copyToString(
                new FileInputStream(luaFile), StandardCharsets.UTF_8
        );
        log.debug("File: " + luaFile.getAbsolutePath() + ", content: " + addTaskScriptContent);

        String luaSha1 = Objects.requireNonNull(redisTemplate.getConnectionFactory())
                .getClusterConnection()
                .scriptLoad(addTaskScriptContent.getBytes());
        LUA_SHA1.put(luaFileName, luaSha1);

        log.info("load script: " + luaFileName + " success");
        log.debug("script sha1: " + luaSha1);
    }

}
