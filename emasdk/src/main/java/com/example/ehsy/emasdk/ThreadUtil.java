package com.example.ehsy.emasdk;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * handler做为static静态变量不依赖于具体界面 防止内存泄漏
 */
public class ThreadUtil {
    public static void runInSubThread(Runnable r) {
        ThreadPoolManager.getInstance().addTask(r);
    }

    public static Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Log.e("++++++++","getget");
        }
    };

    public static void runInUiThread(Runnable r) {
        handler.post(r);
    }
}
