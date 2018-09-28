package com.sanron.lib;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

class ScrimDrawable extends LayerDrawable {

    ScrimDrawable() {
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
