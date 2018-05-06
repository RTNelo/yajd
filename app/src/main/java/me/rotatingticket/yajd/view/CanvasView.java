package me.rotatingticket.yajd.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * The View for user painting.
 */
public class CanvasView extends View {
    /**
     * the max stroke width
     */
    public static int STROKE_WIDTH = 32;

    /**
     * The background color of the canvas.
     */
    public static int BACKGROUND_COLOR = Color.LTGRAY;

    private boolean initialized = false;

    private float lastX, lastY;

    private boolean pressed = false;
    private Paint paint;
    private Canvas canvas;
    private Bitmap bitmap;
    private Paint bitmapPaint;

    public CanvasView(Context context) {
        this(context, null);
    }

    public CanvasView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CanvasView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CanvasView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        bitmapPaint = new Paint(Paint.DITHER_FLAG);
    }

    /**
     * Initialize for user painting. Will be invoked automatically at user first draw.
     */
    public void initialize() {
        if (!initialized) {
            bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_4444);
            canvas = new Canvas(bitmap);
            canvas.drawColor(BACKGROUND_COLOR);
        }
        initialized = true;
    }

    /**
     * The listener for user drawing strokes.
     */
    public interface OnStrokeListener {
        /**
         * Be invoked when user start a stroke.
         * @param x The stroke start point x.
         * @param y The stroke start point y.
         * @return true if prevent default process, else false.
         */
        boolean onStrokeBegin(float x, float y);

        /**
         * Be invoked when user drawing the stoke.
         * @param x The moving point x.
         * @param y The moving point y.
         * @return true if prevent default process, else false.
         */
        boolean onStrokeMove(float x, float y);

        /**
         * Be invoked when user stop the stroke.
         * @param x The stop point x.
         * @param y The stop point y.
         * @return true if prevent default process, else false.
         */
        boolean onStrokeEnd(float x, float y);
    }
    private OnStrokeListener onStrokeListener;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // auto initialization
                initialize();
                if (pressed) {
                    // multiple action down event may reach at the same time
                    // to guarantee the onStrokeBegin be invoked before onStrokeEnd,
                    // insert a onStrokeEnd before it.
                    this.onStrokeEnd(event.getX(), event.getY(), event.getPressure());
                }
                pressed = true;
                this.onStrokeBegin(event.getX(), event.getY(), event.getPressure());
                break;
            case MotionEvent.ACTION_MOVE:
                // batch event processing
                int historySize = event.getHistorySize();
                for (int i = 0; i != historySize; ++i) {
                    this.onStrokeMove(event.getHistoricalX(i),
                          event.getHistoricalY(i),
                          event.getHistoricalPressure(i));
                }
                this.onStrokeMove(event.getX(), event.getY(), event.getPressure());
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // multiple action up event may reach at the same time
                // to guarantee the onStrokeBegin be invoked before onStrokeEnd,
                // insert a onStrokeBegin before it.
                if (!pressed) {
                    this.onStrokeBegin(event.getX(), event.getY(), event.getPressure());
                }
                pressed = false;
                this.onStrokeEnd(event.getX(), event.getY(), event.getPressure());
                break;
            default:
                return super.onTouchEvent(event);
        }
        // to trigger the onDraw method
        invalidate();
        return true;
    }

    private void onStrokeBegin(float x, float y, float pressure) {
        recordLast(x, y);
        if (onStrokeListener != null && onStrokeListener.onStrokeBegin(x, y)) {
            return;
        }
    }

    private void onStrokeMove(float x, float y, float pressure) {
        if (onStrokeListener != null && onStrokeListener.onStrokeMove(x, y)) {
            recordLast(x, y);
            return;
        }
        // stroke width is in proportion to the pressure
        paint.setStrokeWidth(pressure * STROKE_WIDTH);
        canvas.drawLine(lastX, lastY, x, y, paint);
        recordLast(x, y);
    }

    private void onStrokeEnd(float x, float y, float pressure) {
        if (onStrokeListener != null && onStrokeListener.onStrokeEnd(x, y)) {
            return;
        }
    }

    public void setOnStrokeListener(OnStrokeListener onStrokeListener) {
        this.onStrokeListener = onStrokeListener;
    }

    /**
     * Record target point as last point for line drawing on the canvas.
     * @param lastX Target x.
     * @param lastY Target y.
     */
    private void recordLast(float lastX, float lastY) {
        this.lastX = lastX;
        this.lastY = lastY;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, 0, 0,bitmapPaint);
        } else {
            // only draw background color if not be initialized
            canvas.drawColor(BACKGROUND_COLOR);
        }
    }

    /**
     * Clear the strokes on the canvas
     */
    public void clear() {
        if (canvas == null) {
            return;
        }
        canvas.drawColor(BACKGROUND_COLOR);
        invalidate();
    }
}
