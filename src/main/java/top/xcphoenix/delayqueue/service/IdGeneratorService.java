package top.xcphoenix.delayqueue.service;

/**
 * ID 生成器
 *
 * @author      xuanc
 * @date        2019/12/29 下午6:42
 * @version     1.0
 */ 
public interface IdGeneratorService {

    /**
     * 设置产生 id 的种子
     *
     * @param seed 种子
     */
    void setSeed(long seed);

    /**
     * 生成 id
     *
     * @return id
     */
    long getId();

}
