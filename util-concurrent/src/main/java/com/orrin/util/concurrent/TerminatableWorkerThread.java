package com.orrin.util.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * 串行线程封闭（Serial Thread Confinement）模式WorkerThead参与者可复用实现
 * 该类使用来两阶段终止模式
 * @author orrin on 2018-11-23
 *
 * @param <T>
 *      Serializer向WorkerThead所提交的任务对应的类型
 * @param <V>
 *      任务处理结果的类型
 */
public class TerminatableWorkerThread<T, V> extends AbstractTerminatableThread {
    private final BlockingQueue<Runnable> workQueue;

    //负责正真执行任务的对象
    private final TaskProcssor<T, V> taskProcssor;

    public TerminatableWorkerThread(BlockingQueue<Runnable> workQueue, TaskProcssor<T, V> taskProcssor) {
        super();
        this.workQueue = workQueue;
        this.taskProcssor = taskProcssor;
    }

    /**
     * 接收并行任务，并将其穿行化
     * @param task
     *        任务
     * @return 可借以获取任务处理结果的Promise
     * @throws InterruptedException
     */
    public Future<V> submit(final T task) throws InterruptedException{
        Callable<V> callable = new Callable<V>() {
            @Override
            public V call() throws Exception {
                return taskProcssor.doProcess(task);
            }
        };

        FutureTask<V> ft = new FutureTask<V>(callable);
        workQueue.put(ft);

        terminationToken.reservations.incrementAndGet();
        return ft;
    }

    /**
     * 执行任务的处理逻辑
     *
     * @throws Exception
     */
    @Override
    protected void doRun() throws Exception {
        Runnable ft = workQueue.take();

        try {
            ft.run();
        } finally{
            terminationToken.reservations.decrementAndGet();
        }
    }
}
