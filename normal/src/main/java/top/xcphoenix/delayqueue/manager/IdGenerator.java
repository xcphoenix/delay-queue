package top.xcphoenix.delayqueue.manager;

/**
 * ID 生成器
 *
 * @author      xuanc
 * @date        2019/12/29 下午6:42
 * @version     1.0
 */ 
public interface IdGenerator {

    /**
     * 生成 id
     *
     * @return id
     */
    long getId();

    /**
     * 生成 id
     *
     * @param seed 数据中心种子
     * @return id
     */
    long getId(String seed);
}
