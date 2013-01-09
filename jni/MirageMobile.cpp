#include <jni.h>
#include <android/log.h>
#include <stdio.h>
#include <string.h>
#include <assert.h>

#include <opencv2/opencv.hpp>
#include <opencv2/features2d/features2d.hpp>

#ifdef __cplusplus
extern "C" {
#endif
  JNIEXPORT void JNICALL
  java_com_appmunki_miragemobile_ar_Matcher_fetch(JNIEnv *, jobject){
      printf("Fetch");
  }


#ifdef __cplusplus
}
#endif
