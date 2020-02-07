package top.xcphoenix.delayqueue.constant;

import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author      xuanc
 * @date        2020/2/3 下午12:01
 * @version     1.0
 */
public enum LuaEnum {

    /**
     * 添加任务
     */
    ADD_TASK("addTask"),
    /**
     * 将任务推送至待消费队列
     */
    PUSH_TASK("pushTask"),
    /**
     * 移除未在待消费队列中的任务
     */
    REMOVE_TASK("removeTask"),
    /**
     * 从任务列表中获取任务
     */
    GET_TASKS_IN_LIST("getTasksInList");

    LuaEnum(String fileName) {
        this.fileName = fileName + ".lua";
    }

    private String fileName;
    private static final String LOCATION;
    static {
        String res = "redis/lua";
        URL url = LuaEnum.class.getClassLoader().getResource(res);
        LOCATION = URLDecoder.decode(
                        Objects.requireNonNull(url).getFile(),
                        StandardCharsets.UTF_8
                );
    }

    public String getFileName() {
        return fileName;
    }

    public String getContent() throws IOException {
        File file = new File(LOCATION, this.fileName);
        return StreamUtils.copyToString(
                new FileInputStream(file), StandardCharsets.UTF_8
        );
    }

}