package com.sunrain.timetablev4.utils;


import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RunnableExecutorService {

    private static final Handler sMainHandler;
    private static final ExecutorService sExecutorService;

    static {
        sMainHandler = new Handler(Looper.getMainLooper());
        sExecutorService = new ThreadPoolExecutor(1, Runtime.getRuntime()
                .availableProcessors(), 30L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }

    public static void execute(Runnable runnable) {
        sExecutorService.execute(runnable);
    }

    public static <V> DaoFutureTask<V> when(Callable<V> callable) {
        DaoFutureTask<V> futureTask = new DaoFutureTask<>(callable);
        return futureTask;
    }

    public static class DaoFutureTask<V> extends FutureTask<V> {

        private DoneCallback<V> mDoneCallback;
        private FailCallback mFailCallback;

        DaoFutureTask(@NonNull Callable<V> callable) {
            super(callable);
        }

        @Override
        protected void done() {
            V result = null;
            try {
                result = get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                if (mFailCallback != null) {
                    postOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            mFailCallback.fail(e);
                        }
                    });
                }
            }

            final V finalResult = result;
            postOnMainThread(new Runnable() {
                @Override
                public void run() {
                    mDoneCallback.done(finalResult);
                }
            });
        }

        public void execute() {
            sExecutorService.submit(this);
        }

        public DaoFutureTask done(DoneCallback<V> doneCallback) {
            mDoneCallback = doneCallback;
            return this;
        }

        public DaoFutureTask fail(FailCallback failCallback) {
            mFailCallback = failCallback;
            return this;
        }
    }

    public interface DoneCallback<V> {

        void done(V result);
    }

    public interface FailCallback {

        void fail(Throwable throwable);
    }

    private static void postOnMainThread(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            sMainHandler.post(runnable);
        }
    }
}