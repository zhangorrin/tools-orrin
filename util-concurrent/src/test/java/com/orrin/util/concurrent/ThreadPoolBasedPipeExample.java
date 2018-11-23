package com.orrin.util.concurrent;

import java.util.Random;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author orrin on 2018-11-23
 */
public class ThreadPoolBasedPipeExample {
    public static void main(String[] args) {
        final ThreadPoolExecutor threadPoolExecutor;
        threadPoolExecutor = new ThreadPoolExecutor(1, Runtime.getRuntime().availableProcessors()*2, 60, TimeUnit.MINUTES,
                new SynchronousQueue<Runnable>(), new ThreadPoolExecutor.CallerRunsPolicy());

        final SimplePipeline<String, String> pipeLine = new SimplePipeline<String, String>();

        Pipe<String, String> pipe = new AbsractPipe<String, String>() {
            @Override
            public String doProcess(String input) throws PipeException {
                String result = input + "->[pipe1, " + Thread.currentThread().getName() + "]";
                System.out.println(result);
                return result;
            }
        };

        pipeLine.addAsThreadBasedPipe(pipe, threadPoolExecutor);

        pipe = new AbsractPipe<String, String>() {
            @Override
            public String doProcess(String input) throws PipeException {
                String result = input + "->[pipe2, " + Thread.currentThread().getName() + "]";
                System.out.println(result);

                try {
                    Thread.sleep(new Random().nextInt(100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return result;
            }
        };

        pipeLine.addAsThreadBasedPipe(pipe, threadPoolExecutor);

        pipe = new AbsractPipe<String, String>() {
            @Override
            public String doProcess(String input) throws PipeException {
                String result = input + "->[pipe3, " + Thread.currentThread().getName() + "]";
                System.out.println(result);

                try {
                    Thread.sleep(new Random().nextInt(200));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return result;
            }

            @Override
            public void shutdown(long timeout, TimeUnit unit) {
                threadPoolExecutor.shutdown();

                try {
                    threadPoolExecutor.awaitTermination(timeout, unit);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        pipeLine.addAsThreadBasedPipe(pipe, threadPoolExecutor);

        pipeLine.init(pipeLine.newDefaultPipeContext());

        int N = 100;

        try {
            for(int i=0; i<N; i++){
                pipeLine.process("Task-" + i);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        pipeLine.shutdown(10, TimeUnit.SECONDS);
    }
}
