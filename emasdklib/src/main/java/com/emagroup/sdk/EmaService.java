package com.emagroup.sdk;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.anysdk.framework.java.R;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.support.v4.app.NotificationCompat.PRIORITY_MAX;


public class EmaService extends Service {

    private static final String TAG = "EmaService";

    private static final int INTERVAL_TIME_FIRST = 1000 * 30;//30秒
    private static final int INTERVAL_TIME_SENCOND = 1000 * 50 * 2;//2分钟
    private static final int INTERVAL_TIME_THIRD = 1000 * 60 * 5;//5分钟

    private boolean mFlagRuning = true;
    private HeartThread mHeartThread;

    private static String HEART_CODE = "";         //用来避免code重复通知

    @Override
    public IBinder onBind(Intent arg0) {
        return new LocalBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mHeartThread = new HeartThread();
        mHeartThread.start();
    }

    public void reStartHeart() {
        Log.e(TAG, "reStartHeart");
        if (mHeartThread != null) {
            mHeartThread.reSetHeart();
        }
    }

    private class HeartThread extends Thread {

        public int i = 0;

        @Override
        public void run() {

            while (mFlagRuning) {
                if (i < 2) {  //前1分钟 30秒一次 发送2次心跳包
                    sendOnlineAlive();
                    trySleep(INTERVAL_TIME_FIRST);
                } else if (2 <= i && i < 4) {  //第1分钟到第5分钟  2分钟一次  发送2次心跳包
                    sendOnlineAlive();
                    trySleep(INTERVAL_TIME_SENCOND);
                } else {  //之后都是5分钟发送一次
                    if (EmaUser.getInstance().getIsLogin()) {
                        sendOnlineAlive();
                    }
                    trySleep(INTERVAL_TIME_THIRD);
                }
                i++;
            }
        }

        private void reSetHeart() {
            i = 0;
            sendOnlineAlive();
        }
    }


    /**
     * 发送心跳包
     */
    public void sendOnlineAlive() {

        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.e(TAG, "sendOnlineAlive");

                    String url = Url.heartbeat();

                    Map<String, String> params = new HashMap<String, String>();
                    params.put("token", EmaUser.getInstance().getToken());
                    params.put("uid", EmaUser.getInstance().getAllianceUid());
                    params.put("appId", ULocalUtils.getAppId(EmaService.this));
                    params.put("allianceId ", ULocalUtils.getChannelId(EmaService.this));
                    params.put("channelTag", ULocalUtils.getChannelTag(EmaService.this));
                    params.put("extra", "location info");   //渠道先不真写

                    String result = new HttpRequestor().doPost(url, params);

                    JSONObject jsonObject = new JSONObject(result);
                    int status = jsonObject.getInt("status");
                    if (0 == status) {
                        JSONObject data = jsonObject.getJSONObject("data");
                        if (data.toString().contains("code")) {
                            String code = data.getString("code");

                            if (!HEART_CODE.equals(code)) {
                                String applicationName = ULocalUtils.getApplicationName(EmaService.this);
                                showNotification(EmaService.this,applicationName + " 通知", "您的验证码为：" + code);
                                HEART_CODE = code;
                            }
                        }
                    }


                    Log.e(TAG, "heartbeat result__:" + result);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("heart beat", "maybe is SocketTimeoutException");
                }
            }
        });
    }


    private static void showNotification(Context context, String title, String content) {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setPriority(PRIORITY_MAX);
        mBuilder.setSmallIcon(R.drawable.ema_bottom_promotion_checked);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(content);

        //Intent resultIntent = new Intent(this, MainActivity.class);
        //PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //mBuilder.setContentIntent(resultPendingIntent);

        Notification notification = mBuilder.build();

        //notification.flags = Notification.FLAG_ONLY_ALERT_ONCE;

        notification.defaults = Notification.DEFAULT_SOUND;//通知带有系统默认声音

        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        mNotifyMgr.notify(1, notification);
    }


    private void trySleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFlagRuning = false;
    }

    public class LocalBinder extends Binder {
        EmaService getService() {
            // Return this instance of LocalService so clients can call public methods
            return EmaService.this;
        }
    }
}
