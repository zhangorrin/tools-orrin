package com.orrin.util.concurrent;

/**
 * 对复合Pipe的抽象。一个Pipeline实例可包含多个Pipe实例
 * @author orrin on 2018-11-23
 *
 * @param <IN>
 *     输入类型
 * @param <OUT>
 *     输出类型
 */
public interface PipeLine<IN, OUT> extends Pipe<IN, OUT> {
    /**
     * 往该Pipeline实例中添加一个Pipe实例
     * @param pipe
     */
    void addPipe(Pipe<?,?> pipe);
}