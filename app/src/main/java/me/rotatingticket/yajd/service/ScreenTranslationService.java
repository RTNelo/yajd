package me.rotatingticket.yajd.service;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;


public class ScreenTranslationService extends AccessibilityService {
    public interface OnAccessibilityEventListener {
        void onWindowContentChanged(AccessibilityEvent event);
    }

    private static OnAccessibilityEventListener onAccessibilityEventListener;
    public static OnAccessibilityEventListener getOnAccessibilityEventListener() {
        return onAccessibilityEventListener;
    }
    public static void setOnAccessibilityEventListener(OnAccessibilityEventListener onAccessibilityEventListener) {
        ScreenTranslationService.onAccessibilityEventListener = onAccessibilityEventListener;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.e("YAJD_S", String.valueOf(onAccessibilityEventListener));
        if (onAccessibilityEventListener != null) {
            onAccessibilityEventListener.onWindowContentChanged(event);
        }
    }

    @Override
    public void onInterrupt() {
    }
}
