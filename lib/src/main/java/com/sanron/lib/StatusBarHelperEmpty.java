package com.sanron.lib;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;

import com.sanron.lib.StatusBarHelper;

/**
 * Created by chenrong on 2017/9/21.
 */

class StatusBarHelperEmpty extends StatusBarHelper {

    StatusBarHelperEmpty(Activity activity) {
        super(activity);
    }

    @Override
    public StatusBarHelper install() {
        return this;
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
    public StatusBarHelper setDarkIconOrScrim(boolean dark, float alpha) {
        return this;
    }

    @Override
    public StatusBarHelper setDarkIconOrScrim(boolean dark) {
        return this;
    }

    @Override
    public boolean setStatusBarDarkIcon(boolean dark) {
        return false;
    }

    @Override
    public StatusBarHelper setLayoutFullScreen(boolean fullScreen) {
        return this;
    }

    @Override
    public StatusBarHelper setScrimAlpha(float alpha) {
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
