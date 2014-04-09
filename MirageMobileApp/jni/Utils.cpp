/*
 * Utils.cpp
 *
 *  Created on: Jan 8, 2013
 *      Author: radzell
 */

#include "Utils.h"


namespace Utils
{
  std::string
    type2str(const int type)
    {
      std::string r;

      uchar depth = type & CV_MAT_DEPTH_MASK;
      uchar chans = 1 + (type >> CV_CN_SHIFT);

      switch (depth)
      {
      case CV_8U:
        r = "8U";
        break;
      case CV_8S:
        r = "8S";
        break;
      case CV_16U:
        r = "16U";
        break;
      case CV_16S:
        r = "16S";
        break;
      case CV_32S:
        r = "32S";
        break;
      case CV_32F:
        r = "32F";
        break;
      case CV_64F:
        r = "64F";
        break;
      default:
        r = "User";
        break;
      }

      r += "C";
      r += (chans + '0');

      return r;
    }
  void dispMat(const cv::Mat *N, const std::string varName)
  {
    double *data = (double*) N->data;
    int rowNum = N->rows;
    int colNum = N->cols;

    printf("\n========= Matrix Display System ==============\n");
    printf("(%d,%d) %s \n", rowNum, colNum, varName.c_str());
    for (int i = 0; i < rowNum; i++)
      {
        printf("%.3f %.3f %.3f", N->at<double>(i, 0), N->at<double>(i, 1),
            N->at<double>(i, 2));
      }
    printf("=================================================");
    printf("\n");
  }
}

