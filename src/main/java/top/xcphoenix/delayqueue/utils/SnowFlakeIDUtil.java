package top.xcphoenix.delayqueue.utils;

/**
 * @author      xuanc
 * @date        2019/12/24 下午2:50
 * @version     1.0
 */ 
public class SnowFlakeIDUtil {

    private final static long START_STAMP;
    static {
        START_STAMP = System.currentTimeMillis();
    }

    private final static long SEQUENCE_BIT = 12;
    private final static long MACHINE_BIT = 5;
    private final static long DATA_CENTER_BIT = 5;

    private final static long MAX_DATA_CENTER_NUM = ~(-1L << DATA_CENTER_BIT);
    private final static long MAX_MACHINE_NUM = ~(-1L << MACHINE_BIT);
    private final static long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);

    /**
     * 机器标志较序列号的偏移量
     */
    private final static long MACHINE_OFFSET = SEQUENCE_BIT;

    /**
     * 数据中心较机器标志的偏移量
     */
    private final static long DATA_CENTER_OFFSET = SEQUENCE_BIT + MACHINE_BIT;

    /**
     * 时间戳较数据中心的偏移量
     */
    private final static long TIMESTAMP_OFFSET = DATA_CENTER_OFFSET + DATA_CENTER_BIT;

    private static long dataCenterId;
    private static long machineId = 0L;
    private static long sequence = 0L;
    private static long lastStamp = -1L;

    public static synchronized long nextId() {
        return nextId(SnowFlakeIDUtil.machineId);
    }

    public static synchronized long nextId(long machineId) {
        long currStamp = getNewStamp();

        if (currStamp < lastStamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id");
        }
        if (currStamp == lastStamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0L) {
                currStamp = getNextMill();
            }
        } else {
            sequence = 0L;
        }
        lastStamp = currStamp;

        return (currStamp - START_STAMP) << TIMESTAMP_OFFSET
                | dataCenterId << DATA_CENTER_OFFSET
                | machineId << MACHINE_OFFSET
                | sequence;
    }

    private static long getNextMill() {
        long mill = getNewStamp();
        while (mill <= lastStamp) {
            mill = getNewStamp();
        }
        return mill;
    }

    private static long getNewStamp() {
        return System.currentTimeMillis();
    }

}
