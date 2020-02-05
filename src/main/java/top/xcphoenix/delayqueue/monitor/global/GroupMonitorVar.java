package top.xcphoenix.delayqueue.monitor.global;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author      xuanc
 * @date        2020/2/5 下午8:10
 * @version     1.0
 */
@Component
public class GroupMonitorVar {

    /**
     * 组与线程的映射
     *
     * thread safe
     */
    private Map<String, Runnable> groupRunnable = new ConcurrentHashMap<>();

    /**
     * 获取当前的群组列表
     *
     * @return group list
     */
    public Set<String> getCurrGroups() {
        return groupRunnable.keySet();
    }

    /**
     * 添加新的组，若组存在则忽略，若不存在则创建新的线程监视加入的 group
     *
     * @param group 新的组
     */
    public void pushNewGroup(String group) {
        if (groupRunnable.containsKey(group)) {
            return;
        }
        // TODO 新建线程，添加到 groupRunnable
        groupRunnable.put(group, new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    /**
     * 当 group 全部处理完毕（taskKey、waitingKey 中没有对应数据）后，移除组以及监视线程
     *
     * @param group 要移除的 group
     */
    public void remOldGroup(String group) {
        Runnable runnable = groupRunnable.get(group);
        groupRunnable.remove(group);
        // TODO
        //   - 停止线程执行的任务
        //   - 设置标识位为结束，若线程超时仍未结束，强行中断
    }

    /**
     * 重启后定时从 redis 中获取 group 列表同步信息，防止特殊情况下数据丢失
     *
     * @param currGroups 当前
     */
    public void syncGroup(Set<String> currGroups) {
        Set<String> oriGroups = groupRunnable.keySet();
        Collection<String> oldGroups = CollectionUtils.subtract(oriGroups, currGroups);
        Collection<String> newGroups = CollectionUtils.subtract(currGroups, oriGroups);

        oldGroups.forEach(this::remOldGroup);
        newGroups.forEach(this::pushNewGroup);
    }

}
