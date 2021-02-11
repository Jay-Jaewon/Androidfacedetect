#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>
#include <android/log.h>

using namespace cv;
using namespace std;

//변수명 앞에 const가 붙으면 상수취급한다. (절대로 바꿀 수 없음).
//포인터의 경우 가리키는 대상은 변경할 수 있지만, 대상의 값은 변경할 수 없다.
//

//opencv에서의 Mat이란?
//opencv 컴퓨터 비전을 처리해주는 오픈소스 라이브러리이다. 우리는 컴퓨터로 이미지를 보지만, 이미지는 각지점에 대한
//강도를 숫자로 보여줄 뿐이다. 이때 이 숫자들을 처리하는 객체가 Mat이다.
//Mat 객체는 더 이상 수동으로 메모리를 할당하고 해제할 필요가 없음. 작업을 수행하는데 필요한 메모리만 사용한다.

//Mat 기본적으로 2가지로 구성됨. 1.매트릭스 헤더(매트릭스(행렬)의 크기, 저장에 사용되는 방법, 매트릭스가 저장되는 주 등)를 포함.
//2. 픽셀값을 가리키는 포인터.(저장하기위해 선택한 모든차원(채널)을 포함함.)
//opencv는 참조 계산 시스템을 사용함.(매트릭스 포인터를 공유함)
//복사 생상저와 대입연산자 는 헤더와 큰 매트릭스를 가리키는 포인터만 복사함. 데이터 그자체를 복사하지 않음.
// ROI(부분관심영역)을 사용하면 데이터의 서브파트부분만 따로 가리키게 할 수 잇다.

//Mat D (A, Rect(10, 10, 100, 100) ); // using a rectangle
//Mat E = A(Range::all(), Range(1,3)); // using row and column boundaries

//독립된 새로은 Mat를 복사하고 싶다면, cv::Mat::clone(), cv::Mat::copyTo() 사용.

//RGB가 일반적이지만 opencv에서는 BGR이 사용됨 (B,R의 위치가 바뀜)
//Mat 생성하기 Mat MatName(행수,열수,포맷,값)Mat M(2,2, CV_8UC3, Scalar(0,0,255));
//https://docs.opencv.org/4.3.0/d6/d6d/tutorial_mat_the_basic_image_container.html  그외 다양한  생성 방법

//Point2f, Point3f 2차원, 3차원 포인트. [2,1], [3,1,2] 이런 느낌임.

//c언어에서는 포인터라는 변수를 사용함. 포인터 변수 앞에는 * 값이 붙음. 포인터는 변수의 주소를 나타내는 변수임.
// & 는 주소값을 반환하는 용도로 사용됨.



//Sunglasses함수에서 선글라스 이미지와 화면이미지를 합치는(overlay)해주는 함수. 미리 선언해주었다.
void overlayImage(const Mat &background, const Mat &foreground,
                  Mat &output, Point2i location);

//
extern "C"
JNIEXPORT void JNICALL
Java_com_teamnova_jaycameraapp1_MainActivity_ConvertRGBA2Gray(JNIEnv *env, jobject thiz,
                                                              jlong mat_addr_input,
                                                              jlong mat_addr_result) {
    //파라미터로 받은 input 과 result 값의 주소를 가져와서 Mat 객체 생성.
    Mat &matInput = *(Mat *)mat_addr_input;
    Mat &matResult = *(Mat *)mat_addr_result;

    //input, result, 색상변경방식 을 입력하여 색상 변경. result 회색색상으로 변경.
    cvtColor(matInput, matResult, COLOR_RGBA2GRAY);

}

extern "C"
JNIEXPORT void JNICALL
Java_com_teamnova_jaycameraapp1_MainActivity_ConvertRGBA2Canny(JNIEnv *env, jobject thiz,
                                                               jlong mat_addr_input,
                                                               jlong mat_addr_result) {
    //파라미터로 받은 input 과 result 값의 주소를 가져와서 Mat 객체 생성.
    Mat &matInput = *(Mat *)mat_addr_input;
    Mat &matResult = *(Mat *)mat_addr_result;

    // input, result, 회색으로 변경.
    cvtColor(matInput, matResult, COLOR_RGBA2GRAY);
    // Canny에서 입력값 result 는 회색이어야한다.

    blur (matResult,matResult, Size(5,5));
    //threhold 값이 증가할 수록 희미해짐.(경계표시가 되지 않음)
    Canny(matResult,matResult,40,40);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_teamnova_jaycameraapp1_MainActivity_ConvertRGBA2Reverse(JNIEnv *env, jobject thiz,
                                                                 jlong mat_addr_input,
                                                                 jlong mat_addr_result) {
    //파라미터로 받은 input 과 result 값의 주소를 가져와서 Mat 객체 생성.
    Mat &matInput = *(Mat *)mat_addr_input;
    Mat &matResult = *(Mat *)mat_addr_result;
    // result의 색상을 반전시켜줌. (정확하게는 hue saturation value로 변환.)
    cvtColor(matInput, matResult, COLOR_RGB2HSV);
}
// comic필터는 mainActivity에 구현되어 있음.
//extern "C"
//JNIEXPORT void JNICALL
//Java_com_teamnova_jaycameraapp1_MainActivity_ConvertRGBA2Comic(JNIEnv *env, jobject thiz,
//                                                               jlong mat_addr_input,
//                                                               jlong mat_addr_result) {
//    Mat &matInput = *(Mat *)mat_addr_input;
//    Mat &matResult = *(Mat *)mat_addr_result;
//
//
//}

// 얼굴인식에서 사용되는 함수.
float resize(Mat img_src, Mat &img_resize, int resize_width){

    float scale = resize_width / (float)img_src.cols ;
    if (img_src.cols > resize_width) {
        int new_height = cvRound(img_src.rows * scale);
        resize(img_src, img_resize, Size(resize_width, new_height));
    }
    else {
        img_resize = img_src;
    }
    return scale;
}


extern "C"
JNIEXPORT jlong JNICALL
Java_com_teamnova_jaycameraapp1_MainActivity_loadCascade(JNIEnv *env, jobject thiz,
                                                         jstring cascade_file_name) {

    const char *nativeFileNameString = env->GetStringUTFChars(cascade_file_name, 0);


    string baseDir("/storage/emulated/0/");

    baseDir.append(nativeFileNameString);

    const char *pathDir = baseDir.c_str();


    jlong ret = 0;
    //이름을 입력받으면 저장한 xml파일을 cascadeClassifier로 생성함.
    ret = (jlong) new CascadeClassifier(pathDir);
    if (((CascadeClassifier *) ret)->empty()) {
        __android_log_print(ANDROID_LOG_DEBUG, "native-lib :: ",
                            "CascadeClassifier로 로딩 실패  %s", nativeFileNameString);
    }
    else
        __android_log_print(ANDROID_LOG_DEBUG, "native-lib :: ",
                            "CascadeClassifier로 로딩 성공 %s", nativeFileNameString);
    env->ReleaseStringUTFChars(cascade_file_name, nativeFileNameString);
    return ret;



}

//얼굴인식 함수.
extern "C"
JNIEXPORT void JNICALL
Java_com_teamnova_jaycameraapp1_MainActivity_detect(JNIEnv *env, jobject thiz,
                                                    jlong cascade_classifier_face,
                                                    jlong cascade_classifier_eye,
                                                    jlong mat_addr_input, jlong mat_addr_result) {
    //파라미터로 받은 input 과 result 값의 주소를 가져와서 Mat 객체 생성.
    Mat &img_input = *(Mat *) mat_addr_input;
    Mat &img_result = *(Mat *) mat_addr_result;
    img_result = img_input.clone();

    //검출할 얼굴 을 네모모양좌표 저장.
    std::vector<Rect> faces;

    Mat img_gray;
    cvtColor(img_input, img_gray, COLOR_BGR2GRAY);

    //이미지의 명암대비를 높여 인식율을 높여줌. fps가 너무 떨어져서 생략. 실제로 기능 저하는 크지않음.
    //equalizeHist(img_gray, img_gray);
    Mat img_resize;
    float resizeRatio = resize(img_gray, img_resize, 240);

    //-- 얼굴인식 부분. minNeighbors = 3으로 수정.
    //img_resize 는 gray scale 이다.
    ((CascadeClassifier *) cascade_classifier_face)->detectMultiScale( img_resize, faces, 1.1, 3, 0|CASCADE_SCALE_IMAGE, Size(30, 30) );

    //얼굴별로 좌표값을 받아와서 타원을 그려준다. 그리고 눈 탐색.
    for (int i = 0; i < faces.size(); i++) {
        double real_facesize_x = faces[i].x / resizeRatio;
        double real_facesize_y = faces[i].y / resizeRatio;
        double real_facesize_width = faces[i].width / resizeRatio;
        double real_facesize_height = faces[i].height / resizeRatio;

        Point center( real_facesize_x + real_facesize_width / 2, real_facesize_y + real_facesize_height/2);
        ellipse(img_result, center, Size( real_facesize_width / 2, real_facesize_height / 2), 0, 0, 360,
                Scalar(0, 255, 0), 2, 4, 0);
        //얼굴영역 좌표.
        Rect face_area(real_facesize_x, real_facesize_y, real_facesize_width,real_facesize_height);
        Mat faceROI = img_gray( face_area );
        std::vector<Rect> eyes;
        //-- 각 얼굴마다 눈을 인식.
        ((CascadeClassifier *) cascade_classifier_eye)->detectMultiScale( faceROI, eyes, 1.1, 3, 0 |CASCADE_SCALE_IMAGE, Size(30, 30) );
        for ( size_t j = 0; j < eyes.size(); j++ )
        {
            //눈 하나의 중심 좌표.
            Point eye_center( real_facesize_x + eyes[j].x + eyes[j].width/2, real_facesize_y + eyes[j].y + eyes[j].height/2 );
            int radius = cvRound( (eyes[j].width + eyes[j].height)*0.25 );
            circle( img_result, eye_center, radius, Scalar( 255, 0, 0 ), 2, 4, 0 );
        }
    }


//실험
// opencv 공식 홈페이지 예제
    //파라미터로 받은 input 과 result 값의 주소를 가져와서 Mat 객체 생성.
//    Mat &img_input = *(Mat *) mat_addr_input;
//    Mat &img_result = *(Mat *) mat_addr_result;
//    img_result = img_input.clone();
//
//    double scale = 1.3;
//    std::vector<Rect> faces;
//    Mat img_gray,img_small_roi;
//    cvtColor(img_input, img_gray, COLOR_BGR2GRAY);
//    resize(img_gray, img_small_roi, Size(),1,1,INTER_LINEAR_EXACT);
//    equalizeHist(img_small_roi,img_small_roi);
//
//    ((CascadeClassifier *) cascade_classifier_face)->detectMultiScale(img_small_roi, faces, 1.1, 2,
//            0|CASCADE_SCALE_IMAGE, Size(30, 30) );
//
//    for ( size_t i = 0; i < faces.size(); i++ )
//    {
//        Rect r = faces[i];
//        Mat smallImgROI;
//        vector<Rect> eyes;
//        Point center;
//        Scalar color = Scalar(100,0,0);
//        int radius;
//        double aspect_ratio = (double)r.width/r.height;
//
//
//        center.x = cvRound((r.x + r.width*0.5)*scale);
//        center.y = cvRound((r.y + r.height*0.5)*scale);
//        radius = cvRound((r.width + r.height)*0.25*scale);
//        circle( img_result, center, radius, color, 2, 8, 0 );
//        smallImgROI = img_small_roi( r );
//        ((CascadeClassifier *) cascade_classifier_eye)->detectMultiScale(smallImgROI, eyes, 1.1, 2,
//                0 |CASCADE_SCALE_IMAGE, Size(30, 30) );
//        for (size_t j = 0; j < eyes.size(); j++ )
//        {
//            Rect nr = eyes[j];
//            center.x = cvRound((r.x + nr.x + nr.width*0.5)*scale);
//            center.y = cvRound((r.y + nr.y + nr.height*0.5)*scale);
//            radius = cvRound((nr.width + nr.height)*0.25*scale);
//            circle( img_result, center, radius, (255,0,0), 2, 8, 0 );
//        }
//    }


}
//일반 카메라 필터. A는 투명도를 나타내는 값인데 input을 그냥 반환하면 일관성이 없어져서 result로
//A값을 뺀 RGB를 반환하도록함.
extern "C"
JNIEXPORT void JNICALL
Java_com_teamnova_jaycameraapp1_MainActivity_ConvertRGBA2RGB(JNIEnv *env, jobject thiz,
                                                             jlong mat_addr_input,
                                                             jlong mat_addr_result) {
    Mat &matInput = *(Mat *)mat_addr_input;
    Mat &matResult = *(Mat *)mat_addr_result;
    cvtColor(matInput, matResult, COLOR_RGBA2RGB);
}


//얼굴인식후 눈인식후 왼쪽눈을 기준으로 선글라스를 씌움.
//선글라스 예제는 수정이 필요하여 수정함.
extern "C"
JNIEXPORT void JNICALL
Java_com_teamnova_jaycameraapp1_MainActivity_SunGlasses(JNIEnv *env, jobject thiz,
                                                        jlong cascade_classifier_face,
                                                        jlong cascade_classifier_eye,
                                                        jlong sun_glass, jlong mat_addr_input,
                                                        jlong mat_addr_result) {
    //파라미터로 받은 input 과 result 값의 주소를 가져와서 Mat 객체 생성.
    Mat &img_input = *(Mat *) mat_addr_input;
    Mat &img_result = *(Mat *) mat_addr_result;

    Mat &glass = *(Mat *) sun_glass;

    img_result = img_input.clone();
    double scale =  1;

    //인식된 얼굴의 네모 좌표.
   vector<Rect> faces;

    Mat gray, smallImg;

    cvtColor( img_input, gray, COLOR_RGBA2GRAY );
    double fx = 1 / scale;
    resize( gray, smallImg, Size(), fx, fx, INTER_LINEAR_EXACT );
    //그레이 이미지의 명암대비를 증가시켜 뚜렷하게 해주는 함수. fps가 너무 떨어지고 성능차이가 크지 않아 주석 처리하였다.
    //equalizeHist( smallImg, smallImg );

    ((CascadeClassifier*)cascade_classifier_face)->detectMultiScale( smallImg, faces,
                                                                     1.1, 3, 0|CASCADE_SCALE_IMAGE,Size(30, 30) );

    for ( size_t i = 0; i < faces.size(); i++ )
    {
        Rect r = faces[i];

        // 감지된 얼굴 영역.
        Mat smallImgROI;

        // 감지된 얼굴 영역에서 눈의 네모 좌표.
        vector<Rect> eyes;

        Point center;

        int radius;
        double aspect_ratio = (double)r.width/r.height;

        center.x = cvRound((r.x + r.width*0.5)*scale);
        center.y = cvRound((r.y + r.height*0.5)*scale);

        smallImgROI = smallImg( r );
        ((CascadeClassifier*)cascade_classifier_eye)->detectMultiScale(smallImgROI, eyes,
                                                                       1.1, 3, 0|CASCADE_SCALE_IMAGE, Size(30, 30) );
        vector<Point> points;
        for (size_t j = 0; j < eyes.size(); j++ )
        {
            Rect eyes_rect = eyes[j];
            center.x = cvRound((r.x + eyes_rect.x + eyes_rect.width * 0.5) * scale);
            center.y = cvRound((r.y + eyes_rect.y + eyes_rect.height * 0.5) * scale);
            Point p(center.x, center.y);
            points.push_back(p);
        }
        //눈이 2개 감지되었을때,
        if ( points.size() == 2){
            Point center1 = points[0];
            Point center2 = points[1];
            //눈이 어느쪽인지 분별.(오른쪽 왼쪽)
            if ( center1.x > center2.x ){
                Point temp;
                temp = center1;
                center1 = center2;
                center2 = temp;
            }
            //선글라스의 크기를 조절하기 위해 두눈 사이의 x,y좌표 차이를 구함.
            int width = abs(center2.x - center1.x);
            int height = abs(center2.y - center1.y);
            if (width > height){
                //이미지 크기 변수.
                float imgScale = width/70.0;
                int w, h;
                w = glass.cols * imgScale;
                h = glass.rows * imgScale;
                // x,y좌표 적절하게 조졍해줌.
                int offsetX = 210 * imgScale;
                int offsetY = 105 * imgScale;
                Mat resized_glasses;
                //선글라스 크기 조절.
                resize( glass, resized_glasses, cv::Size( w, h), 0, 0 );
                overlayImage(img_input, resized_glasses, img_result, Point(center1.x-offsetX, center1.y-offsetY));
            }
        }
    }


// 실패
//    //파라미터로 받은 input 과 result 값의 주소를 가져와서 Mat 객체 생성.
//    Mat &img_input = *(Mat *) mat_addr_input;
//    Mat &img_result = *(Mat *) mat_addr_result;
//    img_result = img_input.clone();
//
//    Mat &glass = *(Mat *) sun_glass;
//    //검출할 얼굴 을 네모모양좌표 저장.
//    std::vector<Rect> faces;
//
//    Mat img_gray;
//    cvtColor(img_input, img_gray, COLOR_BGR2GRAY);
//    double scale = 1;
//    //이미지의 명암대비를 높여 인식율을 높여줌. fps가 너무 떨어져서 생략. 실제로 기능 저하는 크지않음.
//    //equalizeHist(img_gray, img_gray);
//    Mat img_resize;
//    resize( gray, smallImg, Size(), fx, fx, INTER_LINEAR_EXACT );
//    float resizeRatio = resize(img_gray, img_resize, 240);
//
//    //-- 얼굴인식 부분. minNeighbors = 3으로 수정.
//    //img_resize 는 gray scale 이다.
//    ((CascadeClassifier *) cascade_classifier_face)->detectMultiScale( img_resize, faces, 1.1, 2, 0|CASCADE_SCALE_IMAGE, Size(30, 30) );
//
//    //얼굴별로 좌표값을 받아와서 타원을 그려준다. 그리고 눈 탐색.
//    for (int i = 0; i < faces.size(); i++) {
//        double real_facesize_x = faces[i].x / resizeRatio;
//        double real_facesize_y = faces[i].y / resizeRatio;
//        double real_facesize_width = faces[i].width / resizeRatio;
//        double real_facesize_height = faces[i].height / resizeRatio;
//
////        Point center( real_facesize_x + real_facesize_width / 2, real_facesize_y + real_facesize_height/2);
////        ellipse(img_result, center, Size( real_facesize_width / 2, real_facesize_height / 2), 0, 0, 360,
////                Scalar(0, 255, 0), 2, 4, 0);
//        //얼굴영역 좌표.
//        Rect face_area(real_facesize_x, real_facesize_y, real_facesize_width,real_facesize_height);
//        Mat faceROI = img_gray( face_area );
//        std::vector<Rect> eyes;
//        //-- 각 얼굴마다 눈을 인식.
//        ((CascadeClassifier *) cascade_classifier_eye)->detectMultiScale( faceROI, eyes, 1.1, 2, 0 |CASCADE_SCALE_IMAGE, Size(30, 30) );
//        if(eyes.size() >= 2){
//            //첫번째 눈, 두번째 눈 부득이하게 1,2를 사용하였습니다.
//            Rect eyes_rect1 = eyes[0];
//            Rect eyes_rect2 = eyes[1];
//            Point center1 = Point((faces[i].x + eyes[0].x + eyes[0].width*0.5)*scale,(faces[i].y + eyes[0].y + eyes[0].height*0.5)*scale);
//            Point center2 = Point((faces[i].x + eyes[1].x + eyes[1].width*0.5)*scale,(faces[i].y + eyes[1].y + eyes[1].height*0.5)*scale);
//            if ( center1.x > center2.x ){
//                Point temp;
//                temp = center1;
//                center1 = center2;
//                center2 = temp;
//            }
//            int width = abs(center2.x - center1.x);
//            int height = abs(center2.y - center1.y);
//            float imgScale = width/70.0;
//            int glass_width = glass.cols * imgScale;
//            int glass_height = glass.rows * imgScale;
//            Mat resized_glasses;
//            int offsetX = 210 * imgScale;
//            int offsetY = 105 * imgScale;
//            resize( glass, resized_glasses, cv::Size(glass_width, glass_height), 0, 0 );
//            overlayImage(img_input, resized_glasses, img_result, Point(center1.x-offsetX, center1.y-offsetY));
//        }
//
//    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_teamnova_jaycameraapp1_MainActivity_ConvertRGBA2YcrCb(JNIEnv *env, jobject thiz,
                                                               jlong mat_addr_input,
                                                               jlong mat_addr_result) {
    //파라미터로 받은 input 과 result 값의 주소를 가져와서 Mat 객체 생성.
    Mat &matInput = *(Mat *)mat_addr_input;
    Mat &matResult = *(Mat *)mat_addr_result;
    //결과프레임에 주황색 필터를 씌움.
    cvtColor(matInput, matResult, COLOR_RGB2YCrCb);
}extern "C"
JNIEXPORT void JNICALL
Java_com_teamnova_jaycameraapp1_MainActivity_ConvertRGBA2Luv(JNIEnv *env, jobject thiz,
                                                             jlong mat_addr_input,
                                                             jlong mat_addr_result) {
    //파라미터로 받은 input 과 result 값의 주소를 가져와서 Mat 객체 생성.
    Mat &matInput = *(Mat *)mat_addr_input;
    Mat &matResult = *(Mat *)mat_addr_result;
    //결과프레임에 분홍색 필터를 씌움.
    cvtColor(matInput, matResult, COLOR_RGB2Luv);
}

//좌표값을 받으면 이미지를 올려주는
//TODO 함수... 이해가 좀더 필요하다.
void overlayImage(const Mat &background, const Mat &foreground,
                  Mat &output, Point2i location)
{
    background.copyTo(output);
    // start at the row indicated by location, or at row 0 if location.y is negative.
    // 열부터 시작.
    for (int y = std::max(location.y, 0); y < background.rows; ++y)
    {
        int foregroundY = y - location.y; // because of the translation
        // we are done of we have processed all rows of the foreground image.
        if (foregroundY >= foreground.rows){
            break;
        }
        // start at the column indicated by location, or at column 0 if location.x is negative.
        // 행 시작.
        for (int x = std::max(location.x, 0); x < background.cols; ++x)
        {
            int foregroundX = x - location.x; // because of the translation.
            // we are done with this row if the column is outside of the foreground image.
            // 열끝.
            if (foregroundX >= foreground.cols){
                break;
            }
            // determine the opacity of the foregrond pixel, using its fourth (alpha) channel.
            // 투명도 정의
            double opacity =
                    ((double)foreground.data[foregroundY * foreground.step + foregroundX * foreground.channels() + 3])
                    / 255.;
            // and now combine the background and foreground pixel, using the opacity,
            // but only if opacity > 0.
            //배경과 사진 픽셀을 투명도와 함께 결합암. 투명도가 0 초과 일때만.
            for (int c = 0; opacity > 0 && c < output.channels(); ++c)
            {
                unsigned char foregroundPx =
                        foreground.data[foregroundY * foreground.step + foregroundX * foreground.channels() + c];
                unsigned char backgroundPx =
                        background.data[y * background.step + x * background.channels() + c];
                output.data[y*output.step + output.channels()*x + c] =
                        backgroundPx * (1. - opacity) + foregroundPx * opacity;
            }
        }
    }
}


// 아래 코드들은 MainActivity에서 카메라 프레임이 변수였다면 단순히 이미지로 변환된것. 큰 차이는 없다.
extern "C"
JNIEXPORT void JNICALL
Java_com_teamnova_jaycameraapp1_EditActivity_ConvertRGBA2YcrCb(JNIEnv *env, jobject thiz,
                                                               jlong mat_addr_input,
                                                               jlong mat_addr_result) {
    //파라미터로 받은 input 과 result 값의 주소를 가져와서 Mat 객체 생성.
    Mat &matInput = *(Mat *)mat_addr_input;
    Mat &matResult = *(Mat *)mat_addr_result;
    //결과프레임에 주황색 필터를 씌움.
    cvtColor(matInput, matResult, COLOR_RGB2YCrCb);

}extern "C"
JNIEXPORT void JNICALL
Java_com_teamnova_jaycameraapp1_EditActivity_ConvertRGBA2Luv(JNIEnv *env, jobject thiz,
                                                             jlong mat_addr_input,
                                                             jlong mat_addr_result) {
    //파라미터로 받은 input 과 result 값의 주소를 가져와서 Mat 객체 생성.
    Mat &matInput = *(Mat *)mat_addr_input;
    Mat &matResult = *(Mat *)mat_addr_result;
    //결과프레임에 분홍색 필터를 씌움.
    cvtColor(matInput, matResult, COLOR_RGB2Luv);
}extern "C"
JNIEXPORT void JNICALL
Java_com_teamnova_jaycameraapp1_EditActivity_ConvertRGBA2Gray(JNIEnv *env, jobject thiz,
                                                              jlong mat_addr_input,
                                                              jlong mat_addr_result) {
    //파라미터로 받은 input 과 result 값의 주소를 가져와서 Mat 객체 생성.
    Mat &matInput = *(Mat *)mat_addr_input;
    Mat &matResult = *(Mat *)mat_addr_result;

    //input, result, 색상변경방식 을 입력하여 색상 변경. result 회색색상으로 변경.
    cvtColor(matInput, matResult, COLOR_RGBA2GRAY);

}extern "C"
JNIEXPORT void JNICALL
Java_com_teamnova_jaycameraapp1_EditActivity_ConvertRGBA2Canny(JNIEnv *env, jobject thiz,
                                                               jlong mat_addr_input,
                                                               jlong mat_addr_result) {
    //파라미터로 받은 input 과 result 값의 주소를 가져와서 Mat 객체 생성.
    Mat &matInput = *(Mat *)mat_addr_input;
    Mat &matResult = *(Mat *)mat_addr_result;

    // input, result, 회색으로 변경.
    cvtColor(matInput, matResult, COLOR_RGBA2GRAY);
    // Canny에서 입력값 result 는 회색이어야한다.
    blur (matResult,matResult, Size(5,5));
    //threhold 값이 증가할 수록 희미해짐.(경계표시가 되지 않음)
    Canny(matResult,matResult,40,40);


}extern "C"
JNIEXPORT void JNICALL
Java_com_teamnova_jaycameraapp1_EditActivity_ConvertRGBA2Reverse(JNIEnv *env, jobject thiz,
                                                                 jlong mat_addr_input,
                                                                 jlong mat_addr_result) {
    //파라미터로 받은 input 과 result 값의 주소를 가져와서 Mat 객체 생성.
    Mat &matInput = *(Mat *)mat_addr_input;
    Mat &matResult = *(Mat *)mat_addr_result;
    // result의 색상을 반전시켜줌. (정확하게는 hue saturation value로 변환.)
    cvtColor(matInput, matResult, COLOR_RGB2HSV);
}extern "C"
JNIEXPORT void JNICALL
Java_com_teamnova_jaycameraapp1_EditActivity_ConvertRGBA2RGB(JNIEnv *env, jobject thiz,
                                                             jlong mat_addr_input,
                                                             jlong mat_addr_result) {
    Mat &matInput = *(Mat *)mat_addr_input;
    Mat &matResult = *(Mat *)mat_addr_result;
    cvtColor(matInput, matResult, COLOR_RGBA2RGB);
}extern "C"
JNIEXPORT void JNICALL
Java_com_teamnova_jaycameraapp1_EditActivity_SunGlasses(JNIEnv *env, jobject thiz,
                                                        jlong cascade_classifier_face,
                                                        jlong cascade_classifier_eye,
                                                        jlong sun_glass, jlong mat_addr_input,
                                                        jlong mat_addr_result) {
    //파라미터로 받은 input 과 result 값의 주소를 가져와서 Mat 객체 생성.
    Mat &img_input = *(Mat *) mat_addr_input;
    Mat &img_result = *(Mat *) mat_addr_result;

    Mat &glass = *(Mat *) sun_glass;

    img_result = img_input.clone();
    double scale =  1;

    //인식된 얼굴의 네모 좌표.
    vector<Rect> faces;

    Mat gray, smallImg;

    cvtColor( img_input, gray, COLOR_RGBA2GRAY );
    double fx = 1 / scale;
    resize( gray, smallImg, Size(), fx, fx, INTER_LINEAR_EXACT );
    //그레이 이미지의 명암대비를 증가시켜 뚜렷하게 해주는 함수. fps가 너무 떨어지고 성능차이가 크지 않아 주석 처리하였다.
    //equalizeHist( smallImg, smallImg );

    ((CascadeClassifier*)cascade_classifier_face)->detectMultiScale( smallImg, faces,
                                                                     1.1, 3, 0|CASCADE_SCALE_IMAGE,Size(30, 30) );

    for ( size_t i = 0; i < faces.size(); i++ )
    {
        Rect r = faces[i];

        // 감지된 얼굴 영역.
        Mat smallImgROI;

        // 감지된 얼굴 영역에서 눈의 네모 좌표.
        vector<Rect> eyes;

        Point center;

        int radius;
        double aspect_ratio = (double)r.width/r.height;

        center.x = cvRound((r.x + r.width*0.5)*scale);
        center.y = cvRound((r.y + r.height*0.5)*scale);

        smallImgROI = smallImg( r );
        ((CascadeClassifier*)cascade_classifier_eye)->detectMultiScale(smallImgROI, eyes,
                                                                       1.1, 3, 0|CASCADE_SCALE_IMAGE, Size(30, 30) );
        vector<Point> points;
        for (size_t j = 0; j < eyes.size(); j++ )
        {
            Rect eyes_rect = eyes[j];
            center.x = cvRound((r.x + eyes_rect.x + eyes_rect.width * 0.5) * scale);
            center.y = cvRound((r.y + eyes_rect.y + eyes_rect.height * 0.5) * scale);
            Point p(center.x, center.y);
            points.push_back(p);
        }
        //눈이 2개 감지되었을때,
        if ( points.size() == 2){
            Point center1 = points[0];
            Point center2 = points[1];
            //눈이 어느쪽인지 분별.(오른쪽 왼쪽)
            if ( center1.x > center2.x ){
                Point temp;
                temp = center1;
                center1 = center2;
                center2 = temp;
            }
            //선글라스의 크기를 조절하기 위해 두눈 사이의 x,y좌표 차이를 구함.
            int width = abs(center2.x - center1.x);
            int height = abs(center2.y - center1.y);
            if (width > height){
                //이미지 크기 변수.
                float imgScale = width/70.0;
                int w, h;
                w = glass.cols * imgScale;
                h = glass.rows * imgScale;
                // x,y좌표 적절하게 조졍해줌.
                int offsetX = 210 * imgScale;
                int offsetY = 105 * imgScale;
                Mat resized_glasses;
                //선글라스 크기 조절.
                resize( glass, resized_glasses, cv::Size( w, h), 0, 0 );
                overlayImage(img_input, resized_glasses, img_result, Point(center1.x-offsetX, center1.y-offsetY));
            }
        }
    }

}extern "C"
JNIEXPORT jlong JNICALL
Java_com_teamnova_jaycameraapp1_EditActivity_loadCascade(JNIEnv *env, jobject thiz,
                                                         jstring cascade_file_name) {
    const char *nativeFileNameString = env->GetStringUTFChars(cascade_file_name, 0);


    string baseDir("/storage/emulated/0/");

    baseDir.append(nativeFileNameString);

    const char *pathDir = baseDir.c_str();


    jlong ret = 0;
    //이름을 입력받으면 저장한 xml파일을 cascadeClassifier로 생성함.
    ret = (jlong) new CascadeClassifier(pathDir);
    if (((CascadeClassifier *) ret)->empty()) {
        __android_log_print(ANDROID_LOG_DEBUG, "native-lib :: ",
                            "CascadeClassifier로 로딩 실패  %s", nativeFileNameString);
    }
    else
        __android_log_print(ANDROID_LOG_DEBUG, "native-lib :: ",
                            "CascadeClassifier로 로딩 성공 %s", nativeFileNameString);
    env->ReleaseStringUTFChars(cascade_file_name, nativeFileNameString);
    return ret;

}extern "C"
JNIEXPORT void JNICALL
Java_com_teamnova_jaycameraapp1_EditActivity_detect(JNIEnv *env, jobject thiz,
                                                    jlong cascade_classifier_face,
                                                    jlong cascade_classifier_eye,
                                                    jlong mat_addr_input, jlong mat_addr_result) {
    //파라미터로 받은 input 과 result 값의 주소를 가져와서 Mat 객체 생성.
    Mat &img_input = *(Mat *) mat_addr_input;
    Mat &img_result = *(Mat *) mat_addr_result;
    img_result = img_input.clone();

    //검출할 얼굴 을 네모모양좌표 저장.
    std::vector<Rect> faces;

    Mat img_gray;
    cvtColor(img_input, img_gray, COLOR_BGR2GRAY);

    //이미지의 명암대비를 높여 인식율을 높여줌. fps가 너무 떨어져서 생략. 실제로 기능 저하는 크지않음.
    //equalizeHist(img_gray, img_gray);
    Mat img_resize;
    float resizeRatio = resize(img_gray, img_resize, 240);

    //-- 얼굴인식 부분. minNeighbors = 3으로 수정.
    //img_resize 는 gray scale 이다.
    ((CascadeClassifier *) cascade_classifier_face)->detectMultiScale( img_resize, faces, 1.1, 3, 0|CASCADE_SCALE_IMAGE, Size(30, 30) );

    //얼굴별로 좌표값을 받아와서 타원을 그려준다. 그리고 눈 탐색.
    for (int i = 0; i < faces.size(); i++) {
        double real_facesize_x = faces[i].x / resizeRatio;
        double real_facesize_y = faces[i].y / resizeRatio;
        double real_facesize_width = faces[i].width / resizeRatio;
        double real_facesize_height = faces[i].height / resizeRatio;

        Point center( real_facesize_x + real_facesize_width / 2, real_facesize_y + real_facesize_height/2);
        ellipse(img_result, center, Size( real_facesize_width / 2, real_facesize_height / 2), 0, 0, 360,
                Scalar(0, 255, 0), 2, 4, 0);
        //얼굴영역 좌표.
        Rect face_area(real_facesize_x, real_facesize_y, real_facesize_width,real_facesize_height);
        Mat faceROI = img_gray( face_area );
        std::vector<Rect> eyes;
        //-- 각 얼굴마다 눈을 인식.
        ((CascadeClassifier *) cascade_classifier_eye)->detectMultiScale( faceROI, eyes, 1.1, 3, 0 |CASCADE_SCALE_IMAGE, Size(30, 30) );
        for ( size_t j = 0; j < eyes.size(); j++ )
        {
            //눈 하나의 중심 좌표.
            Point eye_center( real_facesize_x + eyes[j].x + eyes[j].width/2, real_facesize_y + eyes[j].y + eyes[j].height/2 );
            int radius = cvRound( (eyes[j].width + eyes[j].height)*0.25 );
            circle( img_result, eye_center, radius, Scalar( 255, 0, 0 ), 2, 4, 0 );
        }
    }
}extern "C"
JNIEXPORT void JNICALL
Java_com_teamnova_jaycameraapp1_EditActivity_mosaic(JNIEnv *env, jobject thiz, jlong mat_addr_input,
                                                    jlong mat_addr_result) {
    int mosaic_rate = 30;
    Mat &matInput = *(Mat *)mat_addr_input;
    Mat &matResult = *(Mat *)mat_addr_result;
    int width = matResult.cols;
    int height = matResult.rows;
    //resize(img_src, img_resize, Size(resize_width, new_height));
    //모자이크율 만큼 나누었다가 다시 확대함. 이미지를
    resize(matResult, matResult, Size(width/mosaic_rate, height/mosaic_rate));
    resize(matResult, matResult, Size(width, height));

}