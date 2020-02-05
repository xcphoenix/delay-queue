package top.xcphoenix.delayqueue;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * @author      xuanc
 * @date        2020/2/5 下午9:31
 * @version     1.0
 */
public class CollectionTest {

    @Test
    void testSet() {
        Set<String> a = new HashSet<>();
        Set<String> b = new HashSet<>();
        a.add("1");
        a.add("2");
        a.add("3");
        a.add("4");

        b.add("3");
        b.add("4");
        b.add("5");
        System.out.println(CollectionUtils.subtract(a, b));
        System.out.println(CollectionUtils.subtract(b, a));
    }

}
