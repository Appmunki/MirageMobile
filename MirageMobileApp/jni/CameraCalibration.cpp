
// File includes:
#include "CameraCalibration.hpp"

CameraCalibration::CameraCalibration()
{
}

CameraCalibration::CameraCalibration(float _fx, float _fy, float _cx, float _cy)
{
    m_intrinsic = cv::Matx33f::zeros();

    fx() = _fx;
    fy() = _fy;
    cx() = _cx;
    cy() = _cy;

    m_distortion.create(5,1);
    for (int i=0; i<5; i++)
        m_distortion(i) = 0;
}

CameraCalibration::CameraCalibration(float _fx, float _fy, float _cx, float _cy, float distorsionCoeff[5])
{
    m_intrinsic = cv::Matx33f::zeros();

    fx() = _fx;
    fy() = _fy;
    cx() = _cx;
    cy() = _cy;

    m_distortion.create(5,1);
    for (int i=0; i<5; i++)
        m_distortion(i) = distorsionCoeff[i];
}

const cv::Matx33f& CameraCalibration::getIntrinsic() const
{
    return m_intrinsic;
}

const cv::Mat_<float>&  CameraCalibration::getDistorsion() const
{
    return m_distortion;
}

float& CameraCalibration::fx()
{
    return m_intrinsic(1,1);
}

float& CameraCalibration::fy()
{
    return m_intrinsic(0,0);
}

float& CameraCalibration::cx()
{
    return m_intrinsic(0,2);
}

float& CameraCalibration::cy()
{
    return m_intrinsic(1,2);
}

float CameraCalibration::fx() const
{
    return m_intrinsic(1,1);
}

float CameraCalibration::fy() const
{
    return m_intrinsic(0,0);
}

float CameraCalibration::cx() const
{
    return m_intrinsic(0,2);
}

float CameraCalibration::cy() const
{
    return m_intrinsic(1,2);
}

void CameraCalibration::getProjectionMatrix(int screen_width, int screen_height, Matrix44& projectionMatrix)
{
   float nearPlane = 0.01f; // Near clipping distance
   float farPlane = 100.0f; // Far clipping distance



   // Camera parameters
   float f_x = CameraCalibration::fx(); // Focal length in x axis
   float f_y = CameraCalibration::fy(); // Focal length in y axis (usually the same?)
   float c_x = CameraCalibration::cx(); // Camera primary point x
   float c_y = CameraCalibration::cy(); // Camera primary point y
   LOG("calibration %f,%f,%f,%f",f_x,f_y,c_x,c_y);

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
