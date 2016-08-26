package com.missmess.calendarview;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TypeEvaluator;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import java.util.Map;

/**
 * YearView and MonthView transition animator utils
 *
 * @author wl
 * @since 2016/08/15 17:52
 */
public final class YearMonthTransformer {
    private final int STAY_DELAY_TIME = 200;
    private final int BASE_TRANSITION_ANIM_DURATION = 300;
    private final TransitRootView mRootView;
    private final View rootChild1;
    private final View rootChild2;
    private YearView mYearView;
    private MonthView mMonthView;
    private final MonthViewObserver monthViewObserver;
    private boolean mvShowMonthTitle; //original month showing status
    private boolean mvShowWeekLabel; //before anim, week showing status
    private OnTransitListener mTransiter;
    private boolean animating = false;
    private final MonthTitleClicker monthTitleClicker;

    /**
     * transition listener
     */
    public interface OnTransitListener {
        /**
         * When YearView to MonthView transit process start.
         */
        void onY2MTransitStart(int duration);
        /**
         * When YearView to MonthView transit process finished.
         */
        void onY2MTransitEnd(int duration);
        /**
         * When MonthView to YearView transit process start.
         */
        void onM2YTransitStart(int duration);
        /**
         * When MonthView to YearView transit process finished.
         */
        void onM2YTransitEnd(int duration);
    }

    public YearMonthTransformer(TransitRootView transitRootView, YearView yearView, MonthView monthView) {
        this.mRootView = transitRootView;
        this.rootChild1 = transitRootView.child1;
        this.rootChild2 = transitRootView.child2;

        monthViewObserver = new MonthViewObserver();
        monthTitleClicker = new MonthTitleClicker();

        updateYearView(yearView);
        updateMonthView(monthView);
    }

    /**
     * when your YearView changed, should call this to update
     * @param yearView new YearView
     */
    public void updateYearView(YearView yearView) {
        this.mYearView = yearView;
    }

    /**
     * when your MonthView changed, should call this to update
     * @param monthView new MonthView
     */
    public void updateMonthView(MonthView monthView) {
        this.mMonthView = monthView;
        mMonthView.setOnMonthTitleClickListener(monthTitleClicker);
    }

    /**
     * apply to show MonthView
     * @param month month
     */
    public void applyShow(int month) {
        if (rootChild1.getVisibility() != View.VISIBLE || mYearView.getVisibility() != View.VISIBLE || animating)
            return;

        // init data
        animating = true;
        mvShowMonthTitle = mMonthView.mShowMonthTitle;
        mvShowWeekLabel = mMonthView.mShowWeekLabel;
        passPropertyY2M(mYearView, mMonthView, month);

        // scroll to top
        mRootView.fullScroll(View.FOCUS_UP);
        // not handler any event again
        mRootView.setReceiveEvent(false);
        // start layout but not need to be visible
        rootChild2.setVisibility(View.VISIBLE);
        rootChild2.setAlpha(0);
        // add layout listener
        monthViewObserver.setMonth(month);
        rootChild2.getViewTreeObserver().addOnGlobalLayoutListener(monthViewObserver);
    }

    private void animShowMonth(int month) {
        MonthView child = (MonthView) mYearView.getChildAt(month - 1);
        // screen position
        int[] fromLocation = new int[2];
        child.getLocationOnScreen(fromLocation);
        int[] parentLocation = new int[2];
        mRootView.getLocationOnScreen(parentLocation);
        int[] toLocation = new int[2];
        mMonthView.getLocationOnScreen(toLocation);
        // label height of MonthView
        int labelHeight = mMonthView.MONTH_HEADER_HEIGHT + mMonthView.WEEK_LABEL_HEIGHT;
        int padding = mMonthView.getPaddingLeft();

        // calculate original position
        int oriL = fromLocation[0] - parentLocation[0];
        int oriT = fromLocation[1] - parentLocation[1];
        // calculate final position
        int finL = toLocation[0] - parentLocation[0] + mMonthView.getPaddingLeft();
        int finT = toLocation[1] - parentLocation[1] + labelHeight;

        MonthView transitView = mRootView.useTransitView();
        passPropertyY2M(mYearView, transitView, month);
        // 1-7
        ObjectAnimator propertyAnim = createMonthPropertyAnimator(child, mMonthView, transitView);
        // 8 LayoutParams
        FrameLayout.LayoutParams oriLps = new FrameLayout.LayoutParams(child.getWidth(), child.getHeight());
        oriLps.setMargins(oriL, oriT, 0, 0);
        FrameLayout.LayoutParams finLps = new FrameLayout.LayoutParams(mMonthView.getWidth() - 2 * padding, mMonthView.getHeight() - labelHeight);
        finLps.setMargins(finL, finT, 0, 0);
        ViewGroup.LayoutParams rubbish = new ViewGroup.LayoutParams(0, 0);
        ObjectAnimator positionAnim = ObjectAnimator.ofObject(transitView, "layoutParams", new LpsEvaluator(oriLps, finLps), rubbish, rubbish);

        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(propertyAnim, positionAnim);
        int transitDuration = obtainTransitAnimDuration(Math.abs(finT - oriT), child.getHeight());
        animSet.setDuration(transitDuration);
        animSet.setInterpolator(new DecelerateInterpolator(2.5f));
        animSet.addListener(new DelayAnimListener(STAY_DELAY_TIME) {
            @Override
            public void onStart(Animator animator) {
                // transit prepare
                alphaView(mYearView, STAY_DELAY_TIME, false);
                if(mTransiter != null) {
                    mTransiter.onY2MTransitStart(STAY_DELAY_TIME);
                }
            }

            @Override
            public void onContinue(Animator animator) {
                rootChild1.setVisibility(View.GONE);
            }

            @Override
            public void onEnd(Animator animator) {
                mRootView.setReceiveEvent(true);
                mRootView.recycleTransitView();
                rootChild2.setAlpha(1);
                animating = false;
                if (mTransiter != null) {
                    mTransiter.onY2MTransitEnd(STAY_DELAY_TIME);
                }
                animShowLabel();
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
            mMonthView.showMonthTitle(true);
            int sMonthOffset = -mMonthView.MONTH_HEADER_HEIGHT;
            // delay start should initial its position
            mMonthView.setMonthLabelOffset(sMonthOffset);
            // 1
            monthAnim = ObjectAnimator.ofInt(mMonthView, "monthLabelOffset", sMonthOffset, 0);
            monthAnim.setDuration(200);
            monthAnim.setStartDelay(mvShowWeekLabel ? 100 : 0);
        }
        if (mvShowWeekLabel) {
            mMonthView.showWeekLabel(true);
            int sWeekOffset = -2 * mMonthView.WEEK_LABEL_HEIGHT;
            // 2
            weekAnim = ObjectAnimator.ofInt(mMonthView, "weekLabelOffset", sWeekOffset, 0);
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
            int sMonthOffset = -mMonthView.MONTH_HEADER_HEIGHT;
            // 1
            monthAnim = ObjectAnimator.ofInt(mMonthView, "monthLabelOffset", 0, sMonthOffset);
            monthAnim.setDuration(200);
        }
        if (mvShowWeekLabel) {
            int sWeekOffset = -mMonthView.MONTH_HEADER_HEIGHT - mMonthView.WEEK_LABEL_HEIGHT;
            // 2
            weekAnim = ObjectAnimator.ofInt(mMonthView, "weekLabelOffset", 0, sWeekOffset);
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
                if(mTransiter != null) {
                    mTransiter.onM2YTransitStart(STAY_DELAY_TIME);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // recovery MonthView label layout
                mMonthView.setMonthLabelOffset(0);
                mMonthView.setWeekLabelOffset(0);
                rootChild2.setVisibility(View.GONE);
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
        MonthView child = (MonthView) mYearView.getChildAt(mMonthView.getCurrentMonth() - 1);
        // screen position
        int[] fromLocation = new int[2];
        mMonthView.getLocationOnScreen(fromLocation);
        int[] toLocation = new int[2];
        child.getLocationOnScreen(toLocation);
        int[] parentLocation = new int[2];
        mRootView.getLocationOnScreen(parentLocation);
        // label height of MonthView
        int labelHeight = mMonthView.MONTH_HEADER_HEIGHT + mMonthView.WEEK_LABEL_HEIGHT;
        int padding = mMonthView.getPaddingLeft();

        // calculate original position
        int oriL = fromLocation[0] - parentLocation[0] + padding;
        int oriT = fromLocation[1] - parentLocation[1] + labelHeight;
        // calculate final position
        int finL = toLocation[0] - parentLocation[0];
        int finT = toLocation[1] - parentLocation[1];

        MonthView transitView = mRootView.useTransitView();
        passPropertyM2M(mMonthView, transitView);

        // 1-7
        ObjectAnimator animators = createMonthPropertyAnimator(mMonthView, child, transitView);
        // 8 LayoutParams
        FrameLayout.LayoutParams oriLps = new FrameLayout.LayoutParams(mMonthView.getWidth() - 2 * padding, mMonthView.getHeight() - labelHeight);
        oriLps.setMargins(oriL, oriT, 0, 0);
        FrameLayout.LayoutParams finLps = new FrameLayout.LayoutParams(child.getWidth(), child.getHeight());
        finLps.setMargins(finL, finT, 0, 0);
        ViewGroup.LayoutParams rubbish = new ViewGroup.LayoutParams(0, 0);
        ObjectAnimator positionAnim = ObjectAnimator.ofObject(transitView, "layoutParams", new LpsEvaluator(oriLps, finLps), rubbish, rubbish);

        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(animators, positionAnim);
        int transitDuration = obtainTransitAnimDuration(Math.abs(finT - oriT), child.getHeight());
        animSet.setDuration(transitDuration);
        animSet.setInterpolator(new DecelerateInterpolator());
        animSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                rootChild1.setAlpha(1);
                alphaView(mYearView, STAY_DELAY_TIME, true);
                if(mTransiter != null) {
                    mTransiter.onM2YTransitEnd(STAY_DELAY_TIME);
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRootView.setReceiveEvent(true);
                        mRootView.recycleTransitView();

                        animating = false;
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

    public void alphaView(View view, int duration, boolean show) {
        float start = show ? 0 : 1;
        float end = show ? 1 : 0;
        AlphaAnimation alphaA = new AlphaAnimation(start, end);
        alphaA.setDuration(duration);
        view.startAnimation(alphaA);
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
        if (rootChild2.getVisibility() != View.VISIBLE || mMonthView.getVisibility() != View.VISIBLE || animating) {
            // not necessary to hide
            return false;
        }

        // init data
        animating = true;
        mvShowMonthTitle = mMonthView.mShowMonthTitle;
        mvShowWeekLabel = mMonthView.mShowWeekLabel;
        passPropertyM2Y(mMonthView, mYearView);

        // scroll to top
        mRootView.fullScroll(View.FOCUS_UP);
        // not handler click event again
        mRootView.setReceiveEvent(false);
        // necessary to be visible
        rootChild1.setVisibility(View.VISIBLE);
        rootChild1.setAlpha(0);
        // clear selection
        mMonthView.clearSelection();
        // add layout listener
        mMonthView.getViewTreeObserver().addOnGlobalLayoutListener(monthViewObserver);
        return true;
    }

    // pass property of YearView to MonthView
    private void passPropertyY2M(YearView yearView, MonthView monthView, int month) {
        monthView.setToday(yearView.today);
        monthView.setYearAndMonth(yearView.getYear(), month);
        Map<Integer, Integer> map = yearView.getMonthDecors(month);
        if (map != null) {
            monthView.decorColors.clear();
            monthView.decorColors.putAll(map);
        }
    }

    // pass property of YearView to MonthView
    private void passPropertyM2M(MonthView start, MonthView end) {
        end.setToday(start.today);
        end.setYearAndMonth(start.getCurrentYear(), start.getCurrentMonth());
        end.decorColors.clear();
        end.decorColors.putAll(start.decorColors);
    }

    // pass property of MonthView to YearView
    private void passPropertyM2Y(MonthView monthView, YearView yearView) {
        yearView.setYear(monthView.getCurrentYear());
    }

    private ObjectAnimator createMonthPropertyAnimator(MonthView start, MonthView end, final MonthView target) {
        // animators
        ArgbEvaluator argbEvaluator = new ArgbEvaluator();
        // 7 properties
        int property1 = start.normalDayTextColor;
        PropertyValuesHolder pvh1 = PropertyValuesHolder.ofObject("normalDayTextColor", argbEvaluator, property1, end.normalDayTextColor);
        int property2 = start.normalDayTextSize;
        PropertyValuesHolder pvh2 = PropertyValuesHolder.ofInt("normalDayTextSize", property2, end.normalDayTextSize);
        int property3 = start.dayCircleRadius;
        PropertyValuesHolder pvh3 = PropertyValuesHolder.ofInt("dayCircleRadius", property3, end.dayCircleRadius);
        int property4 = start.dayRowHeight;
        PropertyValuesHolder pvh4 = PropertyValuesHolder.ofInt("dayRowHeight", property4, end.dayRowHeight);
        int property5 = start.circleTextColor;
        PropertyValuesHolder pvh5 = PropertyValuesHolder.ofObject("circleTextColor", argbEvaluator, property5, end.circleTextColor);
        int property6 = start.todayCircleBgColor;
        PropertyValuesHolder pvh6 = PropertyValuesHolder.ofObject("todayCircleBgColor", argbEvaluator, property6, end.todayCircleBgColor);
        int property7 = start.todayTextColor;
        PropertyValuesHolder pvh7 = PropertyValuesHolder.ofObject("todayTextColor", argbEvaluator, property7, end.todayTextColor);
        return ObjectAnimator.ofPropertyValuesHolder(target, pvh1, pvh2, pvh3, pvh4, pvh5, pvh6, pvh7);
    }

    public void setOnTransitListener(OnTransitListener listener) {
        mTransiter = listener;
    }

    class MonthViewObserver implements ViewTreeObserver.OnGlobalLayoutListener {
        private int month;

        public void setMonth(int month) {
            this.month = month;
        }

        @Override
        public void onGlobalLayout() {
            if (rootChild2.getAlpha() == 0) {
                animShowMonth(month);
            } else {
                animHideLabel();
            }
            rootChild2.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }
    }

    class MonthTitleClicker implements MonthView.OnMonthTitleClickListener {
        @Override
        public void onMonthClick(MonthView monthView, CalendarMonth calendarMonth) {
            applyHide();
        }
    }
    
    class LpsEvaluator implements TypeEvaluator<ViewGroup.LayoutParams> {
        private FrameLayout.LayoutParams start;
        private FrameLayout.LayoutParams end;
        private FrameLayout.LayoutParams lps;

        public LpsEvaluator(FrameLayout.LayoutParams start, FrameLayout.LayoutParams end) {
            this.start = start;
            this.end = end;
            lps = new FrameLayout.LayoutParams(0, 0);
        }

        @Override
        public ViewGroup.LayoutParams evaluate(float t, ViewGroup.LayoutParams startValue, ViewGroup.LayoutParams endValue) {
            float width = (float)start.width + ((float)end.width - (float)start.width) * t;
            float height = (float)start.height + ((float)end.height - (float)start.height) * t;
            float leftMargin = (float)start.leftMargin + ((float)end.leftMargin - (float)start.leftMargin) * t;
            float topMargin = (float)start.topMargin + ((float)end.topMargin - (float)start.topMargin) * t;

//            FrameLayout.LayoutParams newLps = new FrameLayout.LayoutParams((int)width, (int)height);
//            newLps.setMargins((int)leftMargin, (int)topMargin, 0, 0);
            lps.width = (int) width;
            lps.height = (int) height;
            lps.leftMargin = (int) leftMargin;
            lps.topMargin = (int) topMargin;
            return lps;
        }
    }
}
