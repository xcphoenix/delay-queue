package top.xcphoenix.delayqueue.thread;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author      xuanc
 * @date        2020/2/6 下午4:15
 * @version     1.0
 */
public class ThreadInterruptedTest {

    @Test
    void stopThread() {
        ExampleThread target = new ExampleThread();
        Thread thread = new Thread(target);
        thread.start();

        try {
            Thread.sleep(5 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("I want closed thread");
        // target.endJob();
        thread.interrupt();
        System.out.println("I want closed thread");

        try {
            Thread.sleep(20 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testNonBlock() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    System.out.println("I'm live");
                }
            }
        });
        thread.start();

        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("[Main] I want close thread");
        // 非阻塞下不会停止
        thread.interrupt();
        System.out.println("[Main] I tried close thread");

        try {
            Thread.sleep(5 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testFuture() {
        Runnable runnable = new ExampleThread();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future future = executor.submit(runnable);

        try {
            Thread.sleep(5 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("I want closed thread");
        future.cancel(true);
        System.out.println("I tried closed thread");

        try {
            Thread.sleep(5 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testNonBlockFuture() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    // System.out.print('.');
                }
            }
        };
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future future = executor.submit(runnable);

        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("I want closed thread");
        future.cancel(true);
        System.out.println("I tried closed thread");

        try {
            Thread.sleep(5 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
