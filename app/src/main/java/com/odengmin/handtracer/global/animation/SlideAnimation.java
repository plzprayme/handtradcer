package com.odengmin.handtracer.global.animation;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class SlideAnimation {
    public AnimatorSet animationSet;
    public ValueAnimator slideAnimator;

    public void slideView(View view,
                          int currentHeight,
                          int newHeight) {

        slideAnimator = ValueAnimator
                .ofInt(currentHeight, newHeight)
                .setDuration(500);

        slideAnimator.addUpdateListener(animation1 -> {
            Integer value = (Integer) animation1.getAnimatedValue();
            view.getLayoutParams().height = value.intValue();
            view.requestLayout();
        });

        /* We use an update listener which listens to each tick
         * and manually updates the height of the view  */

        /*  We use an animationSet to play the com.odengmin.handtracer.global.animation  */

        if (animationSet == null) {
            animationSet = new AnimatorSet();
        }
        animationSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animationSet.play(slideAnimator);
        animationSet.start();

    }
}
