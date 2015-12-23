package com.yflog.util;

/**
 * Created by vincent on 12/23/15.
 */
public interface ThreadPool <Job extends Runnable> {
    void submit(Job job);
}
