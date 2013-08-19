LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
#opencv
#OPENCV_LIB_TYPE:=STATIC
OPENCV_INSTALL_MODULES:=on
include $(OPENCV)/OpenCV.mk 
#/home/diego/OpenCV-2.4.4-android-sdk/sdk/native/jni/OpenCV.mk

ifdef DEBUG
CONFIG_DIR  := Debug
LOCAL_CFLAGS    := -Werror -Wno-psabi -O0 -ggdb -D_DEBUG
LOCAL_CXXFLAGS    := -Werror -Wno-psabi -O0 -ggdb -D_DEBUG -fexceptions
LOCAL_LINK_FLAGS    := -ggdb
else
CONFIG_DIR  := Release
LOCAL_CFLAGS    := -Werror -Wno-psabi -O2 -DNDEBUG
LOCAL_CXXFLAGS  := -Werror -Wno-psabi -O2 -DNDEBUG -fexceptions
endif


LOCAL_MODULE    := MirageMobile
LOCAL_SRC_FILES := MirageMobile.cpp
LOCAL_LDLIBS    += -landroid -llog -ldl  -lz

include $(BUILD_SHARED_LIBRARY)
