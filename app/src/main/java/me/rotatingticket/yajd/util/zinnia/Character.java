package me.rotatingticket.yajd.util.zinnia;

import java.io.Closeable;

/**
 * Represent a character to classify.
 */
public class Character implements Closeable {
    /**
     * Native code initializer.
     */
    public static native void nativeInit();

    static {
        System.loadLibrary("zinnia-jni");
        nativeInit();
    }

    private long id;
    private long strokeId;

    public Character() {
        id = create();
        strokeId = 0;
        clear();
    }

    private static native long create();

    /**
     * Clear the strokes in the character.
     */
    public native void clear();

    /**
     * Set character width.
     * @param width Character width.
     */
    public native void setWidth(long width);

    /**
     * Set character height.
     * @param height Character height.
     */
    public native void setHeight(long height);

    /**
     * Begin a stroke at point (x, y).
     * The x-axis point from left to right.
     * The y-axis point from top to bottom.
     * @param x Point.x
     * @param y Point.y
     */
    public void beginStroke(int x, int y) {
        draw(strokeId, x, y);
    }

    /**
     * End a stroke at point (x, y)
     * The x-axis point from left to right.
     * The y-axis point from top to bottom.
     * @param x Point.x
     * @param y Point.y
     */
    public void endStroke(int x, int y) {
        draw(strokeId++, x, y);
    }

    /**
     * Draw the internal point of a stroke.
     * The x-axis point from left to right.
     * The y-axis point from top to bottom.
     * @param x Point.x
     * @param y Point.y
     */
    public void draw(int x, int y) {
        draw(strokeId, x, y);
    }

    private native void draw(long id, int x, int y);

    /**
     * Release resource obtained by the character.
     */
    @Override
    public native void close();
}
