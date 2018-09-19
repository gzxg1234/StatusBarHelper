package com.sanron.lib;

import android.support.annotation.ColorInt;

/**
 * Author:sanron
 * Time:2018/9/18
 * Description:
 */
public class ColorUtil {

    //判断是否暗色时的最大灰度值，灰度值大于它则表示是亮色
    private static final int BLACK_GRAY_MAX = 50;

    /**
     * 判断颜色是否偏黑色
     *
     * @param color 颜色
     * @return
     */
    public static boolean isBlackColor(int color) {
        int grey = toGrey(color);
        return grey < BLACK_GRAY_MAX;
    }

    /**
     * 颜色转换成灰度值
     *
     * @param rgb 颜色
     * @return　灰度值
     */
    public static int toGrey(int rgb) {
        int blue = rgb & 0x000000FF;
        int green = (rgb & 0x0000FF00) >> 8;
        int red = (rgb & 0x00FF0000) >> 16;
        return (red * 38 + green * 75 + blue * 15) >> 7;
    }

    /**
     * 计算状态栏颜色
     *
     * @param color color值
     * @param alpha alpha值
     * @return 最终的状态栏颜色
     */
    public static int calculateStatusColor(@ColorInt int color, float alpha) {
        if (alpha >= 1) {
            return color;
        }
        int red = color >> 16 & 0xff;
        int green = color >> 8 & 0xff;
        int blue = color & 0xff;
        red = (int) (red * alpha + 0.5);
        green = (int) (green * alpha + 0.5);
        blue = (int) (blue * alpha + 0.5);
        return 0xff << 24 | red << 16 | green << 8 | blue;
    }
}
