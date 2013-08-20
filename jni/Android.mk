LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
#opencv
OPENCV_LIB_TYPE:=STATIC
OPENCV_INSTALL_MODULES:=on
include $(OPENCV)/OpenCV.mk

#ifdef DEBUG
#CONFIG_DIR  := Debug
#LOCAL_CFLAGS    := -Werror -Wno-psabi -O0 -ggdb -D_DEBUG
#LOCAL_CXXFLAGS    := -Werror -Wno-psabi -O0 -ggdb -D_DEBUG -fexceptions
#LOCAL_LINK_FLAGS    := -ggdb
#else
#CONFIG_DIR  := Release
#LOCAL_CFLAGS    := -Werror -Wno-psabi -O2 -DNDEBUG
#LOCAL_CXXFLAGS  := -Werror -Wno-psabi -O2 -DNDEBUG -fexceptions
#endif


LOCAL_MODULE    := MirageMobile
LOCAL_SRC_FILES := Utils.cpp MirageMobile.cpp GeometryTypes.cpp ARPipeline.cpp CameraCalibration.cpp Pattern.cpp PatternDetector.cpp
#ARPipeline.cpp CameraCalibration.cpp GeometryTypes.cpp Pattern.cpp PatternDetector.cpp Utils.cpp
LOCAL_LDLIBS    += -llog -ldl -landroid

include $(BUILD_SHARED_LIBRARY)
