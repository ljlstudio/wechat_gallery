package com.lee.album.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;

import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.MotionEventCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewGroupCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.customview.widget.ViewDragHelper;

import java.util.List;

public class VerticalDrawerLayout extends ViewGroup {
    private static final String TAG = "VerticalDrawerLayout";

    /**
     * Indicates that any drawers are in an idle, settled state. No animation is
     * in progress.
     */
    public static final int STATE_IDLE = ViewDragHelper.STATE_IDLE;

    /**
     * Indicates that a drawer is currently being dragged by the user.
     */
    public static final int STATE_DRAGGING = ViewDragHelper.STATE_DRAGGING;

    /**
     * Indicates that a drawer is in the process of settling to a final
     * position.
     */
    public static final int STATE_SETTLING = ViewDragHelper.STATE_SETTLING;

    /**
     * The drawer is unlocked.
     */
    public static final int LOCK_MODE_UNLOCKED = 0;

    /**
     * The drawer is locked closed. The user may not open it, though
     * the app may open it programmatically.
     */
    public static final int LOCK_MODE_LOCKED_CLOSED = 1;

    /**
     * The drawer is locked open. The user may not close it, though the app
     * may close it programmatically.
     */
    public static final int LOCK_MODE_LOCKED_OPEN = 2;

    private static final int MIN_DRAWER_MARGIN = 1; // dp

    private static final int DEFAULT_SCRIM_COLOR = 0x99000000;

    /**
     * Length of time to delay before peeking the drawer.
     */
    private static final int PEEK_DELAY = 160; // ms

    /**
     * Minimum velocity that will be detected as a fling
     */
    private static final int MIN_FLING_VELOCITY = 400; // dips per second

    /**
     * Experimental feature.
     */
    private static final boolean ALLOW_EDGE_LOCK = false;

    private static final boolean CHILDREN_DISALLOW_INTERCEPT = true;

    private static final float TOUCH_SLOP_SENSITIVITY = 1.f;

    private static final int[] LAYOUT_ATTRS = new int[] {
            android.R.attr.layout_gravity
    };

    private final ChildAccessibilityDelegate mChildAccessibilityDelegate =
            new ChildAccessibilityDelegate();

    private int mMinDrawerMargin;

    private int mScrimColor = DEFAULT_SCRIM_COLOR;
    private float mScrimOpacity;
    private Paint mScrimPaint = new Paint();

    private final ViewDragHelper mTopDragger;
    private final ViewDragHelper mBottomDragger;
    private final ViewDragCallback mTopCallback;
    private final ViewDragCallback mBottomCallback;
    private int mDrawerState;
    private boolean mInLayout;
    private boolean mFirstLayout = true;
    private int mLockModeTop;
    private int mLockModeBottom;
    private boolean mDisallowInterceptRequested;
    private boolean mChildrenCanceledTouch;

    private VerticalDrawerListener mListener;

    private float mInitialMotionX;
    private float mInitialMotionY;

    private Drawable mShadowTop;
    private Drawable mShadowBottom;

    private CharSequence mTitleTop;
    private CharSequence mTitleBottom;

    /**
     * Listener for monitoring events about drawers.
     */
    public interface VerticalDrawerListener {
        /**
         * Called when a drawer's position changes.
         *
         * @param drawerView
         *            The child view that was moved
         * @param slideOffset
         *            The new offset of this drawer within its range, from 0-1
         */
        public void onVerticalDrawerSlide(View drawerView, float slideOffset);

        /**
         * Called when a drawer has settled in a completely open state.
         * The drawer is interactive at this point.
         *
         * @param drawerView
         *            Drawer view that is now open
         */
        public void onVerticalDrawerOpened(View drawerView);

        /**
         * Called when a drawer has settled in a completely closed state.
         *
         * @param drawerView
         *            Drawer view that is now closed
         */
        public void onVerticalDrawerClosed(View drawerView);

        /**
         * Called when the drawer motion state changes. The new state will
         * be one of {@link #STATE_IDLE}, {@link #STATE_DRAGGING} or
         * {@link #STATE_SETTLING}.
         *
         * @param newState
         *            The new drawer motion state
         */
        public void onVerticalDrawerStateChanged(int newState);
    }

    /**
     * Stub/no-op implementations of all methods of {@link }.
     * Override this if you only care about a few of the available callback
     * methods.
     */
    public static abstract class SimpleDrawerListener implements VerticalDrawerListener {
        @Override
        public void onVerticalDrawerSlide(View drawerView, float slideOffset) {
        }

        @Override
        public void onVerticalDrawerOpened(View drawerView) {
        }

        @Override
        public void onVerticalDrawerClosed(View drawerView) {
        }

        @Override
        public void onVerticalDrawerStateChanged(int newState) {
        }
    }

    public VerticalDrawerLayout(Context context) {
        this(context, null);
    }

    public VerticalDrawerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        final float density = getResources().getDisplayMetrics().density;
        mMinDrawerMargin = (int) (MIN_DRAWER_MARGIN * density + 0.5f);
        final float minVel = MIN_FLING_VELOCITY * density;

        mTopCallback = new ViewDragCallback(Gravity.TOP);
        mBottomCallback = new ViewDragCallback(Gravity.BOTTOM);

        mTopDragger = ViewDragHelper.create(this, TOUCH_SLOP_SENSITIVITY, mTopCallback);
        mTopDragger.setEdgeTrackingEnabled(ViewDragHelper.EDGE_TOP);
        mTopDragger.setMinVelocity(minVel);
        mTopCallback.setDragger(mTopDragger);

        mBottomDragger = ViewDragHelper.create(this, TOUCH_SLOP_SENSITIVITY, mBottomCallback);
        mBottomDragger.setEdgeTrackingEnabled(ViewDragHelper.EDGE_BOTTOM);
        mBottomDragger.setMinVelocity(minVel);
        mBottomCallback.setDragger(mBottomDragger);

        // So that we can catch the back button
        setFocusableInTouchMode(true);

        ViewCompat.setImportantForAccessibility(this,
                ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);

        ViewCompat.setAccessibilityDelegate(this, new AccessibilityDelegate());
        ViewGroupCompat.setMotionEventSplittingEnabled(this, false);
    }

    /**
     * Set a simple drawable used for the left or right shadow.
     * The drawable provided must have a nonzero intrinsic width.
     *
     * @param shadowDrawable
     *            Shadow drawable to use at the edge of a drawer
     * @param gravity
     *            Which drawer the shadow should apply to
     */
    public void setDrawerShadow(Drawable shadowDrawable, int gravity) {
        /*
         * TODO Someone someday might want to set more complex drawables here.
         * They're probably nuts, but we might want to consider registering
         * callbacks,
         * setting states, etc. properly.
         */

        final int absGravity = GravityCompat.getAbsoluteGravity(gravity,
                ViewCompat.getLayoutDirection(this));
        if ((absGravity & Gravity.TOP) == Gravity.TOP) {
            mShadowTop = shadowDrawable;
            invalidate();
        }
        if ((absGravity & Gravity.BOTTOM) == Gravity.BOTTOM) {
            mShadowBottom = shadowDrawable;
            invalidate();
        }
    }

    /**
     * Set a simple drawable used for the left or right shadow.
     * The drawable provided must have a nonzero intrinsic width.
     *
     * @param resId
     *            Resource id of a shadow drawable to use at the edge of a
     *            drawer
     * @param gravity
     *            Which drawer the shadow should apply to
     */
    public void setDrawerShadow(int resId, int gravity) {
        setDrawerShadow(getResources().getDrawable(resId), gravity);
    }

    /**
     * Set a color to use for the scrim that obscures primary content while a
     * drawer is open.
     *
     * @param color
     *            Color to use in 0xAARRGGBB format.
     */
    public void setScrimColor(int color) {
        mScrimColor = color;
        invalidate();
    }

    /**
     * Set a listener to be notified of drawer events.
     *
     * @param listener
     *            Listener to notify when drawer events occur
     * @see
     */
    public void setDrawerListener(VerticalDrawerListener listener) {
        mListener = listener;
    }

    /**
     * Enable or disable interaction with all drawers.
     *
     * <p>
     * This allows the application to restrict the user's ability to open or
     * close any drawer within this layout. DrawerLayout will still respond to
     * calls to {@link #openDrawer(int)}, {@link #closeDrawer(int)} and friends
     * if a drawer is locked.
     * </p>
     *
     * <p>
     * Locking drawers open or closed will implicitly open or close any drawers
     * as appropriate.
     * </p>
     *
     * @param lockMode
     *            The new lock mode for the given drawer. One of
     *            {@link #LOCK_MODE_UNLOCKED}, {@link #LOCK_MODE_LOCKED_CLOSED}
     *            or {@link #LOCK_MODE_LOCKED_OPEN}.
     */
    public void setDrawerLockMode(int lockMode) {
        setDrawerLockMode(lockMode, Gravity.TOP);
        setDrawerLockMode(lockMode, Gravity.BOTTOM);
    }

    /**
     * Enable or disable interaction with the given drawer.
     *
     * <p>
     * This allows the application to restrict the user's ability to open or
     * close the given drawer. DrawerLayout will still respond to calls to
     * {@link #openDrawer(int)}, {@link #closeDrawer(int)} and friends if a
     * drawer is locked.
     * </p>
     *
     * <p>
     * Locking a drawer open or closed will implicitly open or close that drawer
     * as appropriate.
     * </p>
     *
     * @param lockMode
     *            The new lock mode for the given drawer. One of
     *            {@link #LOCK_MODE_UNLOCKED}, {@link #LOCK_MODE_LOCKED_CLOSED}
     *            or {@link #LOCK_MODE_LOCKED_OPEN}.
     * @param edgeGravity
     *            Gravity.LEFT, RIGHT, START or END.
     *            Expresses which drawer to change the mode for.
     *
     * @see #LOCK_MODE_UNLOCKED
     * @see #LOCK_MODE_LOCKED_CLOSED
     * @see #LOCK_MODE_LOCKED_OPEN
     */
    public void setDrawerLockMode(int lockMode, int edgeGravity) {
        final int absGravity = GravityCompat.getAbsoluteGravity(edgeGravity,
                ViewCompat.getLayoutDirection(this));
        if (absGravity == Gravity.TOP) {
            mLockModeTop = lockMode;
        } else if (absGravity == Gravity.BOTTOM) {
            mLockModeBottom = lockMode;
        }
        if (lockMode != LOCK_MODE_UNLOCKED) {
            // Cancel interaction in progress
            final ViewDragHelper helper = absGravity == Gravity.TOP ? mTopDragger : mBottomDragger;
            helper.cancel();
        }
        switch (lockMode) {
            case LOCK_MODE_LOCKED_OPEN:
                final View toOpen = findDrawerWithGravity(absGravity);
                if (toOpen != null) {
                    openDrawer(toOpen);
                }
                break;
            case LOCK_MODE_LOCKED_CLOSED:
                final View toClose = findDrawerWithGravity(absGravity);
                if (toClose != null) {
                    closeDrawer(toClose);
                }
                break;
            // default: do nothing
        }
    }

    /**
     * Enable or disable interaction with the given drawer.
     *
     * <p>
     * This allows the application to restrict the user's ability to open or
     * close the given drawer. DrawerLayout will still respond to calls to
     * {@link #openDrawer(int)}, {@link #closeDrawer(int)} and friends if a
     * drawer is locked.
     * </p>
     *
     * <p>
     * Locking a drawer open or closed will implicitly open or close that drawer
     * as appropriate.
     * </p>
     *
     * @param lockMode
     *            The new lock mode for the given drawer. One of
     *            {@link #LOCK_MODE_UNLOCKED}, {@link #LOCK_MODE_LOCKED_CLOSED}
     *            or {@link #LOCK_MODE_LOCKED_OPEN}.
     * @param drawerView
     *            The drawer view to change the lock mode for
     *
     * @see #LOCK_MODE_UNLOCKED
     * @see #LOCK_MODE_LOCKED_CLOSED
     * @see #LOCK_MODE_LOCKED_OPEN
     */
    public void setDrawerLockMode(int lockMode, View drawerView) {
        if (!isDrawerView(drawerView)) {
//            throw new IllegalArgumentException("View " + drawerView + " is not a " +
//                    "drawer with appropriate layout_gravity");
        }
        final int gravity = ((LayoutParams) drawerView.getLayoutParams()).gravity;
        setDrawerLockMode(lockMode, gravity);
    }

    /**
     * Check the lock mode of the drawer with the given gravity.
     *
     * @param edgeGravity
     *            Gravity of the drawer to check
     * @return one of {@link #LOCK_MODE_UNLOCKED},
     *         {@link #LOCK_MODE_LOCKED_CLOSED} or
     *         {@link #LOCK_MODE_LOCKED_OPEN}.
     */
    public int getDrawerLockMode(int edgeGravity) {
        final int absGravity = GravityCompat.getAbsoluteGravity(
                edgeGravity, ViewCompat.getLayoutDirection(this));
        if (absGravity == Gravity.TOP) {
            return mLockModeTop;
        } else if (absGravity == Gravity.BOTTOM) {
            return mLockModeBottom;
        }
        return LOCK_MODE_UNLOCKED;
    }

    /**
     * Check the lock mode of the given drawer view.
     *
     * @param drawerView
     *            Drawer view to check lock mode
     * @return one of {@link #LOCK_MODE_UNLOCKED},
     *         {@link #LOCK_MODE_LOCKED_CLOSED} or
     *         {@link #LOCK_MODE_LOCKED_OPEN}.
     */
    public int getDrawerLockMode(View drawerView) {
        final int absGravity = getDrawerViewAbsoluteGravity(drawerView);
        if (absGravity == Gravity.TOP) {
            return mLockModeTop;
        } else if (absGravity == Gravity.BOTTOM) {
            return mLockModeBottom;
        }
        return LOCK_MODE_UNLOCKED;
    }

    /**
     * Sets the title of the drawer with the given gravity.
     * <p>
     * When accessibility is turned on, this is the title that will be used to
     * identify the drawer to the active accessibility service.
     *
     * @param edgeGravity
     *            Gravity.LEFT, RIGHT, START or END. Expresses which
     *            drawer to set the title for.
     * @param title
     *            The title for the drawer.
     */
    public void setDrawerTitle(int edgeGravity, CharSequence title) {
        final int absGravity = GravityCompat.getAbsoluteGravity(
                edgeGravity, ViewCompat.getLayoutDirection(this));
        if (absGravity == Gravity.TOP) {
            mTitleTop = title;
        } else if (absGravity == Gravity.BOTTOM) {
            mTitleBottom = title;
        }
    }

    /**
     * Returns the title of the drawer with the given gravity.
     *
     * @param edgeGravity
     *            Gravity.LEFT, RIGHT, START or END. Expresses which
     *            drawer to return the title for.
     * @return The title of the drawer, or null if none set.
     * @see #setDrawerTitle(int, CharSequence)
     */
    public CharSequence getDrawerTitle(int edgeGravity) {
        final int absGravity = GravityCompat.getAbsoluteGravity(
                edgeGravity, ViewCompat.getLayoutDirection(this));
        if (absGravity == Gravity.TOP) {
            return mTitleTop;
        } else if (absGravity == Gravity.BOTTOM) {
            return mTitleBottom;
        }
        return null;
    }

    /**
     * Resolve the shared state of all drawers from the component
     * ViewDragHelpers.
     * Should be called whenever a ViewDragHelper's state changes.
     */
    void updateDrawerState(int forGravity, int activeState, View activeDrawer) {
        final int topState = mTopDragger.getViewDragState();
        final int bottomState = mBottomDragger.getViewDragState();

        final int state;
        if (topState == STATE_DRAGGING || bottomState == STATE_DRAGGING) {
            state = STATE_DRAGGING;
        } else if (topState == STATE_SETTLING || bottomState == STATE_SETTLING) {
            state = STATE_SETTLING;
        } else {
            state = STATE_IDLE;
        }

        if (activeDrawer != null && activeState == STATE_IDLE) {
            final LayoutParams lp = (LayoutParams) activeDrawer.getLayoutParams();
            if (lp.onScreen == 0) {
                dispatchOnDrawerClosed(activeDrawer);
            } else if (lp.onScreen == 1) {
                dispatchOnDrawerOpened(activeDrawer);
            }
        }

        if (state != mDrawerState) {
            mDrawerState = state;

            if (mListener != null) {
                mListener.onVerticalDrawerStateChanged(state);
            }
        }
    }

    void dispatchOnDrawerClosed(View drawerView) {
        final LayoutParams lp = (LayoutParams) drawerView.getLayoutParams();
        if (lp.knownOpen) {
            lp.knownOpen = false;
            if (mListener != null) {
                mListener.onVerticalDrawerClosed(drawerView);
            }

            // If no drawer is opened, all drawers are not shown
            // for accessibility and the content is shown.
            View content = getChildAt(0);
            if (content != null) {
                ViewCompat.setImportantForAccessibility(content,
                        ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
            }
            ViewCompat.setImportantForAccessibility(drawerView,
                    ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);

            // Only send WINDOW_STATE_CHANGE if the host has window focus. This
            // may change if support for multiple foreground windows (e.g. IME)
            // improves.
            if (hasWindowFocus()) {
                final View rootView = getRootView();
                if (rootView != null) {
                    rootView.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
                }
            }
        }
    }

    void dispatchOnDrawerOpened(View drawerView) {
        final LayoutParams lp = (LayoutParams) drawerView.getLayoutParams();
        if (!lp.knownOpen) {
            lp.knownOpen = true;
            if (mListener != null) {
                mListener.onVerticalDrawerOpened(drawerView);
            }

            // If a drawer is opened, only it is shown for
            // accessibility and the content is not shown.
            View content = getChildAt(0);
            if (content != null) {
                ViewCompat.setImportantForAccessibility(content,
                        ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
            }
            ViewCompat.setImportantForAccessibility(drawerView,
                    ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);

            sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
            drawerView.requestFocus();
        }
    }

    void dispatchOnDrawerSlide(View drawerView, float slideOffset) {
        if (mListener != null) {
            mListener.onVerticalDrawerSlide(drawerView, slideOffset);
        }
    }

    void setDrawerViewOffset(View drawerView, float slideOffset) {
        final LayoutParams lp = (LayoutParams) drawerView.getLayoutParams();
        if (slideOffset == lp.onScreen) {
            return;
        }

        lp.onScreen = slideOffset;
        dispatchOnDrawerSlide(drawerView, slideOffset);
    }

    float getDrawerViewOffset(View drawerView) {
        return ((LayoutParams) drawerView.getLayoutParams()).onScreen;
    }

    /**
     * @return the absolute gravity of the child drawerView, resolved according
     *         to the current layout direction
     */
    int getDrawerViewAbsoluteGravity(View drawerView) {
        final int gravity = ((LayoutParams) drawerView.getLayoutParams()).gravity;
        return GravityCompat.getAbsoluteGravity(gravity, ViewCompat.getLayoutDirection(this));
    }

    boolean checkDrawerViewAbsoluteGravity(View drawerView, int checkFor) {
        final int absGravity = getDrawerViewAbsoluteGravity(drawerView);
        return (absGravity & checkFor) == checkFor;
    }

    View findOpenDrawer() {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (((LayoutParams) child.getLayoutParams()).knownOpen) {
                return child;
            }
        }
        return null;
    }

    /**
     * @param gravity
     *            the gravity of the child to return. If specified as a
     *            relative value, it will be resolved according to the current
     *            layout direction.
     * @return the drawer with the specified gravity
     */
    View findDrawerWithGravity(int gravity) {
        final int absHorizGravity = GravityCompat.getAbsoluteGravity(
                gravity, ViewCompat.getLayoutDirection(this)) & Gravity.VERTICAL_GRAVITY_MASK;
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            final int childAbsGravity = getDrawerViewAbsoluteGravity(child);
            if ((childAbsGravity & Gravity.VERTICAL_GRAVITY_MASK) == absHorizGravity) {
                return child;
            }
        }
        return null;
    }

    /**
     * Simple gravity to string - only supports LEFT and RIGHT for debugging
     * output.
     *
     * @param gravity
     *            Absolute gravity value
     * @return LEFT or RIGHT as appropriate, or a hex string
     */
    static String gravityToString(int gravity) {
        if ((gravity & Gravity.TOP) == Gravity.TOP) {
            return "TOP";
        }
        if ((gravity & Gravity.BOTTOM) == Gravity.BOTTOM) {
            return "BOTTOM";
        }
        return Integer.toHexString(gravity);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mFirstLayout = true;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mFirstLayout = true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY) {
            if (isInEditMode()) {
                // Don't crash the layout editor. Consume all of the space if
                // specified
                // or pick a magic number from thin air otherwise.
                // TODO Better communication with tools of this bogus state.
                // It will crash on a real device.
                if (widthMode == MeasureSpec.AT_MOST) {
                    widthMode = MeasureSpec.EXACTLY;
                } else if (widthMode == MeasureSpec.UNSPECIFIED) {
                    widthMode = MeasureSpec.EXACTLY;
                    widthSize = 300;
                }
                if (heightMode == MeasureSpec.AT_MOST) {
                    heightMode = MeasureSpec.EXACTLY;
                }
                else if (heightMode == MeasureSpec.UNSPECIFIED) {
                    heightMode = MeasureSpec.EXACTLY;
                    heightSize = 300;
                }
            } else {
//                throw new IllegalArgumentException(
//                        "DrawerLayout must be measured with MeasureSpec.EXACTLY.");
            }
        }

        setMeasuredDimension(widthSize, heightSize);

        // Gravity value for each drawer we've seen. Only one of each permitted.
        int foundDrawers = 0;
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            if (isContentView(child)) {
                // Content views get measured at exactly the layout's size.
                final int contentWidthSpec = MeasureSpec.makeMeasureSpec(
                        widthSize - lp.leftMargin - lp.rightMargin, MeasureSpec.EXACTLY);
                final int contentHeightSpec = MeasureSpec.makeMeasureSpec(
                        heightSize - lp.topMargin - lp.bottomMargin, MeasureSpec.EXACTLY);
                child.measure(contentWidthSpec, contentHeightSpec);
            } else if (isDrawerView(child)) {
                final int childGravity = getDrawerViewAbsoluteGravity(child) & Gravity.VERTICAL_GRAVITY_MASK;
                if ((foundDrawers & childGravity) != 0) {
//                    throw new IllegalStateException("Child drawer has absolute gravity " +
//                            gravityToString(childGravity) + " but this " + TAG + " already has a " +
//                            "drawer view along that edge");
                }
                final int drawerWidthSpec = getChildMeasureSpec(widthMeasureSpec,
                        lp.leftMargin + lp.rightMargin,
                        lp.width);
                final int drawerHeightSpec = getChildMeasureSpec(heightMeasureSpec,
                        mMinDrawerMargin + lp.topMargin + lp.bottomMargin,
                        lp.height);
                child.measure(drawerWidthSpec, drawerHeightSpec);
            } else {
//                throw new IllegalStateException("Child " + child + " at index " + i +
//                        " does not have a valid layout_gravity - must be Gravity.LEFT, " +
//                        "Gravity.RIGHT or Gravity.NO_GRAVITY");
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mInLayout = true;
        final int height = b - t;
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            if (isContentView(child)) {
                child.layout(lp.leftMargin, lp.topMargin,
                        lp.leftMargin + child.getMeasuredWidth(),
                        lp.topMargin + child.getMeasuredHeight());
            } else { // Drawer, if it wasn't onMeasure would have thrown an
                // exception.
                final int childWidth = child.getMeasuredWidth();
                final int childHeight = child.getMeasuredHeight();
                int childTop;

                final float newOffset;
                if (checkDrawerViewAbsoluteGravity(child, Gravity.TOP)) {
                    childTop = -childHeight + (int) (childHeight * lp.onScreen);
                    newOffset = (float) (childHeight + childTop) / childHeight;
                } else { // Right; onMeasure checked for us.
                    childTop = height - (int) (childHeight * lp.onScreen);
                    newOffset = (float) (height - childTop) / childHeight;
                }

                final boolean changeOffset = newOffset != lp.onScreen;

                final int vgrav = lp.gravity & Gravity.VERTICAL_GRAVITY_MASK;

                switch (vgrav) {
                    default:
                    case Gravity.LEFT: {
                        child.layout(lp.leftMargin, childTop, lp.leftMargin + childWidth, childTop + childHeight);
                        break;
                    }

                    case Gravity.RIGHT: {
                        final int width = r - l;
                        child.layout(width - lp.rightMargin - childWidth, childTop, width - lp.rightMargin, childTop
                                + childHeight);
                        break;
                    }

                    case Gravity.CENTER_HORIZONTAL: {
                        final int width = r - l;
                        int childLeft = (width - childWidth) / 2;

                        // Offset for margins. If things don't fit right because of
                        // bad measurement before, oh well.
                        if (childLeft < lp.leftMargin) {
                            childLeft = lp.leftMargin;
                        } else if (childLeft + childWidth > width - lp.rightMargin) {
                            childLeft = width - lp.rightMargin - childWidth;
                        }
                        child.layout(childLeft, childTop, childLeft + childWidth,
                                childTop + childHeight);
                        break;
                    }
                }

                if (changeOffset) {
                    setDrawerViewOffset(child, newOffset);
                }

                final int newVisibility = lp.onScreen > 0 ? VISIBLE : INVISIBLE;
                if (child.getVisibility() != newVisibility) {
                    child.setVisibility(newVisibility);
                }
            }
        }
        mInLayout = false;
        mFirstLayout = false;
    }

    @Override
    public void requestLayout() {
        if (!mInLayout) {
            super.requestLayout();
        }
    }

    @Override
    public void computeScroll() {
        final int childCount = getChildCount();
        float scrimOpacity = 0;
        for (int i = 0; i < childCount; i++) {
            final float onscreen = ((LayoutParams) getChildAt(i).getLayoutParams()).onScreen;
            scrimOpacity = Math.max(scrimOpacity, onscreen);
        }
        mScrimOpacity = scrimOpacity;

        // "|" used on purpose; both need to run.
        if (mTopDragger.continueSettling(true) | mBottomDragger.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private static boolean hasOpaqueBackground(View v) {
        final Drawable bg = v.getBackground();
        if (bg != null) {
            return bg.getOpacity() == PixelFormat.OPAQUE;
        }
        return false;
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        final int height = getHeight();
        final boolean drawingContent = isContentView(child);
        int clipLeft = 0, clipRight = getWidth();

        final int restoreCount = canvas.save();
        if (drawingContent) {
            final int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View v = getChildAt(i);
                if (v == child || v.getVisibility() != VISIBLE ||
                        !hasOpaqueBackground(v) || !isDrawerView(v) ||
                        v.getHeight() < height) {
                    continue;
                }

                if (checkDrawerViewAbsoluteGravity(v, Gravity.TOP)) {
                    final int vright = v.getRight();
                    if (vright > clipLeft)
                        clipLeft = vright;
                } else {
                    final int vleft = v.getLeft();
                    if (vleft < clipRight)
                        clipRight = vleft;
                }
            }
            canvas.clipRect(clipLeft, 0, clipRight, getHeight());
        }
        final boolean result = super.drawChild(canvas, child, drawingTime);
        canvas.restoreToCount(restoreCount);

        if (mScrimOpacity > 0 && drawingContent) {
            final int baseAlpha = (mScrimColor & 0xff000000) >>> 24;
            final int imag = (int) (baseAlpha * mScrimOpacity);
            final int color = imag << 24 | (mScrimColor & 0xffffff);
            mScrimPaint.setColor(color);

            canvas.drawRect(clipLeft, 0, clipRight, getHeight(), mScrimPaint);
        } else if (mShadowTop != null && checkDrawerViewAbsoluteGravity(child, Gravity.TOP)) {
            final int shadowWidth = mShadowTop.getIntrinsicWidth();
            final int childRight = child.getRight();
            final int drawerPeekDistance = mTopDragger.getEdgeSize();
            final float alpha =
                    Math.max(0, Math.min((float) childRight / drawerPeekDistance, 1.f));
            mShadowTop.setBounds(childRight, child.getTop(),
                    childRight + shadowWidth, child.getBottom());
            mShadowTop.setAlpha((int) (0xff * alpha));
            mShadowTop.draw(canvas);
        } else if (mShadowBottom != null && checkDrawerViewAbsoluteGravity(child, Gravity.BOTTOM)) {
            final int shadowWidth = mShadowBottom.getIntrinsicWidth();
            final int childLeft = child.getLeft();
            final int showing = getWidth() - childLeft;
            final int drawerPeekDistance = mBottomDragger.getEdgeSize();
            final float alpha =
                    Math.max(0, Math.min((float) showing / drawerPeekDistance, 1.f));
            mShadowBottom.setBounds(childLeft - shadowWidth, child.getTop(),
                    childLeft, child.getBottom());
            mShadowBottom.setAlpha((int) (0xff * alpha));
            mShadowBottom.draw(canvas);
        }
        return result;
    }

    boolean isContentView(View child) {
        return child!=null&&child.getLayoutParams()!=null&&((LayoutParams) child.getLayoutParams()).gravity == Gravity.NO_GRAVITY;
    }

    boolean isDrawerView(View child) {
        final int gravity = ((LayoutParams) child.getLayoutParams()).gravity;
        final int absGravity = GravityCompat.getAbsoluteGravity(gravity,
                ViewCompat.getLayoutDirection(child));
        return (absGravity & (Gravity.TOP | Gravity.BOTTOM)) != 0;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);

        // "|" used deliberately here; both methods should be invoked.
        final boolean interceptForDrag = mTopDragger.shouldInterceptTouchEvent(ev) |
                mBottomDragger.shouldInterceptTouchEvent(ev);

        boolean interceptForTap = false;

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();
                mInitialMotionX = x;
                mInitialMotionY = y;
                if (mScrimOpacity > 0 &&
                        isContentView(mTopDragger.findTopChildUnder((int) x, (int) y))) {
                    interceptForTap = true;
                }
                mDisallowInterceptRequested = false;
                mChildrenCanceledTouch = false;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                // If we cross the touch slop, don't perform the delayed peek for an
                // edge touch.
                if (mTopDragger.checkTouchSlop(ViewDragHelper.DIRECTION_ALL)) {
                    mTopCallback.removeCallbacks();
                    mBottomCallback.removeCallbacks();
                }
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                closeDrawers(true);
                mDisallowInterceptRequested = false;
                mChildrenCanceledTouch = false;
            }
        }

        return interceptForDrag || interceptForTap || hasPeekingDrawer() || mChildrenCanceledTouch;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mTopDragger.processTouchEvent(ev);
        mBottomDragger.processTouchEvent(ev);

        final int action = ev.getAction();
        boolean wantTouchEvents = true;


        switch (action & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();
                mInitialMotionX = x;
                mInitialMotionY = y;
                mDisallowInterceptRequested = false;
                mChildrenCanceledTouch = false;
                break;
            }

            case MotionEvent.ACTION_UP: {
                final float x = ev.getX();
                final float y = ev.getY();
                boolean peekingOnly = true;
                final View touchedView = mTopDragger.findTopChildUnder((int) x, (int) y);
                if (touchedView != null && isContentView(touchedView)) {
                    final float dx = x - mInitialMotionX;
                    final float dy = y - mInitialMotionY;
                    final int slop = mTopDragger.getTouchSlop();
                    if (dx * dx + dy * dy < slop * slop) {
                        // Taps close a dimmed open drawer but only if it isn't
                        // locked open.
                        final View openDrawer = findOpenDrawer();
                        if (openDrawer != null) {
                            peekingOnly = getDrawerLockMode(openDrawer) == LOCK_MODE_LOCKED_OPEN;
                        }
                    }
                }
                closeDrawers(peekingOnly);
                mDisallowInterceptRequested = false;
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                closeDrawers(true);
                mDisallowInterceptRequested = false;
                mChildrenCanceledTouch = false;
                break;
            }
        }

        return true;
    }

    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (CHILDREN_DISALLOW_INTERCEPT ||
                (!mTopDragger.isEdgeTouched(ViewDragHelper.EDGE_TOP) &&
                        !mBottomDragger.isEdgeTouched(ViewDragHelper.EDGE_BOTTOM))) {
            // If we have an edge touch we want to skip this and track it for
            // later instead.
            super.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
        mDisallowInterceptRequested = disallowIntercept;
        if (disallowIntercept) {
            closeDrawers(true);
        }
    }

    /**
     * Close all currently open drawer views by animating them out of view.
     */
    public void closeDrawers() {
        closeDrawers(false);
    }

    void closeDrawers(boolean peekingOnly) {
        boolean needsInvalidate = false;
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            if (!isDrawerView(child) || (peekingOnly && !lp.isPeeking)) {
                continue;
            }

            final int childHeight = child.getHeight();

            if (checkDrawerViewAbsoluteGravity(child, Gravity.TOP)) {
                needsInvalidate |= mTopDragger.smoothSlideViewTo(child, child.getLeft(), -childHeight);
            } else {
                needsInvalidate |= mBottomDragger.smoothSlideViewTo(child, child.getLeft(), getHeight());
            }

            lp.isPeeking = false;
        }

        mTopCallback.removeCallbacks();
        mBottomCallback.removeCallbacks();

        if (needsInvalidate) {
            invalidate();
        }
    }

    /**
     * Open the specified drawer view by animating it into view.
     *
     * @param drawerView
     *            Drawer view to open
     */
    public void openDrawer(View drawerView) {
        if (!isDrawerView(drawerView)) {
//            throw new IllegalArgumentException("View " + drawerView + " is not a sliding drawer");
        }

        if (mFirstLayout) {
            final LayoutParams lp = (LayoutParams) drawerView.getLayoutParams();
            lp.onScreen = 1.f;
            lp.knownOpen = true;
        } else {
            if (checkDrawerViewAbsoluteGravity(drawerView, Gravity.TOP)) {
                mTopDragger.smoothSlideViewTo(drawerView, drawerView.getLeft(), 0);
            } else {
                mBottomDragger
                        .smoothSlideViewTo(drawerView, drawerView.getLeft(), getHeight() - drawerView.getHeight());
            }
        }
        invalidate();
    }

    /**
     * Open the specified drawer by animating it out of view.
     *
     * @param gravity
     *            Gravity.TOP to move the top drawer or Gravity.BOTTOM for the
     *            bottom.
     *            GravityCompat.START or GravityCompat.END may also be used.
     */
    public void openDrawer(int gravity) {
        final View drawerView = findDrawerWithGravity(gravity);
        if (drawerView == null) {
//            throw new IllegalArgumentException("No drawer view found with gravity " +
//                    gravityToString(gravity));
        }
        openDrawer(drawerView);
    }

    /**
     * Close the specified drawer view by animating it into view.
     *
     * @param drawerView
     *            Drawer view to close
     */
    public void closeDrawer(View drawerView) {
        if (!isDrawerView(drawerView)) {
//            throw new IllegalArgumentException("View " + drawerView + " is not a sliding drawer");
        }

        if (mFirstLayout) {
            final LayoutParams lp = (LayoutParams) drawerView.getLayoutParams();
            lp.onScreen = 0.f;
            lp.knownOpen = false;
        } else {
            if (checkDrawerViewAbsoluteGravity(drawerView, Gravity.TOP)) {
                mTopDragger.smoothSlideViewTo(drawerView, drawerView.getLeft(), -drawerView.getHeight());
            } else {
                mBottomDragger.smoothSlideViewTo(drawerView, drawerView.getLeft(), getHeight());
            }
        }
        invalidate();
    }

    /**
     * Close the specified drawer view by animating it into view. Not animation
     *
     * @param drawerView
     *            Drawer view to close
     */
    public void closeDrawerNotAnimation(View drawerView) {
        if (!isDrawerView(drawerView)) {
//            throw new IllegalArgumentException("View " + drawerView +
//                    " is not a sliding drawer");
        }

        final LayoutParams lp = (LayoutParams) drawerView.getLayoutParams();
        lp.onScreen = 0.f;
        lp.knownOpen = false;

        setDrawerViewOffset(drawerView, 0);
        drawerView.setVisibility(INVISIBLE);
        super.requestLayout();
        invalidate();
    }

    /**
     * Close the specified drawer by animating it out of view.
     *
     * @param gravity
     *            Gravity.LEFT to move the left drawer or Gravity.RIGHT for the
     *            right.
     *            GravityCompat.START or GravityCompat.END may also be used.
     */
    public void closeDrawer(int gravity) {
        final View drawerView = findDrawerWithGravity(gravity);
        if (drawerView == null) {
//            throw new IllegalArgumentException("No drawer view found with gravity " +
//                    gravityToString(gravity));
        }
        closeDrawer(drawerView);
    }

    /**
     * Check if the given drawer view is currently in an open state.
     * To be considered "open" the drawer must have settled into its fully
     * visible state. To check for partial visibility use
     * {@link #isDrawerVisible(android.view.View)}.
     *
     * @param drawer
     *            Drawer view to check
     * @return true if the given drawer view is in an open state
     * @see #isDrawerVisible(android.view.View)
     */
    public boolean isDrawerOpen(View drawer) {
        if (!isDrawerView(drawer)) {
//            throw new IllegalArgumentException("View " + drawer + " is not a drawer");
        }
        return ((LayoutParams) drawer.getLayoutParams()).knownOpen;
    }

    /**
     * Check if the given drawer view is currently in an open state.
     * To be considered "open" the drawer must have settled into its fully
     * visible state. If there is no drawer with the given gravity this method
     * will return false.
     *
     * @param drawerGravity
     *            Gravity of the drawer to check
     * @return true if the given drawer view is in an open state
     */
    public boolean isDrawerOpen(int drawerGravity) {
        final View drawerView = findDrawerWithGravity(drawerGravity);
        if (drawerView != null) {
            return isDrawerOpen(drawerView);
        }
        return false;
    }

    /**
     * Check if a given drawer view is currently visible on-screen. The drawer
     * may be only peeking onto the screen, fully extended, or anywhere
     * inbetween.
     *
     * @param drawer
     *            Drawer view to check
     * @return true if the given drawer is visible on-screen
     * @see #isDrawerOpen(android.view.View)
     */
    public boolean isDrawerVisible(View drawer) {
        if (!isDrawerView(drawer)) {
//            throw new IllegalArgumentException("View " + drawer + " is not a drawer");
        }
        return ((LayoutParams) drawer.getLayoutParams()).onScreen > 0;
    }

    /**
     * Check if a given drawer view is currently visible on-screen. The drawer
     * may be only peeking onto the screen, fully extended, or anywhere in
     * between.
     * If there is no drawer with the given gravity this method will return
     * false.
     *
     * @param drawerGravity
     *            Gravity of the drawer to check
     * @return true if the given drawer is visible on-screen
     */
    public boolean isDrawerVisible(int drawerGravity) {
        final View drawerView = findDrawerWithGravity(drawerGravity);
        if (drawerView != null) {
            return isDrawerVisible(drawerView);
        }
        return false;
    }

    private boolean hasPeekingDrawer() {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final LayoutParams lp = (LayoutParams) getChildAt(i).getLayoutParams();
            if (lp.isPeeking) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams
                ? new LayoutParams((LayoutParams) p)
                : p instanceof ViewGroup.MarginLayoutParams
                ? new LayoutParams((MarginLayoutParams) p)
                : new LayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams && super.checkLayoutParams(p);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    private boolean hasVisibleDrawer() {
        return findVisibleDrawer() != null;
    }

    private View findVisibleDrawer() {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (isDrawerView(child) && isDrawerVisible(child)) {
                return child;
            }
        }
        return null;
    }

    void cancelChildViewTouch() {
        // Cancel child touches
        if (!mChildrenCanceledTouch) {
            final long now = SystemClock.uptimeMillis();
            final MotionEvent cancelEvent = MotionEvent.obtain(now, now,
                    MotionEvent.ACTION_CANCEL, 0.0f, 0.0f, 0);
            final int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                getChildAt(i).dispatchTouchEvent(cancelEvent);
            }
            cancelEvent.recycle();
            mChildrenCanceledTouch = true;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && hasVisibleDrawer()) {
            event.startTracking();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            final View visibleDrawer = findVisibleDrawer();
            if (visibleDrawer != null && getDrawerLockMode(visibleDrawer) == LOCK_MODE_UNLOCKED) {
                closeDrawers();
            }
            return visibleDrawer != null;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        final SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        if (ss.openDrawerGravity != Gravity.NO_GRAVITY) {
            final View toOpen = findDrawerWithGravity(ss.openDrawerGravity);
            if (toOpen != null) {
                openDrawer(toOpen);
            }
        }

        setDrawerLockMode(ss.lockModeTop, Gravity.TOP);
        setDrawerLockMode(ss.lockModeBottom, Gravity.BOTTOM);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();

        final SavedState ss = new SavedState(superState);

        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (!isDrawerView(child)) {
                continue;
            }

            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (lp.knownOpen) {
                ss.openDrawerGravity = lp.gravity;
                // Only one drawer can be open at a time.
                break;
            }
        }

        ss.lockModeTop = mLockModeTop;
        ss.lockModeBottom = mLockModeBottom;

        return ss;
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        // Until a drawer is open, it is hidden from accessibility.
        if (index > 0 || (index < 0 && getChildCount() > 0)) {
            ViewCompat.setImportantForAccessibility(child,
                    ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
            // Also set a delegate to break the child-parent relation if the
            // child is hidden. For details (see incluceChildForAccessibility).
            ViewCompat.setAccessibilityDelegate(child, mChildAccessibilityDelegate);
        } else {
            // Initially, the content is shown for accessibility.
            ViewCompat.setImportantForAccessibility(child,
                    ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
        }
        super.addView(child, index, params);
    }

    private static boolean includeChildForAccessibilitiy(View child) {
        // If the child is not important for accessibility we make
        // sure this hides the entire subtree rooted at it as the
        // IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDATS is not
        // supported on older platforms but we want to hide the entire
        // content and not opened drawers if a drawer is opened.
        return ViewCompat.getImportantForAccessibility(child)
                != ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
                && ViewCompat.getImportantForAccessibility(child)
                != ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO;
    }

    /**
     * State persisted across instances
     */
    protected static class SavedState extends BaseSavedState {
        int openDrawerGravity = Gravity.NO_GRAVITY;
        int lockModeTop = LOCK_MODE_UNLOCKED;
        int lockModeBottom = LOCK_MODE_UNLOCKED;

        public SavedState(Parcel in) {
            super(in);
            openDrawerGravity = in.readInt();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(openDrawerGravity);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel source) {
                        return new SavedState(source);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }

    private class ViewDragCallback extends ViewDragHelper.Callback {
        private final int mAbsGravity;
        private ViewDragHelper mDragger;

        private final Runnable mPeekRunnable = new Runnable() {
            @Override
            public void run() {
                peekDrawer();
            }
        };

        public ViewDragCallback(int gravity) {
            mAbsGravity = gravity;
        }

        public void setDragger(ViewDragHelper dragger) {
            mDragger = dragger;
        }

        public void removeCallbacks() {
            VerticalDrawerLayout.this.removeCallbacks(mPeekRunnable);
        }

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            // Only capture views where the gravity matches what we're looking
            // for.
            // This lets us use two ViewDragHelpers, one for each side drawer.
            return isDrawerView(child) && checkDrawerViewAbsoluteGravity(child, mAbsGravity)
                    && getDrawerLockMode(child) == LOCK_MODE_UNLOCKED;
        }

        @Override
        public void onViewDragStateChanged(int state) {
            updateDrawerState(mAbsGravity, state, mDragger.getCapturedView());
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            float offset;
            final int childHeight = changedView.getHeight();

            // This reverses the positioning shown in onLayout.
            if (checkDrawerViewAbsoluteGravity(changedView, Gravity.TOP)) {
                offset = (float) (childHeight + top) / childHeight;
            } else {
                final int height = getHeight();
                offset = (float) (height - top) / childHeight;
            }
            setDrawerViewOffset(changedView, offset);
            changedView.setVisibility(offset == 0 ? INVISIBLE : VISIBLE);
            invalidate();
        }

        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {
            final LayoutParams lp = (LayoutParams) capturedChild.getLayoutParams();
            lp.isPeeking = false;

            closeOtherDrawer();
        }

        private void closeOtherDrawer() {
            final int otherGrav = mAbsGravity == Gravity.TOP ? Gravity.BOTTOM : Gravity.TOP;
            final View toClose = findDrawerWithGravity(otherGrav);
            if (toClose != null) {
                closeDrawer(toClose);
            }
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            // Offset is how open the drawer is, therefore left/right values
            // are reversed from one another.
            final float offset = getDrawerViewOffset(releasedChild);
            final int childHeight = releasedChild.getHeight();

            int top;
            if (checkDrawerViewAbsoluteGravity(releasedChild, Gravity.TOP)) {
                top = yvel > 0 || yvel == 0 && offset > 0.5f ? 0 : -childHeight;
            } else {
                final int height = getHeight();
                top = yvel < 0 || yvel == 0 && offset > 0.5f ? height - childHeight : height;
            }

            mDragger.settleCapturedViewAt(releasedChild.getLeft(), top);
            invalidate();
        }

        @Override
        public void onEdgeTouched(int edgeFlags, int pointerId) {
            postDelayed(mPeekRunnable, PEEK_DELAY);
        }

        private void peekDrawer() {
            final View toCapture;
            final int childTop;
            final int peekDistance = mDragger.getEdgeSize();
            final boolean topEdge = mAbsGravity == Gravity.TOP;
            if (topEdge) {
                toCapture = findDrawerWithGravity(Gravity.TOP);
                childTop = (toCapture != null ? -toCapture.getHeight() : 0) + peekDistance;
            } else {
                toCapture = findDrawerWithGravity(Gravity.BOTTOM);
                childTop = getHeight() - peekDistance;
            }
            // Only peek if it would mean making the drawer more visible and the
            // drawer isn't locked
            if (toCapture != null && ((topEdge && toCapture.getTop() < childTop) ||
                    (!topEdge && toCapture.getTop() > childTop)) &&
                    getDrawerLockMode(toCapture) == LOCK_MODE_UNLOCKED) {
                final LayoutParams lp = (LayoutParams) toCapture.getLayoutParams();
                mDragger.smoothSlideViewTo(toCapture, toCapture.getLeft(), childTop);
                lp.isPeeking = true;
                invalidate();

                closeOtherDrawer();

                cancelChildViewTouch();
            }
        }

        @Override
        public boolean onEdgeLock(int edgeFlags) {
            if (ALLOW_EDGE_LOCK) {
                final View drawer = findDrawerWithGravity(mAbsGravity);
                if (drawer != null && !isDrawerOpen(drawer)) {
                    closeDrawer(drawer);
                }
                return true;
            }
            return false;
        }

        @Override
        public void onEdgeDragStarted(int edgeFlags, int pointerId) {
            final View toCapture;
            if ((edgeFlags & ViewDragHelper.EDGE_TOP) == ViewDragHelper.EDGE_TOP) {
                toCapture = findDrawerWithGravity(Gravity.TOP);
            } else {
                toCapture = findDrawerWithGravity(Gravity.BOTTOM);
            }

            if (toCapture != null && getDrawerLockMode(toCapture) == LOCK_MODE_UNLOCKED) {
                mDragger.captureChildView(toCapture, pointerId);
            }
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return child.getHeight();
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return child.getLeft();
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            if (checkDrawerViewAbsoluteGravity(child, Gravity.TOP)) {
                return Math.max(-child.getHeight(), Math.min(top, 0));
            } else {
                final int height = getHeight();
                return Math.max(height - child.getHeight(), Math.min(top, height));
            }
        }
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {

        public int gravity = Gravity.NO_GRAVITY;
        float onScreen;
        boolean isPeeking;
        boolean knownOpen;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            final TypedArray a = c.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
            this.gravity = a.getInt(0, Gravity.NO_GRAVITY);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, int gravity) {
            this(width, height);
            this.gravity = gravity;
        }

        public LayoutParams(LayoutParams source) {
            super(source);
            this.gravity = source.gravity;
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.MarginLayoutParams source) {
            super(source);
        }
    }

    class AccessibilityDelegate extends AccessibilityDelegateCompat {
        private final Rect mTmpRect = new Rect();

        @Override
        public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
            final AccessibilityNodeInfoCompat superNode = AccessibilityNodeInfoCompat.obtain(info);
            super.onInitializeAccessibilityNodeInfo(host, superNode);

            info.setClassName(VerticalDrawerLayout.class.getName());
            info.setSource(host);
            final ViewParent parent = ViewCompat.getParentForAccessibility(host);
            if (parent instanceof View) {
                info.setParent((View) parent);
            }
            copyNodeInfoNoChildren(info, superNode);

            superNode.recycle();

            addChildrenForAccessibility(info, (ViewGroup) host);
        }

        @Override
        public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
            super.onInitializeAccessibilityEvent(host, event);

            event.setClassName(VerticalDrawerLayout.class.getName());
        }

        @Override
        public boolean dispatchPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
            // Special case to handle window state change events. As far as
            // accessibility services are concerned, state changes from
            // DrawerLayout invalidate the entire contents of the screen (like
            // an Activity or Dialog) and they should announce the title of the
            // new content.
            if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                final List<CharSequence> eventText = event.getText();
                final View visibleDrawer = findVisibleDrawer();
                if (visibleDrawer != null) {
                    final int edgeGravity = getDrawerViewAbsoluteGravity(visibleDrawer);
                    final CharSequence title = getDrawerTitle(edgeGravity);
                    if (title != null) {
                        eventText.add(title);
                    }
                }

                return true;
            }

            return super.dispatchPopulateAccessibilityEvent(host, event);
        }

        private void addChildrenForAccessibility(AccessibilityNodeInfoCompat info, ViewGroup v) {
            final int childCount = v.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = v.getChildAt(i);
                if (filter(child)) {
                    continue;
                }

                // Adding children that are marked as not important for
                // accessibility will break the hierarchy, so we need to check
                // that value and re-parent views if necessary.
                final int importance = ViewCompat.getImportantForAccessibility(child);
                switch (importance) {
                    case ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS:
                        // Always skip NO_HIDE views and their descendants.
                        break;
                    case ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO:
                        // Re-parent children of NO view groups, skip NO views.
                        if (child instanceof ViewGroup) {
                            addChildrenForAccessibility(info, (ViewGroup) child);
                        }
                        break;
                    case ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_AUTO:
                        // Force AUTO views to YES and add them.
                        ViewCompat.setImportantForAccessibility(
                                child, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
                    case ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES:
                        info.addChild(child);
                        break;
                }
            }
        }

        @Override
        public boolean onRequestSendAccessibilityEvent(ViewGroup host, View child,
                                                       AccessibilityEvent event) {
            if (includeChildForAccessibilitiy(child)) {
                return super.onRequestSendAccessibilityEvent(host, child, event);
            }
            return false;
        }

        public boolean filter(View child) {
            final View openDrawer = findOpenDrawer();
            return openDrawer != null && openDrawer != child;
        }

        /**
         * This should really be in AccessibilityNodeInfoCompat, but there
         * unfortunately
         * seem to be a few elements that are not easily cloneable using the
         * underlying API.
         * Leave it private here as it's not general-purpose useful.
         */
        private void copyNodeInfoNoChildren(AccessibilityNodeInfoCompat dest,
                                            AccessibilityNodeInfoCompat src) {
            final Rect rect = mTmpRect;

            src.getBoundsInParent(rect);
            dest.setBoundsInParent(rect);

            src.getBoundsInScreen(rect);
            dest.setBoundsInScreen(rect);

            dest.setVisibleToUser(src.isVisibleToUser());
            dest.setPackageName(src.getPackageName());
            dest.setClassName(src.getClassName());
            dest.setContentDescription(src.getContentDescription());

            dest.setEnabled(src.isEnabled());
            dest.setClickable(src.isClickable());
            dest.setFocusable(src.isFocusable());
            dest.setFocused(src.isFocused());
            dest.setAccessibilityFocused(src.isAccessibilityFocused());
            dest.setSelected(src.isSelected());
            dest.setLongClickable(src.isLongClickable());

            dest.addAction(src.getActions());
        }
    }

    final class ChildAccessibilityDelegate extends AccessibilityDelegateCompat {
        @Override
        public void onInitializeAccessibilityNodeInfo(View child,
                                                      AccessibilityNodeInfoCompat info) {
            super.onInitializeAccessibilityNodeInfo(child, info);
            if (!includeChildForAccessibilitiy(child)) {
                // If we are ignoring the sub-tree rooted at the child,
                // break the connection to the rest of the node tree.
                // For details refer to includeChildForAccessibilitiy.
                info.setParent(null);
            }
        }
    }
}
