#include <jni.h>
#include <android/log.h>
#include <string.h>
#include <assert.h>
#include <opencv2/opencv.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <cstdio>
#include <cstdlib>
#include <Utils.h>

#include "TargetImage.h"

#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/legacy/legacy.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/calib3d/calib3d.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <vector>


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
  static vector<TargetImage> targetImages;

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
  inline void readDatabase(float *mdata, int &count) {

    int querySize;
    querySize = mdata[count++];
    for(int i = 0; i < querySize; ++i) {
        vector<KeyPoint> qK;
        Mat qD;
        Size qS;

        qS.width=mdata[count++];
        qS.height=mdata[count++];

        readKeyAndDesc(qK, qD, mdata, count);

        queryKeys.push_back(qK);
        queryDes.push_back(qD);
        queryDims.push_back(qS);
    }
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

        qS.width=mdata[count++];
        qS.height=mdata[count++];

        readKeyAndDesc(qK, qD, mdata, count);

        TargetImage t;
        t.setId(i);
        t.setDescriptor(qD);
        t.setKeypoints(qK);
        t.setSize(qS);
        targetImages.push_back(t);
    }
  }

  JNIEXPORT void JNICALL
  Java_com_appmunki_miragemobile_ar_Matcher_load(JNIEnv *, jobject){
    LOG("Fetch");
    FILE* pFile = fopen("/data/data/com.appmunki.miragemobile/files/Data.txt","rb");

    long lSize;
    char * buffer;
    size_t sresult;

    if (pFile==NULL) {fputs ("File error",stderr); exit (1);}

    // obtain file size:
    fseek (pFile , 0 , SEEK_END);

    lSize = ftell(pFile);

    rewind (pFile);

    // allocate memory to contain the whole file:
    buffer = (char*) malloc (sizeof(char)*lSize);
    if (buffer == NULL) {fputs ("Memory error",stderr); exit (2);}

    // copy the file into the buffer:
    sresult = fread (buffer,1,lSize,pFile);
    if (sresult != lSize) {fputs ("Reading error",stderr); exit (3);}

    /* the whole file is now loaded in the memory buffer. */

    int dataSize, count = 0;
    char *endPtr;
    dataSize = strtol(buffer, &endPtr, 10);
    float *mdata = new float[dataSize];
    // read data as an array of float number
    for(int i = 0; i < dataSize; ++i) {
            mdata[i] = strtod(endPtr, &endPtr);
            //LOGE("data: %f",mdata[i]);
    }
    readDB(mdata, count);
    loaded=true;
    LOG("Done %d",targetImages.size());

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
  inline bool refineMatchesWithHomography
      (int it,float &confidence,
      const std::vector<cv::KeyPoint>& queryKeypoints,
      const std::vector<cv::KeyPoint>& trainKeypoints,
      float reprojectionThreshold,
      std::vector<cv::DMatch>& matches,
      cv::Mat& homography
      )
  {
      const unsigned int minNumberMatchesAllowed = 15;

      if (matches.size() < minNumberMatchesAllowed)
          return false;

      // Prepare data for cv::findHomography
      std::vector<cv::Point2f> srcPoints(matches.size());
      std::vector<cv::Point2f> dstPoints(matches.size());

      for (size_t i = 0; i < matches.size(); i++)
      {
          srcPoints[i] = trainKeypoints[matches[i].trainIdx].pt;
          dstPoints[i] = queryKeypoints[matches[i].queryIdx].pt;
      }

      // Find homography matrix and get inliers mask
      std::vector<unsigned char> inliersMask(srcPoints.size());
      homography = cv::findHomography(srcPoints,
                                      dstPoints,
                                      CV_FM_RANSAC,
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

  /**
   * Match the query image to images in database. The best matches are returned
   */
  inline void match(const Mat& m_grayImg, const vector<KeyPoint> &trainKeys, const Mat &trainDes, vector<pair<float, int> > &result) {
          // use Flann based matcher to match images
         cv::FlannBasedMatcher bf(new flann::LshIndexParams(30,8,2));
         //cv::BFMatcher bf(NORM_HAMMING);
         float confidence=0;
         // train the query image
          int size = targetImages.size();
          for(int i = 0; i < size; ++i) {

                  // compute match score for each image in the database
                  vector<DMatch> matches;
                  vector<DMatch> refinedmatches;


                  bf.match(targetImages[i].getDescriptor(),trainDes, matches);
//

                  //Find homography transformation and detect good matches
                  cv::Mat m_roughHomography;
                  cv::Mat m_refinedHomography;

                  bool homographyFound = refineMatchesWithHomography(i,confidence,
                      targetImages[i].getKeypoints(),trainKeys,

                                         4,
                                          matches,
                                          m_roughHomography);
                  LOG("Matching %d Step 1: %d",i,matches.size());

                  if(homographyFound){
                      LOG("Matching %d Step 1: %d",i,matches.size());

                      Mat m_warpedImg;

                      Size size= targetImages[i].getSize();
                      cv::warpPerspective(m_grayImg, m_warpedImg, m_roughHomography, size, cv::INTER_LINEAR);


                      //Extract Warped Image Keys
                      Mat warpDes;
                      vector<KeyPoint> warpKeys;
                      extractFeatures(m_grayImg,warpDes,warpKeys);

                      //Match
                      bf.match(queryDes[i],warpDes, refinedmatches);

                      homographyFound = refineMatchesWithHomography(i,confidence,
                                                          queryKeys[i],warpKeys,

                                                          4,
                                                          refinedmatches,
                                                          m_refinedHomography);
                      if(homographyFound){
                                LOG("Matching %d Step 1: %d",i,matches.size());
                                pair <float, int> p(matches.size(), i);
                                result.push_back(p);
                      }
                  }
          }

          // sort in descending
          std::sort(result.begin(), result.end(), compare<float, int>);
  }
  inline void drawHomography(Mat& img,
                  const std::vector<KeyPoint>& keypoints_object,
                  const std::vector<KeyPoint>& keypoints_scene, const Size& dim,
                  const vector<DMatch>& good_matches) {

          Mat img_scene = img.clone();

          //-- Localize the object
          std::vector<Point2f> obj;
          std::vector<Point2f> scene;

          for (size_t i = 0; i < good_matches.size(); i++) {
                  //-- Get the keypoints from the good matches
                  obj.push_back(keypoints_object[good_matches[i].queryIdx].pt);
                  scene.push_back(keypoints_scene[good_matches[i].trainIdx].pt);
          }

          Mat H = findHomography(obj, scene, CV_RANSAC, 5);
  //
  //    //-- Get the corners from the image_1 ( the object to be "detected" )
          std::vector<Point2f> obj_corners(4);
          obj_corners[0] = cvPoint(0, 0);
          obj_corners[1] = cvPoint(dim.width, 0);
          obj_corners[2] = cvPoint(dim.width, dim.height);
          obj_corners[3] = cvPoint(0, dim.height);
          std::vector<Point2f> scene_corners(4);
  //
  //
  //
          perspectiveTransform(obj_corners, scene_corners, H);
  //
  //    //-- Draw lines between the corners (the mapped object in the scene - image_2 )
  //
          line(img_scene, scene_corners[0], scene_corners[1], Scalar(0, 255, 0), 10);
          line(img_scene, scene_corners[1], scene_corners[2], Scalar(0, 255, 0), 10);
          line(img_scene, scene_corners[2], scene_corners[3], Scalar(0, 255, 0), 10);
          line(img_scene, scene_corners[3], scene_corners[0], Scalar(0, 255, 0), 10);

          //-- Show detected matches

          //showimage( "Good Matches & Object detection", img_scene );

  }
    /**
     * Match the query image to images in database. The best matches are returned
     */
    inline void matchTest(const Mat& m_grayImg,Mat& m_rgbImg, const vector<KeyPoint> &trainKeys, const Mat &trainDes, vector<pair<float, int> > &result) {
            // use Flann based matcher to match images
           cv::FlannBasedMatcher bf(new flann::LshIndexParams(30,8,2));
           //cv::BFMatcher bf(NORM_HAMMING);
           float confidence=0;
           // train the query image
            int size = targetImages.size();
            for(int i = 0; i < size; ++i) {

                    // compute match score for each image in the database
                    vector<DMatch> matches;
                    vector<DMatch> refinedmatches;


                    bf.match(targetImages[i].getDescriptor(),trainDes, matches);
  //

                    //Find homography transformation and detect good matches
                    cv::Mat m_roughHomography;
                    cv::Mat m_refinedHomography;

                    bool homographyFound = refineMatchesWithHomography(i,confidence,
                        targetImages[i].getKeypoints(),trainKeys,

                                           4,
                                            matches,
                                            m_roughHomography);
                    LOG("Matching %d Step 1: %d",i,matches.size());

                    if(homographyFound){
                        LOG("Matching %d Step 1: %d",i,matches.size());

                        Mat m_warpedImg;

                        Size size= targetImages[i].getSize();
                        cv::warpPerspective(m_grayImg, m_warpedImg, m_roughHomography, size, cv::INTER_LINEAR);


                        //Extract Warped Image Keys
                        Mat warpDes;
                        vector<KeyPoint> warpKeys;
                        extractFeatures(m_grayImg,warpDes,warpKeys);

                        //Match
                        bf.match(queryDes[i],warpDes, refinedmatches);

                        //Finds the homography of the refind match
                        homographyFound = refineMatchesWithHomography(i,confidence,
                                                            queryKeys[i],warpKeys,

                                                            4,
                                                            refinedmatches,
                                                            m_refinedHomography);
                        if(homographyFound){
                                  LOG("Matching %d Step 1: %d",i,matches.size());
                                  //Display the homography
                                  drawHomography(m_rgbImg,targetImages[i].getKeypoints(),trainKeys,targetImages[i].getSize(),matches);
                                  pair <float, int> p(matches.size(), i);
                                  result.push_back(p);
                        }
                    }
            }

            // sort in descending
            std::sort(result.begin(), result.end(), compare<float, int>);
    }

  /**
   * Get min value of two number
   */
  inline int min(int a, int b) {
          return a > b ? b:a;
  }

  JNIEXPORT jintArray JNICALL
  Java_com_appmunki_miragemobile_ar_Matcher_match(JNIEnv* env, jobject obj, long addrGray){
    if(loaded){
        Mat& img  = *(Mat*)addrGray;

        // read image from file
        vector<KeyPoint> trainKeys;
        Mat trainDes;
        vector<pair<float, int> > result;
        // detect image keypoints
        extractFeatures(img,trainDes,trainKeys);

        if(!trainKeys.size()){
          trainDes.release();
          trainKeys.clear();
          return NULL ;
        }
        LOG("Matching begin");
        match(img,trainKeys, trainDes, result);
        int size = min(result.size(), MAX_ITEM);

        jintArray resultArray;
        resultArray = (*env).NewIntArray(size);
        if (resultArray == NULL) {
                return NULL; /* out of memory error thrown */
        }

        jint fill[size];

        // print out the best result
        LOG("Size: %d\n", result.size());

        for(int i = 0; i < size; ++i) {
            fill[i] = result[i].second;
            LOG("%f  %d",result[i].first,result[i].second);
        }

        trainDes.release();
        trainKeys.clear();
        LOG("Matching end");
        (*env).SetIntArrayRegion(resultArray, 0, size, fill);
        return resultArray;
    }
    return NULL;
  }
  JNIEXPORT jintArray JNICALL
  Java_com_appmunki_miragemobile_ar_Matcher_matchDebug(JNIEnv* env, jobject obj, jint width, jint height, jbyteArray yuv, jintArray rgba){
      jbyte* _yuv  = env->GetByteArrayElements(yuv, 0);
      jint*  _rgba = env->GetIntArrayElements(rgba, 0);

      Mat myuv(height + height/2, width, CV_8UC1, (unsigned char *)_yuv);
      Mat mrgba(height, width, CV_8UC4, (unsigned char *)_rgba);
      Mat mgray(height, width, CV_8UC1, (unsigned char *)_yuv);

      cvtColor(myuv, mrgba, CV_YUV420sp2BGR, 4);

      vector<KeyPoint> v;

      ORB detector(1000);
      detector.detect(mgray, v);
      for( size_t i = 0; i < v.size(); i++ ){
          circle(mrgba, Point(v[i].pt.x, v[i].pt.y), 10, Scalar(0,0,255,255));
      }

      /**
       * Do the matching
       */

      // read image from file
      vector<KeyPoint> trainKeys;
      Mat trainDes;
      vector<pair<float, int> > result;


      // detect image keypoints
      extractFeatures(mgray,trainDes,trainKeys);

      if(!trainKeys.size()){
        trainDes.release();
        trainKeys.clear();
        return NULL ;
      }

      //Does the matching
      LOG("Matching begin");
      matchTest(mgray,mrgba,trainKeys, trainDes, result);
      int size = min(result.size(), MAX_ITEM);

      //Write the resultArray
      jintArray resultArray;
      resultArray = (*env).NewIntArray(size);
      if (resultArray == NULL) {
              return NULL; /* out of memory error thrown */
      }

      jint fill[size];

      // print out the best result
      LOG("Size: %d\n", result.size());

      for(int i = 0; i < size; ++i) {
          fill[i] = result[i].second;
          LOG("%f  %d",result[i].first,result[i].second);
      }

      //Clean up
      trainDes.release();
      trainKeys.clear();
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
      for( size_t i = 0; i < v.size(); i++ ){
          circle(mrgba, Point(v[i].pt.x, v[i].pt.y), 10, Scalar(0,0,255,255));
      }

      env->ReleaseIntArrayElements(rgba, _rgba, 0);
      env->ReleaseIntArrayElements(gray, _gray, 0);
      env->ReleaseByteArrayElements(yuv, _yuv, 0);
  }



#ifdef __cplusplus
}
#endif
