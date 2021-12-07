package com.example.test6;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.widget.Toast.LENGTH_SHORT;

public class OcrActivity extends AppCompatActivity  implements View.OnClickListener {
    // true  : Camera On  : 카메라로 직접 찍어 문자 인식
    // false : Camera Off : 샘플이미지를 로드하여 문자 인식
    private boolean CameraOnOffFlag = false;

    private TessBaseAPI m_Tess; //Tess API reference
    private ProgressCircleDialog m_objProgressCircle = null; // 원형 프로그레스바
    private MessageHandler m_messageHandler;

    private long m_start; // 처리시간 시작지점
    private long m_end; //처리시간 끝지점
    private String mDataPath = ""; //언어데이터가 있는 경로
    private String mCurrentPhotoPath; // 사진 경로
    private final String[] mLanguageList = {"eng", "kor"}; // 언어
    // View
    private Context mContext;
    private Button m_btnOCR,m_btnNext; // 인식하기 위해 사진찍는 버튼
    private TextView m_ocrTextView,m_ocrok; // 결과 변환 텍스트
    private ImageView m_ivImage; // 찍은 사진
    private Bitmap image,img; //사용되는 이미지
    private TextView m_tvTime; // 처리시간 표시 텍스트
    private AlertDialog dialog;
    private String textok;

    private final int GET_GALLERY_IMAGE = 200;
    private ImageView imageview;

    private boolean ProgressFlag = false; // 프로그레스바 상태 플래그

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);
        mContext = this;

        m_ocrTextView = findViewById(R.id.tv_view);
        m_ocrok = findViewById((R.id.tv_view2));
        m_tvTime = findViewById(R.id.tv_time);
        m_btnOCR = findViewById(R.id.btn_OCR);
        m_btnNext = findViewById(R.id.btn_Next);

        m_btnOCR.setOnClickListener(this);

        m_objProgressCircle = new ProgressCircleDialog(this);
        m_messageHandler = new MessageHandler();


        imageview = (ImageView) findViewById(R.id.imageView);
        imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 1);
            }
        });
        if (CameraOnOffFlag) {
            PermissionCheck();
            Tesseract();
        } else {
            //이미지 디코딩을 위한 초기화
            image = BitmapFactory.decodeResource(getResources(), R.drawable.sample2); //샘플이미지파일
            Test();

        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                try {
                    // 선택한 이미지에서 비트맵 생성
                    InputStream in = getContentResolver().openInputStream(data.getData());
                    img = BitmapFactory.decodeStream(in);
                    in.close();
                    // 이미지 표시
                    imageview.setImageBitmap(img);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void btn_next(View v){
        Toast.makeText(getApplicationContext(), "장애인식교육 인증되었습니다.", LENGTH_SHORT).show();
        Intent i = new Intent(OcrActivity.this, MainActivity.class);
        startActivity(i);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 카메라를 찍기위해 카메라 앱을 연다
            case R.id.btn_OCR:
                if (CameraOnOffFlag) {
                    dispatchTakePictureIntent();
                } else {
                    m_start = System.currentTimeMillis();
                    processImage(v);
                }
                m_tvTime.setText("처리시간");
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResult) {
        if (requestCode == 0) {

        } else {

        }
    }

    // 이미지를 원본과 같게 회전시킨다.
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public void PermissionCheck() {
        /**
         * 6.0 마시멜로우 이상일 경우에는 권한 체크후 권한을 요청한다.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED &&
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED &&
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                // 권한 없음
                ActivityCompat.requestPermissions(OcrActivity.this,
                        new String[]{Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        ConstantDefine.PERMISSION_CODE);
            } else {
                // 권한 있음
            }
        }
    }


    public void Tesseract() {
        //언어파일 경로
        mDataPath = getFilesDir() + "/tesseract/";

        //트레이닝데이터가 카피되어 있는지 체크
        String lang = "";
        for (String Language : mLanguageList) {
            checkFile(new File(mDataPath + "tessdata/"), Language);
            lang += Language + "+";
        }
        m_Tess = new TessBaseAPI();
        m_Tess.init(mDataPath, lang);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * 기본카메라앱을 실행 시킨다.
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // 사진파일을 생성한다.
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // 사진파일이 정상적으로 생성되었을때
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        this.getApplicationContext().getPackageName() + ".fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, ConstantDefine.ACT_TAKE_PIC);
            }
        }
    }


    //copy file to device
    private void copyFiles(String Language) {
        try {
            String filepath = mDataPath + "/tessdata/" + Language + ".traineddata";
            AssetManager assetManager = getAssets();
            InputStream instream = assetManager.open("tessdata/" + Language + ".traineddata");
            OutputStream outstream = new FileOutputStream(filepath);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }
            outstream.flush();
            outstream.close();
            instream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //check file on the device
    private void checkFile(File dir, String Language) {
        //디렉토리가 없으면 디렉토리를 만들고 그후에 파일을 카피
        if (!dir.exists() && dir.mkdirs()) {
            copyFiles(Language);
        }
        //디렉토리가 있지만 파일이 없으면 파일카피 진행
        if (dir.exists()) {
            String datafilepath = mDataPath + "tessdata/" + Language + ".traineddata";
            File datafile = new File(datafilepath);
            if (!datafile.exists()) {
                copyFiles(Language);
            }
        }
    }

    //region Thread
    public class OCRThread extends Thread {
        private Bitmap rotatedImage;

        OCRThread(Bitmap rotatedImage) {
            this.rotatedImage = rotatedImage;
            if (!ProgressFlag)
                m_objProgressCircle = ProgressCircleDialog.show(mContext, "", "", true);
            ProgressFlag = true;
        }

        @Override
        public void run() {
            super.run();
            // 사진의 글자를 인식해서 옮긴다
            String OCRresult = null;
            m_Tess.setImage(rotatedImage);
            OCRresult = m_Tess.getUTF8Text();

            Message message = Message.obtain();
            message.what = ConstantDefine.RESULT_OCR;
            message.obj = OCRresult;
            m_messageHandler.sendMessage(message);
        }
    }
    //endregion

    //region Handler
    public class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case ConstantDefine.RESULT_OCR:
                    TextView OCRTextView = findViewById(R.id.tv_view);
                    OCRTextView.setText(String.valueOf(msg.obj)); //텍스트 변경

                    // 원형 프로그레스바 종료
                    if (m_objProgressCircle.isShowing() && m_objProgressCircle != null)
                        m_objProgressCircle.dismiss();
                    ProgressFlag = false;
                    m_end = System.currentTimeMillis();
                    long time = (m_end - m_start) / 1000;
                    m_tvTime.setText("처리시간 : " + time + "초");
                    Toast.makeText(mContext, getResources().getString(R.string.CompleteMessage), Toast.LENGTH_SHORT).show();
                    int a,b,c,d,e;
                    if(String.valueOf(msg.obj).contains("인식")){
                        a=1;
                    }else {
                        a = 0;
                    }
                    if(String.valueOf(msg.obj).contains("장애")){
                        b=1;
                    }else{
                        b=0;
                    }
                    if(String.valueOf(msg.obj).contains("수 료")){
                        c=1;
                    }else{
                        c=0;
                    }
                    if(String.valueOf(msg.obj).contains("인개")){
                        d=1;
                    }else{
                        d=0;
                    }
                    if(String.valueOf(msg.obj).contains("edu.or.kr")){
                        e=1;
                    }else{
                        e=0;
                    }
                    if(a+b+c+d+e>=2){
                        m_btnNext.setEnabled(true);
                        m_ocrTextView.setVisibility(View.INVISIBLE);
                        m_ocrok.setText("인식이 완료되었습니다. NEXT PAGE버튼을 눌러주세요.");
                    }else{
                        m_btnNext.setEnabled(false);
                        m_ocrTextView.setVisibility(View.INVISIBLE);
                        m_ocrok.setText("JPG파일 형식인지, 수료증 이미지를 다시 찍어 인식해주세요.");
                    }
                    break;
            }
        }
    }
    //endregion

    public void Test() {
//        String lang = "eng";
        image = BitmapFactory.decodeResource(getResources(), R.drawable.sample2);;
        mDataPath = getFilesDir() + "/tesseract/";

//        checkFile2(new File(mDataPath + "tessdata/"),lang);

        String lang = "";
        for (String Language : mLanguageList) {
            checkFile(new File(mDataPath + "tessdata/"), Language);
            lang += Language + "+";
        }
        lang = lang.substring(0, lang.length() - 1);
        m_Tess = new TessBaseAPI();
        m_Tess.init(mDataPath, lang);
    }

    private void copyFiles2(String lang) {
        try {
            //location we want the file to be at
            String filepath = mDataPath + "/tessdata/" + lang + ".traineddata";

            //get access to AssetManager
            AssetManager assetManager = getAssets();

            //open byte streams for reading/writing
            InputStream instream = assetManager.open("tessdata/" + lang + ".traineddata");
            OutputStream outstream = new FileOutputStream(filepath);

            //copy the file to the location specified by filepath
            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }
            outstream.flush();
            outstream.close();
            instream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkFile2(File dir, String lang) {
        //directory does not exist, but we can successfully create it
        if (!dir.exists() && dir.mkdirs()) {
            copyFiles2(lang);
        }
        //The directory exists, but there is no data file in it
        if (dir.exists()) {
            String datafilepath = mDataPath + "/tessdata/" + lang + ".traineddata";
            File datafile = new File(datafilepath);
            if (!datafile.exists()) {
                copyFiles2(lang);
            }
        }
    }

    //Process an Image
    public void processImage(View view) {
        if(img==null){
            AlertDialog.Builder builder = new AlertDialog.Builder(OcrActivity.this);
            dialog = builder.setMessage("이미지를 등록해주세요.")
                    .setPositiveButton("확인",null)
                    .create();
            dialog.show();
            return;
        }
        if (img.getConfig() != Bitmap.Config.ARGB_8888) {
            AlertDialog.Builder builder = new AlertDialog.Builder(OcrActivity.this);
            dialog = builder.setMessage("jpg파일 형식을 등록해주세요.")
                    .setPositiveButton("확인",null)
                    .create();
            dialog.show();
            return;
        }
        OCRThread ocrThread = new OCRThread(img);
        ocrThread.setDaemon(true);
        ocrThread.start();
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "뒤로가기를 누르셨습니다.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(OcrActivity.this,RegisterActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }
    @Override
    protected void onStop() {
        super .onStop();
        if(dialog !=null)
        {
            dialog.dismiss();
            dialog = null;
        }
    }
}