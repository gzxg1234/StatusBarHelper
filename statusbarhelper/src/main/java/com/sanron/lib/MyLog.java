package com.sanron.lib;

import android.util.Log;

/**
 * Author:sanron
 * Time:2018/9/26
 * Description:
 */
public class MyLog {

    public static boolean DEBUG = BuildConfig.DEBUG;

    public static void d(String tag,String msg,Throwable e){
        if(DEBUG){
            Log.d(tag,msg,e);
        }
    }
}
