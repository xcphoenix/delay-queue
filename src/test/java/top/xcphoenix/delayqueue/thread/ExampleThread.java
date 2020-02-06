package top.xcphoenix.delayqueue.thread;

/**
 * @author      xuanc
 * @date        2020/2/6 下午4:12
 * @version     1.0
 */ 
public class ExampleThread implements Runnable {

    private boolean loop = true;

    /**
     * 通过 synchronized 获取监视器对象所有权
     */
    @Override
    public synchronized void run() {
        System.out.println("thread is running");
        while (!Thread.currentThread().isInterrupted()) {
            System.out.println("I'm live");
            System.out.println("live?" + Thread.currentThread().isInterrupted());
            try {
                wait(10 * 1000);
            } catch (InterruptedException e) {
                System.out.println("i dead");
                e.printStackTrace();
                break;
            }
            System.out.println("I'm live after await");
        }
        System.out.println("I'm dead now");
    }

    public void endJob() {
        this.loop = false;
        /*
         * 获取的线程是调用此函数的线程
         */
        Thread.currentThread().interrupt();
    }

}
