MY_DIR := $(call my-dir)

LOCAL_PATH := $(MY_DIR)/zinnia/zinnia
include $(CLEAR_VARS)
LOCAL_MODULE := zinnia
LOCAL_SRC_FILES := feature.h param.cpp recognizer.cpp stream_wrapper.h \
                      svm.cpp sexp.cpp sexp.h zinnia.h feature.cpp libzinnia.cpp  mmap.h common.h \
		      param.h freelist.h scoped_ptr.h character.cpp svm.h  trainer.cpp
LOCAL_CFLAGS := -DHAVE_CONFIG_H
LOCAL_C_INCLUDES := $(MY_DIR)
include $(BUILD_STATIC_LIBRARY)

LOCAL_PATH := $(MY_DIR)
include $(CLEAR_VARS)
LOCAL_MODULE := zinnia-jni
LOCAL_SRC_FILES := zinniajni.cpp
LOCAL_STATIC_LIBRARIES := zinnia
include $(BUILD_SHARED_LIBRARY)
