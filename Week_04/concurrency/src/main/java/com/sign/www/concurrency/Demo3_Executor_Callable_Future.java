package com.sign.www.concurrency;

import java.util.concurrent.*;

public class Demo3_Executor_Callable_Future {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        int core = Runtime.getRuntime().availableProcessors();
        int keepAliveTime = 2000;
        int queueSize = 2048;
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        ExecutorService service = new ThreadPoolExecutor(core,core,
                keepAliveTime,TimeUnit.MILLISECONDS,new ArrayBlockingQueue<>(queueSize),
                handler);
        Future<Integer> result = service.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                Thread.sleep(1500);
                return 1;
            }
        });
        int data = result.get();
        System.out.println(data);
        service.shutdown();
    }
}
