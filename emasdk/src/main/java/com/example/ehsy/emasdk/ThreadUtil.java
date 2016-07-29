package com.example.ehsy.emasdk;

import android.os.Handler;

/**
 * handler做为static静态变量不依赖于具体界面 防止内存泄漏
 */
public class ThreadUtil {
    public static void runInSubThread(Runnable r) {
        ThreadPoolManager.getInstance().addTask(r);
    }

    public static Handler handler = new Handler();

    public static void runInUiThread(Runnable r) {
        handler.post(r);
    }
}
