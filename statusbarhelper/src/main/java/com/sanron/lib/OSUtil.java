package com.sanron.lib;

import android.annotation.SuppressLint;
import android.os.Build;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 系统帮助类
 * Created by chenrong on 2017/9/15.
 */

@SuppressWarnings({"unchecked", "TryWithIdenticalCatches"})
public class OSUtil {
    enum OS {
        //魅族Flyme
        FLYME,
        //小米MIUI
        MIUI,
        //华为EMUI
        EMUI,
        //其他
        OTHER
    }

    private static OS MY_OS;


    private static Method GET_METHOD;

    private static final String KEY_EMUI_VERSION_NAME = "ro.build.version.emui";

    private static String[] FLYME_KEY = {"ro.meizu.product.model", "ro.meizu.security",
            "ro.meizu.region.enable", "ro.flyme.published"};

    private static String[] MIUI_KEY = {"ro.miui.ui.version.code", "ro.miui.ui.version.name", "ro.miui.internal.storage"};

    private static final String MIUI_VERSION_KEY = "ro.miui.ui.version.name";

    static {
        try {
            @SuppressLint("PrivateApi")
            Class clazz = Class.forName("android.os.SystemProperties");
            GET_METHOD = clazz.getDeclaredMethod("get", String.class);
            GET_METHOD.setAccessible(true);
        } catch (NoSuchMethodException e) {
        } catch (ClassNotFoundException e) {
        }

        if (Build.MANUFACTURER.equals("Meizu")
                || Build.DISPLAY.toLowerCase().contains("flyme")
                || hasProperties(FLYME_KEY)) {
            MY_OS = OS.FLYME;
        } else if (Build.MANUFACTURER.equals("Xiaomi")
                || hasProperties(MIUI_KEY)) {
            MY_OS = OS.MIUI;
        } else {
            MY_OS = OS.OTHER;
        }
    }

    /**
     * 是否是魅族Flyme
     *
     * @return
     */
    public static boolean isFlyme() {
        return MY_OS == OS.FLYME;
    }

    /**
     * 是否是小米Miui
     *
     * @return
     */
    public static boolean isMiui() {
        return MY_OS == OS.MIUI;
    }

    /**
     * 是否emui
     *
     * @return
     */
    public static boolean isEMUI() {
        return MY_OS == OS.EMUI;
    }

    /**
     * 是否emui3.1
     *
     * @return
     */
    public static boolean isEMUI3_1() {
        String version = getEMUIVersion();
        if (version != null) {
            return "EmotionUI 3".equals(version) || version.contains("EmotionUI_3.1");
        }
        return false;
    }

    /**
     * 得到emui的版本
     * Gets emui version.
     *
     * @return the emui version
     */
    private static String getEMUIVersion() {
        return isEMUI() ? getProperty(KEY_EMUI_VERSION_NAME) : null;
    }

    /**
     * 获取miui版本
     *
     * @return
     */
    public static int getMiuiVersion() {
        if (isMiui()) {
            String version = getProperty(MIUI_VERSION_KEY);
            try {
                version = version.substring(1);
                return Integer.parseInt(version);
            } catch (Throwable e) {

            }
        }
        return -1;
    }


    /**
     * 是否有key
     *
     * @param keys
     * @return
     */
    private static boolean hasProperties(String... keys) {
        for (String key : keys) {
            String value = getProperty(key);
            if (!TextUtils.isEmpty(value)) {
                return true;
            }
        }
        return false;
    }

    private static String getProperty(String key) {
        if (GET_METHOD != null) {
            try {
                return (String) GET_METHOD.invoke(null, key);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return getPropCmd(key);
    }

    private static String getPropCmd(String name) {
        String line = null;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + name);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            MyLog.d("Unable to read prop " + name, ex);
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return line;
    }
}
