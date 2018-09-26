package com.sanron.lib;

import android.view.Window;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * MIUI状态栏帮助
 */
class MiuiStatusBarHelper {

    private static final String TAG = MeizuStatusBarHelper.class.getSimpleName();

    public static boolean setStatusBarDarkMode(Window window, boolean darkmode) {
        Class<? extends Window> clazz = window.getClass();
        try {
            int darkModeFlag = 0;
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(window, darkmode ? darkModeFlag : 0, darkModeFlag);
            return true;
        } catch (Exception e) {
            MyLog.d(TAG, "setStatusBarDarkMode error", e);
        }
        return false;
    }
}
