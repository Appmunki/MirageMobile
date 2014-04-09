/*
 * Utils.h
 *
 *  Created on: Jan 8, 2013
 *      Author: radzell
 */

#ifndef UTILS_H_
#define UTILS_H_

#include <stdio.h>
#include <android/log.h>
#include <string.h>
#include <opencv2/opencv.hpp>
#include <opencv2/core/core.hpp>


// Utility for logging:
#define LOG_TAG    "MIRAGE_NATIVE"
#define LOG_TAG_QR    "QRCODE"
#define LOG(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGQR(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG_QR, __VA_ARGS__)
#define printf(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__);


namespace Utils
{
  void function1();
  std::string type2str(const int type);

  void dispMat(const cv::Mat *N, const std::string varName);
}

#endif /* UTILS_H_ */
