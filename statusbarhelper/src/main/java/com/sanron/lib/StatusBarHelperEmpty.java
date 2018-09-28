package com.sanron.lib;

import android.view.View;
import android.view.Window;

/**
 * Created by chenrong on 2017/9/21.
 */

class StatusBarHelperEmpty extends StatusBarHelper {

    StatusBarHelperEmpty(Window activity) {
        super(activity);
    }

    @Override
    protected void install() {
    }

    @Override
    public StatusBarHelper setStatusBarColor(int color) {
        return this;
    }


    @Override
    public StatusBarHelper switchTag(String tag) {
        return this;
    }

    @Override
    public StatusBarHelper removeTag(String tag) {
        return this;
    }

    @Override
    public StatusBarHelper setDarkIcon(float scrimAlpha) {
        return this;
    }

    @Override
    public StatusBarHelper setDarkIcon() {
        return this;
    }

    @Override
    public StatusBarHelper setLightIcon() {
        return this;
    }

    @Override
    public StatusBarHelper setLayoutBelowStatusBar(boolean fullScreen) {
        return this;
    }

    @Override
    public StatusBarHelper setPaddingTop(View view) {
        return this;
    }

    @Override
    public StatusBarHelper removePaddingTop(View view) {
        return this;
    }

    @Override
    public StatusBarHelper setMarginTop(View view) {
        return this;
    }

    @Override
    public StatusBarHelper saveTag(String tag) {
        return this;
    }

    @Override
    public StatusBarHelper removeMarginTop(View view) {
        return this;
    }
}
