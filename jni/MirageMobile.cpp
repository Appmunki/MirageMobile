#include <jni.h>
#include <android/log.h>
#include <stdio.h>
#include <string.h>
#include <assert.h>

#include <opencv2/opencv.hpp>
#include <opencv2/features2d/features2d.hpp>

#include <Utils.h>
#ifdef __cplusplus
extern "C" {
#endif

  JNIEXPORT void JNICALL
  Java_com_appmunki_miragemobile_ar_Matcher_fetch(JNIEnv *, jobject){
    LOG("Fetch");
  }
  JNIEXPORT void JNICALL
  Java_com_appmunki_miragemobile_ar_Matcher_match(JNIEnv *, jobject){
     // LOG("Match");
  }





#ifdef __cplusplus
}
#endif
