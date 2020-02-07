package top.xcphoenix.delayqueue;

import org.junit.jupiter.api.Test;
import top.xcphoenix.delayqueue.pojo.BaseTask;

/**
 * @author      xuanc
 * @date        2020/2/7 下午4:31
 * @version     1.0
 */ 
public class TmpTest {

    @Test
    void testAnnNotNull() {
        BaseTask baseTask = BaseTask.of(null);
    }

}
