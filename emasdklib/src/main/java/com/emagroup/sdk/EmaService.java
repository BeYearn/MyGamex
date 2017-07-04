package com.emagroup.sdk;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static android.support.v4.app.NotificationCompat.PRIORITY_MAX;


public class EmaService extends Service {

    private static final String TAG = "EmaService";

    private static final int INTERVAL_TIME_FIRST = 1000 * 30;//30秒
    private static final int INTERVAL_TIME_SENCOND = 1000 * 50 * 2;//2分钟
    private static final int INTERVAL_TIME_THIRD = 1000 * 60 * 5;//5分钟

    private ResourceManager mResourceManager;

    private static String HEART_CODE = "";         //用来避免code重复通知

    private int count = 0;
    private Timer mTimer;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    heartBeat(0);

                    if(count ==0||count ==1){
                        pushHeart(INTERVAL_TIME_FIRST);
                    }else if(count ==2||count == 3){
                        pushHeart(INTERVAL_TIME_SENCOND);
                    }else if(count>3){
                        pushHeart(INTERVAL_TIME_THIRD);
                    }
                    count++;
                    break;
            }
        }
    };

    @Override
    public IBinder onBind(Intent arg0) {

        mTimer = new Timer(true);

        pushHeart(0);

        return new LocalBinder();
    }

    private void pushHeart(long delayTime) {
        TimerTask mTask = new TimerTask() {
            @Override
            public void run() {
                Message message = Message.obtain();
                message.what = 1;
                mHandler.sendMessage(message);
            }
        };
        mTimer.schedule(mTask, delayTime);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mResourceManager = ResourceManager.getInstance(this);
    }

    public void reStartHeart() {
        Log.e(TAG, "reStartHeart");
        count = 0;
        pushHeart(0);
    }

    public void heartBeat(long delay){

        Timer timer = new Timer(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                sendOnlineAlive();
            }
        };

        timer.schedule(timerTask,delay);
    }


    /**
     * 发送心跳包
     */
    private void sendOnlineAlive() {

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
                    params.put("allianceId", ULocalUtils.getChannelId(EmaService.this));
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


    private void showNotification(Context context, String title, String content) {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setPriority(PRIORITY_MAX);
        mBuilder.setSmallIcon(mResourceManager.getIdentifier("ema_bottom_promotion_checked","drawable"));//R.drawable.ema_bottom_promotion_checked
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
        Log.e("emaService", "onDestory");
        mTimer.cancel();
    }

    public class LocalBinder extends Binder {
        EmaService getService() {
            // Return this instance of LocalService so clients can call public methods
            return EmaService.this;
        }
    }
}
