package top.xcphoenix.delayqueue.demo;

import top.xcphoenix.delayqueue.pojo.Callback;

/**
 * @author      xuanc
 * @date        2020/2/9 下午10:12
 * @version     1.0
 */ 
public class CallbackDemo implements Callback {

    @Override
    public void call() {
        System.out.println("exec ~~~~");
    }

}
