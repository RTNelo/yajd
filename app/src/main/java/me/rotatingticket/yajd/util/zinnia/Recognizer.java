package me.rotatingticket.yajd.util.zinnia;

import java.io.Closeable;

/**
 * The handwriting recognizer based on zinnia.
 */
public class Recognizer implements Closeable {
    private static Recognizer instance;

    /**
     * Native code initializer.
     */
    public static native void nativeInit();

    /**
     * Initialize all related native code.
     * ResultSet.nativeInit may not be invoked without call this method.
     */
    public static void nativeInitAll() {
        nativeInit();
        ResultSet.nativeInit();
        Character.nativeInit();
    }

    static {
        System.loadLibrary("zinnia-jni");
        nativeInitAll();
    }

    private long id;

    /**
     * Open a recognizer and load the model file at modelPath.
     * @param modelPath The model file path.
     */
    public Recognizer(String modelPath) {
        id = create();
        open(modelPath);
    }

    /**
     * Get the singleton instance.
     * @param modelPath If there is no instance before, use the modelPath as the model file path.
     * @return The Recognizer instance.
     */
    public static synchronized Recognizer getInstance(String modelPath) {
        if (instance == null) {
            instance = new Recognizer(modelPath);
        }
        return instance;
    }

    private static native long create();
    private native void open(String modelPath);

    /**
     * Classify an Character instance to get corresponding character.
     * @param character The Character instance to classify.
     * @param nbest At most nbest results will be returned.
     * @return The result set.
     */
    public native ResultSet classify(Character character, long nbest);

    /**
     * Get the error message after something wrong.
     * @return The error message.
     */
    public native String what();

    /**
     * Release the resource obtained by the recognizer.
     */
    @Override
    public native void close();
}
