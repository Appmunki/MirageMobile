#include <jni.h>
#include <android/log.h>
#include <string.h>
#include <assert.h>
#include <opencv2/opencv.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <vector>
#include <Utils.h>

#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/legacy/legacy.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/calib3d/calib3d.hpp>


using namespace std;
using namespace cv;

#ifdef __cplusplus
extern "C" {
#endif

  static Vector<Vector<KeyPoint> > queryKeys;
  static Vector<Mat> queryDes;
  static Vector<Size> queryDims;
  static bool loaded = false;
  /**
   * Convert data stored in an array into keypoints and descriptor
   */
  void readKeyAndDesc(Vector<KeyPoint> &trainKeys, Mat &trainDes, float *mdata, int &count) {
          // doc du lieu
          int keyNum, octave, classId;
          float x, y, angle, size, response;
          keyNum = mdata[count++];
          LOG("readKeyAndDesc1: %d",keyNum);
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
          LOG("readKeyAndDesc2: %d:%d:%d",rows,cols,matSize);

          data = new uchar[matSize];
          for(int i = 0; i < matSize; ++i) {
                  data[i] = mdata[count++];
          }

          trainDes = Mat(rows, cols, CV_8U, data);
          LOG("readKeyAndDesc3");

  }

  /**
   * Read database from an array
   */
  inline void readDatabase(float *mdata, int &count) {

    int querySize;
    querySize = mdata[count++];
    for(int i = 0; i < querySize; ++i) {
        Vector<KeyPoint> qK;
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
  inline void extractFeatures(Mat& img, Mat& des, Vector<KeyPoint>& keys)
  {
    // detect image keypoints

    //cv::Ptr<cv::FeatureDetector>    sfd1  = new cv::ORB(1000);
    //cv::Ptr<cv::DescriptorExtractor> sde = new cv::FREAK();


//    sfd1->detect(img, keys);
//    //cerr << "Train keys size start " << keys.size() << endl;
//    int s = 500;
//    // select only the appropriate number of keypoints
//    while(keys.size() > 1000) {
//            //cerr << "Train keys size " << keys.size() << endl;
//            keys.clear();
//            FREAK sfd(s+500);
//            s += 500;
//            sfd1.detect(img, keys);
//    }
//
//    // compute image descriptor
//    sde.compute(img, keys, des);
  }
  JNIEXPORT void JNICALL
  Java_com_appmunki_miragemobile_ar_Matcher_match(JNIEnv *env, jobject obj, long addrGray){
    if(loaded){
        Mat& img  = *(Mat*)addrGray;
        // read image from file
        Vector<KeyPoint> trainKeys;
        Mat trainDes;
        Vector<pair<float, int> > result;

        // detect image keypoints
        extractFeatures(img,trainDes,trainKeys);
        LOG("Match");
    }
    //LOG("size:%d",queryDes.size());
  }





#ifdef __cplusplus
}
#endif
