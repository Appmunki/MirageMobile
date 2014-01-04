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

#define MAX_ITEM 2
#define HomographyReprojectionThreshold 1f
template <class T, class U>
bool compare(const pair<T, U> &a, const pair<T, U> &b) {
        return a.first > b.first;
}
#ifdef __cplusplus
extern "C" {
#endif

  static vector<vector<KeyPoint> > queryKeys;
  static vector<Mat> queryDes;
  static vector<Size> queryDims;
  static bool loaded = false;
  static vector<Pattern> patterns;
  static CameraCalibration m_calibration;
  bool isPatternPresent = false;
  bool isDebugging = false;
  static vector<pair<int,Matrix44> > mModelViewMatrixs;
  static Matrix44 glProjectionMatrix;


  void convertBitmapToMat(){

  }

  JNIEXPORT void JNICALL
    Java_com_appmunki_miragemobile_ar_ARLib_testMatConvert(JNIEnv* env, jobject obj,jobject bitmap){
      AndroidBitmapInfo  info;
      void*              pixels;
      int                ret;

      AndroidBitmap_getInfo(env, bitmap, &info);



      AndroidBitmap_lockPixels(env, bitmap, &pixels);


      uint16_t *pictureRGB;
      int size = sizeof(uint16_t)*info.width*info.height;
      pictureRGB = (uint16_t*)malloc(sizeof(uint16_t)*info.width*info.height);
      memcpy((char*)pixels, (char*)pictureRGB, info.width*info.height*sizeof(uint16_t));


      AndroidBitmap_unlockPixels(env, bitmap);
  }
  /**
   * Convert data stored in an array into keypoints and descriptor
   */
  void readKeyAndDesc(vector<KeyPoint> &trainKeys, Mat &trainDes, float *mdata, int &count) {
          // doc du lieu
          int keyNum, octave, classId;
          float x, y, angle, size, response;
          keyNum = mdata[count++];
          for(int i = 0; i < keyNum; ++i) {
                  angle = mdata[count++];
                  classId = mdata[count++];
                  octave = mdata[count++];
                  x = mdata[count++];
                  y = mdata[count++];
                  response = mdata[count++];
                  size = mdata[count++];
                  KeyPoint p(x, y, size, angle, response, octave, classId);
                  trainKeys.push_back(p);
          }

          int rows, cols, type;
          uchar *data;
          rows = mdata[count++];
          cols = mdata[count++];
          type = mdata[count++];
          int matSize = rows*cols;

          data = new uchar[matSize];
          for(int i = 0; i < matSize; ++i) {
                  data[i] = mdata[count++];
          }

          trainDes = Mat(rows, cols, CV_8U, data);

  }


  /**
   * Read database from an array
   */
  inline void readDB(float *mdata, int &count) {

    int querySize;
    querySize = mdata[count++];
    for(int i = 0; i < querySize; ++i) {
        vector<KeyPoint> qK;
        Mat qD;
        Size qS;

        qS.height=mdata[count++];
        qS.width=mdata[count++];

        readKeyAndDesc(qK, qD, mdata, count);
        LOG("width %d height %d",qS.width,qS.height);
        Pattern pattern(i,qD,qK,qS);
        patterns.push_back(pattern);
    }
  }

  inline void extractFeatures(const Mat& img, Mat& des, vector<KeyPoint>& keys)
  {
    // detect image keypoints

    cv::ORB  sfd1 (1000);
    cv::FREAK sde ;


    sfd1.detect(img, keys);
    //LOG("Train keys size start %d",keys.size() );
    int s = 3000;
    // selec                                                                     t only the appropriate number of keypoints
    while(keys.size() > 1000) {
            //cerr << "Train keys size " << keys.size() << endl;
            keys.clear();
            ORB sfd1(s+500);
            s += 500;
            sfd1.detect(img, keys);
    }

    // compute image descriptor
    sde.compute(img, keys, des);
  }

 inline void buildPatternFromImage(cv::Mat& frame,const cv::Mat& mgray, Pattern& pattern) {
            pattern.frame = frame;
            pattern.gray = mgray.clone();
            // Store original image in pattern structure
            pattern.size = cv::Size(mgray.cols, mgray.rows);

            // Build 2d and 3d contours (3d contour lie in XY plane since it's planar)
            pattern.points2d.resize(4);
            pattern.points3d.resize(4);

            // Image dimensions
            const float w = mgray.cols;
            const float h = mgray.rows;

            // Normalized dimensions:
            const float maxSize = std::max(w, h);
            const float unitW = w / maxSize;
            const float unitH = h / maxSize;

            pattern.points2d[0] = cv::Point2f(0, 0);
            pattern.points2d[1] = cv::Point2f(w, 0);
            pattern.points2d[2] = cv::Point2f(w, h);
            pattern.points2d[3] = cv::Point2f(0, h);

            pattern.points3d[0] = cv::Point3f(-unitW, -unitH, 0);
            pattern.points3d[1] = cv::Point3f(unitW, -unitH, 0);
            pattern.points3d[2] = cv::Point3f(unitW, unitH, 0);
            pattern.points3d[3] = cv::Point3f(-unitW, unitH, 0);


            extractFeatures(pattern.gray, pattern.descriptor, pattern.keypoints);
  }
  JNIEXPORT void JNICALL
  Java_com_appmunki_miragemobile_ar_ARActivity_addPattern(JNIEnv* env, jobject obj,jint width, jint height,jbyteArray yuv){
    jbyte* _yuv  = env->GetByteArrayElements(yuv, 0);
    int* _rgba = new int[width*height];

    Mat myuv(height + height/2, width, CV_8UC1, (unsigned char *)_yuv);
    Mat mrgba(height, width, CV_8UC4, (unsigned char *)_rgba);
    Mat mgray(height, width, CV_8UC1, (unsigned char *)_yuv);

    Pattern pattern;
    buildPatternFromImage(mrgba,mgray,pattern);
    patterns.push_back(pattern);

    __android_log_print(ANDROID_LOG_INFO, "MirageMobile", "Pattern size %d", (int)patterns.size());
    //LOG("Pattern size %d",(int)pattern.size());
    env->ReleaseByteArrayElements(yuv, _yuv, 0);


  }
  JNIEXPORT void JNICALL
  Java_com_appmunki_miragemobile_ar_Matcher_load(JNIEnv *, jobject obj,jboolean isDebug){

    LOG("Loading and training the images");

    //Add ARpipline here
    m_calibration = CameraCalibration(786.42938232f, 786.42938232f, 217.01358032f, 311.25384521f);

    //m_pipeline  = ARPipeline(patternImage, m_calibration);


  }

  inline bool refineMatchesWithHomography
      (int it,float &confidence,
      const std::vector<cv::KeyPoint>& keypoints_object,
      const std::vector<cv::KeyPoint>& keypoints_scene,
      float reprojectionThreshold,
      std::vector<cv::DMatch>& matches,
      cv::Mat& homography
      )
  {
      const unsigned int minNumberMatchesAllowed = 15;

      if (matches.size() < minNumberMatchesAllowed)
          return false;


      //-- Localize the object
      std::vector<Point2f> obj;
      std::vector<Point2f> scene;

      for (size_t i = 0; i < matches.size(); i++) {
              //-- Get the keypoints from the good matches
              obj.push_back(keypoints_object[matches[i].queryIdx].pt);
              scene.push_back(keypoints_scene[matches[i].trainIdx].pt);
      }

      // Find homography matrix and get inliers mask
      std::vector<unsigned char> inliersMask(obj.size());
      homography = cv::findHomography(obj, scene,
                                      CV_RANSAC,
                                      reprojectionThreshold,
                                      inliersMask);

      std::vector<cv::DMatch> inliers;
      for (size_t i=0; i<inliersMask.size(); i++)
      {
          if (inliersMask[i])
              inliers.push_back(matches[i]);
      }

      confidence = (inliers.size() / (8 + 0.3*matches.size()))*100;
      LOG("Confidence %d %f",it,confidence);
      matches.swap(inliers);
      return matches.size() > minNumberMatchesAllowed&&(confidence>55);
  }


  inline void drawHomography(Mat& img_scene,
                  const std::vector<KeyPoint>& keypoints_object,
                  const std::vector<KeyPoint>& keypoints_scene, const Size& dim,
                  const vector<DMatch>& good_matches) {

          LOG("Drawing Homography %dx%d",dim.width,dim.height);
          //-- Localize the object
          std::vector<Point2f> obj;
          std::vector<Point2f> scene;

          for (size_t i = 0; i < good_matches.size(); i++) {
                  //-- Get the keypoints from the good matches
                  obj.push_back(keypoints_object[good_matches[i].queryIdx].pt);
                  scene.push_back(keypoints_scene[good_matches[i].trainIdx].pt);
          }

          Mat H = findHomography(obj, scene, CV_RANSAC, 4);

          //-- Get the corners from the image_1 ( the object to be "detected" )
          std::vector<Point2f> obj_corners(4);
          obj_corners[0] = Point(0, 0);
          obj_corners[1] = Point(dim.width, 0);
          obj_corners[2] = Point(dim.width,dim.height);
          obj_corners[3] = Point(0, dim.height);
          std::vector<Point2f> scene_corners(4);



          cv::perspectiveTransform(obj_corners, scene_corners, H);

          //-- Draw lines between the corners (the mapped object in the scene - image_2 )
          line( img_scene, scene_corners[0] , scene_corners[1] , Scalar(255, 255, 255,255), 2 );
          line( img_scene, scene_corners[1], scene_corners[2] , Scalar(255, 255, 255,255), 2 );
          line( img_scene, scene_corners[2], scene_corners[3],  Scalar(255, 255, 255,255), 2 );
          line( img_scene, scene_corners[3] , scene_corners[0],  Scalar(255, 255, 255,255), 2 );

          //Displaying keypoint
          for( size_t i = 0; i < keypoints_scene.size(); i++ ){
              //circle(img_scene, Point(keypoints_scene[i].pt.x, keypoints_scene[i].pt.y), 5, Scalar(255,191,0,255),-1);
          }
//
          circle(img_scene, scene_corners[0], 10, Scalar(0,0,255,255),2);
          circle(img_scene, scene_corners[1], 10,  Scalar(0,255,0,255),2);
          circle(img_scene, scene_corners[2], 10, Scalar(255,0,0,255),2);
          circle(img_scene, scene_corners[3], 10,  Scalar(0,255,255,255),2);



  }
  /**
   * Match the query image to images in database. The best matches are returned
   */
  inline void match(Pattern& framepattern, vector< pair<int, PatternTrackingInfo&> >& result) {
            // use Flann based matcher to match images
           cv::FlannBasedMatcher bf(new flann::LshIndexParams(10,10,2));
           float confidence=0;
           // train the query image
            int size = patterns.size();
            for(int i = 0; i < size; ++i) {

                    // compute match score for each image in the database
                    vector<DMatch> matches;
                    vector<DMatch> refinedmatches;


                    bf.match(patterns[i].descriptor,framepattern.descriptor, matches);


                    //Find homography transformation and detect good matches
                    cv::Mat m_roughHomography;
                    cv::Mat m_refinedHomography;

                    bool homographyFound = refineMatchesWithHomography(i,confidence,
                        patterns[i].keypoints,framepattern.keypoints,

                                           4,
                                            matches,
                                            m_roughHomography);

                    if(homographyFound){

                        Mat m_warpedImg;

                        Size size= patterns[i].size;
                        cv::warpPerspective(framepattern.gray, m_warpedImg, m_roughHomography, size, cv::INTER_LINEAR);

                        //Extract Warped Image Keys
                        Mat warpDes;
                        vector<KeyPoint> warpKeys;
                        //TODO fix this it should be m_warpedImg
                        extractFeatures(framepattern.gray,warpDes,warpKeys);


                        //Match
                        bf.match(patterns[i].descriptor,warpDes, refinedmatches);


                        //Finds the homography of the refind match
                        homographyFound = refineMatchesWithHomography(i,confidence,
                            patterns[i].keypoints,warpKeys,

                                                            4,
                                                            refinedmatches,
                                                            m_refinedHomography);
                        if(homographyFound){

                                  // Transform contour with precise homography
                                  // cv::perspectiveTransform(m_pattern.points2d, info.points2d, info.homography);
                                  //drawHomography(framepattern.frame,patterns[i].keypoints,framepattern.keypoints,patterns[i].size,matches);
                                  PatternTrackingInfo info;
                                  info.homography=m_roughHomography;

                                  cv::perspectiveTransform(framepattern.points2d, info.points2d, info.homography);
                                  info.draw2dContour(framepattern.frame,Scalar(0,0,255,255));
                                  pair <int,PatternTrackingInfo&> p(i, info);
                                  result.push_back(p);
                        }
                    }
            }

            // sort in descending
            //std::sort(result.begin(), result.end(), compare<int, PatternTrackingInfo&>);
    }

  /**
   * Get min value of two number
   */
  inline int min(int a, int b) {
          return a > b ? b:a;
  }

  JNIEXPORT void JNICALL
  Java_com_appmunki_miragemobile_ar_Matcher_convertFrame(JNIEnv* env, jobject obj, jint width, jint height, jbyteArray yuv, jintArray rgba){
    //Conversion of frame
    jbyte* _yuv  = env->GetByteArrayElements(yuv, 0);
    jint*  _rgba = env->GetIntArrayElements(rgba, 0);

    Mat myuv(height + height/2, width, CV_8UC1, (unsigned char *)_yuv);
    Mat mrgba(height, width, CV_8UC4, (unsigned char *)_rgba);
    Mat mgray(height, width, CV_8UC1, (unsigned char *)_yuv);

    cvtColor(myuv, mrgba, CV_YUV420sp2BGR, 4);

    env->ReleaseIntArrayElements(rgba, _rgba, 0);
    env->ReleaseByteArrayElements(yuv, _yuv, 0);
  }

  JNIEXPORT jintArray JNICALL
  Java_com_appmunki_miragemobile_ar_Matcher_matchDebug(JNIEnv* env, jobject obj, jint width, jint height, jbyteArray yuv){
    LOG("------------------MatchDebug----------------");
    //Conversion of frame
    jbyte* _yuv  = env->GetByteArrayElements(yuv, 0);
    int* _rgba = new int[width*height];

    Mat myuv(height + height/2, width, CV_8UC1, (unsigned char *)_yuv);
    Mat mrgba(height, width, CV_8UC4, (unsigned char *)_rgba);
    Mat mgray(height, width, CV_8UC1, (unsigned char *)_yuv);


    // read image from file
    vector< pair<int, PatternTrackingInfo& > > result;


    //Changed trainkeys to framepattern

    Pattern framepattern;
    buildPatternFromImage(mrgba,mgray,framepattern);


    if(!framepattern.keypoints.size()){
      framepattern.descriptor.release();
      framepattern.keypoints.clear();
      return NULL ;
    }

    //Calls Matching
    LOG("Matching begin");
    match(framepattern,result);
    LOG("Results size %d",result.size());
    imwrite();
    env->ReleaseByteArrayElements(yuv, _yuv, 0);

  }
  JNIEXPORT jintArray JNICALL
  Java_com_appmunki_miragemobile_ar_Matcher_match(JNIEnv* env, jobject obj, jint width, jint height, jbyteArray yuv, jintArray rgba){
      //Conversion of frame
      jbyte* _yuv  = env->GetByteArrayElements(yuv, 0);
      jint*  _rgba = env->GetIntArrayElements(rgba, 0);

      Mat myuv(height + height/2, width, CV_8UC1, (unsigned char *)_yuv);
      Mat mrgba(height, width, CV_8UC4, (unsigned char *)_rgba);
      Mat mgray(height, width, CV_8UC1, (unsigned char *)_yuv);


      // read image from file
      vector< pair<int, PatternTrackingInfo& > > result;


      //Changed trainkeys to framepattern

      Pattern framepattern;
      buildPatternFromImage(mrgba,mgray,framepattern);


      if(!framepattern.keypoints.size()){
        framepattern.descriptor.release();
        framepattern.keypoints.clear();
        return NULL ;
      }

      //Calls Matching
      LOG("Matching begin");
      match(framepattern,result);
      LOG("Results size %d",result.size());

      //Get the patterlocation
      for(int i = 0;i<result.size();i++){
          //result[i].second.computePose(framepattern, m_calibration);
          //Transformation patternPose = result[i].second.pose3d;
          //Matrix44 glMatrixTest = patternPose.getMat44();
          //mModelViewMatrixs.push_back(pair<int,Matrix44>(result[i].first,glMatrixTest));
      }
      int size = min(result.size(), MAX_ITEM);
      size>0 ? isPatternPresent = true : isPatternPresent=false;

      //Write the resultArray
      jintArray resultArray;
      resultArray = (*env).NewIntArray(size);
      if (resultArray == NULL) {
              return NULL; /* out of memory error thrown */
      }

      jint fill[size];

      // print out the best result
      LOG("Size: %d\n", result.size());

//      for(int i = 0; i < size; ++i) {
//          fill[i] = result[i].second;
//          //LOG("%f  %d",result[i].first,result[i].second);
//      }

      //Clean up

      LOG("Matching end");
      (*env).SetIntArrayRegion(resultArray, 0, size, fill);


      env->ReleaseIntArrayElements(rgba, _rgba, 0);
      env->ReleaseByteArrayElements(yuv, _yuv, 0);

      return resultArray;
  }
  JNIEXPORT void JNICALL Java_com_appmunki_miragemobile_ar_Matcher_FindFeatures(JNIEnv* env, jobject thiz, jint width, jint height, jbyteArray yuv, jintArray rgba,jintArray gray)
  {
      jbyte* _yuv  = env->GetByteArrayElements(yuv, 0);
      jint*  _rgba = env->GetIntArrayElements(rgba, 0);
      jint*  _gray = env->GetIntArrayElements(gray, 0);

      Mat myuv(height + height/2, width, CV_8UC1, (unsigned char *)_yuv);
      Mat mrgba(height, width, CV_8UC4, (unsigned char *)_rgba);
      Mat mgray(height, width, CV_8UC1, (unsigned char *)_gray);

      cvtColor(myuv, mrgba, CV_YUV420sp2RGBA, 4);
      cvtColor(myuv, mgray, CV_YUV420sp2GRAY, 1);

      vector<KeyPoint> v;

      ORB detector(1000);
      detector.detect(mgray, v);
      int size= v.size();
      /*if(size>30)
        size=20;*/
      for( size_t i = 0; i < size; i++ ){

          //place circles on RGB Image
          circle(mrgba, Point(v[i].pt.x, v[i].pt.y), 5, Scalar(255,191,0,255),-1);

          //place circles on YUV Image
          circle(myuv, Point(v[i].pt.x, v[i].pt.y), 5, Scalar(255,191,0,255),-1);

          //place keypoints on Gray Image
          circle(mgray, Point(v[i].pt.x, v[i].pt.y), 5, Scalar(255,191,0,255),-1);

      }

      env->ReleaseIntArrayElements(rgba, _rgba, 0);
      env->ReleaseIntArrayElements(gray, _gray, 0);
      env->ReleaseByteArrayElements(yuv, _yuv, 0);
  }
void buildProjectionMatrix(int screen_width, int screen_height, Matrix44& projectionMatrix) {
          float nearPlane = 0.01f; // Near clipping distance
          float farPlane = 100.0f; // Far clipping distance

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
JNIEXPORT jboolean JNICALL
Java_com_appmunki_miragemobile_ar_Matcher_isPatternPresent(JNIEnv *env, jobject obj) {
        jboolean isPresent = isPatternPresent;
        return isPatternPresent;
}

JNIEXPORT jfloatArray JNICALL
Java_com_appmunki_miragemobile_ar_Matcher_getMatrix(JNIEnv *env, jobject obj) {
        jfloatArray result;
        result = env->NewFloatArray(16);
        if (result == NULL) {
                return NULL; /* out of memory error thrown */
        }

        jfloat array1[16];
        Matrix44 glMatrixTest = mModelViewMatrixs[0].second;
        for (int i = 0; i < 16; ++i) {
                array1[i] = glMatrixTest.data[i];
        }

        env->SetFloatArrayRegion(result, 0, 16, array1);

        return result;
}

JNIEXPORT jfloatArray JNICALL
Java_com_appmunki_miragemobile_ar_Matcher_getProjectionMatrix(JNIEnv *env, jobject obj) {
        buildProjectionMatrix(480, 640, glProjectionMatrix);

        jfloatArray result;
        result = env->NewFloatArray(16);
        if (result == NULL) {
                return NULL; /* out of memory error thrown */
        }

        jfloat array1[16];

        for (int i = 0; i < 16; ++i) {
                array1[i] = glProjectionMatrix.data[i];
        }

        env->SetFloatArrayRegion(result, 0, 16, array1);
        return result;
}


#ifdef __cplusplus
}
#endif
