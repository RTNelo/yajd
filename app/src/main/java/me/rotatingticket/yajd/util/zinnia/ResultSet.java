package me.rotatingticket.yajd.util.zinnia;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

/**
 * The classify result by Recognizer.
 */
public class ResultSet implements Closeable {
    public static native void nativeInit();

    static {
        System.loadLibrary("zinnia-jni");
        nativeInit();
    }

    private long id;

    private ResultSet(long id) {
        this.id = id;
    }

    /**
     * Get the character of the result at index.
     * @param index The result index.
     * @return The corresponding value.
     */
    public native String value(long index);

    /**
     * Get the score of the result at index.
     * @param index The result index.
     * @return The corresponding score. The larger value determines the stronger confidence.
     */
    public native float score(long index);

    /**
     * Get the size of the result set.
     * @return Result set size.
     */
    public native long size();

    /**
     * Release the resource obtained by the result set.
     */
    @Override
    public native void close();

    /**
     * Convert the ResultSet to a list of value string in the same order.
     * @return The list of value string.
     */
    public List<String> toList() {
        int size = (int)size();
        ArrayList<String> result = new ArrayList<>(size);
        for (int i = 0; i != size; ++i) {
            result.add(value(i));
        }
        return result;
    }
}
