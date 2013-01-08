LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
#opencv
OPENCV_LIB_TYPE:=STATIC
OPENCV_INSTALL_MODULES:=off
OPENCV_CAMERA_MODULES:=on
include /host/Development/OpenCV-2.4.3.2-android-sdk/sdk/native/jni/OpenCV.mk



LOCAL_MODULE    := MirageMobile
LOCAL_SRC_FILES := MirageMobile.cpp
LOCAL_LDLIBS    += -landroid -llog -ldl

include $(BUILD_SHARED_LIBRARY)
