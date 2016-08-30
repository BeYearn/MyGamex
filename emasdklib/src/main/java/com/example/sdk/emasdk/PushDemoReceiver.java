package com.example.sdk.emasdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.igexin.sdk.PushConsts;

public class PushDemoReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("PushDemoReceiver", "push test get info ");
        Bundle bundle = intent.getExtras();

        switch (bundle.getInt(PushConsts.CMD_ACTION)) {
            case PushConsts.GET_CLIENTID:

                String cid = bundle.getString("clientid");
                // TODO:处理cid返回

                break;
            case PushConsts.GET_MSG_DATA:
                String taskid = bundle.getString("taskid");
                String messageid = bundle.getString("messageid");
                byte[] payload = bundle.getByteArray("payload");
                if (payload != null) {
                    String data = new String(payload);
                    Log.e("个推透传数据",data);
                    // TODO:接收处理透传（payload）数据

                    EmaSDK.getInstance().makeCallBack(EmaCallBackConst.RECIVEMSG_MSG,data);
                }
                break;
            default:
                break;
        }
    }
}
