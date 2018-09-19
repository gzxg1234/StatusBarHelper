package com.sanron.lib;

import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.Set;

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
        //其他
        OTHER
    }

    private static Method GET_METHOD;
    private static Properties BUILD_PROPERTIES;
    private static OS MY_OS;

    private static String[] FLYME_KEY = {"ro.meizu.product.model", "ro.meizu.security", "ro.meizu.region.enable"};
    private static String[] FLYME_KEY_WORD = {"flyme", "meizu"};

    private static String[] MIUI_KEY = {"ro.miui.ui.version.code", "ro.miui.ui.version.name", "ro.miui.internal.storage"};
    private static String[] MIUI_KEY_WORD = {"miui"};

    private static final String MIUI_VERSION_KEY = "ro.miui.ui.version.name";

    static {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(new File(Environment.getRootDirectory(), "build.prop"));
            BUILD_PROPERTIES = new Properties();
            BUILD_PROPERTIES.load(fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            Class clazz = Class.forName("SystemProperties");
            GET_METHOD = clazz.getDeclaredMethod("get", String.class);
            GET_METHOD.setAccessible(true);
        } catch (NoSuchMethodException e) {
        } catch (ClassNotFoundException e) {
        }

        if (Build.MANUFACTURER.equals("Meizu")
                || hasProperties(FLYME_KEY)
                || hasPropertyForWord(FLYME_KEY_WORD)) {
            MY_OS = OS.FLYME;
        } else if (Build.MANUFACTURER.equals("Xiaomi")
                || hasProperties(MIUI_KEY)
                || hasPropertyForWord(MIUI_KEY_WORD)) {
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
     * 是否有包含word的key存在
     *
     * @param words
     * @return
     */
    private static boolean hasPropertyForWord(String... words) {
        if (BUILD_PROPERTIES != null) {
            Set<Object> keys = BUILD_PROPERTIES.keySet();
            for (Object key : keys) {
                if (key instanceof String) {
                    for (String word : words) {
                        if (((String) key).contains(word)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
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
            if (value != null) {
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
        if (BUILD_PROPERTIES != null) {
            return BUILD_PROPERTIES.getProperty(key);
        }
        return "";
    }
}
