package me.rotatingticket.yajd.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import me.rotatingticket.yajd.R;

public class FloatingWindow extends RelativeLayout {
    private View viewLeftTop;
    private View viewRightBottom;
    private TextView viewResult;
    private float lastPosX, lastPosY;
    private GestureDetector gestureDetector;
    private boolean isCapturing;
    private WindowManager.LayoutParams layoutParams;

    public void setParam(WindowManager.LayoutParams layoutParams) {
        this.layoutParams = layoutParams;
    }

    public WindowManager.LayoutParams getLayoutParams() {
        return layoutParams;
    }

    public void setContent(String text) {
        viewResult.setText(text);
    }

    public boolean isCapturing() {
        return isCapturing;
    }

    public interface OnLayoutParamsChangedListener {
        void onLayoutParamsChanged(WindowManager.LayoutParams layoutParams);
    }

    private OnLayoutParamsChangedListener onLayoutParamsChangedListener;

    public void setOnLayoutParamsChangedListener(OnLayoutParamsChangedListener onLayoutParamsChangedListener) {
        this.onLayoutParamsChangedListener = onLayoutParamsChangedListener;
    }

    public FloatingWindow(Context context) {
        super(context);
        init();
    }

    public FloatingWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FloatingWindow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public FloatingWindow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        inflate(getContext(), R.layout.view_floating_window, this);
        viewLeftTop = findViewById(R.id.view_left_top);
        viewRightBottom = findViewById(R.id.view_right_bottom);
        viewResult = findViewById(R.id.view_screen_translate_result);
        viewLeftTop.setOnTouchListener(this::onCornerTouchEvent);
        viewRightBottom.setOnTouchListener(this::onCornerTouchEvent);
        isCapturing = false;

        gestureDetector = new GestureDetector(this.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (!isCapturing) {
                    startCapture();
                }
                return true;
            }
        });

        this.setOnTouchListener((v, motionEvent) -> {
            gestureDetector.onTouchEvent(motionEvent);
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastPosX = motionEvent.getRawX();
                    lastPosY = motionEvent.getRawY();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_MOVE: {
                    float dX = motionEvent.getRawX() - lastPosX;
                    float dY = motionEvent.getRawY() - lastPosY;

                    WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                          this.layoutParams.width,
                          this.layoutParams.height,
                          this.layoutParams.x,
                          this.layoutParams.y,
                          this.layoutParams.type,
                          this.layoutParams.flags,
                          this.layoutParams.format
                    );
                    layoutParams.gravity = this.layoutParams.gravity;

                    if (onLayoutParamsChangedListener != null) {
                        layoutParams.x += (int) dX;
                        layoutParams.y += (int) dY;
                    }
                    onLayoutParamsChangedListener.onLayoutParamsChanged(layoutParams);
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        setParam(layoutParams);
                    }
                    break;
                }
            }
            return true;
        });
    }

    private void startCapture() {
        viewLeftTop.setVisibility(View.GONE);
        viewRightBottom.setVisibility(View.GONE);
        layoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        if (onLayoutParamsChangedListener != null) {
            onLayoutParamsChangedListener.onLayoutParamsChanged(layoutParams);
        }
        isCapturing = true;
    }

    private boolean onCornerTouchEvent(View view, MotionEvent motionEvent) {
        if (onLayoutParamsChangedListener == null) {
            return false;
        }

        Log.e("YAJD_TE", motionEvent.toString());

        boolean isLeftTop = view.getId() == R.id.view_left_top;

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastPosX = motionEvent.getRawX();
                lastPosY = motionEvent.getRawY();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_MOVE: {
                float dX = motionEvent.getRawX() - lastPosX;
                float dY = motionEvent.getRawY() - lastPosY;

                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                      this.layoutParams.width,
                      this.layoutParams.height,
                      this.layoutParams.x,
                      this.layoutParams.y,
                      this.layoutParams.type,
                      this.layoutParams.flags,
                      this.layoutParams.format
                );
                layoutParams.gravity = this.layoutParams.gravity;

                if (onLayoutParamsChangedListener != null) {
                    if (isLeftTop) {
                        layoutParams.x += (int)dX;
                        layoutParams.y += (int)dY;
                        layoutParams.width += (int) -dX;
                        layoutParams.height += (int) -dY;
                    } else {
                        layoutParams.width += (int) dX;
                        layoutParams.height += (int) dY;
                    }
                    onLayoutParamsChangedListener.onLayoutParamsChanged(layoutParams);
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    setParam(layoutParams);
                }
                break;
            }
        }
        return true;
    }
}
