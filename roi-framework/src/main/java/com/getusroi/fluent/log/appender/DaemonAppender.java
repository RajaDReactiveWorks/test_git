package com.getusroi.fluent.log.appender;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DaemonAppender<E> implements Runnable {
    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

    private static final Logger LOG = LoggerFactory.getLogger(DaemonAppender.class);

    private AtomicBoolean start = new AtomicBoolean(false);
    private final BlockingQueue<E> queue;

    DaemonAppender(int maxQueueSize) {
        this.queue = new LinkedBlockingQueue<E>(maxQueueSize);
    }

    protected void execute() {
        THREAD_POOL.execute(this);
    }

    void log(E eventObject) {
        if (!queue.offer(eventObject)) {
            LOG.warn("Message queue is full. Ignore the message.");
        } else if (start.compareAndSet(false, true)) {
            execute();
        }
    }

    public void run() {

        try {
            for (; ; ) {
                append(queue.take());
            }
        } catch (InterruptedException ignore) {
            run();
        } catch (Exception ignore) {
            close();
        }
    }

    abstract protected void append(E rawData);

    protected void close() {
        synchronized (THREAD_POOL) {
            if (!THREAD_POOL.isShutdown()) {
                shutdownAndAwaitTermination(THREAD_POOL);
            }
        }
    }

    private static void shutdownAndAwaitTermination(ExecutorService pool) {
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