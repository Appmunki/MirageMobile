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
static cv::Ptr<cv::FeatureDetector> dbDetector = new cv::OrbFeatureDetector(300);
static cv::Ptr<cv::DescriptorExtractor> dbExtractor= new cv::BRISK();


static cv::Ptr<cv::FeatureDetector> sceneDetector = new cv::OrbFeatureDetector(500);
static cv::Ptr<cv::DescriptorExtractor> sceneExtractor= new cv::BRISK();

struct Pattern
{

  Pattern(const cv::Mat& frame,bool pScene=false){

    isScene=pScene;
    if(isScene){
      gray=frame;
    }
    // Store original image in pattern structure
    size = cv::Size(frame.cols, frame.rows);

    // Build 2d and 3d contours (3d contour lie in XY plane since it's planar)
    points2d.resize(4);
    points3d.resize(4);

    // Image dimensions
    const float w = frame.cols;
    const float h = frame.rows;

    // Normalized dimensions:
    const float maxSize = std::max(w, h);
    const float unitW = w / maxSize;
    const float unitH = h / maxSize;


    points2d[0] = cv::Point(0, 0);
    points2d[1] = cv::Point(w, 0);
    points2d[2] = cv::Point(w, h);
    points2d[3] = cv::Point(0, h);

    points3d[0] = cv::Point3f(-unitW, -unitH, 0);
    points3d[1] = cv::Point3f(unitW, -unitH, 0);
    points3d[2] = cv::Point3f(unitW, unitH, 0);
    points3d[3] = cv::Point3f(-unitW, unitH, 0);

    extractFeatures(frame, descriptor, keypoints);
  }
  Pattern();
  ~Pattern(){LOGE("Pattern is being destroyed");};

  /**
  * Extracts features from a image
  **/
  void extractFeatures(const Mat& img, Mat& des, vector<KeyPoint>& keys)
  {
    // detect image keypoints
    struct timespec tstart={0,0}, tend={0,0};
    clock_gettime(CLOCK_MONOTONIC, &tstart);
    cv::Ptr<FeatureDetector> sfd1;
    cv::Ptr<cv::DescriptorExtractor> sde;
    if(isScene){
        sfd1=sceneDetector;
        sde=sceneExtractor;

    }else{
        sfd1=dbDetector;
        sde=dbExtractor;

    }

    clock_gettime(CLOCK_MONOTONIC, &tend);
//    LOG("Creation took %.5f seconds\n",
//                              ((double)tend.tv_sec + 1.0e-9*tend.tv_nsec) -
//                              ((double)tstart.tv_sec + 1.0e-9*tstart.tv_nsec));
    clock_gettime(CLOCK_MONOTONIC, &tstart);
    sfd1->detect(img, keys);
    clock_gettime(CLOCK_MONOTONIC, &tend);
//    LOG("Detection took %.5f seconds\n",
//                                  ((double)tend.tv_sec + 1.0e-9*tend.tv_nsec) -
//                                  ((double)tstart.tv_sec + 1.0e-9*tstart.tv_nsec));
    clock_gettime(CLOCK_MONOTONIC, &tstart);
    // compute image descriptor
    sde->compute(img, keys, des);
    clock_gettime(CLOCK_MONOTONIC, &tend);
    LOG("Extraction took %.5f seconds\n",
                                      ((double)tend.tv_sec + 1.0e-9*tend.tv_nsec) -
                                      ((double)tstart.tv_sec + 1.0e-9*tstart.tv_nsec));

  }
  vector<cv::KeyPoint> keypoints;
  Mat descriptor;
  Size size;
  std::vector<cv::Point2f>  points2d;
  std::vector<cv::Point3f>  points3d;
  Mat gray;
  bool isScene;
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

  ~PatternTrackingInfo(){LOGE("PatternTrackingInfo is being destroyed");};

};
#endif /* PATTERN_H_ */
