package com.emagroup.sdk;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 将每一次的支付过程当作一个对象
 */
public class EmaPay {

    private static final String TAG = "EmaPay";
    private static final int ORDER_SUCCESS = 11; // 订单创建成功
    private static final int ORDER_FAIL = 12;

    private static EmaPay mInstance;
    private static final Object synchron = new Object();

    private Context mContext;
    private EmaSDKListener mListener;

    private EmaUser mEmaUser;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ORDER_SUCCESS://成功
                    Log.e("EmaPay", "订单创建成功");
                    doRealPay((EmaPayInfo) msg.obj);
                    break;
                case ORDER_FAIL:
                    Toast.makeText(mContext, "订单创建失败", Toast.LENGTH_LONG).show();
                    break;

            }
        }

    };
    private EmaPayInfo mPayInfo;

    private void doRealPay(EmaPayInfo emaPayInfo) {

        EmaUtils.getInstance((Activity) mContext).realPay(mListener, emaPayInfo);


    }

    private EmaPay(Context context) {
        mContext = context;
        mEmaUser = EmaUser.getInstance();
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
    public void pay(EmaPayInfo payInfo, EmaSDKListener listener) {
        this.mListener = listener;
        this.mPayInfo = payInfo;

        try {
            if(mPayInfo.getGameTransCode().getBytes().length>256){
                throw new RuntimeException("参数过长，超过256byte");
            }
        }catch(Exception e){
            Log.e("pay","参数过长，超过256byte");
            return;
        }
        if (!mEmaUser.getIsLogin()) {
            Log.e(TAG, "没有登陆，或者已经退出！订单创建失败");
            return;
        }
        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                //发起购买---->对订单号及信息的请求
                Map<String, String> params = new HashMap<>();
                params.put("pid", mPayInfo.getProductId());
                params.put("token", mEmaUser.getToken());
                params.put("quantity", mPayInfo.getProductNum());
                params.put("appId", ULocalUtils.getAppId(mContext));
                if (!TextUtils.isEmpty(mPayInfo.getGameTransCode())) {
                    params.put("gameTransCode", mPayInfo.getGameTransCode());
                }
                Log.e("Emapay_pay", mPayInfo.getProductId() + ".." + mEmaUser.getToken() + ".." + mPayInfo.getProductNum());

                String sign = ULocalUtils.getAppId(mContext) + (TextUtils.isEmpty(mPayInfo.getGameTransCode()) ? null : mPayInfo.getGameTransCode()) + mPayInfo.getProductId() + mPayInfo.getProductNum() + mEmaUser.getToken() + EmaUser.getInstance().getAppkey();
                //LOG.e("rawSign",sign);
                sign = ULocalUtils.MD5(sign);
                params.put("sign", sign);

                try {
                    String result = new HttpRequestor().doPost(Url.createOrder(), params);

                    Log.e("creatOrder", result);
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

                    mPayInfo.setOrderId(orderId);
                    mPayInfo.setUid(mEmaUser.getAllianceUid());
                    mPayInfo.setProductName(productName);
                    mPayInfo.setPrice(Integer.parseInt(productPrice)*Integer.parseInt(mPayInfo.getProductNum()));  // 总额
                    mPayInfo.setDescription(description);
                    mPayInfo.setProductId(channelProductCode);

                    Log.e("createOrder", orderId + channelProductCode + description + productName + productPrice);

                    Message msg = new Message();
                    msg.what = ORDER_SUCCESS;
                    msg.obj = mPayInfo;
                    mHandler.sendMessage(msg);
                } catch (Exception e) {
                    mHandler.sendEmptyMessage(ORDER_FAIL);
                    e.printStackTrace();
                }

            }
        });
    }


    /**
     * 取消订单
     */
    public void cancelOrder() {
        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                Map<String, String> params = new HashMap<>();
                params.put("orderId", mPayInfo.getOrderId());
                params.put("token", mEmaUser.getToken());
                try {

                    String result = new HttpRequestor().doPost(Url.rejectOrder(), params);

                    Log.e("rejectOrder",result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}