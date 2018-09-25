package com.sanron.lib;

import android.app.Activity;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by wangchende on 15-9-7.
 */
class MeizuStatusBarHelper {
    private static Method mSetStatusBarDarkIcon;

    static {
        try {
            mSetStatusBarDarkIcon = Activity.class.getMethod("setStatusBarDarkIcon", boolean.class);
            mSetStatusBarDarkIcon.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private static boolean changeMeizuFlag(WindowManager.LayoutParams winParams, String flagName, boolean on) {
        try {
            Field f = winParams.getClass().getDeclaredField(flagName);
            f.setAccessible(true);
            int bits = f.getInt(winParams);
            Field f2 = winParams.getClass().getDeclaredField("meizuFlags");
            f2.setAccessible(true);
            int meizuFlags = f2.getInt(winParams);
            int oldFlags = meizuFlags;
            if (on) {
                meizuFlags |= bits;
            } else {
                meizuFlags &= ~bits;
            }
            if (oldFlags != meizuFlags) {
                f2.setInt(winParams, meizuFlags);
                return true;
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean setStatusBarDarkIcon(Window window, boolean dark) {
        if (mSetStatusBarDarkIcon != null && window.getContext() instanceof Activity) {
            try {
                mSetStatusBarDarkIcon.invoke(window.getContext(), dark);
                return true;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else {
            return changeMeizuFlag(window.getAttributes(),
                    "MEIZU_FLAG_DARK_STATUS_BAR_ICON", dark);
        }
        return false;
    }
}
