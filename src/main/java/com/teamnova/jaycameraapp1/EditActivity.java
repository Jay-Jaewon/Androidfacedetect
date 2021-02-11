package com.teamnova.jaycameraapp1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

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
import java.util.Date;
import java.util.concurrent.Semaphore;

public class EditActivity extends AppCompatActivity {

    private static final String TAG = "EditActivity";
    private static final int GET_GALLERY_IMAGE = 200;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private ImageView edit_image;
    private Button edit_save_btn;
    private Mat mat_img;
    private Mat mat_img_normal;

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
    public static final int MOSAIC = 9;

    // 주황색 필터.
    public native void ConvertRGBA2YcrCb(long matAddrInput, long matAddrResult);
    // 핑크색 필터.
    public native void ConvertRGBA2Luv(long matAddrInput, long matAddrResult);
    //이미지에 회색필터를 적용시키는 함수.
    public native void ConvertRGBA2Gray(long matAddrInput, long matAddrResult);

    //이미지에 캐니(테두리만흰색 나머지 검은색)필터를 적용시키는 함수.
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

    //이미지 모자이크 처리 필터
    public native void mosaic(long matAddrInput, long matAddrResult);

    private RecyclerView filter_btn_recycler_view;
    private FilterListAdapter filter_btn_adapter;

    public long cascadeClassifier_face = 0;
    public long cascadeClassifier_eye = 0;
    public Mat sun_glasses;


    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        init();
        if (!hasPermissions(PERMISSIONS)) { //퍼미션 허가를 했었는지 여부를 확인
            requestNecessaryPermissions(PERMISSIONS);//퍼미션 허가안되어 있다면 사용자에게 요청
        }


    }

    //버튼 이미지뷰 정의. 클릭시 사진 가져오기, 사진 저장기능 구현. 리사이클러뷰 구현
    public void init(){
        edit_image = findViewById(R.id.edit_image_view);
        edit_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                //onActivityresult 수정해줌(Imageview, mat_img에 사진정보 입력).
                startActivityForResult(intent, GET_GALLERY_IMAGE);
            }
        });

        edit_save_btn = findViewById(R.id.edit_save_btn);
        edit_save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(EditActivity.this, "사진을 저장합니다.", Toast.LENGTH_SHORT).show();
                try{
                    getWriteLock();

                    //중복을 피하기 위해서 시간을 가져와 파일이름으로 정의함.
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    File path = new File(Environment.getExternalStorageDirectory() + "/Images/");
                    path.mkdirs();
                    File file = new File(path, timeStamp+"image.jpg");

                    String filename = file.toString();

                    Imgproc.cvtColor(mat_img, mat_img, Imgproc.COLOR_BGR2RGBA);
                    //파일저장.
                    boolean save_sucess  = Imgcodecs.imwrite( filename, mat_img);
                    if ( save_sucess ) Log.d(TAG, "SUCESS");
                    else Log.d(TAG, "FAIL");

                    //파일을 저장했다는 것을 프로드캐스트로 알림.
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    mediaScanIntent.setData(Uri.fromFile(file));
                    sendBroadcast(mediaScanIntent);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                releaseWriteLock();
            }
        });

        filter_btn_recycler_view = findViewById(R.id.edit_recycler_view);
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
        itemList.add(new FilterBtn("모자이크",9));

        filter_btn_adapter = new FilterListAdapter(itemList, this, onClickItem);
        filter_btn_recycler_view.setAdapter(filter_btn_adapter);

        //데코 :: 버튼간에 간격을 줌
        FilterListDecoration decoration = new FilterListDecoration();
        filter_btn_recycler_view.addItemDecoration(decoration);

        try {
            sun_glasses = Utils.loadResource(this,R.drawable.ralosunglasses);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG,"랄로선글라스이미지로딩실패");
        }
    }

    //리사이클뷰의 버튼 클릭 리스너
    private View.OnClickListener onClickItem = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FilterBtn filterBtn =((FilterBtn)v.getTag());
            camera_filter = filterBtn.getTAG();
            Toast.makeText(EditActivity.this, filterBtn.getName(), Toast.LENGTH_SHORT).show();
            EditImage();
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart 호출");
        read_cascade_file();
    }

    private void EditImage(){
        //edit_image 와 mat_img 를 수정해줌.
        switch (camera_filter){
            case NORMAL:
                ConvertRGBA2RGB(mat_img_normal.getNativeObjAddr(), mat_img.getNativeObjAddr());
                break;
            case GRAY:
                ConvertRGBA2Gray(mat_img_normal.getNativeObjAddr(), mat_img.getNativeObjAddr());
                break;
            case REVERSE:
                ConvertRGBA2Reverse(mat_img_normal.getNativeObjAddr(), mat_img.getNativeObjAddr());
                break;
            case CANNY:
                ConvertRGBA2Canny(mat_img_normal.getNativeObjAddr(), mat_img.getNativeObjAddr());
                break;
            case CARTOON:

                ConvertRGBA2Gray(mat_img_normal.getNativeObjAddr(), mat_img.getNativeObjAddr());
                Mat cartoon = new Mat();
                Mat gray = new Mat();
                Imgproc.bilateralFilter(mat_img,cartoon,9, 9, 7);
                Imgproc.medianBlur(mat_img,gray, 7);
                Imgproc.adaptiveThreshold(gray,gray, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C,Imgproc.THRESH_BINARY,9,2);
                Core.bitwise_and(cartoon,gray, mat_img);//ADDS THE BOTH IMAGES
                break;
            case FACEDETECT:

                detect(cascadeClassifier_face,cascadeClassifier_eye, mat_img_normal.getNativeObjAddr(),
                        mat_img.getNativeObjAddr());
                break;
            case SUNGLASSES:
                SunGlasses(cascadeClassifier_face, cascadeClassifier_eye,sun_glasses.getNativeObjAddr(),mat_img_normal.getNativeObjAddr(),
                        mat_img.getNativeObjAddr());
                break;
            case PINK:
                ConvertRGBA2Luv(mat_img_normal.getNativeObjAddr(), mat_img.getNativeObjAddr());
                break;
            case ORANGE:
                ConvertRGBA2YcrCb(mat_img_normal.getNativeObjAddr(), mat_img.getNativeObjAddr());
                break;
            case MOSAIC:
                mosaic(mat_img_normal.getNativeObjAddr(), mat_img.getNativeObjAddr());
                break;
            default:
                Log.d(TAG,"EditImage error 필터 디폴트");
                return;
        }
        Bitmap bitmapOutput = Bitmap.createBitmap(mat_img.cols(), mat_img.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat_img, bitmapOutput);
        edit_image.setImageBitmap(bitmapOutput);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //사진을 가져오는 request일때,
        if (requestCode == GET_GALLERY_IMAGE) {
            if (data.getData() != null) {
                Uri uri = data.getData();

                try {

                    String path = getRealPathFromURI(uri);
                    int orientation = getOrientationOfImage(path); // 런타임 퍼미션 필요
                    Bitmap temp = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    Bitmap bitmap = getRotatedBitmap(temp, orientation);
                    //이미지뷰에 비트맵 할당 (이미지 출력).
                    edit_image.setImageBitmap(bitmap);

                    //비트맵을 Mat 객체로 만들어줌. 저장할때 이 객체를 사용함.
                    mat_img = new Mat();
                    mat_img_normal = new Mat();
                    Bitmap bmp32 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    Utils.bitmapToMat(bmp32, mat_img);

                    //기본 이미지 저장.
                    Utils.bitmapToMat(bmp32, mat_img_normal);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    //uri 를 입력해 경로를 string 값으로 받아옴.
    private String getRealPathFromURI(Uri contentUri) {

        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        cursor.moveToFirst();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        return cursor.getString(column_index);
    }

    // 출처 - http://snowdeer.github.io/android/2016/02/02/android-image-rotation/
    //파일이 회전되어 있으면 회전되있는 만큼 각도를 반환함.
    public int getOrientationOfImage(String filepath) {
        ExifInterface exif = null;

        try {
            exif = new ExifInterface(filepath);
        } catch (IOException e) {
            Log.d(TAG, e.toString()+"getOrientationOfImage error");
            return -1;
        }

        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
        //일반적인 경우라면 -1이고, return 0; 함. 이경우를 보지못함.
        if (orientation != -1) {
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
            }
        }
        return 0;
    }

    //비트맵이 돌아간 만큼 비트맵 을 회전함. 원상복귀시킴.
    public Bitmap getRotatedBitmap(Bitmap bitmap, int degrees) throws Exception {
        if(bitmap == null) return null;
        if (degrees == 0) return bitmap;

        Matrix m = new Matrix();
        m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
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


    //사진저장에서 카메라 프레임을 맞추기 위한 세마포. //TODO 이미지 편집에서 필요한지 확인이 필요함
    private final Semaphore writeLock = new Semaphore(1);

    public void getWriteLock() throws InterruptedException {
        writeLock.acquire();
    }

    public void releaseWriteLock() {
        writeLock.release();
    }

    // 아래는 권한요청  코드
    String[] PERMISSIONS  = {"android.permission.WRITE_EXTERNAL_STORAGE"};

    private boolean hasPermissions(String[] permissions) {
        //스트링 배열에 있는 퍼미션들의 허가 상태 여부 확인
        for (String perms : permissions){
            if (!(checkCallingOrSelfPermission(perms) == PackageManager.PERMISSION_GRANTED)){
                //퍼미션 허가 안된 경우
                return false;
            }
        }
        //모든 퍼미션이 허가된 경우
        return true;
    }

    private void requestNecessaryPermissions(String[] permissions) {
        //마시멜로( API 23 )이상에서 런타임 퍼미션(Runtime Permission) 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        }
    }

    //권한요청
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        switch(permsRequestCode){
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean writeAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                        if (!writeAccepted )
                        {
                            showDialogforPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");
                            return;
                        }
                    }
                }
                break;
        }
    }
// 권한요청을 위한 다이얼로그.
    private void showDialogforPermission(String msg) {

        final AlertDialog.Builder myDialog = new AlertDialog.Builder(  EditActivity.this);
        myDialog.setTitle("알림");
        myDialog.setMessage(msg);
        myDialog.setCancelable(false);
        myDialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(PERMISSIONS, PERMISSION_REQUEST_CODE);
                }

            }
        });
        myDialog.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });
        myDialog.show();
    }


}
