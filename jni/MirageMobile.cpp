#include <jni.h>
#include <android/log.h>
#include <string.h>
#include <assert.h>
#include <opencv2/opencv.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <cstdio>
#include <cstdlib>
#include <Utils.h>

#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/legacy/legacy.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/calib3d/calib3d.hpp>


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


  /**
   * Convert data stored in an array into keypoints and descriptor
   */
  void readKeyAndDesc(vector<KeyPoint> &trainKeys, Mat &trainDes, float *mdata, int &count) {
          // doc du lieu
          int keyNum, octave, classId;
          float x, y, angle, size, response;
          keyNum = mdata[count++];
          for(int i = 0; i < keyNum; ++i) {
                  //scanf("%f%d%d%f%f%f%f", &angle, &classId, &octave, &x, &y, &response, &size);
                  //ss >> angle >> classId >> octave >> x >> y >> response >> size;
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

  JNIEXPORT void JNICALL
  Java_com_appmunki_miragemobile_ar_Matcher_fetch(JNIEnv *, jobject){
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
    readDatabase(mdata, count);
    loaded=true;
    LOG("Done");

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
      (
      const std::vector<cv::KeyPoint>& queryKeypoints,
      const std::vector<cv::KeyPoint>& trainKeypoints,
      float reprojectionThreshold,
      std::vector<cv::DMatch>& matches,
      cv::Mat& homography
      )
  {
      const unsigned int minNumberMatchesAllowed = 8;

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


      matches.swap(inliers);
      return matches.size() > minNumberMatchesAllowed;
  }
  inline void ratiotest(vector<DMatch> &matches,const Mat& queryDes,int j){
      double max_dist = 0; double min_dist = 100;

       //-- Quick calculation of max and min distances between keypoints
       for( int i = 0; i < queryDes.rows; i++ )
       { double dist = matches[i].distance;
         if( dist < min_dist ) min_dist = dist;
         if( dist > max_dist ) max_dist = dist;
       }

       //printf("-- Max dist %d: %f \n", j,max_dist );
       //printf("-- Min dist %d: %f \n", j,min_dist );

       //-- Draw only "good" matches (i.e. whose distance is less than 3*min_dist )
       std::vector< DMatch > good_matches;

       for( int i = 0; i < queryDes.rows; i++ )
       { if( matches[i].distance < 4*min_dist )
          { good_matches.push_back( matches[i]); }
       }
       matches.swap(good_matches);
  }
  /**
   * Match the query image to images in database. The best matches are returned
   */
  inline void match(const Mat& m_grayImg, const vector<KeyPoint> &trainKeys, const Mat &trainDes, vector<pair<float, int> > &result) {
          int runs = 0;
          // use Flann based matcher to match images
          cv::FlannBasedMatcher bf(new flann::LshIndexParams(30,8,2));
          // train the query image
          int size = queryDes.size();
          for(int i = 0; i < size; ++i) {

                  // compute match score for each image in the database
                  vector<DMatch> matches;
                  vector<DMatch> refinedmatches;
                  bf.match(queryDes[i],trainDes, matches);
//

                  //Find homography transformation and detect good matches
                  cv::Mat m_roughHomography;
                  cv::Mat m_refinedHomography;

                  bool homographyFound = refineMatchesWithHomography(
                                          queryKeys[i],trainKeys,

                                         1,
                                          matches,
                                          m_roughHomography);
                  LOG("Matching %d Step 1: %d",i,matches.size());

                  if(homographyFound){
                          LOG("Matching %d Step 1: %d",i,matches.size());

                          Mat m_warpedImg;
                          Size size= queryDims[i];
                          //cerr<<"Size"<<m_grayImg.cols<<" : "<<m_grayImg.rows<<endl;
                          cv::warpPerspective(m_grayImg, m_warpedImg, m_roughHomography, size, cv::INTER_LINEAR);


              //Extract Warped Image Keys
              Mat warpDes;
              vector<KeyPoint> warpKeys;
              extractFeatures(m_grayImg,warpDes,warpKeys);

              //Match
             bf.match(queryDes[i],warpDes, refinedmatches);

              homographyFound = refineMatchesWithHomography(
                                                  queryKeys[i],warpKeys,

                                                  1,
                                                  refinedmatches,
                                                  m_refinedHomography);
                    if(homographyFound){
                        LOG("Matching %d Step 1: %d",i,matches.size());
                        pair <float, int> p(matches.size(), i);
                        result.push_back(p);
                        runs++;
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

  JNIEXPORT void JNICALL
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
          return ;
        }
        LOG("Matching begin");
        match(img,trainKeys, trainDes, result);
        int size = min(result.size(), MAX_ITEM);

        // print out the best result
        if(result.size()){
          LOG("Size: %d\n", result.size());

          for(int i = 0; i < size; ++i) {
              LOG("%f  %d",result[i].first,result[i].second);
          }
        }
        trainDes.release();
        trainKeys.clear();
        LOG("Matching end");
        return ;
    }
  }





#ifdef __cplusplus
}
#endif
