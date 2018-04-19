package com.nilesh.lockbutton.customview;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.nilesh.lockbutton.R;
import com.nilesh.lockbutton.utils.AnimUtils;

public class FloatingLockView extends FrameLayout {

    private static int mLastLeftPosition = 0;
    private static int mLastTopPosition = 0;
    private View mLockIcon;
    private View bgView;
    private LayoutParams mLayoutParams;
    private int availableHeight;
    private int availableWidth;
    private int lockHeight;

    public FloatingLockView(Context context) {
        super(context);
        init(context, null);
    }

    public FloatingLockView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FloatingLockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public FloatingLockView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    public void attachToScreen(Activity activity) {
        View content = (View) activity.findViewById(android.R.id.content).getParent();
        ViewGroup parent = (ViewGroup) content.getParent();
        parent.addView(this);
    }

    public void update() {
        mLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        if (mLastLeftPosition != 0 && mLastTopPosition != 0) {
            mLayoutParams.leftMargin = mLastLeftPosition;
            mLayoutParams.topMargin = mLastTopPosition;
            mLockIcon.setLayoutParams(mLayoutParams);
        } else {
            mLockIcon.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        public void onGlobalLayout() {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                mLockIcon.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            } else {
                                mLockIcon.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            }
                            mLayoutParams.leftMargin = (int) mLockIcon.getX();
                            mLayoutParams.topMargin = (int) mLockIcon.getY();
                            mLockIcon.setLayoutParams(mLayoutParams);
                        }
                    }
            );
        }
    }

    public void init(Context context, AttributeSet attrs) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.layout_include_lock_view, this, true);
        bgView = rootView.findViewById(R.id.t_view);
        mLockIcon = rootView.findViewById(R.id.lock_icon);
        update();

        mLockIcon.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mLockIcon.setAlpha(1F);
                AnimUtils.scaleViewAnim(v, 1.5f, 1.5f, 1.5f, 1.5f);
                mLockIcon.setOnTouchListener(touchListener);
                return false;
            }
        });

        mLockIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLockIcon.isSelected()) {
                    unlock();
                } else {
                    lock();
                }
            }
        });

        final FrameLayout parent = findViewById(R.id.parent_view);

        parent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                availableHeight = parent.getHeight();
                availableWidth = parent.getWidth();
                if (availableHeight > 0) {
                    parent.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });

        mLockIcon.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                lockHeight = mLockIcon.getHeight();

                if (lockHeight > 0) {
                    mLockIcon.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });
    }

    OnTouchListener touchListener = new OnTouchListener() {
        private int lastAction;
        private int initialX;
        private int initialY;
        private float initialTouchX;
        private float initialTouchY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:

                    initialX = mLayoutParams.leftMargin;
                    initialY = mLayoutParams.topMargin;

                    initialTouchX = event.getRawX();
                    initialTouchY = event.getRawY();
                    lastAction = event.getAction();
                    return true;
                case MotionEvent.ACTION_UP:
                    if (lastAction == MotionEvent.ACTION_DOWN) {
                        if (mLockIcon.isSelected()) {
                            unlock();
                        } else {
                            lock();
                        }
                    } else {
                        AnimUtils.scaleViewAnim(v, 1f, 1f, 1f, 1f);
                        if (mLockIcon.isSelected()) {
                            mLockIcon.setAlpha(1F);
                        } else {
                            mLockIcon.setAlpha(.6F);
                        }
                        mLockIcon.setOnTouchListener(null);

                        if (mLayoutParams.leftMargin <= 0) {
                            mLayoutParams.leftMargin = 30;
                        }
                        if (mLayoutParams.topMargin <= 0) {
                            mLayoutParams.topMargin = 30;
                        }
                        if (mLayoutParams.topMargin >= availableHeight - lockHeight) {
                            mLayoutParams.topMargin = availableHeight - lockHeight - 30;
                        }
                        if (mLayoutParams.leftMargin >= availableWidth - lockHeight) {
                            mLayoutParams.leftMargin = availableWidth - lockHeight - 30;
                        }
                        mLastLeftPosition = mLayoutParams.leftMargin;
                        mLastTopPosition = mLayoutParams.topMargin;
                        mLockIcon.setLayoutParams(mLayoutParams);
                    }

                    lastAction = event.getAction();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (mLayoutParams.leftMargin >= 0 && mLayoutParams.topMargin >= 0 && mLayoutParams.rightMargin >= 0 &&
                            mLayoutParams.bottomMargin >= 0 && mLayoutParams.topMargin < availableHeight - lockHeight && mLayoutParams.leftMargin < availableWidth - lockHeight) {
                        mLayoutParams.leftMargin = initialX + (int) (event.getRawX() - initialTouchX) - lockHeight;
                        mLayoutParams.topMargin = initialY + (int) (event.getRawY() - initialTouchY) - lockHeight;
                        lastAction = event.getAction();
                        mLockIcon.setLayoutParams(mLayoutParams);
                    } else {
                        lastAction = event.getAction();
                        if (mLayoutParams.leftMargin <= 0) {
                            mLayoutParams.leftMargin = 30;
                        }
                        if (mLayoutParams.topMargin <= 0) {
                            mLayoutParams.topMargin = 30;
                        }
                        if (mLayoutParams.topMargin >= availableHeight - lockHeight) {
                            mLayoutParams.topMargin = availableHeight - lockHeight - 30;
                        }
                        if (mLayoutParams.leftMargin >= availableWidth - lockHeight) {
                            mLayoutParams.leftMargin = availableWidth - lockHeight - 30;
                        }
                    }
                    return true;
            }
            return false;
        }
    };

    public void lock() {
        mLockIcon.setAlpha(1F);
        mLockIcon.setSelected(true);
        bgView.setVisibility(VISIBLE);
    }

    public void unlock() {
        mLockIcon.setAlpha(.6F);
        mLockIcon.setSelected(false);
        bgView.setVisibility(GONE);
    }

    public boolean isLocked() {
        return mLockIcon.isSelected();
    }

}
