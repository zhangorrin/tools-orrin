package com.orrin.util.concurrent;

import java.lang.ref.WeakReference;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程停止标志
 * @author orrin on 2018-11-23
 */
public class TerminationToken {
    protected volatile boolean toShutDown = false;
    public final AtomicInteger reservations = new AtomicInteger();

    /**
     * 在多个可停止线程实例共享一个TerminationToken实例的情况下，该队列用于记录那些共享TerminationToken实例
     * 的可停止线程，以便尽可能减少锁的使用情况下，实现这些线程的停止
     */
    private final Queue<WeakReference<Terminatable>> coordinatedThread;


    public TerminationToken() {
        coordinatedThread = new ConcurrentLinkedQueue<WeakReference<Terminatable>>();
    }

    public boolean isToShutDown() {
        return toShutDown;
    }

    public void setToShutDown(boolean toShutDown) {
        this.toShutDown = toShutDown;
    }

    protected void register(Terminatable thread) {
        coordinatedThread.add(new WeakReference<Terminatable>(thread));
    }

    /**
     * 通知TerminationToken实例，共享该实例的所有可停止线程中的一个线程停止了，以便其停止其他为被停止的线程
     *
     * @param thread 已停止的线程
     */
    protected void notifyThreadTermination(Terminatable thread) {
        WeakReference<Terminatable> wrThread;
        Terminatable otherThread;

        while (null != (wrThread = coordinatedThread.poll())) {
            otherThread = wrThread.get();
            if (null != otherThread && otherThread != thread) {
                otherThread.terminate();
            }
        }
    }
}

