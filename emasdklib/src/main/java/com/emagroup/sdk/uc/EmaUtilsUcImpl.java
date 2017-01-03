package com.emagroup.sdk.uc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
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
import com.emagroup.sdk.HttpRequestor;
import com.emagroup.sdk.ThreadUtil;
import com.emagroup.sdk.ULocalUtils;
import com.emagroup.sdk.Url;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cn.uc.gamesdk.UCGameSdk;
import cn.uc.gamesdk.exception.UCCallbackListenerNullException;
import cn.uc.gamesdk.exception.UCMissActivityException;
import cn.uc.gamesdk.open.GameParamInfo;
import cn.uc.gamesdk.open.OrderInfo;
import cn.uc.gamesdk.open.PaymentInfo;
import cn.uc.gamesdk.open.UCCallbackListener;
import cn.uc.gamesdk.open.UCGameSdkStatusCode;
import cn.uc.gamesdk.open.UCLogLevel;
import cn.uc.gamesdk.open.UCOrientation;

/**
 * Created by Administrator on 2016/10/9.
 */
public class EmaUtilsUcImpl {

    private static EmaUtilsUcImpl instance;

    private Activity mActivity;
    private String mChannelAppId; //uc的gameID

    public static EmaUtilsUcImpl getInstance(Activity activity) {
        if (instance == null) {
            instance = new EmaUtilsUcImpl(activity);
        }
        return instance;
    }

    private EmaUtilsUcImpl(Activity activity) {
        this.mActivity = activity;
    }

    public void realInit(final EmaSDKListener listener, JSONObject data) {
        try {

            mChannelAppId = data.getString("channelAppId");

            GameParamInfo gpi = new GameParamInfo();
            gpi.setGameId(Integer.parseInt(mChannelAppId)); // 从UC九游开放平台获取自己游戏的参数信息
            gpi.setEnablePayHistory(true);//开启查询充值历史功能
            gpi.setEnableUserChange(false);//开启账号切换功能
            gpi.setOrientation(mActivity.getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE
                    ?UCOrientation.LANDSCAPE:UCOrientation.PORTRAIT);//LANDSCAPE：横屏，横屏游戏必须设置为横屏 PORTRAIT： 竖屏

            UCGameSdk.defaultSdk().initSdk(mActivity, UCLogLevel.DEBUG, false, gpi, new UCCallbackListener<String>() {//在为 true 的时候，会连接到联调测试环境（ sdk.test4.g.uc.cn），当为 false 的时候则会连接到正式环境（ sdk.g.uc.cn）。
                @Override
                public void callback(int code, String msg) {
                    Log.e("UCinit msg:", msg);//返回的消息
                    switch (code) {
                        case UCGameSdkStatusCode.SUCCESS://初始化成功,可以执行后续的登录充值操作

                            listener.onCallBack(EmaCallBackConst.INITSUCCESS, "初始化成功");
                            //初始化成功之后再检查公告更新等信息
                            EmaUtils.getInstance(mActivity).checkSDKStatus();

                            //uc特有 用于创建悬浮窗 悬浮按钮须指定一个 Activity 与关联，一个游戏中可以有多个 Activity 拥有悬浮按钮，彼此独立操作
                            UCGameSdk.defaultSdk().createFloatButton(mActivity);
                            break;
                        case UCGameSdkStatusCode.INIT_FAIL://初始化失败,不能进行后续操作

                            listener.onCallBack(EmaCallBackConst.INITFALIED, "初始化失败");
                            break;
                        default:
                            break;
                    }
                }
            });

            //对于需要支持账户切换/退出账号的游戏， 必须在此设置退出侦听器， 请放在初始化成功后监听
            UCGameSdk.defaultSdk().setLogoutNotifyListener(new UCCallbackListener<String>() {
                @Override
                public void callback(int statuscode, String data) {
                    switch (statuscode) {
                        case UCGameSdkStatusCode.NO_INIT:
                            break;
                        case UCGameSdkStatusCode.NO_LOGIN:
                            break;
                        case UCGameSdkStatusCode.SUCCESS:
                            listener.onCallBack(EmaCallBackConst.LOGOUTSUCCESS,"登出成功回调");
                            break;
                        case UCGameSdkStatusCode.FAIL:
                            listener.onCallBack(EmaCallBackConst.LOGOUTFALIED,"登出失败回调");
                            break;
                    }
                }
            });

        } catch (Exception e) {
            listener.onCallBack(EmaCallBackConst.INITFALIED, "初始化失败");
            e.printStackTrace();
        }

    }


    public void realLogin(final EmaSDKListener listener, String userid, String deviceId) {


        try {
            UCGameSdk.defaultSdk().login(new UCCallbackListener<String>() {
                @Override
                public void callback(int code, String msg) {
                    if (code == UCGameSdkStatusCode.SUCCESS) {//登录成功，可以执行后续操作
                        // 登陆成功
                        //登录成功回调放在下面updateWeakAccount和docallback成功以后在回调

                        String sid = UCGameSdk.defaultSdk().getSid();
                        Log.e("emasdk UCsid",sid);
                        getUCAccontInfo(sid,listener);  // 绑定和补充弱账户在这里面了
                        submitGameRole();
                    }
                    if (code == UCGameSdkStatusCode.LOGIN_EXIT) {//登录界面关闭， 游戏需判断此时是否已登录成功进行相应操作

                    }
                    if (code == UCGameSdkStatusCode.NO_INIT) {//没有初始化就进行登录调用，需要游戏调用SDK初始化方法
                        // 登陆失败
                        listener.onCallBack(EmaCallBackConst.LOGINFALIED, "登陆失败回调");
                    }
                    if (code == UCGameSdkStatusCode.NO_LOGIN) {//未登录成功， 需要游戏重新调登录方法
                        // 登陆失败
                        listener.onCallBack(EmaCallBackConst.LOGINFALIED, "登陆失败回调");

                    }
                }
            });
        } catch (UCCallbackListenerNullException e) {//异常处理
            listener.onCallBack(EmaCallBackConst.LOGINFALIED, "登陆失败回调");
            e.printStackTrace();
        }
    }

    /**
     * xiaomi的监听在发起支付时已设置好 此处空实现
     *
     * @param listener
     */
    public void doPayPre(final EmaSDKListener listener) {
    }

    public void realPay(final EmaSDKListener listener, EmaPayInfo emaPayInfo) {


        PaymentInfo pInfo = new PaymentInfo(); //创建Payment对象，用于传递充值信息

        pInfo.setCustomInfo("custOrderId=PX299392#ip=139.91.192.29#...");//设置充值自定义参数，此参数不作任何处理，在充值完成后通知游戏服务器充值结果时原封不动传给游戏服务器。此参数为可选参数，默认为空。充值前建议
        pInfo.setRoleId((String) ULocalUtils.spGet(mActivity,"roleId_R","")); //设置用户的游戏角色的ID，此为可选参数
        pInfo.setRoleName((String) ULocalUtils.spGet(mActivity,"roleName_R","")); //设置用户的游戏角色名字，此为可选参数
        pInfo.setGrade((String) ULocalUtils.spGet(mActivity,"roleLevel_R","")); //设置用户的游戏角色等级，此为可选参数
        pInfo.setAmount(emaPayInfo.getPrice()); //单位：元 设置允许充值的金额，此为可选参数，默认为 0。如果设置了此金额不为 0，则表示只允许用户按指定金额充值；如果不指定金额或指定为 0，则表示用户在充值时可以自由选择或输入希望充入的金额。 设置定额充值的游戏服务端收到回调信息必须校验 amount 值与客户端下单时传递的是否一致
        //pInfo.setNotifyUrl("http://192.168.1.1/notifypage.do");// 回调地址，非必填参数，此处设置或开放平台录入，优先取客户端设置的地址，设置后游戏在支付完成后SDK回调充值信息到此地址，必须为带有http头的URL形式。
        pInfo.setTransactionNumCP(emaPayInfo.getOrderShortId());// 设置CP自有的订单号，此为可选参数，对于需要使用此参数的游戏， 充值前建议先判断下此参数传递的值是否正常不为空再调充值接口，注意长度不能超过30

        //如游戏为横屏，请在initSDK接口增加横屏属性
        try {
            UCGameSdk.defaultSdk().pay(pInfo, new UCCallbackListener<OrderInfo>() {
                        @Override
                        public void callback(int statudcode, OrderInfo orderInfo) {
                            if (statudcode == UCGameSdkStatusCode.NO_INIT) {
                                //没有初始化就进行调用，需要游戏调用SDK初始化方法
                            }
                            if (statudcode == UCGameSdkStatusCode.SUCCESS) {
                                //订单生成生成，非充值成功，充值结果由服务端回调判断,请勿显示充值成功的弹窗或toast
                                if (orderInfo != null) {
                                    String ordered = orderInfo.getOrderId();//获取订单号
                                    float amount = orderInfo.getOrderAmount();//获取订单金额
                                    int payWay = orderInfo.getPayWay();//获取充值类型，具体可参考支付通道编码列表
                                    String payWayName = orderInfo.getPayWayName();//充值类型的中文名称
                                }
                                try {
                                    Thread.sleep(1000);
                                    // 购买成功
                                    listener.onCallBack(EmaCallBackConst.PAYSUCCESS, "购买成功");
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (statudcode == UCGameSdkStatusCode.PAY_USER_EXIT) {
                                //用户退出充值界面。
                            }
                        }
                    }
            );
        } catch (UCCallbackListenerNullException e) {
            //异常处理
            // 购买失败
            //call一次取消订单
            EmaPay.getInstance(mActivity).cancelOrder();

            listener.onCallBack(EmaCallBackConst.PAYFALIED, "购买失败");
        }

    }


    public void logout() {
        UCGameSdk.defaultSdk().logout();
    }

    public void swichAccount() {

    }

    public void doShowToolbar() {
        UCGameSdk.defaultSdk().showFloatButton(mActivity,0,80);
    }

    public void doHideToobar() {
        UCGameSdk.defaultSdk().hideFloatButton(mActivity);
    }

    public void onResume() {
    }

    public void onPause() {
    }

    public void onStop() {
    }

    public void onDestroy() {
        UCGameSdk.defaultSdk().destoryFloatButton(mActivity);
    }

    public void onBackPressed(EmaBackPressedAction action) {
        //action.doBackPressedAction();  uc有自己的逻辑
        try {
            UCGameSdk.defaultSdk().exitSDK(mActivity, new UCCallbackListener<String>() {
                public void callback(final int statuscode, final String data) {
                    switch (statuscode) {
                        case UCGameSdkStatusCode.SDK_EXIT:// 退出程序
                            mActivity.finish();
                            System.exit(0);
                            break;
                        case UCGameSdkStatusCode.SDK_EXIT_CONTINUE:// 继续游戏
                            break;
                        case UCGameSdkStatusCode.NO_INIT:// 没有初始化
                            break;
                    }
                }
            });
        } catch (UCCallbackListenerNullException e) {
            e.printStackTrace();
        } catch (UCMissActivityException e) {
            e.printStackTrace();
        }
    }


    //-----------------------------------UC的各种网络请求方法-------------------------------------------------------------------------

    public void getUCAccontInfo(final String sid, final EmaSDKListener listener) {
        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                try {
                    //耗时操作 阻塞
                    String url = Url.getUCAccontInfo();

                    Map<String, String> paramMap = new HashMap<String, String>();
                    paramMap.put("gameId", mChannelAppId);
                    paramMap.put("sid", sid);
                    paramMap.put("appId", ULocalUtils.getAppId(mActivity));
                    paramMap.put("channelId", ULocalUtils.getChannelId(mActivity));

                    String result = new HttpRequestor().doPost(url, paramMap);
                    Log.e("emasdkgetUCAccontInfo",result);

                    JSONObject jsonObject = new JSONObject(result);
                    JSONObject data = jsonObject.getJSONObject("data");

                    JSONObject datadata = data.getJSONObject("data");

                    String accountId = datadata.getString("accountId");
                    String nickName = datadata.getString("nickName");

                    EmaUser.getInstance().setAllianceUid(accountId);
                    EmaUser.getInstance().setNickName(nickName);

                    //绑定服务
                    Intent serviceIntent = new Intent(mActivity, EmaService.class);
                    mActivity.bindService(serviceIntent, EmaUtils.getInstance(mActivity).mServiceCon, Context.BIND_AUTO_CREATE);
                    //补充弱账户信息
                    EmaSDKUser.getInstance().updateWeakAccount(listener, ULocalUtils.getAppId(mActivity), ULocalUtils.getChannelId(mActivity), ULocalUtils.getChannelTag(mActivity), ULocalUtils.getDeviceId(mActivity), EmaUser.getInstance().getAllianceUid());

                    Log.e("getUCAccontInfo", "结果:" + accountId + ".." + nickName);

                    // uc 登录成功后显示那个浮标
                    UCGameSdk.defaultSdk().showFloatButton(mActivity,0,80);

                } catch (Exception e) {
                    listener.onCallBack(EmaCallBackConst.LOGINFALIED, "登陆失败回调");
                    Log.e("getUCAccontInfo", "maybe is SocketTimeoutException");
                    e.printStackTrace();
                }

            }
        });
    }

    /**
     * uc要求的必接的角色信息提交
     */
    private void submitGameRole() {
        try {
            //角色登录成功或升级时调用此段，请根据实际业务数据传入真实数据，以下数据仅是示例
            JSONObject jsonExData = new JSONObject();
            jsonExData.put("roleId", ULocalUtils.spGet(mActivity,"roleId_R",""));//同一区服角色ID需保持唯一性
            jsonExData.put("roleName", ULocalUtils.spGet(mActivity,"roleName_R",""));//未取名时传默认值，不可传空
            jsonExData.put("roleLevel", ULocalUtils.spGet(mActivity,"roleLevel_R",""));//如游戏存在转生，转职等，等级需累加
            jsonExData.put("zoneId", ULocalUtils.spGet(mActivity,"zoneId_R",""));
            jsonExData.put("zoneName", ULocalUtils.spGet(mActivity,"zoneId_R",""));//服务器名必须与界面展示的名称保持一致
            jsonExData.put("roleCTime", 1456397360);//获取服务器存储的角色创建时间，不可用本地手机时间， 同一角色创建时间不可变
            UCGameSdk.defaultSdk().submitExtendData("loginGameRole", jsonExData);
        } catch (Exception e) {
            //处理异常
        }
    }

}
