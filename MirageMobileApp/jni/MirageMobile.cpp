#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>
#include <string.h>
#include <assert.h>
#include <cstdio>
#include <cstdlib>
#include <iostream>
#include <memory>
#include <unordered_map>

#include "Pattern.hpp"
#include "CameraCalibration.hpp"
#include "Utils.h"

#include <opencv2/opencv.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/legacy/legacy.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/calib3d/calib3d.hpp>
#include <opencv2/imgproc/imgproc.hpp>

using namespace std;
using namespace cv;

#ifdef __cplusplus
extern "C"
{
#endif
  typedef std::shared_ptr<Pattern> PatternPtr;
  typedef std::shared_ptr<PatternTrackingInfo> PatternTrackingInfoPtr;

  //Global variables
  static vector<Pattern*> patterns;
  static CameraCalibration m_calibration = CameraCalibration(786.42938232f,
      786.42938232f, 217.01358032f, 311.25384521f);

  static cv::Ptr<cv::DescriptorMatcher> matcher_ = new cv::FlannBasedMatcher(
      new cv::flann::LshIndexParams(5, 10, 1));
  static bool isPatternPresent = false;
  static Matrix44 glProjectionMatrix;
  static vector<PatternTrackingInfoPtr> patternResults;

  static Mat mProjectionMatrix;

  //function prototypes
  void
  dbMatcher(Pattern* scenePattern, vector<Pattern*>& matchresults);
  void
  computeTracker(Pattern* scenePattern, vector<Pattern*>& resultsPatterns,
      vector<PatternTrackingInfoPtr>& results);
  void
  buildProjectionMatrix(int screen_width, int screen_height,
      Matrix44& projectionMatrix);

  void
  filterMatches(Pattern* objPattern, const Pattern* scenePattern,
      const std::pair<vector<int>, vector<int> > & id_match,
      vector<Pattern*>& matchresults)
  {
    if (id_match.first.size() < 4)
      return;

    //-- Localize the object
    std::vector < Point2f > objPoints;
    std::vector < Point2f > scenePoints;
    KeyPoint::convert(objPattern->keypoints, objPoints, id_match.first);
    KeyPoint::convert(scenePattern->keypoints, scenePoints, id_match.second);

    // Find homography matrix and get inliers mask
    std::vector<unsigned char> inliersMask(id_match.first.size());
    Mat homography = cv::findHomography(objPoints, scenePoints, CV_RANSAC, 3.0f,
        inliersMask);
    int inliersum = std::accumulate(inliersMask.begin(), inliersMask.end(), 0);

    LOG("inlier sum: %d\n", inliersum);
    if (inliersum > 4)
      {
        matchresults.push_back(objPattern);
      }
  }
  void
  filterMatchesbyId(const Pattern* scenePattern,
      std::vector<std::pair<vector<int>, vector<int> > >& matches_by_id)
  {
    vector < vector<DMatch> > knnmatches;
    matcher_->knnMatch(scenePattern->descriptor, knnmatches, 2);
    LOG("%d KNN Matches\n", (int )knnmatches.size());
    for (size_t i = 0; i < knnmatches.size(); i++)
      {

        DMatch* _m;
        if (knnmatches[i].size() == 1)
          {
            _m = &knnmatches[i][0]; // only one neighbor
          }
        else if (knnmatches[i].size() > 1)
          {
            // 2 neighbors â check how close they are
            double ratio = knnmatches[i][0].distance
                / knnmatches[i][1].distance;
            if (ratio < 0.7)
              { // not too close
                // take the closest (first) one
                _m = &knnmatches[i][0];
              }
            else
              { // too close â we cannot tell which is better
                continue; // did not pass ratio test â throw away
              }
          }
        else
          {
            continue; // no neighbors... :(
          }

        matches_by_id[_m->imgIdx].first.push_back(_m->queryIdx);
        matches_by_id[_m->imgIdx].second.push_back(_m->trainIdx);

      }
  }
  /**
   * Runs matching code for a frame against the pattern index
   * @param patternScene camera frame being matched to
   * @param resulting patterns found in the scene.
   */
  void
  dbMatcher(Pattern* scenePattern, vector<Pattern*>& matchresults)
  {
    LOG("-------------------testMatcher---------------");
    std::vector < std::pair<vector<int>, vector<int> >
        > matches_by_id(patterns.size());

   filterMatchesbyId(scenePattern,matches_by_id);

    for (unsigned int i = 0; i < matches_by_id.size(); i++)
      {
        Pattern * pattern = patterns[i];
        filterMatches(pattern, scenePattern, matches_by_id[i], matchresults);
      }
    LOG("Prediction %d", matchresults.size());
    LOG("-------------------Database Matcher End---------------");

  }

  bool
  computeHomography(const Pattern* objPattern, const Pattern* scenePattern,
      Mat& homography)
  {
    //-- Step 3: Matching descriptor vectors using FLANN matcher
    std::vector < DMatch > matches;
    matcher_->match(objPattern->descriptor, scenePattern->descriptor, matches);

    double max_dist = 0;
    double min_dist = 100;

    //-- Quick calculation of max and min distances between keypoints
    for (int i = 0; i < objPattern->descriptor.rows; i++)
      {
        double dist = matches[i].distance;
        if (dist < min_dist)
          min_dist = dist;
        if (dist > max_dist)
          max_dist = dist;
      }
    printf("Matches: %d", matches.size());
    printf("-- Max dist : %f \n", max_dist);
    printf("-- Min dist : %f \n", min_dist);

    //-- Save only "good" matches (i.e. whose distance is less than 3*min_dist )

    printf("-- Matches: %d \n", matches.size());

    vector<int> queryIdxs, trainIdxs;
    queryIdxs.reserve(matches.size());
    trainIdxs.reserve(matches.size());
    for (int i = 0; i < matches.size(); i++)
      {
        if (matches[i].distance < 3 * min_dist)
          {
            queryIdxs.push_back(matches[i].queryIdx);
            trainIdxs.push_back(matches[i].trainIdx);

          }
      }
    printf("-- New Matches: %d \n", matches.size());

    if (queryIdxs.size() < 4)
      {
        return false;
      }

    //-- Localize the object
    std::vector < Point2f > objPoints;
    std::vector < Point2f > scenePoints;
    objPoints.reserve((matches.size()));
    scenePoints.reserve(matches.size());

    KeyPoint::convert(objPattern->keypoints, objPoints, queryIdxs);
    KeyPoint::convert(scenePattern->keypoints, scenePoints, trainIdxs);

    printf("-- Obj Points: %d \n", objPoints.size());
    printf("-- Scene Points: %d \n", scenePoints.size());

    homography = findHomography(objPoints, scenePoints, CV_RANSAC, 4.0f);
    return true;
  }
  /**
   * Computes the homography and the tracking information for a specific element.
   * @param patternScene camera frame being tracked
   * @param list of patterns found in scene
   * @param Tracking information for patterns found in the scene
   */
  void
  computeTracker(Pattern* scenePattern, vector<Pattern*>& resultsPatterns,
      vector<PatternTrackingInfoPtr>& results)
  {

    LOG("------------Computing Tracker-----------");

    for (int j = 0; j < resultsPatterns.size(); j++)
      {
        Pattern* pattern = resultsPatterns[j];
        PatternTrackingInfoPtr info(new PatternTrackingInfo());
        Mat roughhomography;
        if (!computeHomography(pattern, scenePattern, roughhomography))
          return;

        bool refinehomography = true;
        info->homography = roughhomography;

        if (refinehomography)
          {
            LOG("Refining Matches");
            Mat warpedImg;
            cv::warpPerspective(scenePattern->image, warpedImg,
                info->homography, pattern->size,
                cv::WARP_INVERSE_MAP | cv::INTER_CUBIC);

            Pattern* warpPattern = new Pattern(warpedImg);
            Mat refinehomography;
            if (computeHomography(pattern, warpPattern, refinehomography))
            {
                info->homography *= refinehomography;
            }
            delete warpPattern;
          }
        double scale_x = 1.85;
        double scale_y = 1.5;
        Mat S = Mat::ones(3, 3, CV_64F);

        S.at<double>(0, 0) = scale_x;
        S.at<double>(1, 1) = scale_y;

        double Ax = info->homography.at<double>(0, 2);
        double Ay = info->homography.at<double>(1, 2);
        double Px = info->homography.at<double>(2, 0);
        double Py = info->homography.at<double>(2, 1);
        double Axnew = scale_x * (Ax - Px) + Px;
        double Aynew = scale_y * (Ay - Py) + Py;

        info->scaledhomography = info->homography.mul(S);
        info->scaledhomography.at<double>(0, 2) = Axnew;
        info->scaledhomography.at<double>(1, 2) = Aynew;

        info->scaledhomography.copyTo(info->scaledrotatedhomography);

        double rotation00 = acos(
            info->scaledhomography.at<double>(0, 0) / scale_x);
        double rotation10 = asin(info->scaledhomography.at<double>(1, 0));
        double rotation01 = asin(-1 * info->scaledhomography.at<double>(0, 1));
        double rotation11 = acos(
            info->scaledhomography.at<double>(1, 1) / scale_y);

        /*printf("rotation00 degrees %.3f, %.3f", rotation00 * 180 / CV_PI,
         info->scaledhomography.at<double>(0, 0) / scale_x);
         printf("rotation10 degrees %.3f", rotation10 * 180 / CV_PI);
         printf("rotation01 degrees %.3f", rotation01 * 180 / CV_PI);
         printf("rotation11 degrees %.3f, %.3f", rotation11 * 180 / CV_PI,
         info->scaledhomography.at<double>(1, 1) / scale_y);*/

        rotation00 += (CV_PI / 2.0);
        rotation10 += (CV_PI / 2.0);
        rotation01 += (CV_PI / 2.0);
        rotation11 += (CV_PI / 2.0);

        //printf("new rotation00 degrees %.3f", rotation00 * 180 / CV_PI);
        // printf("new rotation10 degrees %.3f", rotation10 * 180 / CV_PI);
        // printf("new rotation01 degrees %.3f", rotation01 * 180 / CV_PI);
        // printf("new rotation11 degrees %.3f", rotation11 * 180 / CV_PI);
        double rotationx = info->scaledhomography.at<double>(0, 2);
        double rotationy = info->scaledrotatedhomography.at<double>(1, 2);



        info->scaledrotatedhomography.at<double>(0, 0) = scale_x
            * cos(rotation00);
        info->scaledrotatedhomography.at<double>(0, 1) = -1 * sin(rotation01);
        info->scaledrotatedhomography.at<double>(0, 2) =
            info->scaledhomography.at<double>(0, 2);
        info->scaledrotatedhomography.at<double>(1, 0) = sin(rotation10);
        info->scaledrotatedhomography.at<double>(1, 1) = scale_y
            * cos(rotation11);
        info->scaledrotatedhomography.at<double>(1, 2) =
            info->scaledhomography.at<double>(1, 2) + 5;
        info->scaledrotatedhomography.at<double>(2, 0) =
            info->scaledhomography.at<double>(2, 0);
        info->scaledrotatedhomography.at<double>(2, 1) =
            info->scaledhomography.at<double>(2, 1);
        info->scaledrotatedhomography.at<double>(2, 2) =
            info->scaledhomography.at<double>(2, 2);

        //Utils::dispMat(&info->homography, "H");
        //Utils::dispMat(&info->scaledhomography, "scaledrotatedH");
        //Utils::dispMat(&info->scaledrotatedhomography, "scaledH");

        double rotation1 = asin(info->homography.at<double>(1, 0));
        double rotation2 = asin(info->scaledhomography.at<double>(1, 0));
        double rotation3 = asin(info->scaledrotatedhomography.at<double>(1, 0));

        /*printf("rotation degrees %.3f", rotation1 * 180 / CV_PI);
         printf("scaled rotation degrees %.3f", rotation2 * 180 / CV_PI);
         printf("scaled rotated rotation degrees %.3f", rotation3 * 180 / CV_PI);*/

        cv::perspectiveTransform(pattern->points2d, info->points2d,
            info->homography);
        cv::perspectiveTransform(pattern->points2d, info->scaledpoints2d,
            info->scaledhomography);
        cv::perspectiveTransform(pattern->points2d, info->scaledrotatedpoints2d,
            info->scaledrotatedhomography);

        info->computePose(pattern, m_calibration);

        results.push_back(info);

      }
    LOG("------------End Computing Tracker-----------");

  }
  void
  buildProjectionMatrix(int screen_width, int screen_height,
      Matrix44& projectionMatrix)
  {
    float nearPlane = 0.01f; // Near clipping distance
    float farPlane = 100.0f; // Far clipping distance

    m_calibration = CameraCalibration(786.42938232f, 786.42938232f,
        217.01358032f, 311.25384521f);

    // Camera parameters
    float f_x = m_calibration.fx(); // Focal length in x axis
    float f_y = m_calibration.fy(); // Focal length in y axis (usually the same?)
    float c_x = m_calibration.cx(); // Camera primary point x
    float c_y = m_calibration.cy(); // Camera primary point y

    LOG("build %f,%f,%f,%f", f_x, f_y, c_x, c_y);
    projectionMatrix.data[0] = -2.0f * f_x / screen_width;
    projectionMatrix.data[1] = 0.0f;
    projectionMatrix.data[2] = 0.0f;
    projectionMatrix.data[3] = 0.0f;

    projectionMatrix.data[4] = 0.0f;
    projectionMatrix.data[5] = 2.0f * f_y / screen_height;
    projectionMatrix.data[6] = 0.0f;
    projectionMatrix.data[7] = 0.0f;

    projectionMatrix.data[8] = 2.0f * c_x / screen_width - 1.0f;
    projectionMatrix.data[9] = 2.0f * c_y / screen_height - 1.0f;
    projectionMatrix.data[10] = -(farPlane + nearPlane)
        / (farPlane - nearPlane);
    projectionMatrix.data[11] = -1.0f;

    projectionMatrix.data[12] = 0.0f;
    projectionMatrix.data[13] = 0.0f;
    projectionMatrix.data[14] = -2.0f * farPlane * nearPlane
        / (farPlane - nearPlane);
    projectionMatrix.data[15] = 0.0f;

  }
  JNIEXPORT void JNICALL
  Java_com_appmunki_miragemobile_ar_Matcher_addPattern(JNIEnv* env, jobject obj,
      long addrGray)
  {
    Mat& image = *(Mat*) addrGray;
    if (!image.data)
      LOGE("FRAME ERROR");

    printf("adding Pattern");
    Pattern* pattern = new Pattern(image);
    //PatternPtr pattern(new Pattern(mgray));
    patterns.push_back(pattern);

    vector < Mat > descriptors;
    descriptors.push_back(pattern->descriptor);

    matcher_->add(descriptors);
    matcher_->train();

    printf("Pattern size %d",
        (int) patterns.size());

  }

  JNIEXPORT jint JNICALL
  Java_com_appmunki_miragemobile_ar_Matcher_matchDebug(JNIEnv* env, jobject obj,
      long addrGray)
  {
    struct timespec tstart =
      { 0, 0 }, tend =
      { 0, 0 };
    struct timespec totalstart =
      { 0, 0 }, totalend =
      { 0, 0 };
    clock_gettime(CLOCK_MONOTONIC, &totalstart);
    LOG("------------------MatchDebug----------------");

    //Conversion of frame
    Mat& image = *(Mat*) addrGray;
    if (!image.data)
      LOGE("FRAME ERROR");

    // read image from file
    vector<PatternTrackingInfoPtr> trackingResults;
    vector<Pattern*> matchPatternResults;

    //Changed trainkeys to framepattern
    LOG("Scene Extraction begin");
    clock_gettime(CLOCK_MONOTONIC, &tstart);
    Pattern scenePattern(image, true);
    clock_gettime(CLOCK_MONOTONIC, &tend);
    LOG("SeneExtraction took %.5f seconds\n",
        ((double )tend.tv_sec + 1.0e-9 * tend.tv_nsec)
            - ((double )tstart.tv_sec + 1.0e-9 * tstart.tv_nsec));

    //Calls Matching
    clock_gettime(CLOCK_MONOTONIC, &tstart);
    dbMatcher(&scenePattern, matchPatternResults);
    clock_gettime(CLOCK_MONOTONIC, &tend);
    LOG("Matching took %.5f seconds\n",
        ((double )tend.tv_sec + 1.0e-9 * tend.tv_nsec)
            - ((double )tstart.tv_sec + 1.0e-9 * tstart.tv_nsec));

    clock_gettime(CLOCK_MONOTONIC, &tstart);
    computeTracker(&scenePattern, matchPatternResults, trackingResults);
    clock_gettime(CLOCK_MONOTONIC, &tend);
    LOG("ComputingHomography took %.5f seconds\n",
        ((double )tend.tv_sec + 1.0e-9 * tend.tv_nsec)
            - ((double )tstart.tv_sec + 1.0e-9 * tstart.tv_nsec));
    LOG("Results size %d", trackingResults.size());

    !trackingResults.empty() ? isPatternPresent = true : isPatternPresent =
                                   false;

    clock_gettime(CLOCK_MONOTONIC, &totalend);
    LOG("Total took %.5f seconds in total\n",
        ((double )totalend.tv_sec + 1.0e-9 * totalend.tv_nsec)
            - ((double )totalstart.tv_sec + 1.0e-9 * totalstart.tv_nsec));

    std::swap(patternResults,trackingResults);
    return trackingResults.size();
  }
  JNIEXPORT void JNICALL
  Java_com_appmunki_miragemobile_ar_Matcher_debugHomography(JNIEnv* env,
      jobject obj, jint pos, long addrGray)
  {
    PatternTrackingInfoPtr info = patternResults[pos];

    //Conversion of frame
    Mat& mgray = *(Mat*) addrGray;
    if (!mgray.data)
      LOGE("FRAME ERROR");

    //-- Draw lines between the corners (the mapped object in the scene - image_2 )
    line(mgray, info->points2d[0], info->points2d[1], Scalar(0, 255, 0), 4);
    line(mgray, info->points2d[1], info->points2d[2], Scalar(0, 255, 0), 4);
    line(mgray, info->points2d[2], info->points2d[3], Scalar(0, 255, 0), 4);
    line(mgray, info->points2d[3], info->points2d[0], Scalar(0, 255, 0), 4);
    //red
    cv::circle(mgray, info->points2d[0], 10, Scalar(255, 51, 51), 2);
    //blue
    cv::circle(mgray, info->points2d[1], 10, Scalar(51, 255, 255), 2);
    //orange
    cv::circle(mgray, info->points2d[2], 10, Scalar(255, 153, 51), 2);

    //yellow
    cv::circle(mgray, info->points2d[3], 10, Scalar(255, 255, 51), 2);
  }
  JNIEXPORT void JNICALL
  Java_com_appmunki_miragemobile_ar_Matcher_debugScaledHomography(JNIEnv* env,
      jobject obj, jint pos, long addrGray)
  {
    PatternTrackingInfoPtr info = patternResults[pos];

    //Conversion of frame
    Mat& mgray = *(Mat*) addrGray;
    if (!mgray.data)
      LOGE("FRAME ERROR");

    //-- Draw lines between the corners (the mapped object in the scene - image_2 )
    line(mgray, info->scaledpoints2d[0], info->scaledpoints2d[1],
        Scalar(0, 255, 0), 4);
    line(mgray, info->scaledpoints2d[1], info->scaledpoints2d[2],
        Scalar(0, 255, 0), 4);
    line(mgray, info->scaledpoints2d[2], info->scaledpoints2d[3],
        Scalar(0, 255, 0), 4);
    line(mgray, info->scaledpoints2d[3], info->scaledpoints2d[0],
        Scalar(0, 255, 0), 4);
    //red
    cv::circle(mgray, info->scaledpoints2d[0], 10, Scalar(255, 51, 51), 2);
    //blue
    cv::circle(mgray, info->scaledpoints2d[1], 10, Scalar(51, 255, 255), 2);
    //orange
    cv::circle(mgray, info->scaledpoints2d[2], 10, Scalar(255, 153, 51), 2);

    //yellow
    cv::circle(mgray, info->scaledpoints2d[3], 10, Scalar(255, 255, 51), 2);

  }
  JNIEXPORT void JNICALL
  Java_com_appmunki_miragemobile_ar_Matcher_debugScaledRotatedHomography(
      JNIEnv* env, jobject obj, jint pos, long addrGray)
  {
    PatternTrackingInfoPtr info = patternResults[pos];

    //Conversion of frame
    Mat& mgray = *(Mat*) addrGray;
    if (!mgray.data)
      LOGE("FRAME ERROR");

    //-- Draw lines between the corners (the mapped object in the scene - image_2 )
    line(mgray, info->scaledrotatedpoints2d[0], info->scaledrotatedpoints2d[1],
        Scalar(0, 255, 0), 4);
    line(mgray, info->scaledrotatedpoints2d[1], info->scaledrotatedpoints2d[2],
        Scalar(0, 255, 0), 4);
    line(mgray, info->scaledrotatedpoints2d[2], info->scaledrotatedpoints2d[3],
        Scalar(0, 255, 0), 4);
    line(mgray, info->scaledrotatedpoints2d[3], info->scaledrotatedpoints2d[0],
        Scalar(0, 255, 0), 4);
    //red
    cv::circle(mgray, info->scaledrotatedpoints2d[0], 10, Scalar(255, 51, 51),
        2);
    //blue
    cv::circle(mgray, info->scaledrotatedpoints2d[1], 10, Scalar(51, 255, 255),
        2);
    //orange
    cv::circle(mgray, info->scaledrotatedpoints2d[2], 10, Scalar(255, 153, 51),
        2);

    //yellow
    cv::circle(mgray, info->scaledrotatedpoints2d[3], 10, Scalar(255, 255, 51),
        2);
  }
  JNIEXPORT jfloatArray JNICALL
  Java_com_appmunki_miragemobile_ar_Matcher_getHomography(JNIEnv *env,
      jobject obj, jint pos)
  {

    PatternTrackingInfoPtr info = patternResults[pos];
    jfloatArray newArray = env->NewFloatArray(8);
    jfloat *narr = env->GetFloatArrayElements(newArray, NULL);
    if (newArray == NULL)
      {
        LOGE("ERROR GET HOMOGRAPHY");
        return NULL; /* out of memory error thrown */
      }
    int index = 0;
    for (int i = 0; i < 4; i++)
      {
        narr[index] = info->points2d[i].x;
        index++;
        narr[index] = info->points2d[i].y;
        index++;
        LOG("Point %f,%f", info->points2d[i].x, info->points2d[i].y);
      }

    env->ReleaseFloatArrayElements(newArray, narr, 0);
    return newArray;
  }
  JNIEXPORT jint JNICALL
  Java_com_appmunki_miragemobile_ar_Matcher_getNumpatternResults(JNIEnv *env,
      jobject obj)
  {
    return patternResults.size();
  }

  JNIEXPORT jboolean JNICALL
  Java_com_appmunki_miragemobile_ar_Matcher_isPatternPresent(JNIEnv *env,
      jobject obj)
  {
    jboolean isPresent = isPatternPresent;
    return isPatternPresent;
  }

  JNIEXPORT jfloatArray JNICALL
  Java_com_appmunki_miragemobile_ar_Matcher_getMatrix(JNIEnv *env, jobject obj)
  {
    if (patternResults.empty())
      {
        return NULL;
      }
    Matrix44 glTestMatrix = patternResults[0]->pose3d.getMat44();
    jfloatArray result;
    result = env->NewFloatArray(16);
    if (result == NULL)
      {
        return NULL; /* out of memory error thrown */
      }

    jfloat array1[16];

    for (int i = 0; i < 16; ++i)
      {
        array1[i] = glTestMatrix.data[i];
      }

    env->SetFloatArrayRegion(result, 0, 16, array1);

    return result;
  }

  JNIEXPORT jfloatArray JNICALL
  Java_com_appmunki_miragemobile_ar_Matcher_getProjectionMatrix(JNIEnv *env,
      jobject obj, jint screenwidth, jint screenheight)
  {
    m_calibration.getProjectionMatrix(screenwidth, screenheight,
        glProjectionMatrix);

    jfloatArray result;
    result = env->NewFloatArray(16);
    if (result == NULL)
      {
        return NULL; /* out of memory error thrown */
      }

    jfloat array1[16];

    for (int i = 0; i < 16; ++i)
      {
        array1[i] = glProjectionMatrix.data[i];
      }

    env->SetFloatArrayRegion(result, 0, 16, array1);
    return result;
  }

#ifdef __cplusplus
}
#endif
