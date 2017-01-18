package com.jojo.jiaminsun.nestingscroll.One;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.jojo.jiaminsun.nestingscroll.R;
import com.jojo.jiaminsun.nestingscroll.Util;

/**
 * Created by sunjiamin on 2017/1/11.
 */

public class EventDispatchPlanLayout extends ViewGroup {

    private static final String TAG = "EventDispatchPlanLayout";

    static final int INVALID_POINTER = -1;

    private int mHeaderViewId = 0;
    private int mTargetViewId = 0;
    private View mHeaderView;//头部View
    private View mTargetView;//下面可滑动View
    private ITargetView mTarget;//下面可滑动View


    private int mHeaderInitOffset;
    private int mHeaderCurrentOffset;
    private int mHeaderEndOffset = 0;

    private int mTargetInitOffset;
    private int mTargetCurrentOffset;
    private int mTargetEndOffset = 0;

    private int mTouchSlop;
    private int mActivePointerId = INVALID_POINTER;
    private boolean mIsDragging;
    private float mInitialDownY;
    private float mInitialMotionY;
    private float mLastMotionY;

    private VelocityTracker mVelocityTracker;
    private float mMaxVelocity;

    private Scroller mScroller;
    private boolean mNeedScrollToInitPos = false;
    private boolean mNeedScrollToEndPos = false;

    public EventDispatchPlanLayout(Context context) {
        super(context);
    }

    public EventDispatchPlanLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.EventDispatchPlanLayout, 0, 0);
        mHeaderViewId = array.getResourceId(R.styleable.EventDispatchPlanLayout_header_view_id,0);
        mTargetViewId = array.getResourceId(R.styleable.EventDispatchPlanLayout_target_view_id,0);

        mHeaderInitOffset = array.getDimensionPixelSize(R.styleable.
                EventDispatchPlanLayout_header_init_offset, Util.dp2px(getContext(), 20));
        mTargetInitOffset = array.getDimensionPixelSize(R.styleable.
                EventDispatchPlanLayout_target_init_offset, Util.dp2px(getContext(), 40));
        mHeaderCurrentOffset = mHeaderInitOffset;
        mTargetCurrentOffset = mTargetInitOffset;
        array.recycle();



        ViewCompat.setChildrenDrawingOrderEnabled(this, true);

        final ViewConfiguration vc = ViewConfiguration.get(getContext());
        mMaxVelocity = vc.getScaledMaximumFlingVelocity();
        mTouchSlop = Util.px2dp(context, vc.getScaledTouchSlop()); //系统的值是8dp,太大了。。。

        //Scroller是一个专门用于处理滚动效果的工具类
        // 第一步，创建Scroller的实例
        mScroller = new Scroller(getContext());
        mScroller.setFriction(0.98f);


    }

    /**
     * 当View中所有的子控件均被映射成xml后触发
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if(mHeaderViewId!=0){
            mHeaderView = findViewById(mHeaderViewId);
        }
        if(mTargetViewId!=0){
            mTargetView = findViewById(mTargetViewId);
            ensureTarget();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        ensureHeaderViewAndScrollView();
        int scrollMeasureWidthSpec = MeasureSpec.makeMeasureSpec(
                getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY);
        int scrollMeasureHeightSpec = MeasureSpec.makeMeasureSpec(
                getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY);
        mTargetView.measure(scrollMeasureWidthSpec, scrollMeasureHeightSpec);
        measureChild(mHeaderView, widthMeasureSpec, heightMeasureSpec);
    }


    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        if(getChildCount()==0){
            return;
        }
        ensureHeaderViewAndScrollView();

        final int childLeft = getPaddingLeft();
        final int childTop = getPaddingTop();
        final int childWidth = width - getPaddingLeft() - getPaddingRight();
        final int childHeight = height - getPaddingTop() - getPaddingBottom();

        mTargetInitOffset=mHeaderView.getMeasuredHeight();
        mTargetView.layout(childLeft, childTop + mTargetCurrentOffset,
                childLeft + childWidth, childTop + childHeight + mTargetCurrentOffset);
        int refreshViewWidth = mHeaderView.getMeasuredWidth();
        int refreshViewHeight = mHeaderView.getMeasuredHeight();
        mHeaderView.layout((width / 2 - refreshViewWidth / 2), mHeaderCurrentOffset,
                (width / 2 + refreshViewWidth / 2), mHeaderCurrentOffset + refreshViewHeight);

    }

    /**
     * 确保子view 中存在HeadView 和TargetView
     */
    private void ensureHeaderViewAndScrollView() {
        if (mHeaderView != null && mTargetView != null) {
            return;
        }
        if (mHeaderView == null && mTargetView == null &&getChildCount()>=2) {
            mHeaderView = getChildAt(0);
            mTargetView= getChildAt(1);
            ensureTarget();
            return;
        }
         throw  new RuntimeException("ensure headview and targetview");

    }

    /**
     * 确保targetView 实现
     */
    private void ensureTarget() {
        if(mTargetView instanceof  ITargetView){
            mTarget  = (ITargetView)mTargetView;
            return;
        }
        throw  new RuntimeException("TargetView should implement interface ITargetView");
    }


    @Override
    public void requestDisallowInterceptTouchEvent(boolean b) {
        // 去掉默认行为，使得每个事件都会经过这个Layout
    }

    /**
     * getChildDrawingOrder 用于 返回当前迭代子视图的索引.
     * 就是说 获取当前正在绘制的视图索引.
     * 如果需要改变ViewGroup子视图绘制的顺序,则需要重载这个方法.
     * 并且需要先调用 setChildrenDrawingOrderEnabled(boolean) 方法来启用子视图排序功能.
     * @param childCount 子类个数
     * @param i 当前迭代顺序   i  这个参数就是当前刷新的次序，就是现在要刷新第 i 个item
     *          return 的返回值，就是上面的那个你在第 i 次需要刷新的那个item
     * @return 绘制该迭代子类的索引
     */
    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        ensureHeaderViewAndScrollView();
        int headerIndex = indexOfChild(mHeaderView);
        int scrollIndex = indexOfChild(mTargetView);
        if (headerIndex < scrollIndex) {
            return i;
        }
        //如果scroll跑到header下层
        if (headerIndex == i) {
            return scrollIndex;
        } else if (scrollIndex == i) {
            return headerIndex;
        }
        return i;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        ensureHeaderViewAndScrollView();
        final int action = MotionEventCompat.getActionMasked(ev);
        int pointerIndex;

        if (!isEnabled() || mTarget.canChildScrollUp()) {
            Log.d(TAG, "fast end onIntercept: isEnabled = " + isEnabled() + "; canChildScrollUp = "
                    + mTarget.canChildScrollUp());
            // 不拦截   TargetView 触发事件
            return false;
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                //手指按下
                mActivePointerId = ev.getPointerId(0);//得到第一个触摸点
                mIsDragging = false;
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    //没有触摸到 不拦截
                    return false;
                }
                // 在down的时候记录初始的y值
                mInitialDownY = ev.getY(pointerIndex);
                break;

            case MotionEvent.ACTION_MOVE:
                //手指移动
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    //没有触摸到 不拦截
                    Log.e(TAG, "Got ACTION_MOVE event but have an invalid active pointer id.");
                    return false;
                }
                //获取触摸点的Y左边 通过Y左边判断是否拖动
                final float y = ev.getY(pointerIndex);
                // 判断是否dragging
                startDragging(y);
                break;

            //多点触控  双指逻辑处理
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                //手指抬起活 取消
                mIsDragging = false;
                mActivePointerId = INVALID_POINTER;
                break;
        }

        return mIsDragging;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        int pointerIndex;

        if (!isEnabled() || mTarget.canChildScrollUp()) {
            Log.d(TAG, "fast end onTouchEvent: isEnabled = " + isEnabled() + "; canChildScrollUp = "
                    + mTarget.canChildScrollUp());
            return false;
        }
        // 速度追踪
        acquireVelocityTracker(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getPointerId(0);
                mIsDragging = false;
                break;

            case MotionEvent.ACTION_MOVE: {
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e(TAG, "Got ACTION_MOVE event but have an invalid active pointer id.");
                    return false;
                }
                final float y = ev.getY(pointerIndex);
                startDragging(y);

                if (mIsDragging) {
                    float dy = y - mLastMotionY;
                    Log.e(TAG,"ACTION_MOVE dy:"+dy);
                    if (dy >= 0) {
                        moveTargetView(dy);
                    } else {
                        if (mTargetCurrentOffset + dy <= mTargetEndOffset) {
                            moveTargetView(dy);
                            // 重新dispatch一次down事件，使得列表可以继续滚动
                            int oldAction = ev.getAction();
                            ev.setAction(MotionEvent.ACTION_DOWN);
                            dispatchTouchEvent(ev);
                            ev.setAction(oldAction);
                        } else {
                            moveTargetView(dy);
                        }
                    }
                    mLastMotionY = y;
                }
                break;
            }
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                pointerIndex = MotionEventCompat.getActionIndex(ev);
                if (pointerIndex < 0) {
                    Log.e(TAG, "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                    return false;
                }
                mActivePointerId = ev.getPointerId(pointerIndex);
                break;
            }

            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP: {
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e(TAG, "Got ACTION_UP event but don't have an active pointer id.");
                    return false;
                }

                if (mIsDragging) {
                    mIsDragging = false;
                    // 获取瞬时速度
                    mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                    final float vy = mVelocityTracker.getYVelocity(mActivePointerId);
                    Log.e("sjm","当前瞬时速度："+vy);
                    finishDrag((int) vy);
                }
                mActivePointerId = INVALID_POINTER;
                //释放速度追踪
                releaseVelocityTracker();
                return false;
            }
            case MotionEvent.ACTION_CANCEL:
                releaseVelocityTracker();
                return false;
        }

        return mIsDragging;
    }

    private void acquireVelocityTracker(final MotionEvent event) {
        if (null == mVelocityTracker) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    private void releaseVelocityTracker() {
        if (null != mVelocityTracker) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void finishDrag(int vy) {
        Log.i(TAG, "TouchUp: vy = " + vy);
        if (vy > 0) {
            // 向下触发fling,需要滚动到Init位置
            mNeedScrollToInitPos = true;
            //抛(fling)：手指触动屏幕后，稍微滑动后立即松开
            mScroller.fling(0, mTargetCurrentOffset, 0, vy,
                    0, 0, mTargetEndOffset, Integer.MAX_VALUE);
            invalidate();
        } else if (vy < 0) {
            // 向上触发fling,需要滚动到End位置
            mNeedScrollToEndPos = true;
            mScroller.fling(0, mTargetCurrentOffset, 0, vy,
                    0, 0, mTargetEndOffset, Integer.MAX_VALUE);
            invalidate();
        } else {
            //没有触发fling,就近原则  没有快速滑动
            if (mTargetCurrentOffset <= (mTargetEndOffset + mTargetInitOffset) / 2) {
                mNeedScrollToEndPos = true;
            } else {
                mNeedScrollToInitPos = true;
            }
            invalidate();
        }
    }

    /**
     * 判断是否拖动
     * @param y 触摸点的Y坐标
     */
    private void startDragging(float y) {
        if (y > mInitialDownY || mTargetCurrentOffset > mTargetEndOffset) {
            final float yDiff = Math.abs(y - mInitialDownY);
            if (yDiff > mTouchSlop && !mIsDragging) {
                mInitialMotionY = mInitialDownY + mTouchSlop;
                mLastMotionY = mInitialMotionY;
                mIsDragging = true;
            }
        }
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }

    private void moveTargetView(float dy) {
        int target = (int) (mTargetCurrentOffset + dy);
        //target
        moveTargetViewTo(target);
    }

    /**
     *
     * @param target 滑动的偏移量 加上 上次偏移量
     */
    private void moveTargetViewTo(int target) {
        target = Math.max(target, mTargetEndOffset);
        //Offset this view's vertical location by the specified number of pixels.
        //垂直方向偏移
        ViewCompat.offsetTopAndBottom(mTargetView, target - mTargetCurrentOffset);
        Log.e("sjm","****target:"+target +" mTargetCurrentOffset: "+mTargetCurrentOffset +" mTargetView Move:"+(target - mTargetCurrentOffset));
        mTargetCurrentOffset = target;

        int headerTarget;
        if (mTargetCurrentOffset >= mTargetInitOffset) {
            headerTarget = mHeaderInitOffset;
        } else if (mTargetCurrentOffset <= mTargetEndOffset) {
            headerTarget = mHeaderEndOffset;
        } else {
            float percent = (mTargetCurrentOffset - mTargetEndOffset) * 1.0f / mTargetInitOffset - mTargetEndOffset;
            headerTarget = (int) (mHeaderEndOffset + percent * (mHeaderInitOffset - mHeaderEndOffset));
        }
        ViewCompat.offsetTopAndBottom(mHeaderView, headerTarget - mHeaderCurrentOffset);
        Log.e("sjm","###headerTarget:"+headerTarget +" mHeaderCurrentOffset: "+mHeaderCurrentOffset +"mHeaderView Move:"+(headerTarget - mHeaderCurrentOffset));
        mHeaderCurrentOffset = headerTarget;
    }

    @Override
    public void computeScroll() {

        // 第三步，重写computeScroll()方法，并在其内部完成平滑滚动的逻辑
        //computeScrollOffset 为false 时表示 fling 操作已经结束，为 true 时表示 fling 正在进行
        if (mScroller.computeScrollOffset()) {
            int offsetY = mScroller.getCurrY();
            moveTargetViewTo(offsetY);
            invalidate();
        } else if (mNeedScrollToInitPos) {
            mNeedScrollToInitPos = false;
            if (mTargetCurrentOffset == mTargetInitOffset) {
                return;
            }
            // 第二步，调用startScroll()方法来初始化滚动数据并刷新界面
            mScroller.startScroll(0, mTargetCurrentOffset, 0, mTargetInitOffset - mTargetCurrentOffset);
            invalidate();
        } else if (mNeedScrollToEndPos) {
            mNeedScrollToEndPos = false;
            if (mTargetCurrentOffset == mTargetEndOffset) {
                if (mScroller.getCurrVelocity() > 0) {
                    // 如果还有速度，则传递给子view
                    mTarget.fling(-mScroller.getCurrVelocity());
                }
            }
            // 第二步，调用startScroll()方法来初始化滚动数据并刷新界面
            mScroller.startScroll(0, mTargetCurrentOffset, 0, mTargetEndOffset - mTargetCurrentOffset);
            invalidate();
        }
    }

    public interface ITargetView {
        boolean canChildScrollUp();

        void fling(float vy);
    }
}
