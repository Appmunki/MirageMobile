/*
 * Utils.h
 *
 *  Created on: Jan 8, 2013
 *      Author: radzell
 */

#ifndef UTILS_H_
#define UTILS_H_


#include <android/log.h>

// Utility for logging:
#define LOG_TAG    "MIRAGE_NATIVE"
#define LOG_TAG_QR    "QRCODE"
#define LOG(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGQR(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG_QR, __VA_ARGS__)
class Utils {
public:
	Utils();
	virtual
	~Utils();
};

#endif /* UTILS_H_ */
