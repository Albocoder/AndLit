LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := app
LOCAL_LDFLAGS := -Wl,--build-id
LOCAL_SRC_FILES := \
	$LOCAL_PATH/src/main/jniLibs/arm64-v8a/libopencv_java3.so \
	$LOCAL_PATH/src/main/jniLibs/armeabi-v7a/libopencv_java3.so \
	$LOCAL_PATH/src/main/jniLibs/x86_64/libopencv_java3.so \
	$LOCAL_PATH/src/main/jniLibs/x86/libopencv_java3.so \

LOCAL_C_INCLUDES += $LOCAL_PATH/src/main/jniLibs

include $(BUILD_SHARED_LIBRARY)
