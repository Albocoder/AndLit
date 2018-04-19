package com.nilesh.lockbutton.utils;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

public class AnimUtils {
    public static void scaleViewAnim(View v, float fromX, float toX, float fromY, float toY) {
        Animation anim = new ScaleAnimation(
                fromX, toX, fromY, toY,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 1f);
        anim.setFillAfter(true);
        v.startAnimation(anim);
    }
}
