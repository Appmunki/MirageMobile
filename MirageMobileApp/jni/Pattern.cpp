/*
 * TargetImage.cpp
 *
 *  Created on: Jan 28, 2013
 *      Author: radzell
 */

#include "Pattern.hpp"
#include "Utils.h"

void extractFeatures(const Mat& img, Mat& des, vector<KeyPoint>& keys);

Pattern::Pattern(const cv::Mat& frame, bool pScene)
{

  isScene = pScene;
  if (isScene)
    {
      image = frame;
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

/**
 * Extracts features from a image
 **/
void
Pattern::extractFeatures(const Mat& img, Mat& des, vector<KeyPoint>& keys)
{
  // detect image keypoints
  struct timespec tstart =
    { 0, 0 }, tend =
    { 0, 0 };
  clock_gettime(CLOCK_MONOTONIC, &tstart);
  cv::Ptr < FeatureDetector > sfd1;
  cv::Ptr < cv::DescriptorExtractor > sde;
  if (isScene)
    {
      sfd1 = sceneDetector;
      sde = sceneExtractor;

    }
  else
    {
      sfd1 = dbDetector;
      sde = dbExtractor;

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
      ((double )tend.tv_sec + 1.0e-9 * tend.tv_nsec)
          - ((double )tstart.tv_sec + 1.0e-9 * tstart.tv_nsec));

}
void
PatternTrackingInfo::computePose(const Pattern* pattern,
    const CameraCalibration& calibration)
{
  cv::Mat Rvec;
  cv::Mat_<float> Tvec;
  cv::Mat raux, taux;
  cv::solvePnP(pattern->points3d, points2d, calibration.getIntrinsic(),
      calibration.getDistorsion(), raux, taux);
  raux.convertTo(Rvec, CV_32F);
  taux.convertTo(Tvec, CV_32F);

  cv::Mat_<float> rotMat(3, 3);
  cv::Rodrigues(Rvec, rotMat);

  // Copy to transformation matrix
  for (int col = 0; col < 3; col++)
    {
      for (int row = 0; row < 3; row++)
        {
          pose3d.r().mat[row][col] = rotMat(row, col); // Copy rotation component
        }
      pose3d.t().data[col] = Tvec(col); // Copy translation component
    }

  // Since solvePnP finds camera location, w.r.t to marker pose, to get marker pose w.r.t to the camera we invert it.
  pose3d = pose3d.getInverted();

}

void
PatternTrackingInfo::draw2dContour(cv::Mat& image, cv::Scalar color) const
{
  std::vector < cv::Scalar > colors;
  colors.push_back(cv::Scalar(0, 0, 255, 255));
  colors.push_back(cv::Scalar(0, 255, 0, 255));
  colors.push_back(cv::Scalar(255, 0, 0, 255));
  colors.push_back(cv::Scalar(0, 255, 255, 255));

  for (size_t i = 0; i < points2d.size(); i++)
    {
      //LOG("draw2dContour points %d: (%d,%d)",i,i,(i + 1) % points2d.size());
      //LOG("draw2dContour line %d: (%d,%d) to (%d,%d)",i,points2d[i].x,points2d[i].y, points2d[(i + 1) % points2d.size()].x,points2d[(i + 1) % points2d.size() ].y);
      cv::circle(image, points2d[i], 10, colors[i], 2);

      cv::line(image, points2d[i], points2d[(i + 1) % points2d.size()], color,
          2, CV_AA);
    }
}

