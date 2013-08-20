/*****************************************************************************
*   Markerless AR desktop application.
******************************************************************************
*   by Khvedchenia Ievgen, 5th Dec 2012
*   http://computer-vision-talks.com
******************************************************************************
*   Ch3 of the book "Mastering OpenCV with Practical Computer Vision Projects"
*   Copyright Packt Publishing 2012.
*   http://www.packtpub.com/cool-projects-with-opencv/book
*****************************************************************************/

#ifndef ARPIPELINE_HPP
#define ARPIPELINE_HPP

////////////////////////////////////////////////////////////////////
// File includes:
#include "PatternDetector.hpp"
#include "CameraCalibration.hpp"
#include "GeometryTypes.hpp"
#include "Utils.h"

class ARPipeline
{
public:



  ARPipeline();

  ARPipeline(const cv::Mat& patternImage, const CameraCalibration& calibration);

  bool processFrame(const cv::Mat& inputFrame);

  const Transformation& getPatternLocation() const;

  void startTimer();
  void stopTimer();

  PatternDetector     m_patternDetector;
  Pattern             m_pattern;
  PatternTrackingInfo m_patternInfo;

  timeval t1, t2;
  double elapsedTime;

private:

private:
  CameraCalibration   m_calibration;
  //Pattern             m_pattern;
  //PatternTrackingInfo m_patternInfo;
  //PatternDetector     m_patternDetector;
};

#endif
