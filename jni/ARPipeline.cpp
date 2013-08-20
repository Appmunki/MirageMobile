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

////////////////////////////////////////////////////////////////////
// File includes:
#include "ARPipeline.hpp"

ARPipeline::ARPipeline() {
	elapsedTime = 0;
}



void ARPipeline::startTimer() {
	gettimeofday(&t1, NULL);
}

void ARPipeline::stopTimer() {
	gettimeofday(&t2, NULL);

	// compute and print the elapsed time in millisec
	elapsedTime = (t2.tv_sec - t1.tv_sec) * 1000.0;      // sec to ms
	elapsedTime += (t2.tv_usec - t1.tv_usec) / 1000.0;   // us to ms
	LOG("elapsedTime %f ms.\n", elapsedTime);
}

ARPipeline::ARPipeline(const cv::Mat& patternImage, const CameraCalibration& calibration) :
		m_calibration(calibration) {
	m_patternDetector.buildPatternFromImage(patternImage, m_pattern);
	m_patternDetector.train(m_pattern);
}

bool ARPipeline::processFrame(const cv::Mat& inputFrame) {
	bool patternFound = m_patternDetector.findPattern(inputFrame, m_patternInfo);
	if (patternFound) {
		m_patternInfo.computePose(m_pattern, m_calibration);
	}

	return patternFound;
}

const Transformation& ARPipeline::getPatternLocation() const {
	return m_patternInfo.pose3d;
}
