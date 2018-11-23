package com.orrin.util.concurrent;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author orrin on 2018-11-23
 *
 * @param <IN>
 *     输入类型
 * @param <OUT>
 *     输出类型
 */
public class SimplePipeline<IN, OUT> extends AbsractPipe<IN, OUT> implements PipeLine<IN, OUT> {
    private final Queue<Pipe<?, ?>> pipes = new LinkedList<Pipe<?, ?>>();
    private final ExecutorService helperService;

    public SimplePipeline() {
        //创建固定线程数为1的线程池，整型的最大数的LinkedBlockingQueue的缓存队列
        this(Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "SimplePpeLine-Helper");
                t.setDaemon(true);
                return t;
            }
        }));
    }

    public SimplePipeline(final ExecutorService helperService) {
        super();
        this.helperService = helperService;
    }

    @Override
    public void shutdown(long timeout, TimeUnit unit) {
        Pipe<?,?> pipe;

        while(null != (pipe = pipes.poll())){
            pipe.shutdown(timeout, unit);
        }

        helperService.shutdown();
    }

    @Override
    public void addPipe(Pipe<?, ?> pipe) {
        pipes.add(pipe);
    }

    @Override
    public OUT doProcess(IN input) throws PipeException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void process(IN input) throws InterruptedException {
        @SuppressWarnings("unchecked")
        Pipe<IN, ?> firstPipe = (Pipe<IN, ?>) pipes.peek();

        firstPipe.process(input);
    }

    @Override
    public void init(PipeContext pipeCtx) {
        LinkedList<Pipe<?, ?>> pipesList = (LinkedList<Pipe<?, ?>>) pipes;
        Pipe<?, ?> prevPipe = this;
        //设置处理任务的先后顺序
        for(Pipe<?, ?> pipe: pipesList){
            prevPipe.setNextPipe(pipe);
            prevPipe = pipe;
        }

        Runnable task = new Runnable() {
            @Override
            public void run() {
                for(Pipe<?, ?> pipe: pipes){
                    pipe.init(pipeCtx);
                }
            }
        };

        helperService.submit(task);
    }

    public <INPUT, OUTPUT> void addAsWorkerThreadBasedPipe(Pipe<INPUT, OUTPUT> delegate, int workCount){
        addPipe(new WorkThreadPipeDecorator<INPUT, OUTPUT>(delegate, workCount));
    }

    public <INPUT, OUTPUT> void addAsThreadBasedPipe(Pipe<INPUT, OUTPUT> delegate, ExecutorService executorService){
        addPipe(new ThreadPoolPipeDecorator<INPUT, OUTPUT>(delegate, executorService));
    }

    public PipeContext newDefaultPipeContext(){
        return new PipeContext() {
            @Override
            public void handleError(PipeException exp) {
                helperService.submit(new Runnable() {
                    @Override
                    public void run() {
                        exp.printStackTrace();
                    }
                });
            }
        };
    }
}

