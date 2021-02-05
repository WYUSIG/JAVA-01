package com.sign.www.concurrency;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @ClassName Demo4
 * @Description: TODO
 * @Author 钟显东
 * @Date 2021/2/3 0003
 * @Version V1.0
 **/
public class Demo4_Callable_FutureTask {

    public static void main(String[] args) throws Exception {
        FutureTask<Integer> task = new FutureTask<>(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                Thread.sleep(1500);
                return 1;
            }
        });
        new Thread(task).start();
        System.out.println(task.get());
    }
}
