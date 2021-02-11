package com.teamnova.jaycameraapp1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCamera2View;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static org.opencv.core.Core.flip;
import static org.opencv.core.Core.transpose;


//TODO 기능구현.
//TODO 선글라스 부분 사람이 1명밖에 적용안됨. 많이 버벅임.
//TODO 전면카메라

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {


    private static final String TAG = "MainActivity";
    public static final int front_camera = 1;
    public static final int back_camera = 0;

    private Mat mat_input;
    private Mat mat_result;

    private Button take_picture_btn;
    private RecyclerView filter_btn_recycler_view;
    private FilterListAdapter filter_btn_adapter;

    //카메라 필터 변수.
    private int camera_filter = 1;
    public static final int GRAY = 0;
    public static final int NORMAL = 1;
    public static final int REVERSE = 2;
    public static final int CANNY = 3; // 물체의 테두만 가져오는 흑백화면.
    public static final int CARTOON = 4;
    public static final int FACEDETECT = 5;
    public static final int SUNGLASSES = 6;
    public static final int PINK = 7;
    public static final int ORANGE = 8;


    private JavaCamera2View opencv_camera_view;


    // 주황색 필터.
    public native void ConvertRGBA2YcrCb(long matAddrInput, long matAddrResult);
    // 핑크색 필터.
    public native void ConvertRGBA2Luv(long matAddrInput, long matAddrResult);
    //이미지에 회색필터를 적용시키는 함수.
    public native void ConvertRGBA2Gray(long matAddrInput, long matAddrResult);

    //이미지에 캐니(테두리만흰색 나머지 검은색)필터를 적용시키는 함수. ->blur 도 추가함.
    public native void ConvertRGBA2Canny(long matAddrInput, long matAddrResult);

    //이미지에 색상반전필터를 적용시키는 함수.
    public native void ConvertRGBA2Reverse(long matAddrInput, long matAddrResult);
   // public native void ConvertRGBA2Comic(long matAddrInput, long matAddrResult);

    //일반필터. 투명도를 설정하지 않아서 RGB로 변환시켜줌.
    public native void ConvertRGBA2RGB(long matAddrInput, long matAddrResult);

    //얼굴, 눈 감지후 선글라스 이미지를 입혀주는 필터. 선글라스필터.
    public native void SunGlasses(long cascadeClassifier_face, long cascadeClassifier_eye, long sun_glass,
                                long matAddrInput, long matAddrResult);

    //얼굴과 눈을 인식하는 학습된 xml파일을 불러오는 함수.
    public native long loadCascade(String cascadeFileName );

    //얼굴, 눈 감지함수 얼굴인식 필터.
    public native void detect(long cascadeClassifier_face,
                              long cascadeClassifier_eye, long matAddrInput, long matAddrResult);

    public long cascadeClassifier_face = 0;
    public long cascadeClassifier_eye = 0;
    public Mat sun_glasses;



    static {
        //OpenCVLoader.initDebug() 는 onResume에서 실행.
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate 호출");

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        opencv_camera_view = findViewById(R.id.main_java_camera_view);
        opencv_camera_view.setVisibility(SurfaceView.VISIBLE);
        opencv_camera_view.setCvCameraViewListener(this);

        //앞면카메라 or 뒷면카메라 지정.
        opencv_camera_view.setCameraIndex(back_camera);

        opencv_camera_view.enableFpsMeter();
        init();

    }


    //리사이클뷰의 버튼 클릭 리스너
    private View.OnClickListener onClickItem = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FilterBtn filterBtn =((FilterBtn)v.getTag());
            camera_filter = filterBtn.getTAG();
            Toast.makeText(MainActivity.this, filterBtn.getName(), Toast.LENGTH_SHORT).show();
        }
    };


    //버튼을 정의, 버튼의 클릭리스너정의, 사진저장구현.
    //리사이클러뷰도 여기서 정의함.
    //sun_glasses 필터이미지 가져옴.



    public void init(){

        take_picture_btn = findViewById(R.id.main_take_picture_btn);


        filter_btn_recycler_view = findViewById(R.id.main_filter_btn_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false);
        filter_btn_recycler_view.setLayoutManager(layoutManager);

        ArrayList<FilterBtn> itemList = new ArrayList<>();
        itemList.add(new FilterBtn("일반",1));
        itemList.add(new FilterBtn("캐니",3));
        itemList.add(new FilterBtn("그레이",0));
        itemList.add(new FilterBtn("반전",2));
        itemList.add(new FilterBtn("주황필터",8));
        itemList.add(new FilterBtn("핑크필터",7));
        itemList.add(new FilterBtn("카툰",4));
        itemList.add(new FilterBtn("얼굴인식",5));
        itemList.add(new FilterBtn("선글라스",6));

        filter_btn_adapter = new FilterListAdapter(itemList, this, onClickItem);
        filter_btn_recycler_view.setAdapter(filter_btn_adapter);

        //데코 :: 버튼간에 간격을 줌
        FilterListDecoration decoration = new FilterListDecoration();
        filter_btn_recycler_view.addItemDecoration(decoration);

        //사진저장 부분. 외부저장소에 사진 저장.
        take_picture_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "찰캌.", Toast.LENGTH_SHORT).show();
                try{
                    getWriteLock();

                    //중복을 피하기 위해서 시간을 가져와 파일이름으로 정의함.
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    File path = new File(Environment.getExternalStorageDirectory() + "/Images/");
                    path.mkdirs();
                    File file = new File(path, timeStamp+"image.jpg");

                    String filename = file.toString();

                    Imgproc.cvtColor(mat_result, mat_result, Imgproc.COLOR_BGR2RGBA);
                    boolean ret  = Imgcodecs.imwrite( filename, mat_result);
                    if ( ret ) Log.d(TAG, "SUCESS");
                    else Log.d(TAG, "FAIL");

                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    mediaScanIntent.setData(Uri.fromFile(file));
                    sendBroadcast(mediaScanIntent);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                releaseWriteLock();
            }
        });

        try {
            sun_glasses = Utils.loadResource(this,R.drawable.ralosunglasses);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG,"랄로선글라스이미지로딩실패");
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"onPause 호출");
        if (opencv_camera_view != null){
            Log.d(TAG,"onPause :: 카메라끔");
            opencv_camera_view.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume 호출");

        if(!OpenCVLoader.initDebug()) {
            Log.d(TAG,"onResume :: 내부 OpenCV 라이브러리를 찾을 수 없음");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, opencvLoaderCallback);
        } else {
            Log.d(TAG, "onResume :: 내부 OpenCV 라이브러리 찾음");
            opencvLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy 호출");

        if(opencv_camera_view != null){
            Log.d(TAG,"onDestroy :: 카메라끔");
            opencv_camera_view.disableView();
        }
    }


    //앱이 실행될때, opencv라이브러리를 가져오고, 실행전에 디버그해서
    private BaseLoaderCallback opencvLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.d(TAG,"카메라킴");
                    opencv_camera_view.enableView();
                } break;
                default:
                {
                    Log.d(TAG,"Manager?");
                    super.onManagerConnected(status);
                } break;
            }
        }
    };




    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    //javacamera2view에 보여지는 프레임은 아래의 함수로부터 반환된다. 이미지 필터, 얼굴인식등의 카메라 조작은
    //여기서 이루어 짐. camera_filter의 상태에 따라서 화면 프레임 조작해줌.
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        try{
            getWriteLock();
            mat_input = inputFrame.rgba();

            // opencv 카메라 90도 회전되어 있어서 바꾸어줌.
            if(camera_filter != CARTOON){
                //cartoon 은 따로 바꿈.
                transpose(mat_input,mat_input);
                flip(mat_input,mat_input,1);
            }


            if( mat_result == null){
                mat_result = new Mat(mat_input.rows(), mat_input.cols(), mat_input.type());
            }

            // 카메라필터 변수에 따라 프레임에 변화를 줌. default 는 null이다.
            switch (camera_filter){
                case GRAY:
                    ConvertRGBA2Gray(mat_input.getNativeObjAddr(), mat_result.getNativeObjAddr());
                    break;
                case CANNY:
                    ConvertRGBA2Canny(mat_input.getNativeObjAddr(), mat_result.getNativeObjAddr());
                    break;
                case REVERSE:
                    ConvertRGBA2Reverse(mat_input.getNativeObjAddr(), mat_result.getNativeObjAddr());
                    break;
                case NORMAL:
                    ConvertRGBA2RGB(mat_input.getNativeObjAddr(), mat_result.getNativeObjAddr());
                    break;
                case CARTOON:
                    Mat cartoon = new Mat();
                    Mat gray = new Mat();
                    Imgproc.bilateralFilter(inputFrame.gray(),cartoon,9, 9, 7);
                    Imgproc.medianBlur(inputFrame.gray(),gray, 7);
                    Imgproc.adaptiveThreshold(gray,gray, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C,Imgproc.THRESH_BINARY,9,2);
                    Core.bitwise_and(cartoon,gray, mat_result);//ADDS THE BOTH IMAGES
                    //따로 바꾸어줌.
                    transpose(mat_result,mat_result);
                    flip(mat_result,mat_result,1);
                    break;
                case FACEDETECT:
                   // Core.flip(matInput,matInput, BackCamera); 카메라 화면 좌우반전시켜주는 코드.
                    detect(cascadeClassifier_face,cascadeClassifier_eye, mat_input.getNativeObjAddr(),
                            mat_result.getNativeObjAddr());
                    break;
                case SUNGLASSES:
                    SunGlasses(cascadeClassifier_face, cascadeClassifier_eye,sun_glasses.getNativeObjAddr(),mat_input.getNativeObjAddr(),
                            mat_result.getNativeObjAddr());
                    break;
                case PINK:
                    ConvertRGBA2Luv(mat_input.getNativeObjAddr(), mat_result.getNativeObjAddr());
                    break;
                case ORANGE:
                    ConvertRGBA2YcrCb(mat_input.getNativeObjAddr(), mat_result.getNativeObjAddr());
                    break;
                default:
                    Log.d(TAG,"onCameraFrame :: 필터오류");
                    return null;
            }
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        releaseWriteLock();

        //opencv 의 내장된 fps함수사용. 주석처리함.
        //fps가 짤려서 fps를 서브스레스에서 계산해서 UI에 적용하는 함수. fps를 보여주는 함수.
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if(current_time - start_time >= 1000){
//                    camera_fps_text_view.setText("FPS : "+ String.valueOf(mFPS));
//                    mFPS = 0;
//                    start_time = System.currentTimeMillis();
//                }
//                current_time = System.currentTimeMillis();
//                mFPS += 1;
//            }
//        });
        return mat_result;
    }

    //assets 에 저장된  cascade파일사용하기 위 외부저장소에 복사함.
    private void copyFile(String filename) {
        String baseDir = Environment.getExternalStorageDirectory().getPath();
        String pathDir = baseDir + File.separator + filename;

        AssetManager assetManager = this.getAssets();

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            Log.d( TAG, "copyFile :: 다음 경로로 파일복사 "+ pathDir);
            inputStream = assetManager.open(filename);
            outputStream = new FileOutputStream(pathDir);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            inputStream = null;
            outputStream.flush();
            outputStream.close();
            outputStream = null;
        } catch (Exception e) {
            Log.d(TAG, "copyFile :: 파일 복사 중 예외 발생 "+e.toString() );
        }

    }

    //cascade파일을 로드함.
    private void read_cascade_file(){
        copyFile("haarcascade_frontalface_alt.xml");
        copyFile("haarcascade_eye_tree_eyeglasses.xml");

        Log.d(TAG, "read_cascade_file:");

        cascadeClassifier_face = loadCascade( "haarcascade_frontalface_alt.xml");
        Log.d(TAG, "read_cascade_file:");

        cascadeClassifier_eye = loadCascade( "haarcascade_eye_tree_eyeglasses.xml");
    }
    //사진저장에서 카메라 프레임을 맞추기 위한 세마포.
    private final Semaphore writeLock = new Semaphore(1);

    public void getWriteLock() throws InterruptedException {
        writeLock.acquire();
    }

    public void releaseWriteLock() {
        writeLock.release();
    }






    //아래는 permission 코드 ,
    protected List<? extends CameraBridgeViewBase> getCameraViewList(){
        return Collections.singletonList(opencv_camera_view);
    }

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    protected void onCameraPermissionGrated() {
        List<? extends CameraBridgeViewBase> cameraViews = getCameraViewList();
        if (cameraViews == null){
            return;
        }
        for (CameraBridgeViewBase cameraBridgeViewBase :cameraViews){
            if(cameraBridgeViewBase != null) {
                cameraBridgeViewBase.setCameraPermissionGranted();

                read_cascade_file();
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG,"onStart 호출");
        boolean havePermission = true;
        //권한이 하나라도 없다면, 외부저장소입력, 카메라 권한 모두 요청받음.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(CAMERA) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, CAMERA_PERMISSION_REQUEST_CODE);
                havePermission = false;
            }
        }
        if (havePermission){
            onCameraPermissionGrated();
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.length>0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED){
            //권한을 획득한 경우.
            onCameraPermissionGrated();
        }else{
            showDialogForPermission("앱을 실행하려면 권한이 필요합니다.");
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void showDialogForPermission(String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder( MainActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id){
                requestPermissions(new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, CAMERA_PERMISSION_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });
        builder.create().show();
    }
}