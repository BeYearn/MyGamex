package com.emagroup.sdk;

import android.app.Activity;
import android.util.Log;

import java.util.Map;

/**
 * Created by Young on 2016/7/9.
 */
public class EmaSDK {
    private static EmaSDK instance = null;
    public static Activity mActivity = null;
    private EmaSDKListener mListener;
    private static EmaSDKListener reciveMsgListener;

    public static EmaSDK getInstance() {
        if (instance == null) {
            instance=new EmaSDK();
        }
        return instance;
    }


    public void init(String appKey,Activity activity, EmaSDKListener listener) {

        EmaUser.getInstance().setAppkey(appKey);
        this.mActivity = activity;
        this.mListener = listener;

        //sdk初始化，先sdk初始化，完了再请求公告更新等信息
        EmaUtils.getInstance(activity).getChannelKeyInfo(listener);

        //个推初始化
        EmaUtils.getInstance(activity).initGeTui(activity);
    }


    public void doLogin(){
        //先创建弱账户，随后。。真正登录
        EmaSDKUser.getInstance().creatWeakAccount(mListener);  // 在这其中包含any真正的登录（写在里面是想要两个透传参数）

    }

    public void doLogout() {
        EmaSDKUser.getInstance().logout();
    }

    public void doPay(Map<String,String> info,EmaSDKListener listener){

        EmaUtils.getInstance(mActivity).doPayPre(listener);

        //在这里把这个map转化到emapayinfo里面  目前需要 商品pid，数量
        EmaPayInfo emaPayInfo = new EmaPayInfo();

        for (Map.Entry<String,String> entry :info.entrySet()){
            String infoValue=entry.getValue();
            switch (entry.getKey()){
                case EmaConst.EMA_PAYINFO_PRODUCT_ID:
                    emaPayInfo.setProductId(infoValue);
                    break;
                case EmaConst.EMA_PAYINFO_PRODUCT_COUNT:
                    emaPayInfo.setProductNum(infoValue);
                    break;
                case EmaConst.EMA_GAMETRANS_CODE:
                    emaPayInfo.setGameTransCode(infoValue);
                    break;
            }
        }
        EmaPay.getInstance(mActivity).pay(emaPayInfo,listener);
    }

    public void doShowToolbar() {
        EmaUtils.getInstance(mActivity).doShowToolbar();
    }


    public void doHideToobar() {
        EmaUtils.getInstance(mActivity).doHideToobar();
    }


    public void doSetRecivePushListner(EmaSDKListener listener){
        this.reciveMsgListener=listener;
    }

    /**
     * 个推的reciver收到透传消息后回调该方法
     * @param msgCode
     * @param msgObj
     */
    public void makeCallBack(int msgCode, String msgObj){
        if(reciveMsgListener == null){
            Log.w("warn", "未设置回调");
            return;
        }
        reciveMsgListener.onCallBack(msgCode,msgObj);
    }


    public String getChannelId(){
        return ULocalUtils.getChannelId(mActivity);
    }


    public void onResume() {
        EmaUtils.getInstance(mActivity).onResume();
    }

    public void onPause() {
        EmaUtils.getInstance(mActivity).onPause();
    }

    public void onStop() {
        EmaUtils.getInstance(mActivity).onStop();
    }

    public void onDestroy(){
        EmaUtils.getInstance(mActivity).onDestroy();
    }

    public void onBackPressed(EmaBackPressedAction action){
        EmaUtils.getInstance(mActivity).onBackPressed(action);
    }
}
