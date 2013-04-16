#include <jni.h>
#include <android/log.h>
#include <string.h>
//#include <assert.h>
//#include <cstdio>
//#include <cstdlib>
#include <Utils.h>

#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/legacy/legacy.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/calib3d/calib3d.hpp>

#include "ARPipeline.hpp"
#include "DebugHelpers.hpp"

using namespace std;
using namespace cv;

#define MAX_ITEM 2
#define HomographyReprojectionThreshold 1f
template<class T, class U>
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

CameraCalibration m_calibration;

ARPipeline m_pipeline;

Matrix44 glMatrixTest;
Matrix44 glProjectionMatrix;

const char *nativeString = "/sdcard/Pictures/Mirage/PyramidPattern.jpg";
Mat patternImage;
int counter = 0;

void
processSingleImage(const cv::Mat& patternImage, const cv::Mat& image);

bool
processFrame(const cv::Mat& cameraFrame);

void
extractFeatures(const Mat& img, Mat& des, vector<KeyPoint>& keys);

void
buildProjectionMatrix(int screen_width, int screen_height, Matrix44& projectionMatrix);

//ARDrawingContext& drawingCtx);

//Testing the right position of the pattern on the image
/*void
 test() {

 // Change this calibration to yours:
 CameraCalibration calibration(786.42938232f, 786.42938232f, 217.01358032f, 311.25384521f);
 // Try to read the pattern:

 //CameraCalibration calibration(1.0f, 1.0f, 1.0f, 1.0f);

 LOG("1");

 const char *nativeString = "/sdcard/Pictures/Mirage/PyramidPattern.jpg";
 const char *inputString = "/sdcard/Pictures/Mirage/PyramidPatternTest.bmp";

 Mat img_object = imread(nativeString, 0);
 Mat img_scene = imread(inputString, 0);

 if (!img_object.data || !img_scene.data) {
 LOG(" --(!) Error reading images ");
 }

 LOG("2");
 //-- Step 1: Detect the keypoints using SURF Detector
 int minHessian = 50;

 //FastFeatureDetector detector(minHessian);

 //detector.detect(img_object, keypoints_object);
 //detector.detect(img_scene, keypoints_scene);
 LOG("3");
 //-- Step 2: Calculate descriptors (feature vectors)

 cv::FREAK sde;

 Mat descriptors_object, descriptors_scene;

 std::vector<KeyPoint> keypoints_object, keypoints_scene;
 extractFeatures(img_object, descriptors_object, keypoints_object);
 extractFeatures(img_scene, descriptors_scene, keypoints_scene);

 LOG("4");
 //-- Step 3: Matching descriptor vectors using FLANN matcher
 cv::BFMatcher bf(NORM_HAMMING);

 //FlannBasedMatcher matcher;
 std::vector < DMatch > matches;
 bf.match(descriptors_object, descriptors_scene, matches);
 LOG("5");
 double max_dist = 0;
 double min_dist = 100;

 LOG("ROWS %d", descriptors_object.rows);
 //-- Quick calculation of max and min distances between keypoints
 for (int i = 0; i < descriptors_object.rows; i++) {
 double dist = matches[i].distance;
 LOG("DISTANCE %f", dist);
 if (dist < min_dist)
 min_dist = dist;
 if (dist > max_dist)
 max_dist = dist;
 }

 LOG("6");
 LOG("-- Max dist : %f \n", max_dist);
 LOG("-- Min dist : %f \n", min_dist);

 //-- Draw only "good" matches (i.e. whose distance is less than 3*min_dist )
 std::vector < DMatch > good_matches;

 for (int i = 0; i < descriptors_object.rows; i++) {
 if (matches[i].distance < 3 * min_dist) {
 good_matches.push_back(matches[i]);
 }
 }
 LOG("7 %d", good_matches.size());
 Mat img_matches;
 drawMatches(img_object, keypoints_object, img_scene, keypoints_scene, good_matches, img_matches, Scalar::all(-1), Scalar::all(-1), vector<char>(),
 DrawMatchesFlags::NOT_DRAW_SINGLE_POINTS);

 //-- Localize the object
 vector < Point2f > obj;
 vector < Point2f > scene;
 LOG("8");
 for (int i = 0; i < good_matches.size(); i++) {
 //-- Get the keypoints from the good matches
 obj.push_back(keypoints_object[good_matches[i].queryIdx].pt);
 scene.push_back(keypoints_scene[good_matches[i].trainIdx].pt);
 }
 LOG("9");
 cv::Mat H = cv::findHomography(obj, scene, CV_RANSAC);

 //Mat H = cv::findHomography(obj, scene, CV_FM_RANSAC,5);

 //-- Get the corners from the image_1 ( the object to be "detected" )
 std::vector < Point2f > obj_corners(4);
 obj_corners[0] = cvPoint(0, 0);
 obj_corners[1] = cvPoint(img_object.cols, 0);
 obj_corners[2] = cvPoint(img_object.cols, img_object.rows);
 obj_corners[3] = cvPoint(0, img_object.rows);
 std::vector < Point2f > scene_corners(4);
 LOG("10");
 cv::perspectiveTransform(cv::Mat(obj_corners), cv::Mat(scene_corners), H);
 //perspectiveTransform(obj_corners, scene_corners, H);

 //-- Draw lines between the corners (the mapped object in the scene - image_2 )
 line(img_scene, scene_corners[0], scene_corners[1], Scalar(0, 255, 0), 4);
 line(img_scene, scene_corners[1], scene_corners[2], Scalar(0, 255, 0), 4);
 line(img_scene, scene_corners[2], scene_corners[3], Scalar(0, 255, 0), 4);
 line(img_scene, scene_corners[3], scene_corners[0], Scalar(0, 255, 0), 4);

 LOG("11");
 imwrite("/mnt/sdcard/MirageTest/1.jpg", img_scene);
 LOG("12");
 }
 */

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

void processSingleImage(const cv::Mat& patternImage, const cv::Mat& image) {
	cv::Size frameSize(image.cols, image.rows);

	bool shouldQuit = false;
	shouldQuit = processFrame(image);
}

bool processFrame(const cv::Mat& cameraFrame) {
	// Clone image used for background (we will draw overlay on it)
	cv::Mat img = cameraFrame.clone();

	bool isPatternPresent = m_pipeline.processFrame(cameraFrame);
	Transformation patternPose;
	patternPose = m_pipeline.getPatternLocation();

	m_pipeline.m_patternInfo.draw2dContour(img, Scalar(0, 255, 0));
	imwrite("/mnt/sdcard/MirageTest/1.jpg", img);

	glMatrixTest = patternPose.getMat44();

	/*if (m_pipeline.m_patternInfo.points2d.size() > 1) {
		float distX = m_pipeline.m_patternInfo.points2d[0].x - m_pipeline.m_patternInfo.points2d[1].x;
		float distY = m_pipeline.m_patternInfo.points2d[0].y - m_pipeline.m_patternInfo.points2d[1].y;

		LOG("DISTANCIA X: %f Y:%f", distX, distY);
	}*/

	return false;
}

/**
 * Convert data stored in an array into keypoints and descriptor
 */
void readKeyAndDesc(vector<KeyPoint> &trainKeys, Mat &trainDes, float *mdata, int &count) {
	// doc du lieu
	int keyNum, octave, classId;
	float x, y, angle, size, response;
	keyNum = mdata[count++];
	for (int i = 0; i < keyNum; ++i) {
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
	int matSize = rows * cols;

	data = new uchar[matSize];
	for (int i = 0; i < matSize; ++i) {
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
	for (int i = 0; i < querySize; ++i) {
		vector<KeyPoint> qK;
		Mat qD;
		Size qS;

		qS.width = mdata[count++];
		qS.height = mdata[count++];

		readKeyAndDesc(qK, qD, mdata, count);
		queryKeys.push_back(qK);
		queryDes.push_back(qD);
		queryDims.push_back(qS);
	}
}

JNIEXPORT void JNICALL
Java_com_appmunki_miragemobile_ar_Matcher_fetch(JNIEnv *, jobject) {
	LOG("Fetch");
	//m_calibration = CameraCalibration(526.58037684199849f, 524.65577209994706f, 318.41744018680112f, 202.96659047014398f);
	//m_calibration = CameraCalibration(786.42938232f, 786.42938232f, 217.01358032f, 311.25384521f);

	float distorsion[5] = {0.12511057719838597f, 1.1885259508076416f,
		       0.0018152449454049871f, 0.0058028812916887575f,
		       -18.760884645692691f};

	m_calibration = CameraCalibration(674.85465753264498f, 674.43269416560099f,236.99202219745999f,293.96907219036780f,distorsion);

	patternImage = imread(nativeString, 0);
	m_pipeline = ARPipeline(patternImage, m_calibration);

	LOG("INICIA TEST");
	//test();
	LOG("TERMINA TEST");

	/*FILE* pFile = fopen("/data/data/com.appmunki.miragemobile/files/Data.txt",
	 "rb");

	 long lSize;
	 char * buffer;
	 size_t sresult;

	 if (pFile == NULL)
	 {
	 fputs("File error", stderr);
	 exit(1);
	 }

	 // obtain file size:
	 fseek(pFile, 0, SEEK_END);

	 lSize = ftell(pFile);

	 rewind(pFile);

	 // allocate memory to contain the whole file:
	 buffer = (char*) malloc(sizeof(char) * lSize);
	 if (buffer == NULL)
	 {
	 fputs("Memory error", stderr);
	 exit(2);
	 }

	 // copy the file into the buffer:
	 sresult = fread(buffer, 1, lSize, pFile);
	 if (sresult != lSize)
	 {
	 fputs("Reading error", stderr);
	 exit(3);
	 }

	 // the whole file is now loaded in the memory buffer.

	 int dataSize, count = 0;
	 char *endPtr;
	 dataSize = strtol(buffer, &endPtr, 10);
	 float *mdata = new float[dataSize];
	 // read data as an array of float number
	 for (int i = 0; i < dataSize; ++i)
	 {
	 mdata[i] = strtod(endPtr, &endPtr);
	 //LOGE("data: %f",mdata[i]);
	 }
	 readDatabase(mdata, count);
	 loaded = true;
	 LOG("Done");
	 */
}
inline void extractFeatures(const Mat& img, Mat& des, vector<KeyPoint>& keys) {
	// detect image keypoints

	cv::ORB sfd1(1000);
	cv::FREAK sde;

	sfd1.detect(img, keys);
	//LOG("Train keys size start %d",keys.size() );
	int s = 3000;
	// selec                                                                     t only the appropriate number of keypoints
	while (keys.size() > 1000) {
		//cerr << "Train keys size " << keys.size() << endl;
		keys.clear();
		ORB sfd1(s + 500);
		s += 500;
		sfd1.detect(img, keys);
	}

	// compute image descriptor
	sde.compute(img, keys, des);
}
inline bool refineMatchesWithHomography(int it, float &confidence, const std::vector<cv::KeyPoint>& queryKeypoints,
		const std::vector<cv::KeyPoint>& trainKeypoints, float reprojectionThreshold, std::vector<cv::DMatch>& matches, cv::Mat& homography) {
	const unsigned int minNumberMatchesAllowed = 8;

	if (matches.size() < minNumberMatchesAllowed)
		return false;

	// Prepare data for cv::findHomography
	std::vector<cv::Point2f> srcPoints(matches.size());
	std::vector<cv::Point2f> dstPoints(matches.size());

	for (size_t i = 0; i < matches.size(); i++) {
		srcPoints[i] = trainKeypoints[matches[i].trainIdx].pt;
		dstPoints[i] = queryKeypoints[matches[i].queryIdx].pt;
	}

	// Find homography matrix and get inliers mask
	std::vector<unsigned char> inliersMask(srcPoints.size());
	homography = cv::findHomography(srcPoints, dstPoints, CV_FM_RANSAC, reprojectionThreshold, inliersMask);

	std::vector<cv::DMatch> inliers;
	for (size_t i = 0; i < inliersMask.size(); i++) {
		if (inliersMask[i])
			inliers.push_back(matches[i]);
	}

	confidence = (inliers.size() / (8 + 0.3 * matches.size())) * 100;
	//LOG("Confidence %d %f", it, confidence);
	matches.swap(inliers);
	return matches.size() > minNumberMatchesAllowed && (confidence > 55);
}

/**
 * Match the query image to images in database. The best matches are returned
 */
inline void match(const Mat& m_grayImg, const vector<KeyPoint> &trainKeys, const Mat &trainDes, vector<pair<float, int> > &result) {
	int runs = 0;
	// use Flann based matcher to match images
	// cv::FlannBasedMatcher bf(new flann::LshIndexParams(30,8,2));
	cv::BFMatcher bf(NORM_HAMMING);
	float confidence = 0;
	// train the query image
	int size = queryDes.size();
	for (int i = 0; i < size; ++i) {

		// compute match score for each image in the database
		vector<DMatch> matches;
		vector<DMatch> refinedmatches;

		bf.match(queryDes[i], trainDes, matches);
//

		//Find homography transformation and detect good matches
		cv::Mat m_roughHomography;
		cv::Mat m_refinedHomography;

		bool homographyFound = refineMatchesWithHomography(i, confidence, queryKeys[i], trainKeys,

		4, matches, m_roughHomography);
		LOG("Matching %d Step 1: %d", i, matches.size());

		if (homographyFound) {
			LOG("Matching %d Step 1: %d", i, matches.size());

			Mat m_warpedImg;
			Size size = queryDims[i];
			//cerr<<"Size"<<m_grayImg.cols<<" : "<<m_grayImg.rows<<endl;
			cv::warpPerspective(m_grayImg, m_warpedImg, m_roughHomography, size, cv::INTER_LINEAR);

			//Extract Warped Image Keys
			Mat warpDes;
			vector<KeyPoint> warpKeys;
			extractFeatures(m_grayImg, warpDes, warpKeys);

			//Match
			bf.match(queryDes[i], warpDes, refinedmatches);

			homographyFound = refineMatchesWithHomography(i, confidence, queryKeys[i], warpKeys,

			4, refinedmatches, m_refinedHomography);
			if (homographyFound) {
				LOG("Matching %d Step 1: %d", i, matches.size());
				pair<float, int> p(matches.size(), i);
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
	return a > b ? b : a;
}

JNIEXPORT jfloatArray JNICALL
Java_com_appmunki_miragemobile_ar_Matcher_loadImage(JNIEnv *env, jobject obj, long addrGray) {

	Mat& testImage = *(Mat*) addrGray;
	//LOG("CAMBIAR IMAGEN");

	if (patternImage.empty()) {
		LOG("Input image cannot be read");
	} else {
		//LOG("Input image WORKS");
		processSingleImage(patternImage, testImage);
	}
}

JNIEXPORT jfloatArray JNICALL
Java_com_appmunki_miragemobile_ar_Matcher_getMatrix(JNIEnv *env, jobject obj) {
	jfloatArray result;
	result = env->NewFloatArray(16);
	if (result == NULL) {
		return NULL; /* out of memory error thrown */
	}

	jfloat array1[16];

	for (int i = 0; i < 16; ++i) {
		array1[i] = glMatrixTest.data[i];
	}

	env->SetFloatArrayRegion(result, 0, 16, array1);
	return result;
}



JNIEXPORT jfloatArray JNICALL
Java_com_appmunki_miragemobile_ar_Matcher_getProjectionMatrix(JNIEnv *env, jobject obj) {
	buildProjectionMatrix(480,640,glProjectionMatrix);

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

JNIEXPORT void JNICALL
Java_com_appmunki_miragemobile_ar_Matcher_match(JNIEnv* env, jobject obj, long addrGray) {
	if (loaded) {
		Mat& img = *(Mat*) addrGray;

		// read image from file
		vector<KeyPoint> trainKeys;
		Mat trainDes;
		vector<pair<float, int> > result;
		// detect image keypoints
		extractFeatures(img, trainDes, trainKeys);

		if (!trainKeys.size()) {
			trainDes.release();
			trainKeys.clear();
			return;
		}
		LOG("Matching begin");
		match(img, trainKeys, trainDes, result);
		int size = min(result.size(), MAX_ITEM);

		// print out the best result
		if (result.size()) {
			LOG("Size: %d\n", result.size());

			for (int i = 0; i < size; ++i) {
				LOG("%f  %d", result[i].first, result[i].second);
			}
		}
		trainDes.release();
		trainKeys.clear();
		LOG("Matching end");
		return;
	}
}

#ifdef __cplusplus
}
#endif
