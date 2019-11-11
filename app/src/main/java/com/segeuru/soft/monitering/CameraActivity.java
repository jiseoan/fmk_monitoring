package com.segeuru.soft.monitering;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.Arrays;

public class CameraActivity extends AppCompatActivity {

    private CameraDevice m_camera;
    private TextureView m_textureView;
    private Size m_previewSize;
    private CaptureRequest.Builder m_captureRequestBuilder;
    private CameraCaptureSession m_cameraSession;

    private SurfaceView m_surfaceView;
    private SurfaceHolder m_surfaceHolder;
    private Handler m_handler;
    private SensorManager m_sensorManager;
    private Sensor m_magnetometer;
    private Sensor m_accelerometer;
    private int m_DSI_height, m_DSI_width;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_camera);

//        ImageButton button = findViewById(R.id.btn_take_photo);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });

        m_surfaceView = findViewById(R.id.surfaceView);
        m_sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        m_accelerometer = m_sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        m_magnetometer = m_sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        //InitTextureView();
        initSurfaceView();
    }

    private void initSurfaceView() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        m_DSI_height = displayMetrics.heightPixels;
        m_DSI_width = displayMetrics.widthPixels;

        m_surfaceHolder = m_surfaceView.getHolder();
        m_surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                initCameraAndPreview();
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

            }
        });
    }

    private void initCameraAndPreview() {
//        HandlerThread handlerThread = new HandlerThread("CAMERA2");
//        handlerThread.start();
//
//        m_handler = new Handler(handlerThread.getLooper());
//        Handler mainHandler = new Handler(getMainLooper());
        try {
            String cameraId = "" + CameraCharacteristics.LENS_FACING_FRONT;
            CameraManager cameraManager = (CameraManager)this.getSystemService(Context.CAMERA_SERVICE);
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size largestPreviewSize = map.getOutputSizes(ImageFormat.JPEG)[0];
            Log.e("LargestSize", largestPreviewSize.getWidth() + " " + largestPreviewSize.getHeight());

            setAspectRatioTextureView(largestPreviewSize.getHeight(),largestPreviewSize.getWidth());

        } catch(CameraAccessException e) {
            Toast.makeText(this, "카메라를 열지 못했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setAspectRatioTextureView(int ResolutionWidth , int ResolutionHeight )
    {
        if(ResolutionWidth > ResolutionHeight){
            int newWidth = m_DSI_width;
            int newHeight = ((m_DSI_width * ResolutionWidth)/ResolutionHeight);
            updateTextureViewSize(newWidth,newHeight);

        } else {
            int newWidth = m_DSI_width;
            int newHeight = ((m_DSI_width * ResolutionHeight)/ResolutionWidth);
            updateTextureViewSize(newWidth,newHeight);
        }

    }

    private void updateTextureViewSize(int viewWidth, int viewHeight) {
        Log.e("segeuru", "TextureView Width : " + viewWidth + " TextureView Height : " + viewHeight);
        m_surfaceView.setLayoutParams(new FrameLayout.LayoutParams(viewWidth, viewHeight));
    }

//    private void InitTextureView() {
//        m_textureView = findViewById(R.id.texture);
//        m_textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
//            @Override
//            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
//                openCamera();
//                Log.e("segeuru", "onSurfaceTextureAvailable");
//            }
//
//            @Override
//            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
//                Log.e("segeuru", "onSurfaceTextureSizeChanged");
//            }
//
//            @Override
//            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
//                Log.e("segeuru", "onSurfaceTextureDestroyed");
//                return false;
//            }
//
//            @Override
//            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
//                Log.e("segeuru", "onSurfaceTextureUpdated");
//            }
//        });
//    }

//    @SuppressLint("MissingPermission")
//    private void openCamera() {
//        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
//        try {
//            String[] cameraIdArray = cameraManager.getCameraIdList();
//            Log.e("segeuru", "MMM cameraIds = " + Arrays.deepToString(cameraIdArray));
//
//            String mainCameraId = cameraIdArray[0];
//            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(mainCameraId);
//            Log.e("segeuru", "x => " + cameraCharacteristics);
//
//            StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//            Size[] sizesForStream = map.getOutputSizes(SurfaceTexture.class);
//            Log.e("segeuru", "MMM sizesForStream = " + Arrays.deepToString(sizesForStream));
//
//            // 가장 큰 사이즈부터 들어있다
//            m_previewSize = sizesForStream[0];
//            Log.e("segeuru", m_previewSize.toString());
//
//            cameraManager.openCamera(mainCameraId, new CameraDevice.StateCallback() {
//                @Override
//                public void onOpened(@NonNull CameraDevice cameraDevice) {
//                    m_camera = cameraDevice;
//                    showCameraPreview();
//                }
//
//                @Override
//                public void onDisconnected(@NonNull CameraDevice cameraDevice) {
//
//                }
//
//                @Override
//                public void onError(@NonNull CameraDevice cameraDevice, int i) {
//                    m_camera.close();
//                    m_camera = null;
//                }
//            }, null);
//
//        } catch(CameraAccessException e) {
//
//        }
//    }
//
//    private void showCameraPreview() {
//        try {
//            SurfaceTexture texture = m_textureView.getSurfaceTexture();
//            texture.setDefaultBufferSize(m_previewSize.getWidth(), m_previewSize.getHeight());
//            Surface textureViewSurface = new Surface(texture);
//
//            m_captureRequestBuilder = m_camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
//            m_captureRequestBuilder.addTarget(textureViewSurface);
//            m_captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
//
//            m_camera.createCaptureSession(Arrays.asList(textureViewSurface), new CameraCaptureSession.StateCallback() {
//                @Override
//                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
//                    m_cameraSession = cameraCaptureSession;
//                    updatePreview();
//                }
//
//                @Override
//                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
//                    Log.e("cklee", "MMM onConfigureFailed");
//                }
//            }, null);
//        } catch (CameraAccessException e) {
//            Log.e("cklee", "MMM showCameraPreview ", e);
//        }
//    }
//
//    private void updatePreview() {
//        try {
//            m_cameraSession.setRepeatingRequest(m_captureRequestBuilder.build(), null, null);
//        } catch (CameraAccessException e) {
//            Log.e("cklee", "MMM updatePreview", e);
//        }
//    }

}
