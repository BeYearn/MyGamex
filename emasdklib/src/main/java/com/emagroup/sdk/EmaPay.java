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

    private void doRealPay(EmaPayInfo emaPayInfo) {

        EmaUtils.getInstance((Activity) mContext).realPay(mListener,emaPayInfo);



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
    public void pay(final EmaPayInfo payInfo, EmaSDKListener listener) {
        this.mListener = listener;

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
                params.put("appId",ULocalUtils.getAppId(mContext));
                if(!TextUtils.isEmpty(payInfo.getGameTransCode())){
                    params.put("gameTransCode", payInfo.getGameTransCode());
                }
                Log.e("Emapay_pay", payInfo.getProductId() + ".." + mEmaUser.getToken() + ".." + payInfo.getProductNum());

                String sign = ULocalUtils.getAppId(mContext)+(TextUtils.isEmpty(payInfo.getGameTransCode())?null:payInfo.getGameTransCode())+payInfo.getProductId()+payInfo.getProductNum()+mEmaUser.getToken()+EmaUser.getInstance().getAppkey();
                //LOG.e("rawSign",sign);
                sign = ULocalUtils.MD5(sign);
                params.put("sign", sign);

                try {
                    String result = new HttpRequestor().doPost(Instants.CREAT_ORDER_URL, params);

                    Log.e("pay creatOrder",result);
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
                    payInfo.setUid(mEmaUser.getAllianceUid());
                    payInfo.setProductName(productName);
                    payInfo.setPrice(Integer.parseInt(productPrice));
                    payInfo.setDescription(description);

                    Log.e("createOrder", message + coinEnough + orderId + unit + productPrice);

                    Message msg = new Message();
                    msg.what = ORDER_SUCCESS;
                    msg.obj = payInfo;
                    mHandler.sendMessage(msg);
                } catch (Exception e) {
                    mHandler.sendEmptyMessage(ORDER_FAIL);
                    e.printStackTrace();
                }

            }
        });
    }
}
