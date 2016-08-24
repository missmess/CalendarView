package com.missmess.calendarview;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;

import java.util.Map;

/**
 * YearView and MonthView transition animator utils
 *
 * @author wl
 * @since 2016/08/15 17:52
 */
public class YearMonthTransformer {
    private final int STAY_DELAY_TIME = 200;
    private final int BASE_TRANSITION_ANIM_DURATION = 300;
    private final YearView yearView;
    private final MonthView monthView;
    private final MonthViewObserver monthViewObserver;
    private boolean mvShowMonthTitle; //original month showing status
    private boolean mvShowWeekLabel; //before anim, week showing status

    public YearMonthTransformer(YearView yearView, MonthView monthView) {
        this.yearView = yearView;
        this.monthView = monthView;

        monthViewObserver = new MonthViewObserver();
        monthView.setOnMonthTitleClickListener(new MonthTitleClicker());
    }

    public void applyShow(int month) {
        if (yearView.getVisibility() != View.VISIBLE)
            return;

        mvShowMonthTitle = monthView.mShowMonthTitle;
        mvShowWeekLabel = monthView.mShowWeekLabel;
        passProperties2Month(month);

        // start layout but not need to be visible
        monthView.setVisibility(View.VISIBLE);
        monthView.setAlpha(0);
        // not handler click event again
        yearView.setEnabled(false);
        // add layout listener
        monthViewObserver.setMonth(month);
        monthView.getViewTreeObserver().addOnGlobalLayoutListener(monthViewObserver);
    }

    private void animShowMonth(int month) {
        MonthView child = (MonthView) yearView.getChildAt(month - 1);
        // screen position
        int[] fromLocation = new int[2];
        child.getLocationOnScreen(fromLocation);
        int[] parentLocation = new int[2];
        ((ViewGroup) monthView.getParent()).getLocationOnScreen(parentLocation);
        // label height of MonthView
        int labelHeight = monthView.MONTH_HEADER_HEIGHT + monthView.WEEK_LABEL_HEIGHT;

        // calculate original position
        int posL = fromLocation[0] - parentLocation[0];
        int posT = fromLocation[1] - parentLocation[1];
        int posR = posL + child.getWidth();
        int posB = posT + child.getHeight();

        // 1-7
        ObjectAnimator animators = createMonthPropertyAnimator(child, monthView, monthView);
        // 8 left
        ObjectAnimator leftAnim = ObjectAnimator.ofInt(monthView, "left", posL - monthView.getPaddingLeft(), monthView.getLeft());
        // 9 top
        ObjectAnimator topAnim = ObjectAnimator.ofInt(monthView, "top", posT, monthView.getTop() + labelHeight);
        int transitT = Math.abs(monthView.getTop() + labelHeight - posT);
        // 10 right
        ObjectAnimator rightAnim = ObjectAnimator.ofInt(monthView, "right", posR + monthView.getPaddingLeft(), monthView.getRight());
        // 11 bottom
        ObjectAnimator bottomAnim = ObjectAnimator.ofInt(monthView, "bottom", posB, monthView.getBottom());

        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(animators, leftAnim, topAnim, rightAnim, bottomAnim);
        animSet.setDuration(obtainTransitAnimDuration(transitT, child.getHeight()));
        animSet.setInterpolator(new DecelerateInterpolator(2.5f));
        animSet.addListener(new Animator.AnimatorListener() {
            boolean canceled = false;

            @Override
            public void onAnimationStart(Animator animation) {
                yearView.setVisibility(View.INVISIBLE);
                monthView.setAlpha(1);
                // hide title, leave day number only
                if (mvShowMonthTitle)
                    monthView.showMonthTitle(false);
                if (mvShowWeekLabel)
                    monthView.showWeekLabel(false);

                if (!canceled) { // pause animation to effect as delay
                    animation.cancel();
                } else {
                    canceled = false;
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!canceled) {
                    yearView.setEnabled(true);
                    yearView.setVisibility(View.GONE);
                    animShowLabel();
                }
            }

            @Override
            public void onAnimationCancel(final Animator animation) {
                canceled = true;
                alphaYear(false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // after a interval start animator again
                        animation.start();
                    }
                }, STAY_DELAY_TIME);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animSet.start();
    }

    // show label with anim
    private void animShowLabel() {
        if (!mvShowMonthTitle && !mvShowWeekLabel)
            return;

        ObjectAnimator monthAnim = null;
        ObjectAnimator weekAnim = null;

        if (mvShowMonthTitle) {
            monthView.showMonthTitle(true);
            int sMonthOffset = -monthView.MONTH_HEADER_HEIGHT;
            // delay start should initial its position
            monthView.setMonthLabelOffset(sMonthOffset);
            // 1
            monthAnim = ObjectAnimator.ofInt(monthView, "monthLabelOffset", sMonthOffset, 0);
            monthAnim.setDuration(200);
            monthAnim.setStartDelay(mvShowWeekLabel ? 100 : 0);
        }
        if (mvShowWeekLabel) {
            monthView.showWeekLabel(true);
            int sWeekOffset = -2 * monthView.WEEK_LABEL_HEIGHT;
            // 2
            weekAnim = ObjectAnimator.ofInt(monthView, "weekLabelOffset", sWeekOffset, 0);
            weekAnim.setDuration(300);
        }
        AnimatorSet animSet = new AnimatorSet();
        animSet.setInterpolator(new DecelerateInterpolator(2f));
        if (monthAnim != null && weekAnim != null) {
            animSet.playTogether(weekAnim, monthAnim);
        } else if (monthAnim != null) {
            animSet.play(monthAnim);
        } else {
            animSet.play(weekAnim);
        }
        animSet.start();
    }

    private void animHideLabel() {
        if (!mvShowMonthTitle && !mvShowWeekLabel) {
            animHideMonth();
        }
        ObjectAnimator monthAnim = null;
        ObjectAnimator weekAnim = null;

        if (mvShowMonthTitle) {
            int sMonthOffset = -monthView.MONTH_HEADER_HEIGHT;
            // 1
            monthAnim = ObjectAnimator.ofInt(monthView, "monthLabelOffset", 0, sMonthOffset);
            monthAnim.setDuration(200);
        }
        if (mvShowWeekLabel) {
            int sWeekOffset = -monthView.MONTH_HEADER_HEIGHT - monthView.WEEK_LABEL_HEIGHT;
            // 2
            weekAnim = ObjectAnimator.ofInt(monthView, "weekLabelOffset", 0, sWeekOffset);
            weekAnim.setDuration(400);
            weekAnim.setStartDelay(mvShowMonthTitle ? 100 : 0);
        }
        AnimatorSet animSet = new AnimatorSet();
        animSet.setInterpolator(new AccelerateInterpolator(2f));
        if (monthAnim != null && weekAnim != null) {
            animSet.playTogether(weekAnim, monthAnim);
        } else if (monthAnim != null) {
            animSet.play(monthAnim);
        } else {
            animSet.play(weekAnim);
        }
        animSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // recovery MonthView label layout
                monthView.setMonthLabelOffset(0);
                monthView.setWeekLabelOffset(0);
                animHideMonth();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animSet.start();
    }

    private void animHideMonth() {
        MonthView child = (MonthView) yearView.getChildAt(monthView.getCurrentMonth() - 1);
        // screen position
        int[] toLocation = new int[2];
        child.getLocationOnScreen(toLocation);
        int[] parentLocation = new int[2];
        ((ViewGroup) monthView.getParent()).getLocationOnScreen(parentLocation);
        // label height of MonthView
        int labelHeight = monthView.MONTH_HEADER_HEIGHT + monthView.WEEK_LABEL_HEIGHT;

        // calculate final position
        int posL = toLocation[0] - parentLocation[0];
        int posT = toLocation[1] - parentLocation[1];
        int posR = posL + child.getWidth();
        int posB = posT + child.getHeight();

        // 1-7
        ObjectAnimator animators = createMonthPropertyAnimator(monthView, child, monthView);
        // 8 left
        ObjectAnimator leftAnim = ObjectAnimator.ofInt(monthView, "left", monthView.getLeft(), posL - monthView.getPaddingLeft());
        // 9 top
        ObjectAnimator topAnim = ObjectAnimator.ofInt(monthView, "top", monthView.getTop() + labelHeight, posT);
        int transitT = Math.abs(monthView.getTop() + labelHeight - posT);
        // 10 right
        ObjectAnimator rightAnim = ObjectAnimator.ofInt(monthView, "right", monthView.getRight(), posR + monthView.getPaddingLeft());
        // 11 bottom
        ObjectAnimator bottomAnim = ObjectAnimator.ofInt(monthView, "bottom", monthView.getBottom(), posB);

        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(animators, leftAnim, topAnim, rightAnim, bottomAnim);
        animSet.setDuration(obtainTransitAnimDuration(transitT, child.getHeight()));
        animSet.setInterpolator(new DecelerateInterpolator());
        animSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                monthView.showMonthTitle(false);
                monthView.showWeekLabel(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                alphaYear(true);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        monthView.setVisibility(View.GONE);
                        monthView.showMonthTitle(true);
                        monthView.showWeekLabel(true);
                        yearView.setEnabled(true);
                    }
                }, STAY_DELAY_TIME);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animSet.start();
    }

    private void alphaYear(boolean show) {
        yearView.setAlpha(1);
        float start = show ? 0 : 1;
        float end = show ? 1 : 0;
        AlphaAnimation alphaA = new AlphaAnimation(start, end);
        alphaA.setDuration(STAY_DELAY_TIME);
        yearView.startAnimation(alphaA);
    }

    private int obtainTransitAnimDuration(int transitT, int childHeight) {
        float factor = (float) transitT / (float) childHeight / 2f;
        if(factor < 1)
            factor = 1;
        if(factor > 1.4f)
            factor = 1.4f;
        return (int) (factor * BASE_TRANSITION_ANIM_DURATION);
    }

    /**
     * apply hide OP to MonthView
     *
     * @return false - not necessary to hide; true - attempt to hide
     */
    public boolean applyHide() {
        if (monthView.getVisibility() != View.VISIBLE || yearView.getVisibility() == View.VISIBLE) {
            // not necessary to hide
            return false;
        }

        mvShowMonthTitle = monthView.mShowMonthTitle;
        mvShowWeekLabel = monthView.mShowWeekLabel;
        passProperties2Year();

        yearView.setVisibility(View.VISIBLE);
        yearView.setAlpha(0);
        // not handler click event again
        yearView.setEnabled(false);
        // clear selection
        monthView.clearSelection();
        // add layout listener
        monthView.getViewTreeObserver().addOnGlobalLayoutListener(monthViewObserver);
        return true;
    }

    // pass property of YearView to MonthView
    private void passProperties2Month(int month) {
        monthView.setToday(yearView.today);
        monthView.setYearAndMonth(yearView.getYear(), month);
        Map<Integer, Integer> map = yearView.getMonthDecors(month);
        if (map != null) {
            for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                monthView.decorateDay(entry.getKey(), entry.getValue());
            }
        }
    }

    // pass property of MonthView to YearView
    private void passProperties2Year() {
        yearView.setYear(monthView.getCurrentYear());
    }

    private ObjectAnimator createMonthPropertyAnimator(MonthView start, MonthView end, final MonthView target) {
        // animators
        ArgbEvaluator argbEvaluator = new ArgbEvaluator();
        // 7 properties
        final int property1 = start.normalDayTextColor;
        PropertyValuesHolder pvh1 = PropertyValuesHolder.ofObject("normalDayTextColor", argbEvaluator, property1, end.normalDayTextColor);
        final int property2 = start.normalDayTextSize;
        PropertyValuesHolder pvh2 = PropertyValuesHolder.ofInt("normalDayTextSize", property2, end.normalDayTextSize);
        final int property3 = start.dayCircleRadius;
        PropertyValuesHolder pvh3 = PropertyValuesHolder.ofInt("dayCircleRadius", property3, end.dayCircleRadius);
        final int property4 = start.dayRowHeight;
        PropertyValuesHolder pvh4 = PropertyValuesHolder.ofInt("dayRowHeight", property4, end.dayRowHeight);
        final int property5 = start.circleTextColor;
        PropertyValuesHolder pvh5 = PropertyValuesHolder.ofObject("circleTextColor", argbEvaluator, property5, end.circleTextColor);
        final int property6 = start.todayCircleBgColor;
        PropertyValuesHolder pvh6 = PropertyValuesHolder.ofObject("todayCircleBgColor", argbEvaluator, property6, end.todayCircleBgColor);
        final int property7 = start.todayTextColor;
        PropertyValuesHolder pvh7 = PropertyValuesHolder.ofObject("todayTextColor", argbEvaluator, property7, end.todayTextColor);
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(target, pvh1, pvh2, pvh3, pvh4, pvh5, pvh6, pvh7);
        if (target != end) {
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //recovery properties
                            target.setNormalDayTextColor(property1);
                            target.setNormalDayTextSize(property2);
                            target.setDayCircleRadius(property3);
                            target.setDayRowHeight(property4);
                            target.setCircleTextColor(property5);
                            target.setTodayCircleBgColor(property6);
                            target.setTodayTextColor(property7);
                        }
                    }, STAY_DELAY_TIME);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }
        return animator;
    }

    class MonthViewObserver implements ViewTreeObserver.OnGlobalLayoutListener {
        private int month;

        public void setMonth(int month) {
            this.month = month;
        }

        @Override
        public void onGlobalLayout() {
            if (monthView.getAlpha() == 0) {
                animShowMonth(month);
            } else {
                animHideLabel();
            }
            monthView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }
    }

    class MonthTitleClicker implements MonthView.OnMonthTitleClickListener {

        @Override
        public void onMonthClick(MonthView monthView, CalendarMonth calendarMonth) {
            applyHide();
        }
    }
}
