package com.orrin.util.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;

/**
 * 串行线程封闭（Serial Thread Confinement）模式Serializer参与者可复用实现
 * @author orrin on 2018-11-23
 *
 * @param <T>
 *      Serializer向WorkerThead所提交的任务对应的类型
 * @param <V>
 *      service方法的返回值类型
 */
public abstract class AbstractSerializer<T, V> {
    private final TerminatableWorkerThread<T, V> workerThread;

    public AbstractSerializer(BlockingQueue<Runnable> workQueue, TaskProcssor<T, V> taskProcssor) {
        super();
        this.workerThread = new TerminatableWorkerThread<>(workQueue, taskProcssor);
    }

    /**
     * 留给子类实现。用于根据指定参数生成相应的任务实例
     * @param params
     * @return 任务实例。用于提交给WorkerThead
     */
    public abstract T makeTask(Object... params);

    /**
     * 对外暴露的方法
     * @param params
     *          客户端代码调用该方时所传递的参数列表
     * @return 可借以获取任务处理结果的Promise
     * @throws InterruptedException
     */
    public Future<V> service(Object... params) throws InterruptedException{
        T task = makeTask(params);
        Future<V> resultPromise = workerThread.submit(task);
        return resultPromise;
    }

    /**
     * 初始化该类对外暴露的服务
     */
    public void init(){
        workerThread.start();
    }

    /**
     * 停止该类对外暴露的服务
     */
    public void shutdown(){
        workerThread.terminate();
    }
}
