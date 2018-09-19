package com.sanron.lib;

import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Author:sanron
 * Time:2018/9/18
 * Description:
 */
public class SetFitSystem {

    public static void install(final Window window) {
        final Window.Callback originCallback = window.getCallback();
        Window.Callback callback = (Window.Callback) Proxy.newProxyInstance(window.getClass().getClassLoader(),
                new Class[]{Window.Callback.class}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        //监听content布局变化，在content中插入一个FrameLayout消费SystemInset
                        if (method.getName().equals("onContentChanged")) {
                            ViewGroup contentView = (ViewGroup) window.getDecorView().findViewById(android.R.id.content);
                            if (contentView != null) {
                                FrameLayout contentWrap = new FrameLayout(contentView.getContext());
                                ViewCompat.setFitsSystemWindows(contentWrap, true);
                                for (int i = 0; i < contentView.getChildCount(); i++) {
                                    View child = contentView.getChildAt(i);
                                    contentView.removeView(child);
                                    contentWrap.addView(child);
                                }
                                contentView.addView(contentWrap, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT));
                            }
                        }
                        return method.invoke(originCallback, args);
                    }
                });
        window.setCallback(callback);
        callback.onContentChanged();
    }
}
