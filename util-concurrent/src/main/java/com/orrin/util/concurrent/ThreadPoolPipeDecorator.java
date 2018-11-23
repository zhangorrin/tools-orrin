package com.orrin.util.concurrent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/** 基于线程池的Pipe实现类
 * @author orrin on 2018-11-23
 *
 * @param <IN>
 *     输入类型
 * @param <OUT>
 *     输出类型
 */
public class ThreadPoolPipeDecorator<IN, OUT> implements Pipe<IN, OUT> {
    private final Pipe<IN, OUT> delegate;

    //线程池停止标志
    private final TerminationToken terminationToken;
    private final ExecutorService executorService;
    private final CountDownLatch stageProcessDoneLatch = new CountDownLatch(1);

    public ThreadPoolPipeDecorator(Pipe<IN, OUT> delegate, ExecutorService executorService) {
        super();
        this.delegate = delegate;
        this.executorService = executorService;
        terminationToken = TerminationToken.newInstance(executorService);
    }

    @Override
    public void setNextPipe(Pipe<?, ?> nextPipe) {
        delegate.setNextPipe(nextPipe);
    }

    @Override
    public void process(IN input) throws InterruptedException {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                int remainingReservations = -1;
                try {
                    delegate.process(input);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    remainingReservations = terminationToken.reservations.decrementAndGet();
                }

                if(terminationToken.isToShutDown()  &&  0 == remainingReservations){
                    //最后一个任务执行结束
                    stageProcessDoneLatch.countDown();
                }
            }
        };

        executorService.submit(task);
        terminationToken.reservations.incrementAndGet();
    }

    @Override
    public void init(PipeContext pipeCtx) {
        delegate.init(pipeCtx);
    }

    @Override
    public void shutdown(long timeout, TimeUnit unit) {
        terminationToken.setIsToShutdown();
        if(terminationToken.reservations.get() > 0){
            try {
                if(stageProcessDoneLatch.getCount() > 0){
                    //保证线程池中的所有任务都已经执行结束才delegate.shutdown
                    stageProcessDoneLatch.await(timeout, unit);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        delegate.shutdown(timeout, unit);
    }

    /**
     * 线程池停止标志
     * 每个ExecutorService实例对应唯一的一个TerminationToken实例
     * 这里使用了两阶段终止模式（Two-phase Termination）的思想来停止多个Pipe实例所共用的线程池实例
     */
    private static class TerminationToken extends com.orrin.util.concurrent.TerminationToken{
        private final static ConcurrentHashMap<ExecutorService, TerminationToken>
                INSTANCE_MAP = new ConcurrentHashMap<ExecutorService, TerminationToken>();

        private TerminationToken(){

        }

        void setIsToShutdown(){
            this.toShutDown = true;
        }

        static TerminationToken newInstance(ExecutorService executorService){
            TerminationToken token = INSTANCE_MAP.get(executorService);
            if(null == token){
                token = new TerminationToken();
                TerminationToken existingToken = INSTANCE_MAP.putIfAbsent(executorService, token);

                if(null != existingToken){
                    token = existingToken;
                }
            }

            return token;
        }
    }
}
