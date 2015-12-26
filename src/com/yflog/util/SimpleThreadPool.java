package com.yflog.util;


import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by vincent on 12/23/15.
 */
public class SimpleThreadPool<Job extends Runnable> implements ThreadPool<Job> {

    private static final int MAX_WORKER_NUMBERS = 50;
    private static final int DEFAULT_WORKER_NUMBERS = 20;
    private static final int MIN_WORKER_NUMBERS = 10;

    private final LinkedList<Job> jobs = new LinkedList<Job>();

    private final List<Worker> workers = Collections.synchronizedList(new ArrayList<Worker>());

    private int workerCount = DEFAULT_WORKER_NUMBERS;

    private AtomicInteger threadNum = new AtomicInteger(0);

    public SimpleThreadPool(int num) {
        workerCount = num > MAX_WORKER_NUMBERS ?
                MAX_WORKER_NUMBERS : num < MIN_WORKER_NUMBERS ? MIN_WORKER_NUMBERS : num;
        initWorkers(workerCount);
    }


    @Override
    public void submit(Job job) {
        if (job != null) {
            synchronized (jobs) {
                jobs.add(job);
                jobs.notify();
            }
        }
    }


    public void shutdown() {
        for (Worker worker : workers) {
            worker.shutdown();
        }
    }

    public void addWorkers(int num) {

    }

    public void removeWorkers(int num) {

    }

    public int getJobSize() {
        return jobs.size();
    }

    private void initWorkers(int num) {
        for (int i = 0; i < num; i++) {
            Worker worker = new Worker();
            workers.add(worker);
            Thread thread = new Thread(worker, "ThreadPool-worker-" + threadNum.incrementAndGet());
            thread.start();
        }
    }

    private class Worker implements Runnable {

        private volatile boolean running = true;

        @Override
        public void run() {
            while (running) {
                Job job = null;
                synchronized (jobs) {
                    while (jobs.isEmpty()) {
                        try {
                            jobs.wait();
                        } catch (InterruptedException e) {
                            // 感知到外部对WorkerThread的终端操作，返回
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                    job = jobs.removeFirst();

                }
                if (job != null) {
                    try {
                        job.run();
                    }
                    catch (Exception e) {
                        // ignore exception during job executing
                    }
                }
            }
        }

        public void shutdown() {
            running = false;
        }
    }
}
