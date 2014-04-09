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

class Pattern
{
public:
  Pattern();
  Pattern(const cv::Mat& frame,bool pScene=false);
  ~Pattern(){LOGW("Pattern is being destroyed");};
  void extractFeatures(const cv::Mat& img, cv::Mat& des, vector<cv::KeyPoint>& keys);

  vector<cv::KeyPoint> keypoints;
  Mat descriptor;
  Size size;
  std::vector<cv::Point2f>  points2d;
  std::vector<cv::Point3f>  points3d;
  Mat image;
  bool isScene;
};

/**
 * Store the patterntracking info
 */
class PatternTrackingInfo
{
public:
  cv::Mat                   homography;
  cv::Mat                   scaledhomography;
  cv::Mat                   scaledrotatedhomography;


  std::vector<cv::Point2f>  points2d;
  std::vector<cv::Point2f>  scaledpoints2d;
  std::vector<cv::Point2f>  scaledrotatedpoints2d;

  Transformation            pose3d;

  void draw2dContour(cv::Mat& image, cv::Scalar color) const;

  /**
   * Compute pattern pose using PnP algorithm
   */
  void computePose(const Pattern* pattern, const CameraCalibration& calibration);

  ~PatternTrackingInfo(){LOGW("PatternTrackingInfo is being destroyed");};

};
#endif /* PATTERN_H_ */
