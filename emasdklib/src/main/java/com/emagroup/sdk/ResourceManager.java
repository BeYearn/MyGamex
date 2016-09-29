package com.emagroup.sdk;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.view.LayoutInflater;
import android.view.View;

public class ResourceManager {

	private static ResourceManager mInstance;
	private final static Object syncLock = new Object();  
	private Context mContext;
    private Resources mResources;
    private String mPackageName;
    private LayoutInflater mLayoutInflater;
	
	private ResourceManager(Context context){
		mContext = context;
		mResources = mContext.getResources();
		mPackageName = mContext.getApplicationInfo().packageName;
		mLayoutInflater = LayoutInflater.from(mContext);
	}
	
	public static ResourceManager getInstance(Context context){
		if(mInstance == null){
			synchronized(syncLock)
            {
                if(mInstance == null)
                	mInstance = new ResourceManager(context);
            }
		}
		return mInstance;
	}
	
	/**
	 * 获取资源ID
	 * @param resourceKey
	 * @param type
	 * @return
	 */
	public int getIdentifier(String resourceKey, String type){
		return mResources.getIdentifier(resourceKey, type, mPackageName);
	}
	
	/**
	 * 获取字符串
	 * @param resourceKey
	 * @return
	 */
	public String getString(String resourceKey){
		int resourceId = mResources.getIdentifier(resourceKey, "string", mPackageName);
		return mResources.getString(resourceId);
	}
	
	/**
	 * 获取布局文件实例化出来的view（暂时不考虑横竖屏）
	 * @param resourceKey
	 * @return
	 */
	public View getLayout(String resourceKey){
		int resourceId = mResources.getIdentifier(resourceKey, "layout", mPackageName);
		return mLayoutInflater.inflate(resourceId, null);
	}
	
	/**
	 * 获取Drawable
	 * @param resourceKey
	 * @return
	 */
	public Drawable getDrawable(String resourceKey){
		int resourceId = mResources.getIdentifier(resourceKey, "drawable", mPackageName);
		return mResources.getDrawable(resourceId);
	}
	
	/**
	 * 获取带有2中状态的图片（正常情况，点击情景）
	 * @param origin -- 正常
	 * @param press	--	点击
	 * @return
	 */
	public Drawable getStatusDrawable(String origin, String press){
		int originId = mResources.getIdentifier(origin, "drawable", mPackageName);
		int pressId = mResources.getIdentifier(press, "drawable", mPackageName);
		StateListDrawable listDrawable = new StateListDrawable();
		listDrawable.addState(new int[]{android.R.attr.state_pressed}, mResources.getDrawable(pressId));
		listDrawable.addState(new int[0], mResources.getDrawable(originId));
		return listDrawable;
	}
}
