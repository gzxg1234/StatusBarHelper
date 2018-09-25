package com.sanron.lib;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.ViewCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.util.List;


/**
 * 状态栏帮助类
 * Created by chenrong on 2017/9/21.
 */

public class StatusBarHelper {
    private static final int FAKE_VIEW_ID = R.id.status_bar_fake_view;
    private static final int KET_HELPER_INSTANCE = R.id.status_bar_fake_view;
    private static final int KEY_FIT = R.id.status_bar_fake_view;

    private Window mWindow;
    private View mFakeStatusView;
    private HelperView mHelperView;

    //布局是否延伸到状态栏之下(不延伸到导航栏)
    private boolean mLayoutFullScreen = false;

    //状态栏颜色
    private int mStatusBarColor;

    //是否初始化
    private boolean mInstalled = false;

    //黑色遮罩不透明度
    private float mScrimAlpha = 0f;

    //是否暗色图标
    private boolean mDarkIcon = false;

    //需要添加padding的view
    private WeakList<View> mNeedPaddingView = new WeakList<>();

    //需要添加margin的view
    private WeakList<View> mNeedMarginView = new WeakList<>();

    //保存的状态
    private ArrayMap<String, TagState> mStates = new ArrayMap<>();

    public static StatusBarHelper with(Activity activity) {
        return with(activity.getWindow());
    }

    public static StatusBarHelper with(Dialog dialog) {
        return with(dialog.getWindow());
    }

    public static StatusBarHelper with(Window window) {
        View decorView = window.getDecorView();
        Object helper = decorView.getTag(KET_HELPER_INSTANCE);
        if (helper == null) {
            if (overKitkat()) {
                helper = new StatusBarHelper(window);
                ((StatusBarHelper) helper).switchTag(null);
            } else {
                helper = new StatusBarHelperEmpty(window);
            }
            decorView.setTag(KET_HELPER_INSTANCE, helper);
        }
        return (StatusBarHelper) helper;
    }

    StatusBarHelper(Window window) {
        mWindow = window;
        install();
    }

    private ViewGroup getDecorView() {
        return (ViewGroup) mWindow.getDecorView();
    }

    private Window getWindow() {
        return mWindow;
    }

    public StatusBarHelper install() {
        if (mInstalled) {
            return this;
        }

        addHelperView(getDecorView());
        SetFitSystem.install(getWindow());
        mInstalled = true;
        return this;
    }

    /**
     * 切换Tag，可以有多个Tag保存不同状态
     *
     * @param tag
     * @return
     */
    public StatusBarHelper switchTag(String tag) {
        TagState state = mStates.get(tag);
        if (state == null) {
            return this;
        }
        setStatusBarColor(state.statusBarColor);
        setLayoutFullScreen(state.layoutFullScreen);
        setStatusBarDarkIcon(state.darkIcon);
        setScrimAlpha(state.scrimAlpha);
        return this;
    }

    /**
     * 保存当前状态
     *
     * @param tag
     * @return
     */
    public StatusBarHelper saveTag(String tag) {
        TagState tagState = new TagState(tag);
        tagState.darkIcon = mDarkIcon;
        tagState.layoutFullScreen = mLayoutFullScreen;
        tagState.scrimAlpha = mScrimAlpha;
        tagState.statusBarColor = mStatusBarColor;
        mStates.put(tag, tagState);
        return this;
    }

    public StatusBarHelper removeTag(String tag) {
        mStates.remove(tag);
        return this;
    }

    /**
     * 设置状态栏颜色
     *
     * @param color 颜色
     * @return
     */
    public StatusBarHelper setStatusBarColor(int color) {
        translucentStatus();
        mStatusBarColor = color;
        updateStatusBarColor(color, mScrimAlpha);
        return this;
    }

    /**
     * 统一由本类创建statusBarBackground，方便管理
     */
    private void translucentStatus() {
        if (overLollipop()) {
            //统一由Helper创建View
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(0);
        } else if (overKitkat()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        ViewCompat.requestApplyInsets(getDecorView());
    }


    /**
     * 布局是否延伸到状态栏
     *
     * @param fullScreen true布局延伸到状态栏下
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public StatusBarHelper setLayoutFullScreen(boolean fullScreen) {
        if (mLayoutFullScreen == fullScreen) {
            return this;
        }
        mLayoutFullScreen = fullScreen;
        if (mLayoutFullScreen) {
            getDecorView().setSystemUiVisibility(getDecorView().getSystemUiVisibility()
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        } else {
            getDecorView().setSystemUiVisibility(getDecorView().getSystemUiVisibility()
                    & ~(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE));
        }
        ViewCompat.requestApplyInsets(getDecorView());
        return this;
    }

    /**
     * 设置黑色遮罩
     *
     * @param alpha 透明度
     * @return
     */
    private StatusBarHelper setScrimAlpha(float alpha) {
        if (mScrimAlpha == alpha) {
            return this;
        }
        mScrimAlpha = alpha;
        updateStatusBarColor(mStatusBarColor, alpha);
        return this;
    }

    /**
     * 添加需要添加padding的View
     *
     * @param view
     * @return
     */
    public StatusBarHelper setPaddingTop(View view) {
        if (!mNeedPaddingView.contains(view)) {
            mNeedPaddingView.add(view);
        }
        if (mLayoutFullScreen && mHelperView.localInsets != null) {
            setViewPadding(view, mHelperView.localInsets);
        }
        return this;
    }

    /**
     * 添加需要添加margin的View
     *
     * @param view
     * @return
     */
    public StatusBarHelper setMarginTop(View view) {
        if (!mNeedMarginView.contains(view)) {
            mNeedMarginView.add(view);
        }
        if (mLayoutFullScreen && mHelperView.localInsets != null) {
            setViewMargin(view, mHelperView.localInsets);
        }
        return this;
    }

    /**
     * 移除设置PaddingTop的View
     *
     * @param view
     * @return
     */
    public StatusBarHelper removePaddingTop(View view) {
        mNeedPaddingView.remove(view);
        removeViewPadding(view);
        return this;
    }


    /**
     * 移除设置MarginTop的View
     *
     * @param view
     * @return
     */
    public StatusBarHelper removeMarginTop(View view) {
        mNeedMarginView.remove(view);
        removeViewPadding(view);
        return this;
    }

    private void setViewMargin(View view, Rect rect) {
        //去除设置过的偏移
        removeViewMargin(view);
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            lp.topMargin += rect.top;
            view.setLayoutParams(lp);
        }
        view.setTag(KEY_FIT, new Rect(rect));
    }

    private void removeViewMargin(View view) {
        //去除设置过的偏移
        Object tag = view.getTag(KEY_FIT);
        if (tag instanceof Rect) {
            Rect rect = (Rect) tag;
            if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
                lp.topMargin -= rect.top;
                view.setLayoutParams(lp);
            }
            view.setTag(KEY_FIT, null);
        }
    }

    private void setViewPadding(View view, Rect rect) {
        //去除设置过的偏移
        removeViewPadding(view);

        view.setPadding(view.getPaddingLeft(),
                view.getPaddingTop() + rect.top,
                view.getPaddingRight(),
                view.getPaddingBottom());
        view.setTag(KEY_FIT, new Rect(rect));
    }

    private void removeViewPadding(View view) {
        //去除设置过的偏移
        Object tag = view.getTag(KEY_FIT);
        if (tag instanceof Rect) {
            Rect rect = (Rect) tag;
            view.setPadding(view.getPaddingLeft(),
                    view.getPaddingTop() - rect.top,
                    view.getPaddingRight(),
                    view.getPaddingBottom());
            view.setTag(KEY_FIT, null);
        }
    }

    private void ensureFakeView() {
        ViewGroup decorView = (ViewGroup) mWindow.getDecorView();
        if (mFakeStatusView == null) {
            mFakeStatusView = new View(decorView.getContext());
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    0);
            lp.gravity = Gravity.TOP;
            mFakeStatusView.setLayoutParams(lp);
            mFakeStatusView.setId(FAKE_VIEW_ID);
            mFakeStatusView.setBackgroundDrawable(new FakeDrawable());
            decorView.addView(mFakeStatusView);
        }
    }

    /**
     * 更新高度
     */
    private void updateStatusHeight(int height) {
        ensureFakeView();
        ViewGroup.LayoutParams lp = mFakeStatusView.getLayoutParams();
        if (lp.height != height) {
            lp.height = height;
            mFakeStatusView.setLayoutParams(lp);
        }
    }

    /**
     * 更新颜色
     */
    private void updateStatusBarColor(int color, float scrimAlpha) {
        ensureFakeView();
        FakeDrawable bg = (FakeDrawable) mFakeStatusView.getBackground();
        bg.setScrim(scrimAlpha);
        bg.setColor(color);
    }

    private void addHelperView(ViewGroup decorView) {
        mHelperView = new HelperView(mWindow.getContext());
        mHelperView.setLayoutParams(new ViewGroup.LayoutParams(0, 0));
        decorView.addView(mHelperView, 0);
    }


    /**
     * 设置状态栏暗色图标
     *
     * @param scrimAlpha 设置失败添加遮罩的透明度
     * @return
     */
    public StatusBarHelper setDarkIcon(float scrimAlpha) {
        boolean success = setStatusBarDarkIcon(true);
        if (!success) {
            setScrimAlpha(scrimAlpha);
        }
        return this;
    }

    /**
     * 设置状态栏暗色图标
     *
     * @return this
     */
    public StatusBarHelper setDarkIcon() {
        return setDarkIcon(0);
    }

    /**
     * 设置状态栏亮色色图标
     *
     * @return this
     */
    public StatusBarHelper setLightIcon() {
        setStatusBarDarkIcon(false);
        setScrimAlpha(0f);
        return this;
    }

    /**
     * 设置状态栏暗色图标
     */
    private boolean setStatusBarDarkIcon(boolean dark) {
        mDarkIcon = dark;

        boolean success = false;
        Window window = getWindow();
        if (OSUtil.isFlyme()) {
            success = MeizuStatusBarHelper.setStatusBarDarkIcon(mWindow, dark);
        } else if (OSUtil.isMiui()) {
            final int miuiVersion = OSUtil.getMiuiVersion();
            if ((miuiVersion >= 6 && miuiVersion < 9) ||
                    (miuiVersion >= 9 && Build.VERSION.SDK_INT < Build.VERSION_CODES.M)) {
                //miui6-9用miui内部方法设置状态栏文字颜色，miui9(且android版本大等6.0)开始直接使用原生方法
                success = MiuiStatusBarHelper.setStatusBarDarkMode(mWindow, dark);
            }
        }
        if (overMarshmallow()) {
            int uiVisibility = window.getDecorView().getSystemUiVisibility();
            if (dark) {
                uiVisibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                uiVisibility &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            window.getDecorView().setSystemUiVisibility(uiVisibility);
            success = true;
        }
        return success;
    }


    public static boolean overMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean overKitkat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static boolean overLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * 帮助视图，不显示，处理系统Inset
     */
    private class HelperView extends View {

        Rect localInsets;

        private HelperView(Context context) {
            super(context);
        }

        @SuppressLint("MissingSuperCall")
        @Override
        public void draw(Canvas canvas) {
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(0, 0);
        }

        @SuppressWarnings("deprecation")
        @Override
        protected boolean fitSystemWindows(Rect insets) {
            if (localInsets == null) {
                localInsets = new Rect();
            }

            localInsets.set(insets);

            return applyInsets(insets);
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public WindowInsets onApplyWindowInsets(WindowInsets insets) {
            if (localInsets == null) {
                localInsets = new Rect();
            }

            localInsets.set(insets.getSystemWindowInsetLeft(),
                    insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(),
                    insets.getSystemWindowInsetBottom());

            final Rect rect = new Rect(localInsets);
            if (!applyInsets(rect)) {
                return insets.replaceSystemWindowInsets(rect);
            } else {
                return insets.consumeSystemWindowInsets();
            }
        }

        private boolean applyInsets(Rect insets) {
            updateStatusHeight(insets.top);
            updateStatusBarColor(mStatusBarColor, mScrimAlpha);

            List<View> needPaddingViews = mNeedPaddingView.getLive();
            List<View> needMarginViews = mNeedMarginView.getLive();
            if (mLayoutFullScreen) {
                for (View view : needPaddingViews) {
                    setViewPadding(view, insets);
                }
                for (View view : needMarginViews) {
                    setViewMargin(view, insets);
                }
                insets.top = 0;
                return false;
            } else {
                for (View view : needPaddingViews) {
                    removeViewPadding(view);
                }
                for (View view : needMarginViews) {
                    removeViewMargin(view);
                }
                return false;
            }
        }

    }

    private static class FakeDrawable extends LayerDrawable {

        public FakeDrawable() {
            super(createDrawable());
        }

        private static Drawable[] createDrawable() {
            Drawable[] layers = new Drawable[2];
            layers[0] = new ColorDrawable();
            layers[1] = new ColorDrawable(0);
            return layers;
        }

        public void setColor(int color) {
            ColorDrawable layer1 = (ColorDrawable) getDrawable(0);
            layer1.setColor(color);
        }

        public void setScrim(float alpha) {
            ColorDrawable layer2 = (ColorDrawable) getDrawable(1);
            layer2.setColor(Color.argb((int) (alpha * 0xFF), 0, 0, 0));
        }
    }

    private static class TagState {
        private String tag;

        private boolean layoutFullScreen;

        private int statusBarColor;

        private float scrimAlpha;

        private boolean darkIcon;

        public TagState(String tag) {
            this.tag = tag;
        }
    }
}
