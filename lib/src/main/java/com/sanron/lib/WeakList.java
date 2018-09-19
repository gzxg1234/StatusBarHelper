package com.sanron.lib;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Author:sanron
 * Time:2018/9/19
 * Description:
 */
public class WeakList<T> {

    private List<WeakReference<T>> mList;


    WeakList() {
        mList = new ArrayList<>();
    }

    public void add(T t) {
        WeakReference<T> ref = new WeakReference<>(t);
        mList.add(ref);
    }

    public List<T> getLive() {
        List<T> lives = new ArrayList<>();
        Iterator<WeakReference<T>> it = mList.iterator();
        while (it.hasNext()) {
            WeakReference<T> ref = it.next();
            T t = ref.get();
            if (t != null) {
                lives.add(t);
            } else {
                it.remove();
            }
        }
        return lives;
    }

    public boolean contains(T item) {
        Iterator<WeakReference<T>> it = mList.iterator();
        while (it.hasNext()) {
            WeakReference<T> ref = it.next();
            T t = ref.get();
            if (t == null) {
                it.remove();
            }
            if (t == item) {
                return true;
            }
        }
        return false;
    }

    public void remove(T item) {
        Iterator<WeakReference<T>> it = mList.iterator();
        while (it.hasNext()) {
            WeakReference<T> ref = it.next();
            T t = ref.get();
            if (t == null) {
                it.remove();
            }
            if (t == item) {
                it.remove();
                break;
            }
        }
    }
}
