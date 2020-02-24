package top.xcphoenix.delayqueue.utils;

/**
 * @author xuanc
 * @version 1.0
 * @date 2019/12/24 下午2:50
 */
public class SnowFlakeIDUtil {

    /**
     * 开始时间戳
     */
    private final static long START_STAMP;

    static {
        START_STAMP = System.currentTimeMillis();
    }

    /**
     * 序列号
     */
    private final static long SEQUENCE_BIT = 12;
    /**
     * 机器
     */
    private final static long MACHINE_BIT = 5;
    /**
     * 数据标示
     */
    private final static long DATA_CENTER_BIT = 5;

    public final static long MAX_DATA_CENTER_NUM = ~(-1L << DATA_CENTER_BIT);
    public final static long MAX_MACHINE_NUM = ~(-1L << MACHINE_BIT);
    private final static long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);

    /**
     * 机器标志较序列号的偏移量 12
     */
    private final static long MACHINE_OFFSET = SEQUENCE_BIT;

    /**
     * 数据中心较机器标志的偏移量 17
     */
    private final static long DATA_CENTER_OFFSET = SEQUENCE_BIT + MACHINE_BIT;

    /**
     * 时间戳较数据中心的偏移量 22
     */
    private final static long TIMESTAMP_OFFSET = DATA_CENTER_OFFSET + DATA_CENTER_BIT;

    private static long dataCenterId;
    private static long machineId;
    private static long sequence = 0L;
    private static long lastStamp = -1L;

    public static synchronized long nextId() {
        return nextId(0, SnowFlakeIDUtil.machineId);
    }

    public static synchronized long nextId(long dataCenterId, long machineId) {
        if (dataCenterId >= MAX_DATA_CENTER_NUM || machineId >= MAX_MACHINE_NUM) {
            throw new RuntimeException("invalid data center id or machine id");
        }

        long currStamp = getNewStamp();

        if (currStamp < lastStamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id");
        }
        if (currStamp == lastStamp) {
            // 序列号增加
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // 序列号溢出，获取新的时间戳
            if (sequence == 0L) {
                currStamp = getNextMill();
            }
        } else {
            // 序列号重置
            sequence = 0L;
        }
        lastStamp = currStamp;

        return  ((currStamp - START_STAMP) << TIMESTAMP_OFFSET)
                | (dataCenterId << DATA_CENTER_OFFSET)
                | (machineId << MACHINE_OFFSET)
                | sequence;
    }

    /**
     * 获取比最新时间戳: <code>lastStamp</code> 更新的时间戳
     */
    private static synchronized long getNextMill() {
        long mill = getNewStamp();
        while (mill <= lastStamp) {
            mill = getNewStamp();
        }
        return mill;
    }

    /**
     * 获取当前时间戳
     */
    private static synchronized long getNewStamp() {
        return System.currentTimeMillis();
    }

}
