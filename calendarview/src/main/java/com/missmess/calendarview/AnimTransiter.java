package com.missmess.calendarview;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;

/**
 * use this class to implement animations of other view excluding YearView and MonthView.
 *
 * @author wl
 * @since 2016/08/25 10:45
 */
public class AnimTransiter {
    private int duration;

    void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * alpha view from 0 to 1, or from 1 to 0.
     * @param view view
     * @param showin true-from 0 to 1, false otherwise.
     */
    public void alphaView(View view, boolean showin) {
        AlphaAnimation alphaA = createAlpha(showin);
        view.startAnimation(alphaA);
    }

    /**
     * slide in a view. alpha 0 to 1, and translate.
     * @param view view
     * @param fromTop true - translate -100% to 0, false - translate 0 to 100%.
     */
    public void slideInView(View view, boolean fromTop) {
        slideView(view, true, fromTop);
    }

    /**
     * slide out a view. alpha 1 to 0, and translate.
     * @param view view
     * @param fromTop true - translate -100% to 0, false - translate 0 to 100%.
     */
    public void slideOutView(View view, boolean fromTop) {
        slideView(view, false, fromTop);
    }

    private void slideView(View view, boolean showin, boolean fromTop) {
        AlphaAnimation alphaA = createAlpha(showin);
        TranslateAnimation transA = createTranslate(showin, fromTop);
        AnimationSet anims = new AnimationSet(false);
        anims.addAnimation(alphaA);
        anims.addAnimation(transA);
        view.startAnimation(anims);
    }

    private AlphaAnimation createAlpha(boolean showin) {
        float start = showin ? 0 : 1;
        float end = showin ? 1 : 0;
        AlphaAnimation animation = new AlphaAnimation(start, end);
        animation.setDuration(duration);
        return animation;
    }

    private TranslateAnimation createTranslate(boolean showin, boolean fromTop) {
        float start, end;
        if(showin && fromTop) {
            start = -1;
            end = 0;
        } else if(showin) {
            start = 1;
            end = 0;
        } else if(fromTop) {
            start = 0;
            end = 1;
        } else {
            start = 0;
            end = -1;
        }
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, start, Animation.RELATIVE_TO_SELF, end);
        animation.setDuration(duration);
        return animation;
    }
}
