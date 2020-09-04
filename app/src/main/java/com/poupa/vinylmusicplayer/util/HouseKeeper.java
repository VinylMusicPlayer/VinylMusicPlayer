package com.poupa.vinylmusicplayer.util;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Manages a background thread to run async house-keeping tasks
 *
 * @author SC (soncaokim)
 */
public class HouseKeeper {
    public final static TimeUnit PRECISION = TimeUnit.MICROSECONDS;
    public final static long ONE_MILLIS = PRECISION.convert(1, TimeUnit.MILLISECONDS);
    public final static long ONE_SEC = PRECISION.convert(1, TimeUnit.SECONDS);
    public final static long ONE_MINUTE = PRECISION.convert(60, TimeUnit.SECONDS);

    private abstract static class Task implements Runnable, Delayed {
        private final long expectedStart; // expressed in the unit specified by PRECISION
        private final boolean isRecurrent;

        private static long now() {
            return PRECISION.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }

        public Task(long delay, boolean isRecurrent) {
            this.expectedStart = now() + delay;
            this.isRecurrent = isRecurrent;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            long diff = expectedStart - now();
            return unit.convert(diff, PRECISION);
        }

        @Override
        public int compareTo(Delayed other) {
            long diff = this.expectedStart - ((Task)other).expectedStart;
            if (diff < Integer.MIN_VALUE) {diff = Integer.MIN_VALUE;}
            if (diff > Integer.MAX_VALUE) {diff = Integer.MAX_VALUE;}
            return (int)diff;
        }
    }

    private static HouseKeeper sInstance = null;
    private DelayQueue<Task> taskQueue = new DelayQueue<>();
    private Thread runner;

    public static synchronized HouseKeeper getInstance() {
        if (sInstance == null) {
            sInstance = new HouseKeeper();
        }
        return sInstance;
    }

    private HouseKeeper() {}

    public void start() {
        if (runner != null) {
            return;
        }

        runner = new Thread(() -> {
            do {
                try {Thread.sleep(1);} catch (InterruptedException ignored) {}

                Task task;
                synchronized (this) {
                    task = taskQueue.poll();
                }
                if (task != null) {
                    try {
                        task.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (task.isRecurrent) {
                        taskQueue.add(task);
                    }
                }
            } while (!Thread.interrupted());
        });
        runner.setPriority(Thread.MIN_PRIORITY);
        runner.start();
    }

    // Note: Any pending task are just bluntly ignored
    public void stop() {
        if (runner == null) {
            return;
        }
        runner.interrupt();
        try {
            runner.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void addTask(long delay, boolean isRecurrent, Runnable runnable) {
        Task task = new Task(delay, isRecurrent) {
            @Override
            public void run() {
                runnable.run();
            }
        };
        taskQueue.add(task);
    }
}
