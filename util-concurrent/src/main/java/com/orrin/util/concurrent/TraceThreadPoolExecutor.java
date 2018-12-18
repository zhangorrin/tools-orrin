package com.orrin.util.concurrent;

import java.util.concurrent.*;

/**
 * @author orrin.zhang on 2018/8/16.
 */
public class TraceThreadPoolExecutor extends ThreadPoolExecutor {

	public TraceThreadPoolExecutor(int corePoolSize, int maxPoolSize, int keepAliveTime) {
		super(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
		//对拒绝task的处理策略
		super.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

	}

	@Override
	public void execute(Runnable task) {
		ContextAwareRunnable contextAwareCallable = new ContextAwareRunnable(task, clientTrace(), Thread.currentThread().getName());
		super.execute(contextAwareCallable);
	}

	@Override
	public Future<?> submit(Runnable task) {
		ContextAwareRunnable contextAwareCallable = new ContextAwareRunnable(task, clientTrace(), Thread.currentThread().getName());
		return super.submit(contextAwareCallable);
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		ContextAwareCallable contextAwareCallable = new ContextAwareCallable(task, clientTrace());
		return super.submit(contextAwareCallable);
	}


	@Override
	protected <T> RunnableFuture<T> newTaskFor(Callable<T> task) {
		ContextAwareCallable contextAwareCallable = new ContextAwareCallable(task, clientTrace());
		return super.newTaskFor(contextAwareCallable);
	}

	private Exception clientTrace() {
		return new Exception("Client stack trace");
	}

	static class ContextAwareRunnable implements Runnable {
		private Runnable task;
		private Exception exception;
		private String clientThreadName;

		public ContextAwareRunnable(Runnable task, Exception exception, String clientThreadName) {
			this.task = task;
			this.exception = exception;
			this.clientThreadName = clientThreadName;
		}

		@Override
		public void run(){
			try {
				task.run();
			} catch (Exception e) {
				exception.printStackTrace();
				e.printStackTrace();
				throw e;
			} finally {

			}
		}
	}

	static class ContextAwareCallable<T> implements Callable<T> {
		private Callable<T> task;
		private Exception exception;

		public ContextAwareCallable(Callable<T> task, Exception exception) {
			this.task = task;
			this.exception = exception;
		}

		@Override
		public T call() throws Exception {
			try {
				return task.call();
			} catch (Exception e) {
				exception.printStackTrace();
				e.printStackTrace();
				throw e;
			} finally {

			}
		}
	}

	static void shutdownAndAwaitTermination(ExecutorService pool) {
		pool.shutdown(); // Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
				pool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!pool.awaitTermination(60, TimeUnit.SECONDS))
					System.err.println("Pool did not terminate");
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			pool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}


}
