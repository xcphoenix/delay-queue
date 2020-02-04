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
     * 机器id
     */
    private long defaultMachineId = 0L;
    /**
     * 数据中心id
     */
    private long defaultDataCenterId = 0L;

    @Override
    public long getId() {
        long machineId = getProcessId() % SnowFlakeIDUtil.MAX_MACHINE_NUM;
        return SnowFlakeIDUtil.nextId(this.defaultDataCenterId, machineId);
    }

    @Override
    public long getId(String seed) {
        long machineId = getProcessId() % SnowFlakeIDUtil.MAX_MACHINE_NUM;
        long tmpDataCenterId = JedisClusterCRC16.getSlot(seed) % SnowFlakeIDUtil.MAX_DATA_CENTER_NUM;
        return SnowFlakeIDUtil.nextId(tmpDataCenterId, machineId);
    }

    private long getProcessId() {
        return Thread.currentThread().getId();
    }

}
