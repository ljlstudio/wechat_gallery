package com.lee.album.widget

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.OverScroller
import android.widget.RelativeLayout
import androidx.core.view.ViewCompat

class ZoomLayout : RelativeLayout {
    private val TAG = "ZoomLayout"
    private val DEFAULT_MIN_ZOOM = 1.0f
    private val DEFAULT_MAX_ZOOM = 4.0f
    private val DEFAULT_DOUBLE_CLICK_ZOOM = 2.0f

    private var mDoubleClickZoom = 0f
    private var mMinZoom = 0f
    private var mMaxZoom = 0f
    private var mCurrentZoom = 1f
    private var mMinimumVelocity = 0
    private var mMaximumVelocity = 0
    private var mScrollBegin // 是否已经开始滑动
            = false

    private var mScaleDetector: ScaleGestureDetector? = null
    private var mGestureDetector: GestureDetector? = null
    private var mOverScroller: OverScroller? = null
    private var mScaleHelper: ScaleHelper? = null
    private var mAccelerateInterpolator: AccelerateInterpolator? = null
    private var mDecelerateInterpolator: DecelerateInterpolator? = null
    private var mZoomLayoutGestureListener: ZoomLayoutGestureListener? = null
    private var mLastChildHeight = 0
    private var mLastChildWidth = 0
    private var mLastHeight = 0
    private var mLastWidth = 0
    private var mLastCenterX = 0
    private var mLastCenterY = 0
    private var mNeedReScale = false
    private var doubleFiling = false
    private var mZoomScaleListener: ZoomLayoutScaleListener? = null


    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    fun getmCurrentZoom(): Float {
        return mCurrentZoom
    }


    private fun init(context: Context, attrs: AttributeSet?) {
        mScaleDetector = ScaleGestureDetector(context, mSimpleOnScaleGestureListener)
        mGestureDetector = GestureDetector(context, mSimpleOnGestureListener)
        mOverScroller = OverScroller(getContext())
        mScaleHelper = ScaleHelper()
        val configuration = ViewConfiguration.get(getContext())
        mMinimumVelocity = configuration.scaledMinimumFlingVelocity
        mMaximumVelocity = configuration.scaledMaximumFlingVelocity
        setWillNotDraw(false)
        mMinZoom = DEFAULT_MIN_ZOOM
        mMaxZoom = DEFAULT_MAX_ZOOM
        mDoubleClickZoom = DEFAULT_DOUBLE_CLICK_ZOOM
//        if (attrs != null) {
//            TypedArray array = null;
//            try {
//                array = context.obtainStyledAttributes(attrs, R.styleable.ZoomLayout);
//                mMinZoom = array.getFloat(R.styleable.ZoomLayout_min_zoom, DEFAULT_MIN_ZOOM);
//                mMaxZoom = array.getFloat(R.styleable.ZoomLayout_max_zoom, DEFAULT_MAX_ZOOM);
//                mDoubleClickZoom = array.getFloat(R.styleable.ZoomLayout_double_click_zoom, DEFAULT_DOUBLE_CLICK_ZOOM);
//                if (mDoubleClickZoom > mMaxZoom) {
//                    mDoubleClickZoom = mMaxZoom;
//                }
//            } catch (Exception e) {
//
//            } finally {
//                if (array != null) {
//                    array.recycle();
//                }
//            }
//        }
    }

    private val mSimpleOnScaleGestureListener: ScaleGestureDetector.SimpleOnScaleGestureListener =
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                if (!isEnabled) {
                    return false
                }
                var newScale: Float
                newScale = mCurrentZoom * detector.scaleFactor
                if (newScale > mMaxZoom) {
                    newScale = mMaxZoom
                } else if (newScale < mMinZoom) {
                    newScale = mMinZoom
                }
                if (mZoomScaleListener != null) {
                    mZoomScaleListener!!.onScale(newScale)
                }
                setScale(newScale, detector.focusX.toInt(), detector.focusY.toInt())
                return true
            }

            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                if (mZoomLayoutGestureListener != null) {
                    mZoomLayoutGestureListener!!.onScaleGestureBegin()
                }
                return true
            }

            override fun onScaleEnd(detector: ScaleGestureDetector) {}
        }

    private val mSimpleOnGestureListener: GestureDetector.SimpleOnGestureListener =
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                if (!mOverScroller!!.isFinished) {
                    mOverScroller!!.abortAnimation()
                }
                return true
            }

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                if (mZoomScaleListener != null) {
                    mZoomScaleListener!!.singleUp()
                }
                return false
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
//            float newScale;
//            if (mCurrentZoom < 1) {
//                newScale = 1;
//            } else if (mCurrentZoom < mDoubleClickZoom) {
//                newScale = mDoubleClickZoom;
//            } else {
//                newScale = 1;
//            }
//            smoothScale(newScale, (int) e.getX(), (int) e.getY());
//            if (mZoomLayoutGestureListener != null) {
//                mZoomLayoutGestureListener.onDoubleTap();
//            }
                return true
            }

            override fun onScroll(
                e1: MotionEvent,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                if (!isEnabled) {
                    return false
                }
                if (!doubleFiling) {
                    return false
                }
                if (!mScrollBegin) {
                    mScrollBegin = true
                    if (mZoomLayoutGestureListener != null) {
                        mZoomLayoutGestureListener!!.onScrollBegin()
                    }
                }
                if (mZoomScaleListener != null) {
                    mZoomScaleListener!!.onMove()
                }
                processScroll(
                    distanceX.toInt(),
                    distanceY.toInt(),
                    getScrollRangeX(),
                    getScrollRangeY()
                )
                return true
            }

            /**
             *
             * @param velocityX 滑动的速度 = 滑动的距离(滑动的起点 - 滑动的终点) / 滑动的时长，所以向上滑是负的，向下滑是正的
             * @param velocityY 同上
             * @return
             */
            override fun onFling(
                e1: MotionEvent,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (!isEnabled) {
                    return false
                }
                fling((-velocityX).toInt(), (-velocityY).toInt())
                return true
            }
        }


    private fun fling(velocityX: Int, velocityY: Int): Boolean {
        var velocityX = velocityX
        var velocityY = velocityY
        if (Math.abs(velocityX) < mMinimumVelocity) {
            velocityX = 0
        }
        if (Math.abs(velocityY) < mMinimumVelocity) {
            velocityY = 0
        }
        val scrollY = scrollY
        val scrollX = scrollX
        // 只有在能够滚动的时候，才需要处理 Fling
        val canFlingX = scrollX > 0 && scrollX < getScrollRangeX()
        val canFlingY = scrollY > 0 && scrollY < getScrollRangeY()
        val canFling = canFlingY || canFlingX
        if (canFling) {
            // 下面两行代码的作用是将 Fling 速度限制在  [-mMaximumVelocity, mMaximumVelocity] 之间
            velocityX = Math.max(-mMaximumVelocity, Math.min(velocityX, mMaximumVelocity))
            velocityY = Math.max(-mMaximumVelocity, Math.min(velocityY, mMaximumVelocity))
            val height = height - paddingBottom - paddingTop
            val width = width - paddingRight - paddingLeft
            val bottom = getContentHeight()
            val right = getContentWidth()
            // getScrollX(), getScrollY() 是 fling 开始的位置
            // velocityX, velocityY 滚动速度
            // 0, Math.max(0, right - width), 0, Math.max(0, bottom - height)。是滚动的范围
            // 0, 0 是可以往外滚动的距离，这里不支持往外滚动，直接传 0
            mOverScroller!!.fling(
                getScrollX(), getScrollY(), velocityX, velocityY, 0, Math.max(0, right - width), 0,
                Math.max(0, bottom - height), 0, 0
            )
            notifyInvalidate()
            return true
        }
        return false
    }

    fun smoothScale(newScale: Float, centerX: Int, centerY: Int) {
        if (mCurrentZoom > newScale) {
            if (mAccelerateInterpolator == null) {
                mAccelerateInterpolator = AccelerateInterpolator()
            }
            mScaleHelper?.startScale(
                mCurrentZoom,
                newScale,
                centerX,
                centerY,
                mAccelerateInterpolator
            )
        } else {
            if (mDecelerateInterpolator == null) {
                mDecelerateInterpolator = DecelerateInterpolator()
            }
            mScaleHelper?.startScale(
                mCurrentZoom,
                newScale,
                centerX,
                centerY,
                mDecelerateInterpolator
            )
        }
        notifyInvalidate()
    }

    private fun notifyInvalidate() {
        // 效果和 invalidate 一样，但是会使得动画更平滑
        ViewCompat.postInvalidateOnAnimation(this)
    }


    fun setScale(scale: Float?, centerX: Int?, centerY: Int?) {
        mLastCenterX = centerX!!
        mLastCenterY = centerY!!
        val preScale = mCurrentZoom
        mCurrentZoom = scale!!
        val sX = scrollX
        val sY = scrollY
        val dx = ((sX + centerX) * (scale / preScale - 1)).toInt()
        val dy = ((sY + centerY) * (scale / preScale - 1)).toInt()
        if (getScrollRangeX() < 0) {
            child().pivotX = (child().width / 2).toFloat()
            child().translationX = 0f
        } else {
            child().pivotX = 0f
            val willTranslateX = -child().left
            child().translationX = willTranslateX.toFloat()
        }
        if (getScrollRangeY() < 0) {
            child().pivotY = (child().height / 2).toFloat()
            child().translationY = 0f
        } else {
            val willTranslateY = -child().top
            child().translationY = willTranslateY.toFloat()
            child().pivotY = 0f
        }
        child().scaleX = mCurrentZoom
        child().scaleY = mCurrentZoom
        processScroll(dx, dy, getScrollRangeX(), getScrollRangeY())
        notifyInvalidate()
    }


    private fun processScroll(
        deltaX: Int, deltaY: Int,
        scrollRangeX: Int, scrollRangeY: Int
    ) {
        if (!doubleFiling) {
            return
        }
        val oldScrollX = scrollX
        val oldScrollY = scrollY
        var newScrollX = oldScrollX + deltaX
        var newScrollY = oldScrollY + deltaY
        val left = 0
        val right = scrollRangeX
        val top = 0
        val bottom = scrollRangeY
        if (newScrollX > right) {
            newScrollX = right
        } else if (newScrollX < left) {
            newScrollX = left
        }
        if (newScrollY > bottom) {
            newScrollY = bottom
        } else if (newScrollY < top) {
            newScrollY = top
        }
        if (newScrollX < 0) {
            newScrollX = 0
        }
        if (newScrollY < 0) {
            newScrollY = 0
        }
        scrollTo(newScrollX, newScrollY)
    }


    private fun getScrollRangeX(): Int {
        val contentWidth = width - paddingRight - paddingLeft
        return getContentWidth() - contentWidth
    }

    fun getContentWidth(): Int {
        return (child().width * mCurrentZoom).toInt()
    }

    private fun getScrollRangeY(): Int {
        val contentHeight = height - paddingBottom - paddingTop
        return getContentHeight() - contentHeight
    }

    fun getContentHeight(): Int {
        return (child().height * mCurrentZoom).toInt()
    }

    private fun child(): View {
        return getChildAt(0)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (mNeedReScale) {
            // 需要重新刷新，因为宽高已经发生变化
            setScale(mCurrentZoom, mLastCenterX, mLastCenterY)
            mNeedReScale = false
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        child().isClickable = true
        if (child().height < height || child().width < width) {
            gravity = Gravity.CENTER
        } else {
            gravity = Gravity.TOP
        }
        if (mLastChildWidth != child().width || mLastChildHeight != child().height || mLastWidth != width || mLastHeight != height) {
            // 宽高变化后，记录需要重新刷新，放在下次 onLayout 处理，避免 View 的一些配置：比如 getTop() 没有初始化好
            // 下次放在 onLayout 处理的原因是 setGravity 会在 onLayout 确定完位置，这时候去 setScale 导致位置的变化就不会导致用户看到
            // 闪一下的问题
            mNeedReScale = true
        }
        mLastChildWidth = child().width
        mLastChildHeight = child().height
        mLastWidth = child().width
        mLastHeight = height
        if (mNeedReScale) {
            notifyInvalidate()
        }
    }

    /**
     * 通常配合 Scroller、OverScroller 实现平滑滚动。如 Fling 的时候进行平滑滚动。
     * Scroller、OverScroller 负责计算一段时间内的 ScrollX、ScrollY 的平滑变化
     * 然后调用 ViewCompat.postInvalidateOnAnimation(this); 之后就可以在
     * computeScroll() 不断去获取 ScrollX、ScrollY 的变化了，再通过 ScrollTo 设置给 View
     */
    override fun computeScroll() {
        super.computeScroll()
        mScaleHelper?.let {
            if (mScaleHelper?.computeScrollOffset() == true) {
                if (mScaleHelper?.getCurScale()!! < mMinZoom) {
                    setScale(mMinZoom, mScaleHelper?.getStartX(), mScaleHelper?.getStartY())
                } else {
                    setScale(
                        mScaleHelper?.getCurScale(),
                        mScaleHelper?.getStartX(),
                        mScaleHelper?.getStartY()
                    )
                }
            }
        }

        if (mOverScroller!!.computeScrollOffset()) {
            val oldX = scrollX
            val oldY = scrollY
            val x = mOverScroller!!.currX
            val y = mOverScroller!!.currY
            if (oldX != x || oldY != y) {
                val rangeY = getScrollRangeY()
                val rangeX = getScrollRangeX()
                processScroll(x - oldX, y - oldY, rangeX, rangeY)
            }
            if (!mOverScroller!!.isFinished) {
                notifyInvalidate()
            }
        }
    }

    private var mode = 0
    private var single = 1
    private var multi = 2


    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_UP) {
            // 最后一根手指抬起的时候，重置 mScrollBegin 为 false
            mScrollBegin = false
        }
        mGestureDetector!!.onTouchEvent(ev)
        mScaleDetector!!.onTouchEvent(ev)
        when (ev.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                mode = single
                doubleFiling = false
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                mode = multi
                doubleFiling = true
            }
            MotionEvent.ACTION_MOVE -> {

                if (mode == single) {
                    return super.dispatchTouchEvent(ev)
                } else if (mode == multi) {
                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                doubleFiling = false
                if (mZoomScaleListener != null) {
                    mZoomScaleListener!!.touchUp(mode, ev)
                }
            }
            MotionEvent.ACTION_POINTER_UP -> doubleFiling = false
            else -> {}
        }
        return if (mode == single) {
            super.dispatchTouchEvent(ev)
        } else {
            true
        }
    }


    override fun measureChildWithMargins(
        child: View, parentWidthMeasureSpec: Int, widthUsed: Int,
        parentHeightMeasureSpec: Int, heightUsed: Int
    ) {
        val lp = child.layoutParams as MarginLayoutParams
        val childWidthMeasureSpec = getChildMeasureSpec(
            parentWidthMeasureSpec,
            paddingLeft + paddingRight + lp.leftMargin + lp.rightMargin
                    + widthUsed, lp.width
        )
        val usedTotal = (paddingTop + paddingBottom + lp.topMargin + lp.bottomMargin +
                heightUsed)
        val childHeightMeasureSpec: Int
        if (lp.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                Math.max(0, MeasureSpec.getSize(parentHeightMeasureSpec) - usedTotal),
                MeasureSpec.UNSPECIFIED
            )
        } else {
            childHeightMeasureSpec = getChildMeasureSpec(
                parentHeightMeasureSpec,
                (paddingTop + paddingBottom + lp.topMargin + lp.bottomMargin
                        + heightUsed), lp.height
            )
        }
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
    }


    /**
     * 是否可以在水平方向上滚动
     * 举例: ViewPager 通过这个方法判断子 View 是否可以水平滚动，从而解决滑动冲突
     */
    override fun canScrollHorizontally(direction: Int): Boolean {
        return if (direction > 0) {
            scrollX < getScrollRangeX()
        } else {
            scrollX > 0 && getScrollRangeX() > 0
        }
    }

    /**
     * 是否可以在竖直方向上滚动
     * 举例: ViewPager 通过这个方法判断子 View 是否可以竖直滚动，从而解决滑动冲突
     */
    override fun canScrollVertically(direction: Int): Boolean {
        return if (direction > 0) {
            scrollY < getScrollRangeY()
        } else {
            scrollY > 0 && getScrollRangeY() > 0
        }
    }

    fun setZoomLayoutGestureListener(zoomLayoutGestureListener: ZoomLayoutGestureListener?) {
        mZoomLayoutGestureListener = zoomLayoutGestureListener
    }

    fun resetScale() {
        doubleFiling = true
        setScale(1f, (child().width / 2f).toInt(), (child().height / 2f).toInt())
    }

    fun setZoomLayoutScaleListener(zoomLayoutScaleListener: ZoomLayoutScaleListener?) {
        mZoomScaleListener = zoomLayoutScaleListener
    }

    interface ZoomLayoutScaleListener {
        fun onScale(scale: Float)
        fun touchUp(mode: Int, motionEvent: MotionEvent?)
        fun onMove()
        fun singleUp()
    }


    interface ZoomLayoutGestureListener {
        fun onScrollBegin()
        fun onScaleGestureBegin()
        fun onDoubleTap()
    }
}