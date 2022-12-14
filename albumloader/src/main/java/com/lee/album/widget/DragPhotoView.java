package com.lee.album.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.nfc.Tag;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;

import com.github.chrisbanes.photoview.PhotoView;


public class DragPhotoView extends PhotoView {
    private Paint mPaint;

    // downX
    private float mDownX;
    // down Y
    private float mDownY;

    private float mTranslateY;
    private float mTranslateX;
    private float mScale = 1;
    private int mWidth;
    private int mHeight;
    private float mMinScale = 0.35f;
    private int mAlpha = 255;
    private final static int MAX_TRANSLATE_Y = 500;
    private final static float DAMP_VALUE = 0.55f;

    private final static long DURATION = 300;
    private boolean canFinish = false;
    private boolean isAnimate = false;

    //is event on PhotoView
    private boolean isTouchEvent = false;
    private OnTapListener mTapListener;
    private OnExitListener mExitListener;
    private Rect clipBounds;
    private float px;
    private float py;


    public DragPhotoView(Context context) {
        this(context, null);
    }

    public DragPhotoView(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public DragPhotoView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        mPaint = new Paint();
        mPaint.setColor(Color.TRANSPARENT);


    }

    @Override
    protected void onDraw(Canvas canvas) {
//        mPaint.setAlpha(mAlpha);


        canvas.drawRect(0, 0, mWidth, mHeight, mPaint);

        canvas.translate(mTranslateX, mTranslateY);
        canvas.scale(mScale, mScale, mWidth / 2, mHeight / 2);


        super.onDraw(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mWidth = w;
        mHeight = h;
    }

    public void setOutLine(int height, int width) {

        Log.e("xxx", "height=" + height);
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRect(0, 0, width, height);
            }
        });
        setClipToOutline(true);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        //only scale == 1 can drag
        if (getScale() == 1) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    onActionDown(event);

                    //change the canFinish flag
                    canFinish = !canFinish;

                    break;
                case MotionEvent.ACTION_MOVE:

                    //in viewpager
                    if (mTranslateY == 0 && mTranslateX != 0) {

                        //???????????????????????????????????????
                        if (!isTouchEvent) {
                            mScale = 1;
                            return super.dispatchTouchEvent(event);
                        }
                    }

                    //single finger drag  down
                    if (mTranslateY >= 0 && event.getPointerCount() == 1) {
                        onActionMove(event);

                        //????????????????????? ????????????viewpager
                        if (mTranslateY != 0) {
                            isTouchEvent = true;
                        }
                        return true;
                    }


                    //?????????????????????????????????
                    if (mTranslateY >= 0 && mScale < 0.95) {
                        return true;
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    //?????????????????????????????????
                    if (event.getPointerCount() == 1) {
                        onActionUp(event);
                        isTouchEvent = false;
                        //judge finish or not
                        Log.e("mTranslateY", "mTranslateY=" + mTranslateY + "x=" + mTranslateX + "finish" + canFinish);
                        if (mTranslateX == 0 && mTranslateY == 0 && canFinish) {
                            if (mTapListener != null) {
                                mTapListener.onTap(DragPhotoView.this);
                            }
                        }
                        canFinish = false;
                    }
            }
        }

        return super.dispatchTouchEvent(event);
    }

    private void onActionUp(MotionEvent event) {

        if (mTranslateY > MAX_TRANSLATE_Y) {
            if (mExitListener != null) {
                mExitListener.onExit(this, mTranslateX, mTranslateY, mWidth, mHeight, mScale, px, py);
            } else {
                throw new RuntimeException("DragPhotoView: onExitLister can't be null ! call setOnExitListener() ");
            }
        } else {
            performAnimation();
        }
    }

    private void onActionMove(MotionEvent event) {
        float moveY = event.getY();
        float moveX = event.getX();
        mTranslateX = (moveX - mDownX) * DAMP_VALUE;
        mTranslateY = (moveY - mDownY) * DAMP_VALUE;

        //??????????????????????????????????????????
        if (mTranslateY < 0) {
            mTranslateY = 0;
        }

        float percent = mTranslateY / MAX_TRANSLATE_Y * DAMP_VALUE;
        if (mScale >= mMinScale && mScale <= 1f) {
            mScale = 1 - percent;

            mAlpha = (int) (255 * (1 - percent));
            if (mAlpha > 255) {
                mAlpha = 255;
            } else if (mAlpha < 0) {
                mAlpha = 0;
            }
        }
        if (mScale < mMinScale) {
            mScale = mMinScale;
        } else if (mScale > 1f) {
            mScale = 1;
        }


        invalidate();
        if (mExitListener != null && Math.abs(mTranslateY) > 0) {
            mExitListener.onMove(mAlpha);
        }


    }

    public void performAnimation() {
        getScaleAnimation().start();
        getTranslateXAnimation().start();
        getTranslateYAnimation().start();

        if (Math.abs(mTranslateY) > 0f) {
            getAlphaAnimation().start();
        }
    }


    public ValueAnimator getAlphaAnimation() {


        final ValueAnimator animator = ValueAnimator.ofInt(mAlpha, 255);
        animator.setDuration(DURATION);
        animator.addUpdateListener(valueAnimator -> {
            mAlpha = (int) valueAnimator.getAnimatedValue();
            if (mExitListener != null) {
                mExitListener.onMove(mAlpha);
            }

        });

        return animator;
    }

    private ValueAnimator getTranslateYAnimation() {
        final ValueAnimator animator = ValueAnimator.ofFloat(mTranslateY, 0);
        animator.setDuration(DURATION);
        animator.addUpdateListener(valueAnimator -> mTranslateY = (float) valueAnimator.getAnimatedValue());

        return animator;
    }

    private ValueAnimator getTranslateXAnimation() {
        final ValueAnimator animator = ValueAnimator.ofFloat(mTranslateX, 0);
        animator.setDuration(DURATION);
        animator.addUpdateListener(valueAnimator -> mTranslateX = (float) valueAnimator.getAnimatedValue());

        return animator;
    }


    private ValueAnimator getScaleAnimation() {
        final ValueAnimator animator = ValueAnimator.ofFloat(mScale, 1);
        animator.setDuration(DURATION);
        animator.addUpdateListener(valueAnimator -> {
            mScale = (float) valueAnimator.getAnimatedValue();
            invalidate();
        });

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                isAnimate = true;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                isAnimate = false;
                animator.removeAllListeners();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        return animator;
    }

    private void onActionDown(MotionEvent event) {
        mDownX = event.getX();
        mDownY = event.getY();
    }

    public float getMinScale() {
        return mMinScale;
    }

    public void setMinScale(float minScale) {
        mMinScale = minScale;
    }

    public void setOnTapListener(OnTapListener listener) {
        mTapListener = listener;
    }

    public void setOnExitListener(OnExitListener listener) {
        mExitListener = listener;
    }

    public interface OnTapListener {
        void onTap(DragPhotoView view);
    }

    public interface OnExitListener {
        void onExit(DragPhotoView view, float translateX, float translateY, float w, float h, float currentScale, float px, float py);

        void onMove(float alpha);
    }

    public void finishAnimationCallBack() {
        mTranslateX = -mWidth / 2 + mWidth * mScale / 2;
        mTranslateY = -mHeight / 2 + mHeight * mScale / 2;
        invalidate();
    }
}
