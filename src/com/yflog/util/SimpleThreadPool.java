package com.yflog.util;


/**
 * Created by vincent on 12/23/15.
 */
public class SimpleThreadPool implements ThreadPool {

    private int threadCount;

    public SimpleThreadPool(int threadCount) {
        this.threadCount = threadCount;
    }

    @Override
    public void submit(Runnable runnable) {

    }

    private static class Worker implements Runnable {



        @Override
        public void run() {

        }
    }
}
