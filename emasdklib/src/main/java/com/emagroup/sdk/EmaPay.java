package com.emagroup.sdk;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.anysdk.framework.IAPWrapper;
import com.anysdk.framework.java.AnySDKIAP;
import com.anysdk.framework.java.AnySDKListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 将每一次的支付过程当作一个对象
 *
 * @author yang.zhang
 */
public class EmaPay {

    private static final String TAG = "EmaPay";
    private static final int ORDER_SUCCESS = 11; // 订单创建成功
    private static final int ORDER_FAIL = 12;

    private static EmaPay mInstance;
    private static final Object synchron = new Object();

    private Context mContext;
    private EmaSDKListener listener;

    private EmaUser mEmaUser;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ORDER_SUCCESS://成功
                    Log.e("EmaPay", "订单创建成功");
                    doRealAnyPay((EmaPayInfo) msg.obj);
                    break;
                case ORDER_FAIL:
                    Toast.makeText(mContext, "订单创建失败", Toast.LENGTH_LONG).show();
                    break;

            }
        }

    };

    private void doRealAnyPay(EmaPayInfo emaPayInfo) {

        Map<String, String> anyPayInfo = new HashMap();
        anyPayInfo.put("Product_Price", "1");
        anyPayInfo.put("Product_Id", "10");
        anyPayInfo.put("Product_Name","gold");
        anyPayInfo.put("Server_Id", "13");
        anyPayInfo.put("Product_Count", "1");
        anyPayInfo.put("Role_Id","1");
        anyPayInfo.put("Role_Name", "1");
        anyPayInfo.put("Role_Grade", "1");
        anyPayInfo.put("Role_Balance", "1");

        EmaSDKIAP iap = EmaSDKIAP.getInstance();
        ArrayList<String> idArrayList = iap.getPluginId();
        iap.payForProduct(idArrayList.get(0), anyPayInfo,listener);
        Log.e("dopay","dopay");

    }

    private EmaPay(Context context) {
        mContext = context;
        mEmaUser = EmaUser.getInstance();

        AnySDKIAP.getInstance().setListener(new AnySDKListener() {
            @Override
            public void onCallBack(int arg0, String arg1) {
                switch(arg0)
                {
                    case IAPWrapper.PAYRESULT_SUCCESS://支付成功回调
                        // 购买成功
                        listener.onCallBack(EmaCallBackConst.PAYSUCCESS,"购买成功");
                        break;
                    case IAPWrapper.PAYRESULT_FAIL://支付失败回调
                        // 购买失败
                        listener.onCallBack(EmaCallBackConst.PAYFALIED,"购买失败");
                        break;
                    case IAPWrapper.PAYRESULT_CANCEL://支付取消回调
                        // 取消购买
                        listener.onCallBack(EmaCallBackConst.PAYCANELI,"取消购买");
                        break;
                    case IAPWrapper.PAYRESULT_NETWORK_ERROR://支付超时回调
                        //统一接口里面没有
                        break;
                    case IAPWrapper.PAYRESULT_PRODUCTIONINFOR_INCOMPLETE://支付信息提供不完全回调
                        //统一接口里面没有
                        break;
                    /**
                     * 新增加:正在进行中回调
                     * 支付过程中若SDK没有回调结果，就认为支付正在进行中
                     * 游戏开发商可让玩家去判断是否需要等待，若不等待则进行下一次的支付
                     */
                    case IAPWrapper.PAYRESULT_NOW_PAYING:
                        break;
                    default:
                        break;
                }
            }
        });
    }

    public static EmaPay getInstance(Context context) {
        if (mInstance == null) {
            synchronized (synchron) {
                if (mInstance == null) {
                    mInstance = new EmaPay(context);
                }
            }
        }
        return mInstance;
    }


    /**
     * 开启支付
     *
     * @param
     */
    public void pay(final EmaPayInfo payInfo, EmaSDKListener listener) {
        this.listener = listener;

        if (!mEmaUser.isLogin()) {
            Log.e(TAG, "没有登陆，或者已经退出！订单创建失败");
            return;
        }
        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                //发起购买---->对订单号及信息的请求
                Map<String, String> params = new HashMap<>();
                params.put("pid", payInfo.getProductId());
                params.put("token", mEmaUser.getToken());
                params.put("quantity", payInfo.getProductNum());
                Log.e("Emapay_pay", payInfo.getProductId() + ".." + mEmaUser.getToken() + ".." + payInfo.getProductNum());

                try {
                    String result = new HttpRequestor().doPost(Instants.CREAT_ORDER_URL, params);
                    JSONObject jsonObject = new JSONObject(result);
                    String message = jsonObject.getString("message");
                    String status = jsonObject.getString("status");

                    JSONObject productData = jsonObject.getJSONObject("data");
                    boolean coinEnough = productData.getBoolean("coinEnough");
                    String orderId = productData.getString("orderId");
                    JSONObject productInfo = productData.getJSONObject("productInfo");

                    String appId = productInfo.getString("appId");
                    String channelId = productInfo.getString("channelId");
                    String channelProductCode = productInfo.getString("channelProductCode");
                    String description = productInfo.getString("description");
                    String emaProductCode = productInfo.getString("emaProductCode");
                    String productName = productInfo.getString("productName");
                    String productPrice = productInfo.getString("productPrice");
                    String unit = productInfo.getString("unit");

                    payInfo.setOrderId(orderId);
                    payInfo.setUid(mEmaUser.getmUid());
                    payInfo.setProductName(productName);
                    payInfo.setPrice(Integer.parseInt(productPrice));
                    payInfo.setDescription(description);

                    Log.e("createOrder", message + coinEnough + orderId + unit + productPrice);

                    Message msg = new Message();
                    msg.what = ORDER_SUCCESS;
                    msg.obj = payInfo;
                    mHandler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }
}
