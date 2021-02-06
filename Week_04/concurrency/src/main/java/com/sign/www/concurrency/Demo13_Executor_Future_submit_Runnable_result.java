package com.sign.www.concurrency;

import java.util.concurrent.*;

public class Demo13_Executor_Future_submit_Runnable_result {

    private static volatile int i = 0;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        int core = Runtime.getRuntime().availableProcessors();
        int keepAliveTime = 2000;
        int queueSize = 2048;
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        ExecutorService service = new ThreadPoolExecutor(core, core,
                keepAliveTime, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(queueSize),
                handler);
        Future<Integer> result = service.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                i = 1;
            }
        },1);
        if(result.get() == 1){
            System.out.println(i);
        }
        service.shutdown();
    }
}
