#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>
#include <string.h>
#include <assert.h>
#include <opencv2/opencv.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <cstdio>
#include <cstdlib>
#include <Utils.h>
#include <iostream>

#include "Pattern.hpp"
#include "CameraCalibration.hpp"

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

  //Global variables
  static vector<Pattern> patterns;
  static CameraCalibration m_calibration;
  static cv::Ptr<cv::DescriptorMatcher> matcher_= new cv::FlannBasedMatcher(new cv::flann::LshIndexParams(5, 12, 1));
  static bool isPatternPresent = false;
  static vector<pair<int, Matrix44> > mModelViewMatrixs;
  static Matrix44 glProjectionMatrix;
  static Matrix44 glMatrixTest;


  static vector<pair<int, float*> > mModelViewMatrices;
  static Mat mProjectionMatrix;

  //function prototypes
  void testMatcher(Pattern& scenePattern,  vector<Pattern>& results);
  void computeHomography(Pattern& scenePattern,  vector<Pattern>& resultsPatterns,vector<pair<int,PatternTrackingInfo> >& results);
  void buildProjectionMatrix(int screen_width, int screen_height, Matrix44& projectionMatrix);


  void init(){

  }
  JNIEXPORT void JNICALL
  Java_com_appmunki_miragemobile_ar_ARActivity_addPattern(JNIEnv* env, jobject obj, jint width, jint height, jbyteArray yuv)
  {
    jbyte* _yuv = env->GetByteArrayElements(yuv, 0);
    int* _rgba = new int[width * height];

    Mat myuv(height + height / 2, width, CV_8UC1, (unsigned char *) _yuv);
    Mat mrgba(height, width, CV_8UC4, (unsigned char *) _rgba);
    Mat mgray(height, width, CV_8UC1, (unsigned char *) _yuv);

    LOG("adding Pattern");
    Pattern pattern(mrgba,mgray);
    patterns.push_back(pattern);

    vector<Mat> descriptors;
    descriptors.push_back(pattern.descriptor);

    matcher_->add(descriptors);
    matcher_->train();

    __android_log_print(ANDROID_LOG_INFO, "MirageMobile", "Pattern size %d", (int) patterns.size());
    env->ReleaseByteArrayElements(yuv, _yuv, 0);
  }

  void testMatcher(Pattern& scenePattern,  vector<Pattern>& results){
          vector<DMatch > matches;
          vector<vector<DMatch> > matches_by_id(patterns.size());

          vector<vector<DMatch> > knnmatches;
          matcher_->knnMatch(scenePattern.descriptor,knnmatches,2);
          LOG("%d KNN Matches\n",(int)knnmatches.size());\
          for (size_t i=0; i<knnmatches.size(); i++)
          {

                  DMatch _m;
                  if(knnmatches[i].size()==1) {
                          _m = knnmatches[i][0]; // only one neighbor
                  } else if(knnmatches[i].size()>1) {
                          // 2 neighbors â check how close they are
                          double ratio = knnmatches[i][0].distance / knnmatches[i][1].distance;
                          if(ratio < 0.7) { // not too close
                                  // take the closest (first) one
                                  _m = knnmatches[i][0];
                          } else { // too close â we cannot tell which is better
                                          continue; // did not pass ratio test â throw away
                          }
                  } else {
                                  continue; // no neighbors... :(
                  }
                  matches.push_back(_m);
          }

          LOG("%d Ratio Matches\n",(int)matches.size());


          for(unsigned int i=0;i<matches.size();i++){
                  DMatch m = matches[i];
                  matches_by_id[m.imgIdx].push_back(m);

          }
          for(unsigned int i=0;i<matches_by_id.size();i++){

                  if(matches_by_id[i].size()<4)
                          continue;
                  vector<DMatch> matches = matches_by_id[i];

                  Pattern pattern = patterns[i];

                  //-- Localize the object
                  std::vector < Point2f > obj;
                  std::vector < Point2f > scene;

                  for (size_t j = 0; j < matches.size(); j++)
                  {

                          //-- Get the keypoints from the good matches
                          obj.push_back(pattern.keypoints[matches[j].queryIdx].pt);
                          scene.push_back(scenePattern.keypoints[matches[j].trainIdx].pt);
              }

              // Find homography matrix and get inliers mask
              std::vector<unsigned char> inliersMask(obj.size());
              Mat homography = cv::findHomography(obj, scene, CV_RANSAC, 0.05f, inliersMask);
              std::vector < cv::DMatch > inliers;
              for (size_t j = 0; j < inliersMask.size(); j++)
              {
                      if (inliersMask[j])
                              inliers.push_back(matches[j]);
              }
              int sum = std::accumulate(inliersMask.begin(), inliersMask.end(), 0);
              LOG("inlier sum: %d\n",sum);
              if(inliers.size()<4)
                      continue;

              LOG("%d inlier for %d\n",(int)inliers.size(),i);


              results.push_back(pattern);
          }
          LOG("Prediction %d",results.size());
  }

  void computeHomography(Pattern& scenePattern,  vector<Pattern>& resultsPatterns,vector<pair<int,PatternTrackingInfo> >& results){
    for(int j=0;j<resultsPatterns.size();j++){
      Pattern pattern = resultsPatterns[j];

      Mat img_object = pattern.gray;
      Mat img_scene = scenePattern.gray;

      //-- Step 1: Detect the keypoints using SURF Detector
      int minHessian = 1000;

      ORB detector( minHessian );

      std::vector<KeyPoint> keypoints_object, keypoints_scene;

      detector.detect( img_object, keypoints_object );
      detector.detect( img_scene, keypoints_scene );

      //-- Step 2: Calculate descriptors (feature vectors)
      ORB extractor;

      Mat descriptors_object, descriptors_scene;

      extractor.compute( img_object, keypoints_object, descriptors_object );
      extractor.compute( img_scene, keypoints_scene, descriptors_scene );

      //-- Step 3: Matching descriptor vectors using FLANN matcher
      FlannBasedMatcher matcherp(new cv::flann::LshIndexParams(5, 12, 1));
      std::vector< DMatch > matches;
      matcherp.match( descriptors_object, descriptors_scene, matches );

      double max_dist = 0; double min_dist = 100;

      //-- Quick calculation of max and min distances between keypoints
      for( int i = 0; i < descriptors_object.rows; i++ )
      { double dist = matches[i].distance;
          if( dist < min_dist ) min_dist = dist;
          if( dist > max_dist ) max_dist = dist;
      }


      //-- Draw only "good" matches (i.e. whose distance is less than 3*min_dist )
      std::vector< DMatch > good_matches;

      for( int i = 0; i < descriptors_object.rows; i++ )
      { if( matches[i].distance < 3*min_dist )
         { good_matches.push_back( matches[i]); }
      }

      //-- Localize the object
      std::vector<Point2f> obj;
      std::vector<Point2f> scene;

      for( int i = 0; i < good_matches.size(); i++ )
      {
        //-- Get the keypoints from the good matches
        obj.push_back( keypoints_object[ good_matches[i].queryIdx ].pt );
        scene.push_back( keypoints_scene[ good_matches[i].trainIdx ].pt );
      }

      Mat homography = findHomography( obj, scene, CV_RANSAC );

      PatternTrackingInfo info;
      info.homography = homography;
      cv::perspectiveTransform(scenePattern.points2d, info.points2d, info.homography);
      info.computePose(scenePattern, m_calibration);

      Transformation patternPose;
      patternPose = info.pose3d;

      glMatrixTest = patternPose.getMat44();

      pair<int, PatternTrackingInfo> p(j, info);
      results.push_back(p);

    }

  }


  JNIEXPORT jint JNICALL
  Java_com_appmunki_miragemobile_ar_Matcher_matchDebugDiego(JNIEnv* env, jobject obj, long addrGray)
    {
      Mat& mgray = *(Mat*) addrGray;
      if(!mgray.data)
        LOGE("FRAME ERROR");
      vector<pair<int, PatternTrackingInfo> > result;
      vector<Pattern> resultPatterns;
      Pattern scenePattern(mgray,mgray);


      //Calls Matching
      LOG("Matching begin");
      testMatcher(scenePattern,resultPatterns);
      computeHomography(scenePattern,resultPatterns,result);
      LOG("Results size %d", result.size());



      result.size() > 0 ? isPatternPresent = true : isPatternPresent = false;

      return result.size();
    }

  JNIEXPORT jint JNICALL
  Java_com_appmunki_miragemobile_ar_Matcher_matchDebug(JNIEnv* env, jobject obj, jint width, jint height, jbyteArray yuv, jfloatArray modelviewmatrix, jfloatArray projectionmatrix)
    {
      LOG("------------------MatchDebug----------------");

      // Get C++ pointer to array data
      float* modelViewPtr = env->GetFloatArrayElements(modelviewmatrix, 0);
      float* projectionPtr = env->GetFloatArrayElements(projectionmatrix, 0);

      jbyte* _yuv = env->GetByteArrayElements(yuv, 0);

      //Conversion of frame
      int* _rgba = new int[width * height];
      Mat myuv(height + height / 2, width, CV_8UC1, (unsigned char *) _yuv);
      Mat mrgba(height, width, CV_8UC4, (unsigned char *) _rgba);
      Mat mgray(height, width, CV_8UC1, (unsigned char *) _yuv);

      // read image from file
      vector<pair<int, PatternTrackingInfo> > result;
      vector<Pattern> resultPatterns;

      //Changed trainkeys to framepattern
      Pattern scenePattern(mrgba,mgray);



      LOG("Matching begin");
      testMatcher(scenePattern,resultPatterns);
      computeHomography(scenePattern,resultPatterns,result);
      LOG("Computing Poses");

      result.size() > 0 ? isPatternPresent = true : isPatternPresent = false;

      env->ReleaseFloatArrayElements(projectionmatrix, projectionPtr, 0);
      env->ReleaseFloatArrayElements(modelviewmatrix, modelViewPtr, 0);
      env->ReleaseByteArrayElements(yuv, _yuv, 0);

      return result.size();
    }


  JNIEXPORT jboolean JNICALL
  Java_com_appmunki_miragemobile_ar_Matcher_isPatternPresent(JNIEnv *env, jobject obj)
    {
      jboolean isPresent = isPatternPresent;
      return isPatternPresent;
    }

  JNIEXPORT jfloatArray JNICALL
  Java_com_appmunki_miragemobile_ar_Matcher_getMatrix(JNIEnv *env, jobject obj)
    {

      jfloatArray result;
      result = env->NewFloatArray(16);
      if (result == NULL)
        {
          return NULL; /* out of memory error thrown */
        }

      jfloat array1[16];

      for (int i = 0; i < 16; ++i)
        {
          array1[i] = glMatrixTest.data[i];
        }

      env->SetFloatArrayRegion(result, 0, 16, array1);

      return result;
    }
  void
   buildProjectionMatrix(int screen_width, int screen_height, Matrix44& projectionMatrix)
   {
     float nearPlane = 0.01f; // Near clipping distance
     float farPlane = 100.0f; // Far clipping distance

     m_calibration = CameraCalibration(786.42938232f, 786.42938232f, 217.01358032f, 311.25384521f);


     // Camera parameters
     float f_x = m_calibration.fx(); // Focal length in x axis
     float f_y = m_calibration.fy(); // Focal length in y axis (usually the same?)
     float c_x = m_calibration.cx(); // Camera primary point x
     float c_y = m_calibration.cy(); // Camera primary point y

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
     projectionMatrix.data[10] = -(farPlane + nearPlane) / (farPlane - nearPlane);
     projectionMatrix.data[11] = -1.0f;

     projectionMatrix.data[12] = 0.0f;
     projectionMatrix.data[13] = 0.0f;
     projectionMatrix.data[14] = -2.0f * farPlane * nearPlane / (farPlane - nearPlane);
     projectionMatrix.data[15] = 0.0f;

   }
  JNIEXPORT jfloatArray JNICALL
Java_com_appmunki_miragemobile_ar_Matcher_getProjectionMatrix(JNIEnv *env, jobject obj)
  {

    buildProjectionMatrix(480, 640, glProjectionMatrix);

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
