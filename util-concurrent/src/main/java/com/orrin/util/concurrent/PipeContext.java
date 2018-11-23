package com.orrin.util.concurrent;

/**
 * 对各个处理阶段的计算环境进行抽象，主要用于异常处理
 * @author orrin on 2018-11-23
 */
public interface PipeContext {
    /**
     * 用于对处理阶段抛出的异常进行处理
     * @param exp
     */
    public void handleError(PipeException exp);
}