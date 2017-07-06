package com.emagroup.sdk.impl;


import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.emagroup.sdk.EmaSDKListener;
import com.emagroup.sdk.ResourceManager;
import com.google.android.gms.plus.PlusShare;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShareDialog extends Dialog implements View.OnClickListener {
    private static final String TAG = ShareDialog.class.getSimpleName();
    private static ResourceManager mResourceManager;

    private LinearLayout linear_weixin_friend;
    private LinearLayout linear_weixin_quan;
    private LinearLayout llShareGoogle;
    private LinearLayout linearLayout_qq;
    private LinearLayout linearLayout_qzone;
    private TextView btn_close;
    private Activity mActivity;

    private EmaSDKListener callbackListener;
    private static ShareDialog mDialog;
    private Bitmap shareBitMap;
    private String shareString;
    private int shareType;

    //那种分享类型
    private final int SHARE_TYPE_TEXT = 11;
    private final int SHARE_TYPE_IMAGE = 12;
    private final int SHARE_TYPE_WEB = 13;
    private String shareWUrl;
    private String shareWTitle;
    private String shareWDescription;
    private Bitmap shareWBitmap;
    public static final int REQ_SELECT_PHOTO = 21;


    public ShareDialog(Context context) {
        super(context);
    }

    protected ShareDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public ShareDialog(Activity activity, int theme) {
        super(activity, theme);
        mActivity = activity;
    }

    public static ShareDialog create(Activity activity) {

        mResourceManager = ResourceManager.getInstance(activity.getApplicationContext());

        if (mDialog == null) {
            mDialog = new ShareDialog(activity, mResourceManager.getIdentifier("TransparentDialog", "style"));
            mDialog.setContentView(mResourceManager.getLayout("dialog_share"));
            mDialog.setCancelable(true);
        }

        return mDialog;
    }

    public void showDialog() {

        llShareGoogle = (LinearLayout) findViewById(mResourceManager.getIdentifier("ll_share_google", "id"));

        btn_close = (TextView) findViewById(mResourceManager.getIdentifier("btn_close", "id"));

        llShareGoogle.setOnClickListener(this);
        btn_close.setOnClickListener(this);

        show();
    }

    @Override
    public void onClick(View v) {
        if (v == llShareGoogle) {
            if (shareType == SHARE_TYPE_WEB) {
                // Launch the Google+ share dialog with attribution to your app.
                Intent shareIntent = new PlusShare.Builder(mActivity)
                        .setType("text/plain")
                        .setText(shareWDescription)
                        .setContentUrl(Uri.parse(shareWUrl))
                        .getIntent();
                mActivity.startActivityForResult(shareIntent, 0);
            } else if (shareType == SHARE_TYPE_IMAGE) {
                Intent photoPicker = new Intent(Intent.ACTION_PICK);
                photoPicker.setType("video/*, image/*");
                mActivity.startActivityForResult(photoPicker, REQ_SELECT_PHOTO);
            } else if (shareType == SHARE_TYPE_TEXT) {

            }

        }

        dismiss();
    }

    public void setCallbackListener(EmaSDKListener listener) {
        if (listener == null) {
            Toast.makeText(mActivity, "请传入正确参数", Toast.LENGTH_SHORT).show();
            return;
        }
        this.callbackListener = listener;
    }


    public void setShareBitMap(Bitmap shareBitMap) {
        this.shareType = SHARE_TYPE_IMAGE;
        this.shareBitMap = shareBitMap;
    }

    public void setShareString(String shareString) {
        this.shareType = SHARE_TYPE_TEXT;
        this.shareString = shareString;
    }

    public void setShareWebPage(String url, String title, String description, Bitmap bitmap) {
        this.shareType = SHARE_TYPE_WEB;
        this.shareWUrl= url;
        this.shareWTitle = title;
        this.shareWDescription = description;
        this.shareWBitmap = bitmap;
    }
}
