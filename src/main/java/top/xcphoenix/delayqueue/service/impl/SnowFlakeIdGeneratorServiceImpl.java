package top.xcphoenix.delayqueue.service.impl;

import redis.clients.jedis.util.JedisClusterCRC16;
import top.xcphoenix.delayqueue.service.IdGeneratorService;
import top.xcphoenix.delayqueue.utils.SnowFlakeIDUtil;

/**
 * @author      xuanc
 * @date        2019/12/30 下午9:32
 * @version     1.0
 */
public class SnowFlakeIdGeneratorServiceImpl implements IdGeneratorService {

    /**
     * 雪花算法-机器id
     */
    private long machineId;

    /**
     * 使用 CRC16 设置 machineId
     * @param seed 种子
     */
    @Override
    public void setSeed(long seed) {
        this.machineId = JedisClusterCRC16.getSlot(String.valueOf(seed));
    }

    @Override
    public long getId() {
        return SnowFlakeIDUtil.nextId(this.machineId);
    }

}
