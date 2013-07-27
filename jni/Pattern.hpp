/*
 * TargetImage.h
 *
 *  Created on: Jan 28, 2013
 *      Author: radzell
 */
#include "CameraCalibration.hpp"
#include "GeometryTypes.hpp"
#include "Utils.h"
#include <opencv2/opencv.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/features2d/features2d.hpp>

using namespace cv;

#ifndef PATTERN_H_
#define PATTERN_H_

/*
 * Store the image data
 */
struct Pattern
{

  Pattern(int pID,Mat& pDesc, vector<KeyPoint> &pKeys,Size& pSize){
    ID=pID;
    descriptor=pDesc;
    keypoints=pKeys;
    size = pSize;

    //LOG("size width %d height %d",this->size.width,this->size.height);
    //LOG("pSize width %d height %d",pSize.width,pSize.height);

    // Image dimensions
    const float w = size.width;
    const float h = 500;

     LOG("w %d h %d",pSize.width,pSize.height);

    // Normalized dimensions:
    const float maxSize = std::max(w,h);
    const float unitW = w / maxSize;
    const float unitH = h / maxSize;


    // Build 2d and 3d contours (3d contour lie in XY plane since it's planar)
    points2d.resize(4);
    points3d.resize(4);

    points2d[0] = cv::Point2f(0,0);
    points2d[1] = cv::Point2f(w,0);
    points2d[2] = cv::Point2f(w,h);
    points2d[3] = cv::Point2f(0,h);

    points3d[0] = cv::Point3f(-unitW, -unitH, 0);
    points3d[1] = cv::Point3f( unitW, -unitH, 0);
    points3d[2] = cv::Point3f( unitW,  unitH, 0);
    points3d[3] = cv::Point3f(-unitW,  unitH, 0);
  }
  Pattern();

  int ID;
  vector<cv::KeyPoint> keypoints;
  Mat descriptor;
  Size size;
  std::vector<cv::Point2f>  points2d;
  std::vector<cv::Point3f>  points3d;
  Mat frame;
  Mat gray;
};

/**
 * Store the patterntracking info
 */
struct PatternTrackingInfo
{
  cv::Mat                   homography;
  std::vector<cv::Point2f>  points2d;
  Transformation            pose3d;

  void draw2dContour(cv::Mat& image, cv::Scalar color) const;

  /**
   * Compute pattern pose using PnP algorithm
   */
  void computePose(const Pattern& pattern, const CameraCalibration& calibration);
};
#endif /* PATTERN_H_ */
