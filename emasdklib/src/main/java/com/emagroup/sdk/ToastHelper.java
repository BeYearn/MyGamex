package com.emagroup.sdk;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class ToastHelper
{
	
    public static void toast(Context context, String toast)
    {
        if(Looper.getMainLooper() == Looper.myLooper())
        	doShowToast(context, toast);
        else{
        	final Context _context = context;
        	final String _toast = toast;
        	(new Handler(Looper.getMainLooper())).post(new Runnable() {
        		public void run() {
        			doShowToast(_context, _toast);
        		}
        	});
        }
    }

    private static void doShowToast(Context context, String toast)
    {
    	Toast.makeText(context, toast, Toast.LENGTH_LONG).show();
    }
}
