package com.example.liu.translate.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.liu.translate.R;

import java.nio.ByteBuffer;
import java.util.Arrays;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by liu on 2018/3/10.
 */

public class CameraActivity extends BaseActivity implements View.OnClickListener {
//    @BindView(R.id.surface_view)
    SurfaceView surfaceView;
//    @BindView(R.id.image_show)
    CircleImageView imageShow;
//    @BindView(R.id.take_photo)
    Button takePhoto;

    public static CameraActivity instance;
    private CameraManager cameraManager;
    private SurfaceHolder surfaceHolder;
    private Handler mHandler;
    private Handler mainHandler;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession session;
    private ImageReader mImageReader;
    private static final String TAG = "CameraActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        instance = this;
        surfaceView = findViewById(R.id.surface_view);
        imageShow = findViewById(R.id.image_show);
        takePhoto = findViewById(R.id.take_photo);
//        ButterKnife.bind(this);
        initSurfaceView();
    }

    public static CameraActivity getInstance(){
        return instance;
    }

    /**
     * 初始化View，即相机预览界面
     */
    private void initSurfaceView(){
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            //SurfaceView创建成功
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                Log.d(TAG, "相机 - Surface View创建成功: ");
                initCameraAndPreview();
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                Log.d(TAG, "surfaceChanged: ");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                Log.d(TAG, "surfaceDestroyed: ");
            }
        });
    }

    /**
     * 初始化相机，和相机预览功能
     */
    public void initCameraAndPreview(){
        HandlerThread handlerThread = new HandlerThread("my camera2 first");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());//用来处理普通线程
        mainHandler = new Handler(getMainLooper());//用来处理UI的线程，主线程

        //ImageReader用来读取相机拍照的照片
        mImageReader = ImageReader.newInstance(surfaceView.getWidth(),surfaceView.getHeight(), ImageFormat.JPEG,/*maxImages*/7);
        mImageReader.setOnImageAvailableListener(onImageAvailableListener,mainHandler);
        cameraManager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            //没有获取到权限
            Toast.makeText(this,"没有权限，手动给予",Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 123);
            return;
        }
        try {
            Log.d(TAG, "initCameraAndPreview: " + cameraManager.getCameraIdList());
            cameraManager.openCamera("" + CameraCharacteristics.LENS_FACING_FRONT,stateCallback,mHandler);
        } catch (CameraAccessException e) {
            Toast.makeText(CameraActivity.this,"错误： " + e.getMessage(),Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 相机预览
     */
    private ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader imageReader) {
            //进行相片存储
            mCameraDevice.close();
            Image image = imageReader.acquireLatestImage();
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
            if (bitmap != null){
                imageShow.setImageBitmap(bitmap);
            }
        }
    };

    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            Log.d(TAG, "相机 -- mCameraDevice.getId();: " + mCameraDevice.getId());
            try {
                takePreview();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            if (mCameraDevice != null){
                mCameraDevice.close();
                mCameraDevice = null;
            }
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            Toast.makeText(CameraActivity.this,"打开相机失败",Toast.LENGTH_SHORT).show();
        }
    };

    //显示预览界面
    private void takePreview() throws CameraAccessException{
        mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        mPreviewBuilder.addTarget(surfaceHolder.getSurface());
        mCameraDevice.createCaptureSession(Arrays.asList(surfaceHolder.getSurface(),mImageReader.getSurface()),SessionStateCallback,mHandler);
    }

    private CameraCaptureSession.StateCallback SessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
            //配置完毕开始预览
            session = cameraCaptureSession;
            try {
                //自动对焦
                mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                //无限次重复获取图像
                session.setRepeatingRequest(mPreviewBuilder.build(),null,mHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
            Log.d(TAG, "相机 -- 配置失败: ");
            Toast.makeText(CameraActivity.this,"配置失败",Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * 进行拍照并保存
     */
    public void takePhoto(){
        try {
            //用来设置拍照请求的Request
            CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            builder.addTarget(mImageReader.getSurface());
            builder.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);//自动对焦
            builder.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);//自动曝光
            int rotation = getWindowManager().getDefaultDisplay().getRotation();//获取屏幕方向
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics("" + CameraCharacteristics.LENS_FACING_FRONT);
            builder.set(CaptureRequest.JPEG_ORIENTATION,getJpegOrientation(cameraCharacteristics,rotation));
            CaptureRequest request = builder.build();
            session.setRepeatingRequest(request,null ,mHandler);//通过这步完成拍照
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    //获取图片应该旋转的角度，使图片竖直
    private int getJpegOrientation(CameraCharacteristics c, int deviceOrientation) {
        if (deviceOrientation == android.view.OrientationEventListener.ORIENTATION_UNKNOWN)
            return 0;
        int sensorOrientation = c.get(CameraCharacteristics.SENSOR_ORIENTATION);

        // Round device orientation to a multiple of 90
        deviceOrientation = (deviceOrientation + 45) / 90 * 90;

        // LENS_FACING相对于设备屏幕的方向,LENS_FACING_FRONT相机设备面向与设备屏幕相同的方向
        boolean facingFront = c.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT;
        if (facingFront) deviceOrientation = -deviceOrientation;

        // Calculate desired JPEG orientation relative to camera orientation to make
        // the image upright relative to the device orientation
        int jpegOrientation = (sensorOrientation + deviceOrientation + 360) % 360;

        return jpegOrientation;
    }

    public void onViewClicked(View view) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.image_show:
                break;
            case R.id.take_photo:
                takePhoto();
                break;
        }
    }
}
