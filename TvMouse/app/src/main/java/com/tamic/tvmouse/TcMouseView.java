/*
 * TVMouseView.java
 *
 * Version:1.0.0
 *
 * Date: 2014�?�?�?
 *
 * Copyright 2012-2014 Tamic. All Rights Reserved
 */
package com.tamic.tvmouse;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Scroller;

/**
 * @author liuyongkui
 */
public class TcMouseView extends FrameLayout {
    private static final String TAG = "[2]TcMouseView  ";

    private int mOffsetX;
    private int mOffsetY;

    private ImageView mMouseView;

    private Bitmap mMouseBitmap;

    private TcMouseManager mMouseManager;

    private int mMouseX = TcMouseManager.MOUSE_STARTX;
    private int mMouseY = TcMouseManager.MOUSE_STARY;

    private int mLastMouseX = mMouseX;
    private int mLastMouseY = mMouseY;


    private int mMoveDis = TcMouseManager.MOUSE_MOVE_STEP;

    private OnMouseListener mOnMouseListener;


    public TcMouseView(Context context) {
        super(context);
    }

    public TcMouseView(Context context, TcMouseManager mMouseMrg) {
        super(context);
        init(mMouseMrg);
    }

    private void init(TcMouseManager manager) {
        mMouseManager = manager;
        Drawable drawable = getResources().getDrawable(R.mipmap.shubiao);
        mMouseBitmap = drawableToBitamp(drawable);
        mMouseView = new ImageView(getContext());
        mMouseView.setImageBitmap(mMouseBitmap);
        addView(mMouseView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        mOffsetX = (int) ((mMouseBitmap.getWidth()));
        mOffsetY = (int) ((mMouseBitmap.getHeight()));
        mOffsetX = (int) ((mMouseBitmap.getWidth()) * 30 / 84);
        mOffsetY = (int) ((mMouseBitmap.getHeight()) * 20 / 97);
    }

    public OnMouseListener getOnMouseListener() {
        return mOnMouseListener;
    }

    public void setOnMouseListener(OnMouseListener mOnMouseListener) {
        this.mOnMouseListener = mOnMouseListener;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mMouseView != null && mMouseBitmap != null) {
            mMouseView.measure(MeasureSpec.makeMeasureSpec(mMouseBitmap.getWidth(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(mMouseBitmap.getHeight(), MeasureSpec.EXACTLY));
        }
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        if (mMouseView != null) {
            mMouseView.layout(mMouseX, mMouseY, mMouseX + mMouseView.getMeasuredWidth(), mMouseY + mMouseView.getMeasuredHeight());
        }
    }


    private Bitmap drawableToBitamp(Drawable drawable) {
        BitmapDrawable bd = (BitmapDrawable) drawable;
        Bitmap bitmap = bd.getBitmap();
        return Bitmap.createScaledBitmap(bitmap, 30, 30, true);
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.d(TAG, "dispatchKeyEvent,keycode:" + event.getKeyCode() + ",action:" + event.getAction());// up 19 ,down 20 , left 21, right 22, center 23
        switch (event.getKeyCode()) {
            case TcMouseManager.KEYCODE_UP:
            case TcMouseManager.KEYCODE_DOWN:
            case TcMouseManager.KEYCODE_LEFT:
            case TcMouseManager.KEYCODE_RIGHT:
            case TcMouseManager.KEYCODE_CENTER:
                if (mOnMouseListener != null) {
                    return mOnMouseListener.onclick(TcMouseView.this, event);
                }
            default:
                break;
        }
        return super.dispatchKeyEvent(event);

    }

    public void moveMouse(KeyEvent event, int times) {
        Log.d(TAG, " moveMouse");
        mMoveDis = times * TcMouseManager.MOUSE_MOVE_STEP;
        switch (event.getKeyCode()) {
            case TcMouseManager.KEYCODE_UP:
                if (mMouseY - mMoveDis >= 0) {
                    mMouseY = mMouseY - mMoveDis;
                } else {// 超过底部范围，界面要滑动
                    mMouseY = 0;
                    scrollView(event);
                }
                break;
            case TcMouseManager.KEYCODE_LEFT:
                mMouseX = (mMouseX - mMoveDis > 0) ? mMouseX - mMoveDis : 0;
                break;
            case TcMouseManager.KEYCODE_DOWN:
                if (mMouseY + mMoveDis < getMeasuredHeight() - mMoveDis) {
                    mMouseY = mMouseY + mMoveDis;
                } else {// 超过底部范围，界面要滑动
                    mMouseY = getMeasuredHeight() - mOffsetY;
                    scrollView(event);
                }
                break;
            case TcMouseManager.KEYCODE_RIGHT:
                mMouseX = (mMouseX + mMoveDis < getMeasuredWidth() - mOffsetX) ? mMouseX + mMoveDis : getMeasuredWidth() - mOffsetX;
                break;
        }
        if (mLastMouseX == mMouseX && mLastMouseY == mMouseY) {
            return;
        }

        mLastMouseX = mMouseX;
        mLastMouseY = mMouseY;
        requestLayout();
        mMouseManager.sendMouseHoverEvent(mMouseX + mOffsetX, mMouseY + mOffsetY);

    }

    public void onCenterButtonClicked(KeyEvent event) {
        Log.d(TAG, " onCenterButtonClicked");
        mMouseManager.sendCenterClickEvent(mMouseX + mOffsetX, mMouseY + mOffsetY, event.getAction());//加一点偏移
    }

    private void scrollView(KeyEvent event) {
        Log.d(TAG, " scrollView，dispatchKeyEvent");
        Scroller mScroller = new Scroller(getContext());
        if (mMouseManager.getCurrentActivityType() == TcMouseManager.MOUSE_TYPE) {
            int pageScrollBy = 0;
            if (event.getKeyCode() == TcMouseManager.KEYCODE_UP) {
                pageScrollBy = -mMoveDis;
            } else if (event.getKeyCode() == TcMouseManager.KEYCODE_DOWN) {
                pageScrollBy = mMoveDis;
//                mScroller.startScroll(this.getScrollX(), this.getScrollY(), this.getScrollX(), -this.getScrollY());
            }
//            this.dispatchKeyEvent(event);

            mMouseManager.sendScrollEvent(mMouseX + mOffsetX, mMouseY + mOffsetY);
            invalidate();
//            MainActivity.contentView.dispatchKeyEvent(event);

        }
    }

    /**
     * @author liuyongkui
     */
    public interface OnMouseListener {
        boolean onclick(View v, KeyEvent event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // TODO Auto-generated method stub
        return super.onInterceptTouchEvent(ev);
    }


}