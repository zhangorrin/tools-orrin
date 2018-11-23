package com.orrin.util.concurrent;

import java.util.concurrent.TimeUnit;

/**
 * Pipe的抽象实现类
 * 该类回调用其子类实现的doProcess方法对输入元素进行处理，并将相应的输出作为下一个Pipe实例的输入
 *
 * @author orrin on 2018-11-23
 *
 * @param <IN>
 *     输入类型
 * @param <OUT>
 *     输出类型
 */
public abstract class AbsractPipe<IN, OUT> implements Pipe<IN, OUT> {
    protected volatile Pipe<?, ?> nextPipe = null;
    protected volatile PipeContext PipeCtx = null;

    @Override
    public void setNextPipe(Pipe<?, ?> nextPipe) {
        this.nextPipe = nextPipe;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void process(IN input) throws InterruptedException {
        try {
            OUT out = doProcess(input);
            if(null != nextPipe){
                if(null != out){
                    ((Pipe<OUT, ?>) nextPipe).process(out);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (PipeException e) {
            PipeCtx.handleError(e);
        }

    }

    @Override
    public void init(PipeContext pipeCtx) {
        this.PipeCtx = pipeCtx;
    }

    @Override
    public void shutdown(long timeout, TimeUnit unit) {
        //什么也不做
    }

    /**
     * 留给子类实现，用于子类实现其任务处理逻辑
     */
    public abstract OUT doProcess(IN input) throws PipeException;
}