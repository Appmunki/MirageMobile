/*
 * TargetImage.cpp
 *
 *  Created on: Jan 28, 2013
 *      Author: radzell
 */

#include "Pattern.hpp"
#include "Utils.h"
Pattern::Pattern()
{
  // TODO Auto-generated constructor stub

}

void PatternTrackingInfo::computePose(const Pattern& pattern, const CameraCalibration& calibration) {
  cv::Mat Rvec;
  cv::Mat_<float> Tvec;
  cv::Mat raux, taux;
  cv::solvePnP(pattern.points3d, points2d, calibration.getIntrinsic(), calibration.getDistorsion(), raux, taux);
  raux.convertTo(Rvec, CV_32F);
  taux.convertTo(Tvec, CV_32F);

  cv::Mat_<float> rotMat(3, 3);
  cv::Rodrigues(Rvec, rotMat);

  // Copy to transformation matrix
  for (int col = 0; col < 3; col++) {
    for (int row = 0; row < 3; row++) {
      pose3d.r().mat[row][col] = rotMat(row, col); // Copy rotation component
    }
    pose3d.t().data[col] = Tvec(col); // Copy translation component
  }

  // Since solvePnP finds camera location, w.r.t to marker pose, to get marker pose w.r.t to the camera we invert it.
  pose3d = pose3d.getInverted();

}

void PatternTrackingInfo::draw2dContour(cv::Mat& image, cv::Scalar color) const {
  std::vector<cv::Scalar>  colors;
  colors.push_back(cv::Scalar(0,0,255,255));
  colors.push_back(cv::Scalar(0,255,0,255));
  colors.push_back(cv::Scalar(255,0,0,255));
  colors.push_back(cv::Scalar(0,255,255,255));

  for (size_t i = 0; i < points2d.size(); i++) {
    //LOG("draw2dContour points %d: (%d,%d)",i,i,(i + 1) % points2d.size());
    //LOG("draw2dContour line %d: (%d,%d) to (%d,%d)",i,points2d[i].x,points2d[i].y, points2d[(i + 1) % points2d.size()].x,points2d[(i + 1) % points2d.size() ].y);
    cv::circle(image, points2d[i], 10,colors[i], 2);

    cv::line(image, points2d[i], points2d[(i + 1) % points2d.size()], color, 2, CV_AA);
  }
}





