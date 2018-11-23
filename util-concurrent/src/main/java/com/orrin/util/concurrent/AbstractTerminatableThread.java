package com.orrin.util.concurrent;

/**
 * 可停止的抽象线程
 *
 * 模式角色 两阶段终止模式（Two-phase Termination）
 * @author orrin on 2018-11-23
 */
public abstract class AbstractTerminatableThread extends Thread implements Terminatable {
    public final TerminationToken terminationToken;

    public AbstractTerminatableThread() {
        this(new TerminationToken());
    }

    /**
     * 线程间共享的线程终止标志实例
     *
     * @param terminationToken
     */
    public AbstractTerminatableThread(TerminationToken terminationToken) {
        this.terminationToken = terminationToken;
        terminationToken.register(this);
    }

    /**
     * 留给子类实现其线程处理逻辑
     *
     * @throws Exception
     */
    protected abstract void doRun() throws Exception;


    /**
     * 留给子类实现，用于实现线程停止后的一些清理工作
     */
    protected void doCleanup(Exception cause) {
    }

    /**
     * 留给子类实现，用于实现线程停止所需的操作
     */
    protected void doTerminate() {
    }

    @Override
    public void run() {
        Exception ex = null;
        try {
            while (true) {
                //在执行线程的处理逻辑前先判断线程停止的标志
                if (terminationToken.isToShutDown() && terminationToken.reservations.get() <= 0) {
                    break;
                }
                doRun();
            }
        } catch (Exception e) {
            //使得线程能够响应interrupt调用而退出
            ex = e;
        } finally {
            try {
                doCleanup(ex);
            } finally {
                terminationToken.notifyThreadTermination(this);
            }
        }
    }

    @Override
    public void interrupt() {
        terminate();
    }

    @Override
    public void terminate() {
        terminationToken.setToShutDown(true);
        try {
            doTerminate();
        } finally {
            //若无待处理的任务，则试图强制终止线程
            if (terminationToken.reservations.get() <= 0) {
                super.interrupt();
            }
        }
    }

    public void terminate(boolean waitUtilThreadTerminated) {
        terminate();
        if (waitUtilThreadTerminated) {
            try {
                this.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}

