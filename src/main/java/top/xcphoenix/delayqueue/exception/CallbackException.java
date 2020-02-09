package top.xcphoenix.delayqueue.exception;

/**
 * @author      xuanc
 * @date        2020/2/9 下午8:51
 * @version     1.0
 */ 
public class CallbackException extends Exception {

    public CallbackException(String message) {
        super(message);
    }

    public CallbackException(String message, Throwable cause) {
        super(message, cause);
    }

}

