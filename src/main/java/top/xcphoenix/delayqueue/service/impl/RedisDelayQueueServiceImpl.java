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

    /**
     * 键前缀
     */
    private static final String KEY_PREFIX = ProjectConst.projectName + ":";
    /**
     * lua 脚本资源位置
     */
    private static final String LUA_SCRIPT_RES = "redis/lua/";
    /**
     * 要加载的 lua 脚本
     */
    private static final List<String> LUA_FILE_LIST = Arrays.asList(
            "addTask.lua",
            "pushTask.lua"
    );
    /**
     * lua 脚本及其对应的 sha1 值
     */
    private static Map<String, String> LUA_SHA1 = new HashMap<>();

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
        log.info("Loading lua script");

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

        log.info("Load lua scripts success");
    }

    /**
     * 加载 Lua 脚本
     */
    private void loadLuaScript(File directory, String luaFileName) throws IOException {
        File luaFile = new File(directory, luaFileName);
        String addTaskScriptContent = StreamUtils.copyToString(
                new FileInputStream(luaFile), StandardCharsets.UTF_8
        );
        log.debug(" - Script: " + luaFile.getAbsolutePath());

        String luaSha1 = Objects.requireNonNull(redisTemplate.getConnectionFactory())
                .getClusterConnection()
                .scriptLoad(addTaskScriptContent.getBytes());
        LUA_SHA1.put(luaFileName, luaSha1);

        log.debug("Load script success, SHA1: " + luaSha1);
    }

}
