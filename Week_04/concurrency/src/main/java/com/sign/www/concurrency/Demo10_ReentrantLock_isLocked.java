package com.sign.www.concurrency;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @ClassName Demo10
 * @Description: TODO
 * @Author 钟显东
 * @Date 2021/2/6 0006
 * @Version V1.0
 **/
public class Demo10_ReentrantLock_isLocked {

    private static volatile int i = 0;

    public static void main(String[] args) {
        ReentrantLock lock = new ReentrantLock();
        new Thread(() -> {
            lock.lock();
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i = 1;
            lock.unlock();
        }).start();

        while (!lock.isLocked()) {

        }
        lock.lock();
        System.out.println(i);
        lock.unlock();
    }
}
