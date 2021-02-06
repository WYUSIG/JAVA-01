package com.sign.www.concurrency;


import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @ClassName Demo8
 * @Description: TODO
 * @Author 钟显东
 * @Date 2021/2/5 0005
 * @Version V1.0
 **/
public class Demo8_Lock_Condition {

    private static volatile int i = 0;

    public static void main(String[] args) throws Exception {
        ReentrantLock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        new Thread(() -> {
            lock.lock();
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i = 1;
            condition.signalAll();
            lock.unlock();
        }).start();

        lock.lock();
        try{
            condition.await();
            System.out.println(i);
        }finally {
            lock.unlock();
        }
    }
}
