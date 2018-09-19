package com.sanron.lib;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
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

    private Activity mActivity;
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

    //暗色图标设置失败 标识
    private boolean mDarkFailedFlag = false;

    //设置暗色图标失败时的默认遮罩透明度
    private static final float DEFAULT_FAILED_SCRIM = 0.2f;


    //需要添加padding的view
    private WeakList<View> mNeedPaddingView = new WeakList<>();

    //需要添加margin的view
    private WeakList<View> mNeedMarginView = new WeakList<>();

    //保存的状态
    private ArrayMap<String, TagState> mStates = new ArrayMap<>();


    /**
     * 与Activity绑定，会初始化一些参数，绑定后不要在外面修改与沉浸相关的参数
     *
     * @param activity
     * @return
     */
    public static StatusBarHelper with(Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        Object helper = decorView.getTag(KET_HELPER_INSTANCE);
        if (helper == null) {
            if (overKitkat()) {
                helper = new StatusBarHelper(activity);
                ((StatusBarHelper) helper).switchTag((String) null);
            } else {
                helper = new StatusBarHelperEmpty(activity);
            }
            decorView.setTag(KET_HELPER_INSTANCE, helper);
        }
        return (StatusBarHelper) helper;
    }

    StatusBarHelper(Activity activity) {
        mActivity = activity;
        install();
    }

    private ViewGroup getDecorView() {
        return (ViewGroup) mActivity.getWindow().getDecorView();
    }

    private Window getWindow() {
        return mActivity.getWindow();
    }

    public StatusBarHelper install() {
        if (mInstalled) {
            return this;
        }

        Window window = getWindow();
        if (overLollipop()) {
            //统一由Helper创建View
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (overKitkat()) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
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
        mDarkFailedFlag = state.setDarkFailedFlag;
        return this;
    }

    /**
     * 保存当前状态
     * @param tag
     * @return
     */
    public StatusBarHelper saveTag(String tag) {
        TagState tagState = new TagState(tag);
        tagState.darkIcon = mDarkIcon;
        tagState.layoutFullScreen = mLayoutFullScreen;
        tagState.scrimAlpha = mScrimAlpha;
        tagState.setDarkFailedFlag = mDarkFailedFlag;
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
     * @param color
     * @return
     */
    public StatusBarHelper setStatusBarColor(int color) {
        mStatusBarColor = color;
        updateStatusBarColor(color, mScrimAlpha);
        return this;
    }


    /**
     * 布局是否延伸到状态栏
     *
     * @param fullScreen
     * @return
     */
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
     * 黑色遮罩透明度
     *
     * @param alpha
     * @return
     */
    public StatusBarHelper setScrimAlpha(float alpha) {
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
        ViewGroup decorView = (ViewGroup) mActivity.getWindow().getDecorView();
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
        mHelperView = new HelperView(mActivity);
        mHelperView.setLayoutParams(new ViewGroup.LayoutParams(0, 0));
        decorView.addView(mHelperView, 0);
    }


    /**
     * 设置状态栏暗色图标
     *
     * @param dark
     * @param alpha 设置失败添加遮罩的透明度
     * @return
     */
    public StatusBarHelper setDarkIconOrScrim(boolean dark, float alpha) {
        boolean success = setStatusBarDarkIcon(dark);
        if (!success && dark) {
            mDarkFailedFlag = true;
            setScrimAlpha(alpha);
        }
        return this;
    }

    public StatusBarHelper setDarkIconOrScrim(boolean dark) {
        return setDarkIconOrScrim(dark, DEFAULT_FAILED_SCRIM);
    }

    /**
     * 设置状态栏暗色图标
     */
    public boolean setStatusBarDarkIcon(boolean dark) {
        mDarkIcon = dark;

        //在设置黑色图标失败，恢复白色时去除替代的黑色遮罩
        if (!dark && mDarkFailedFlag) {
            setScrimAlpha(0f);
            mDarkFailedFlag = false;
        }

        boolean success = false;
        Window window = getWindow();
        if (OSUtil.isFlyme()) {
            success = MeizuStatusBarHelper.setStatusBarDarkIcon(mActivity, dark);
        } else if (OSUtil.isMiui()) {
            final int miuiVersion = OSUtil.getMiuiVersion();
            if (miuiVersion >= 6 && miuiVersion < 9) {
                //miui6开始支持设置状态栏颜色，miui9开始直接使用原生,无法用反射设置
                success = MiuiStatusBarHelper.setStatusBarDarkMode(mActivity, dark);
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

        private boolean setDarkFailedFlag;

        public TagState(String tag) {
            this.tag = tag;
        }
    }
}
