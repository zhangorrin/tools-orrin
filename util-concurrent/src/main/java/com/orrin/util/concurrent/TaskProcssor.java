package com.orrin.util.concurrent;

/**
 * 对任务处理的抽象
 * @author orrin on 2018-11-23
 *
 * @param <T>
 *      任务的类型
 * @param <V>
 *      任务处理结果的类型
 */
public interface TaskProcssor<T, V> {
    V doProcess(T task) throws Exception;
}
