package com.sanron.lib;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
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
    private static final int FAKE_VIEW_ID = R.id.sbh_status_bar_fake_view;
    private static final int KET_HELPER_INSTANCE = R.id.sbh_status_bar_fake_view;
    private static final int KEY_FIT = R.id.sbh_status_bar_fake_view;
    private static final int TAG_WRAP = R.id.sbh_content_wrap;

    private Window mWindow;
    private View mFakeStatusView;
    private HelperView mHelperView;

    /**
     * 布局是否延伸到状态栏之下(不延伸到导航栏)
     */
    private boolean mLayoutBelowStatusBar = false;

    /**
     * 状态栏颜色
     */
    private int mStatusBarColor;

    /**
     * 是否初始化
     */
    private boolean mInstalled = false;

    /**
     * 黑色遮罩不透明度
     */
    private float mScrimAlpha = 0f;

    /**
     * 是否暗色图标
     */
    private boolean mDarkIcon = false;

    /**
     * 需要添加padding的view
     */
    private WeakList<View> mNeedPaddingView = new WeakList<>();

    /**
     * 需要添加margin的view
     */
    private WeakList<View> mNeedMarginView = new WeakList<>();

    /**
     * 保存的参数状态
     */
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

    protected void install() {
        if (mInstalled) {
            return;
        }

        addHelperView(getDecorView());
        setContentFit();
        translucentStatus();
        setStatusBarColor(0xFF000000);
        setLayoutBelowStatusBar(false);
        mInstalled = true;
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
    }

    /**
     * 添加一个FrameLayout消费inset
     * 修复当View的SystemUiVisibility设置为{@link View#SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN}，
     * 且inputMode设置为adjustResize时，输入框没有没顶起
     * 原因在于创建完DecorView后，会调用View.makeOptionalFitsSystemWindows方法，导致view不对inset消费
     */
    private void setContentFit() {
        ViewGroup contentView = getWindow().findViewById(android.R.id.content);
        if (contentView != null && contentView.getTag(TAG_WRAP) == null) {
            FrameLayout contentWrap = new FrameLayout(contentView.getContext());
            contentWrap.setId(R.id.sbh_content_wrap);
            ViewCompat.setFitsSystemWindows(contentWrap, true);

            ViewGroup contentParent = (ViewGroup) contentView.getParent();
            contentParent.removeView(contentView);
            contentParent.addView(contentWrap, contentView.getLayoutParams());

            contentWrap.addView(contentView, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            contentView.setTag(TAG_WRAP, new Object());
        }
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
        setLayoutBelowStatusBar(state.belowStatusBar);
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
        tagState.belowStatusBar = mLayoutBelowStatusBar;
        tagState.scrimAlpha = mScrimAlpha;
        tagState.statusBarColor = mStatusBarColor;
        mStates.put(tag, tagState);
        return this;
    }

    /**
     * 移除tag
     *
     * @param tag
     * @return
     */
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
        mStatusBarColor = color;
        updateStatusBarColor(color);
        return this;
    }

    /**
     * 布局是否延伸到状态栏
     *
     * @param layoutBelowStatusBar true布局延伸到状态栏下
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public StatusBarHelper setLayoutBelowStatusBar(boolean layoutBelowStatusBar) {
        mLayoutBelowStatusBar = layoutBelowStatusBar;
        int uiFlag = getDecorView().getSystemUiVisibility();
        if (mLayoutBelowStatusBar) {
            uiFlag = uiFlag | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        } else {
            uiFlag = uiFlag & ~(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        getDecorView().setSystemUiVisibility(uiFlag);
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
        mScrimAlpha = alpha;
        updateScrimAlpha(alpha);
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
        if (mLayoutBelowStatusBar && mHelperView.mLocalInsets != null) {
            setViewPadding(view, mHelperView.mLocalInsets);
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
        if (mLayoutBelowStatusBar && mHelperView.mLocalInsets != null) {
            setViewMargin(view, mHelperView.mLocalInsets);
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

    /**
     * 设置marginTop
     *
     * @param view
     * @param rect
     */
    private void setViewMargin(View view, Rect rect) {
        removeViewMargin(view);
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            lp.topMargin += rect.top;
            view.setLayoutParams(lp);
        }
        view.setTag(KEY_FIT, new Rect(rect));
    }

    /**
     * 移除marginTop
     *
     * @param view
     */
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

    /**
     * 设置paddingTop
     *
     * @param view
     * @param rect
     */
    private void setViewPadding(View view, Rect rect) {
        //去除设置过的偏移
        removeViewPadding(view);

        view.setPadding(view.getPaddingLeft(),
                view.getPaddingTop() + rect.top,
                view.getPaddingRight(),
                view.getPaddingBottom());
        view.setTag(KEY_FIT, new Rect(rect));
    }

    /**
     * 移除paddingTop
     *
     * @param view
     */
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

    /**
     * 创建StatusBarView
     */
    private void ensureFakeView() {
        ViewGroup decorView = (ViewGroup) mWindow.getDecorView();
        if (mFakeStatusView == null) {
            mFakeStatusView = new View(decorView.getContext());
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    0);
            lp.gravity = Gravity.TOP;
            mFakeStatusView.setLayoutParams(lp);
            mFakeStatusView.setId(FAKE_VIEW_ID);
            mFakeStatusView.setBackgroundDrawable(new ScrimDrawable());
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
    private void updateStatusBarColor(int color) {
        ensureFakeView();
        ScrimDrawable bg = (ScrimDrawable) mFakeStatusView.getBackground();
        bg.setColor(color);
    }

    /**
     * 更新遮罩层
     */
    private void updateScrimAlpha(float scrimAlpha) {
        ensureFakeView();
        ScrimDrawable bg = (ScrimDrawable) mFakeStatusView.getBackground();
        bg.setScrim(scrimAlpha);
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


    /**
     * 帮助视图，不显示，处理系统Inset
     */
    private class HelperView extends View {

        Rect mLocalInsets;

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
            if (mLocalInsets == null) {
                mLocalInsets = new Rect();
            }

            mLocalInsets.set(insets);

            return applyInsets(insets);
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public WindowInsets onApplyWindowInsets(WindowInsets insets) {
            if (mLocalInsets == null) {
                mLocalInsets = new Rect();
            }

            mLocalInsets.set(insets.getSystemWindowInsetLeft(),
                    insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(),
                    insets.getSystemWindowInsetBottom());

            final Rect rect = new Rect(mLocalInsets);
            if (!applyInsets(rect)) {
                return insets.replaceSystemWindowInsets(rect);
            } else {
                return insets.consumeSystemWindowInsets();
            }
        }

        private boolean applyInsets(Rect insets) {
            updateStatusHeight(insets.top);

            List<View> needPaddingViews = mNeedPaddingView.getLive();
            List<View> needMarginViews = mNeedMarginView.getLive();
            if (mLayoutBelowStatusBar) {
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

    private static class TagState {
        private String tag;

        private boolean belowStatusBar;

        private int statusBarColor;

        private float scrimAlpha;

        private boolean darkIcon;

        TagState(String tag) {
            this.tag = tag;
        }
    }

    private static boolean overMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    private static boolean overKitkat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    private static boolean overLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }
}
