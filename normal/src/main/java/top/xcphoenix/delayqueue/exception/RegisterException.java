package top.xcphoenix.delayqueue.exception;

/**
 * @author      xuanc
 * @date        2020/2/11 下午4:52
 * @version     1.0
 */ 
public class RegisterException extends Exception {

    public RegisterException(String message) {
        super(message);
    }

    public RegisterException(String message, Throwable cause) {
        super(message, cause);
    }

}
