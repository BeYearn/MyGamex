package com.emagroup.sdk.impl;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.emagroup.sdk.EmaBackPressedAction;
import com.emagroup.sdk.EmaCallBackConst;
import com.emagroup.sdk.EmaPay;
import com.emagroup.sdk.EmaPayInfo;
import com.emagroup.sdk.EmaSDKListener;
import com.emagroup.sdk.EmaSDKUser;
import com.emagroup.sdk.EmaService;
import com.emagroup.sdk.EmaUser;
import com.emagroup.sdk.EmaUtils;
import com.emagroup.sdk.EmaUtilsInterface;
import com.emagroup.sdk.HttpRequestor;
import com.emagroup.sdk.InitCheck;
import com.emagroup.sdk.ThreadUtil;
import com.emagroup.sdk.ToastHelper;
import com.emagroup.sdk.ULocalUtils;
import com.emagroup.sdk.Url;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Administrator on 2016/10/9.
 */
public class EmaUtilsImpl implements EmaUtilsInterface {

    private static EmaUtilsImpl instance;
    private Activity mActivity;
    private EmaSDKListener mILlistener;
    private boolean isInitSucc;
    private String appid;
    private boolean isLoginSucc;
    private ProgressDialog mProgressDialog;


    public static EmaUtilsImpl getInstance(Activity activity) {
        if (instance == null) {
            instance = new EmaUtilsImpl(activity);
        }
        return instance;
    }

    private EmaUtilsImpl(Activity activity) {
        this.mActivity = activity;
    }

    @Override
    public void realInit(final EmaSDKListener listener, JSONObject data) {
        try {
            this.mILlistener=listener;

            isInitSucc=false;

            appid=data.getString("channelAppId");
            Log.e("yybappid",appid);

            // yyb的初始化放到了前面sdk里面的初始化，因为在线程转换后在此处不灵了

            YSDKApi.setUserListener(new UserListener() {
                @Override
                public void OnLoginNotify(UserLoginRet ret) {

                    if(eFlag.Succ==ret.flag){
                        isLoginSucc=true;
                        Log.e("isLoginSucc","///"+isLoginSucc);
                    }
                    if(!isInitSucc){
                        listener.onCallBack(EmaCallBackConst.INITSUCCESS, "初始化成功");
                        isInitSucc=true;
                        //初始化成功之后再检查公告更新等信息
                        EmaUtils.getInstance(mActivity).checkSDKStatus();
                    }

                    switch (ret.flag) {
                        case eFlag.Succ: // 登陆成功

                            platform = ePlatform.getEnum(ret.platform);

                            String uid = ret.open_id;   //用户在该appid下的唯一性标示，appid内唯一
                            String nikename = ret.nick_name;
                            EmaUser.getInstance().setAllianceUid(uid + "");
                            EmaUser.getInstance().setNickName(nikename);

                            //绑定服务
                            Intent serviceIntent = new Intent(mActivity, EmaService.class);
                            mActivity.bindService(serviceIntent, EmaUtils.getInstance(mActivity).mServiceCon, Context.BIND_AUTO_CREATE);

                            //补充弱账户信息
                            EmaSDKUser.getInstance().updateWeakAccount(listener, ULocalUtils.getAppId(mActivity), ULocalUtils.getChannelId(mActivity), ULocalUtils.getChannelTag(mActivity), ULocalUtils.getDeviceId(mActivity), EmaUser.getInstance().getAllianceUid());

                            Log.e("yybloginSuccessful", ret.toString());

                            //  YSDKApi.queryUserInfo(platform);  //在onRelationNotify 中回应

                            break;
                        // 游戏逻辑，对登录失败情况分别进行处理
                        case eFlag.QQ_LoginFail:
                            listener.onCallBack(EmaCallBackConst.LOGINFALIED, "QQ登录失败，请重试");
                            //mainActivity.showToastTips("QQ登录失败，请重试");
                            //mainActivity.letUserLogout();
                            break;
                        case eFlag.QQ_NetworkErr:
                            listener.onCallBack(EmaCallBackConst.LOGINFALIED, "QQ登录异常，请重试");
                            //mainActivity.showToastTips("QQ登录异常，请重试");
                            //mainActivity.letUserLogout();
                            break;
                        case eFlag.QQ_NotInstall:
                            listener.onCallBack(EmaCallBackConst.LOGINFALIED, "手机未安装手Q，请安装后重试");
                            ToastHelper.toast(mActivity, "手机未安装手Q，请安装后重试");
                            //mainActivity.letUserLogout();
                            break;
                        case eFlag.QQ_NotSupportApi:
                            listener.onCallBack(EmaCallBackConst.LOGINFALIED, "手机手Q版本太低，请升级后重试");
                            ToastHelper.toast(mActivity, "手机手Q版本太低，请升级后重试");
                            //mainActivity.letUserLogout();
                            break;
                        case eFlag.WX_NotInstall:
                            listener.onCallBack(EmaCallBackConst.LOGINFALIED, "QQ登录异常，请重试");
                            //mainActivity.showToastTips("手机未安装微信，请安装后重试");
                            //mainActivity.letUserLogout();
                            break;
                        case eFlag.WX_NotSupportApi:
                            listener.onCallBack(EmaCallBackConst.LOGINFALIED, "QQ登录异常，请重试");
                            //mainActivity.showToastTips("手机微信版本太低，请升级后重试");
                            //mainActivity.letUserLogout();
                            break;
                        case eFlag.Login_TokenInvalid:
                            listener.onCallBack(EmaCallBackConst.LOGINFALIED, "QQ登录异常，请重试");
                            //mainActivity.showToastTips("您尚未登录或者之前的登录已过期，请重试");
                            //mainActivity.letUserLogout();
                            break;
                        case eFlag.Login_NotRegisterRealName:
                            listener.onCallBack(EmaCallBackConst.LOGINFALIED, "QQ登录异常，请重试");
                            // 显示登录界面
                            //mainActivity.showToastTips("您的账号没有进行实名认证，请实名认证后重试");
                            //mainActivity.letUserLogout();
                            break;
                        default:
                            listener.onCallBack(EmaCallBackConst.LOGINFALIED, "QQ登录异常，请重试");
                            // 显示登录界面
                            //mainActivity.letUserLogout();
                            break;
                    }
                }

                @Override
                public void OnWakeupNotify(WakeupRet ret) {
                    Log.e("yybLogin", "OnWakeupNotify" + ret.toString());
                    Log.d("yybOnWakeupNotify", "called");
                    Log.d("yybOnWakeupNotify", "flag:" + ret.flag);
                    Log.d("yybOnWakeupNotify", "msg:" + ret.msg);
                    Log.d("yybOnWakeupNotify", "platform:" + ret.platform);
                    // TODO GAME 游戏需要在这里增加处理异账号的逻辑
                    if (eFlag.Wakeup_YSDKLogining == ret.flag) {
                        // 用拉起的账号登录，登录结果在OnLoginNotify()中回调
                    } else if (ret.flag == eFlag.Wakeup_NeedUserSelectAccount) {
                        // 异账号时，游戏需要弹出提示框让用户选择需要登录的账号
                        Log.d("yybOnWakeupNotify", "diff account");
                        showDiffLogin();
                    } else if (ret.flag == eFlag.Wakeup_NeedUserLogin) {
                        // 没有有效的票据，登出游戏让用户重新登录
                        Log.d("yybOnWakeupNotify", "need login");
                        YSDKApi.logout();
                    } else {
                        Log.d("yybOnWakeupNotify", "logout");
                        YSDKApi.logout();
                    }
                }

                @Override
                public void OnRelationNotify(UserRelationRet relationRet) {
                    Log.e("yybLogin", "OnRelationNotify" + relationRet.toString());
                    String result = "";
                    result = result + "flag:" + relationRet.flag + "\n";
                    result = result + "msg:" + relationRet.msg + "\n";
                    result = result + "platform:" + relationRet.platform + "\n";

                    if (relationRet.persons != null && relationRet.persons.size() > 0) {
                        PersonInfo personInfo = (PersonInfo) relationRet.persons.firstElement();
                        String builder = "UserInfoResponse json: \n" +
                                "nick_name: " + personInfo.nickName + "\n" +
                                "open_id: " + personInfo.openId + "\n" +
                                "userId: " + personInfo.userId + "\n" +
                                "gender: " + personInfo.gender + "\n" +
                                "picture_small: " + personInfo.pictureSmall + "\n" +
                                "picture_middle: " + personInfo.pictureMiddle + "\n" +
                                "picture_large: " + personInfo.pictureLarge + "\n" +
                                "provice: " + personInfo.province + "\n" +
                                "city: " + personInfo.city + "\n" +
                                "country: " + personInfo.country + "\n";
                        result = result + builder;
                        Log.e("yybOnRelationNotify", result);

                    } else {
                        result = result + "relationRet.persons is bad";
                    }
                }
            });

            if(!isInitSucc){
                listener.onCallBack(EmaCallBackConst.INITSUCCESS, "初始化成功");  // 防止某时OnLoginNotify开始并不自动调用的
                isInitSucc=true;
                //初始化成功之后再检查公告更新等信息
                InitCheck.getInstance(mActivity).checkSDKStatus();
            }

        } catch (Exception e) {
            listener.onCallBack(EmaCallBackConst.INITFALIED, "初始化失败");
            e.printStackTrace();
        }

    }

    @Override
    public void realLogin(final EmaSDKListener listener, String userid, String deviceId) {

        Log.e("yyb", "login");
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!isLoginSucc){
                    Log.e("isLoginSucc","000"+isLoginSucc);
                    YybLoginDialog.getInstance(mActivity).show();
                }else {
                    Log.e("ysdLogin","您已登录成功");
                }
            }
        });
    }

    /**
     * 用于支付前的一些操作
     *
     * @param listener
     */
    @Override
    public void doPayPre(final EmaSDKListener listener) {

    }

    @Override
    public void realPay(final EmaSDKListener listener, EmaPayInfo emaPayInfo) {
        String zoneId = "1";  //大区id
        String saveValue = emaPayInfo.getPrice()+""; //充值数额
        boolean isCanChange = false;   // 设置的充值数额是否可改
        Bitmap bmp = BitmapFactory.decodeResource(mActivity.getResources(), mActivity.getResources().getIdentifier("yyb_img_zuanshi","drawable",mActivity.getPackageName()));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] appResData = baos.toByteArray();  // 代币图标的二进制数据
        String ysdkExt = "ysdkExt";
        YSDKApi.recharge(zoneId, saveValue, isCanChange, appResData, ysdkExt, new PayListener() {
            @Override
            public void OnPayNotify(PayRet ret) {
                if (PayRet.RET_SUCC == ret.ret) {
                    switch (ret.payState) {
                        //支付成功
                        case PayRet.PAYSTATE_PAYSUCC:

                           /* // 购买成功
                            listener.onCallBack(EmaCallBackConst.PAYSUCCESS, "购买成功");
                            *//*mMainActivity.sendResult(
                                    "用户支付成功，支付金额" + ret.realSaveNum + ";" +
                                            "使用渠道：" + ret.payChannel + ";" +
                                            "发货状态：" + ret.provideState + ";" +
                                            "业务类型：" + ret.extendInfo + ";建议查询余额：" + ret.toString());*/

                            //这个过程是充值成功
                            Log.e("yyb pay succ",ret.toString());
                            showDialog("请稍候...");

                            doCallPay(emaPayInfo,listener);

                            break;
                        case PayRet.PAYSTATE_PAYCANCEL:
                            // 取消购买
                            //call一次取消订单
                            EmaPay.getInstance(mActivity).cancelOrder();

                            listener.onCallBack(EmaCallBackConst.PAYCANELI, "取消购买");
                            break;
                        //支付结果未知
                        case PayRet.PAYSTATE_PAYUNKOWN:
                            break;
                        //支付失败
                        case PayRet.PAYSTATE_PAYERROR:

                            listener.onCallBack(EmaCallBackConst.PAYFALIED, "购买失败");
                            break;
                    }
                } else {
                    switch (ret.flag) {
                        case eFlag.Login_TokenInvalid:
                            //用户取消支付
                            // mMainActivity.sendResult("登陆态过期，请重新登陆：" + ret.toString());
                            // mMainActivity.letUserLogout();
                            break;
                        case eFlag.Pay_User_Cancle:
                            //用户取消支付
                            break;
                        case eFlag.Pay_Param_Error:
                            break;
                        case eFlag.Error:
                        default:
                            break;
                    }
                    // 购买失败
                    //call一次取消订单
                    EmaPay.getInstance(mActivity).cancelOrder();
                    listener.onCallBack(EmaCallBackConst.PAYFALIED, "购买失败");
                }
            }
        });
    }

    @Override
    public void logout() {
        YSDKApi.logout();
        EmaUser.getInstance().setToken("");
        EmaUser.getInstance().setIsLogin(false);
        isLoginSucc=false;
        mILlistener.onCallBack(EmaCallBackConst.LOGOUTSUCCESS,"登出成功");
    }

    @Override
    public void swichAccount() {

    }

    @Override
    public void doShowToolbar() {
    }

    @Override
    public void doHideToobar() {
    }

    @Override
    public void onResume() {
        YSDKApi.onResume(mActivity);
    }

    @Override
    public void onPause() {
        isInitSucc=false;
        YSDKApi.onPause(mActivity);
    }

    @Override
    public void onStop() {
        isInitSucc=false;
        YSDKApi.onStop(mActivity);
    }

    @Override
    public void onDestroy() {
        isInitSucc=false;
        YSDKApi.onDestroy(mActivity);
    }

    @Override
    public void onBackPressed(final EmaBackPressedAction action) {
        action.doBackPressedAction();
    }

    @Override
    public void submitGameRole(Map<String, String> data) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        YSDKApi.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onNewIntent(Intent intent) {
        YSDKApi.handleIntent(intent);
    }

    @Override
    public void onRestart() {
        YSDKApi.onRestart(mActivity);
    }

    //-----------------------------------xxx的网络请求方法-------------------------------------------------------------------------

    private void doCallPay(final EmaPayInfo emaPayInfo, final EmaSDKListener listener) {

        final UserLoginRet ret = new UserLoginRet();
        YSDKApi.getLoginRecord(ret);

        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                Map<String,String> params = new HashMap<>();
                params.put("openId",ret.open_id);
                params.put("openKey",platform.val()==ePlatform.QQ.val()? ret.getPayToken():ret.getAccessToken());
                params.put("appId",appid);
                params.put("amount",emaPayInfo.getPrice()+"");
                params.put("orderId",emaPayInfo.getOrderId());
                params.put("pf",ret.pf);
                params.put("pfKey",ret.pf_key);
                params.put("ifSandBox",isSandbox()?"1":"0");   //ifSandBox:1表示沙盒，0表示正式
                params.put("sessionId",platform.val()==ePlatform.QQ.val()?"openid":"hy_gameid");
                params.put("sessionType",platform.val()==ePlatform.QQ.val()?"kp_actoken":"wc_actoken");

                StringBuilder builder=new StringBuilder();
                for(Map.Entry<String,String> entry:params.entrySet()){
                    builder.append(entry.getKey()+"="+entry.getValue()+"&");
                }
                Log.e("yybdocallpay",builder.toString());

                try {
                    String result = new HttpRequestor().doPost(doCallpay(), params);  //发起扣款
                    Log.e("yybCallPayresult", result);

                    dissmissDialog();

                    JSONObject jsonObject = new JSONObject(result);
                    int retCode = jsonObject.getInt("status");
                    if(retCode==0){
                        listener.onCallBack(EmaCallBackConst.PAYSUCCESS, "购买成功");
                    }else {
                        ToastHelper.toast(mActivity,"yyb:"+jsonObject.getString("message"));
                        listener.onCallBack(EmaCallBackConst.PAYFALIED, "购买失败");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }



    public void showDiffLogin() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setTitle("异账号提示");
                builder.setMessage("你当前拉起的账号与你本地的账号不一致，请选择使用哪个账号登陆：");
                builder.setPositiveButton("本地账号", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        ToastHelper.toast(mActivity, "选择使用本地账号");
                        if (!YSDKApi.switchUser(false)) {
                            YSDKApi.logout();
                        }
                    }
                });
                builder.setNeutralButton("拉起账号", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        ToastHelper.toast(mActivity, "选择使用拉起账号");
                        if (!YSDKApi.switchUser(true)) {
                            YSDKApi.logout();
                        }
                    }
                });
                builder.show();
            }
        });

    }

    public void showDialog(String str){
        if(null==mProgressDialog){
            mProgressDialog = new ProgressDialog(mActivity);
            mProgressDialog.setMessage(str);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.show();
            }
        });
    }
    public void dissmissDialog(){
        if(null!=mProgressDialog){
            mProgressDialog.dismiss();
        }
    }

    /**
     * 读取ysdkconf的，动态配置ifSandBox的
     */

    private boolean isSandbox(){

        boolean isBox = false;
        try {
            InputStream ysdkConfStream = getClass().getResourceAsStream("/assets/ysdkconf.ini");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(ysdkConfStream));
            String line;
            while ((line = bufferedReader.readLine())!=null){
                if(!line.startsWith(";")&&line.startsWith("YSDK_URL")){
                    if(line.contains("ysdktest")){
                        isBox=true;
                    }else {
                        isBox=false;
                    }
                }
            }
            bufferedReader.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return isBox;
    }

    //-----------------------------------xxx 特有接口---------------------------------------------------

    public static String doCallpay(){
        return Url.getServerUrl()+"/ema-platform/pay/tencentPay";
        //return "http://192.168.10.104:8081/ema-platform/pay/tencentPay";
    }
}
