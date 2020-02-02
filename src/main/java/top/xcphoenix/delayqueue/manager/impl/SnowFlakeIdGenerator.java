package top.xcphoenix.delayqueue.manager.impl;

import redis.clients.jedis.util.JedisClusterCRC16;
import top.xcphoenix.delayqueue.manager.IdGenerator;
import top.xcphoenix.delayqueue.utils.SnowFlakeIDUtil;

/**
 * @author      xuanc
 * @date        2019/12/30 下午9:32
 * @version     1.0
 */
public class SnowFlakeIdGenerator implements IdGenerator {

    /**
     * 雪花算法-机器id
     */
    private long machineId;

    /**
     * 使用 CRC16 设置 machineId
     * @param seed 种子
     */
    @Override
    public void setSeed(String seed) {
        this.machineId = JedisClusterCRC16.getSlot(seed);
    }

    @Override
    public long getId() {
        return SnowFlakeIDUtil.nextId(this.machineId);
    }

}
