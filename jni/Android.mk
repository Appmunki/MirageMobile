LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
#opencv
#OPENCV_LIB_TYPE:=STATIC
OPENCV_INSTALL_MODULES:=on
include /home/diego/OpenCV-2.4.3.2-android-sdk/sdk/native/jni/OpenCV.mk



LOCAL_MODULE    := MirageMobile
LOCAL_SRC_FILES := MirageMobile.cpp
LOCAL_LDLIBS    += -landroid -llog -ldl

include $(BUILD_SHARED_LIBRARY)
