package com.orrin.util.concurrent;

import java.util.concurrent.TimeUnit;

/**
 * @author orrin on 2018-11-23
 * 对处理阶段的抽象。
 * 负责对输入进行处理，并将输出作为下一处理阶段的输入
 *
 * @param <IN>
 *     输入类型
 * @param <OUT>
 *     输出类型
 */
public interface Pipe<IN, OUT> {
    /**
     * 设置当前Pipe实例的下个Pipe实例
     * @param nextPipe
     */
    public void setNextPipe(Pipe<?,?> nextPipe);

    /**
     * 对输入的元素进行处理，并将处理结果作为下一个Pipe实例的输入
     * @param input
     * @throws InterruptedException
     */
    public void process(IN input) throws InterruptedException;

    public void init(PipeContext pipeCtx);
    public void shutdown(long timeout, TimeUnit unit);
}
