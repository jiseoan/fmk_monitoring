package com.segeuru.soft.monitering;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.media.Image;
import android.media.ImageReader;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

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

import java.nio.ByteBuffer;
import java.util.Arrays;

public class CameraActivity extends AppCompatActivity {

    private CameraDevice m_camera;
    private TextureView m_textureView;
    private Size m_previewSize;
    private CaptureRequest.Builder m_captureRequestBuilder;
    private CameraCaptureSession m_cameraSession;
    private ImageReader m_imageReader;

    private SurfaceView m_surfaceView;
    private SurfaceHolder m_surfaceHolder;
    private Handler m_handler;
    private SensorManager m_sensorManager;
    private Sensor m_magnetometer;
    private Sensor m_accelerometer;
    private int m_DSI_height, m_DSI_width;
    private CameraDevice m_cameraDevice;
    private CaptureRequest.Builder m_previewBuilder;
    private CameraCaptureSession m_captureSession;

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
            Log.d("segeuru.com", map.toString());
            Log.d("segeuru.com", "large size: " + largestPreviewSize.getWidth() + " " + largestPreviewSize.getHeight());

            setAspectRatioTextureView(largestPreviewSize.getHeight(),largestPreviewSize.getWidth());

            m_imageReader = ImageReader.newInstance(largestPreviewSize.getWidth(), largestPreviewSize.getHeight(), ImageFormat.JPEG,7);
            //m_imageReader.setOnImageAvailableListener(mOnImageAvailableListener, mainHandler);

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                return;

            cameraManager.openCamera(cameraId, deviceStateCallback, null);

        } catch(CameraAccessException e) {
            Toast.makeText(this, "카메라를 열지 못했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private CameraDevice.StateCallback deviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            Log.d("segeuru.com", "camera opened");
            m_cameraDevice = cameraDevice;
            try {
                updatePreview();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            if(m_cameraDevice != null) {
                m_cameraDevice.close();
                m_cameraDevice = null;
            }
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            Toast.makeText(CameraActivity.this, "카메라를 열지 못했습니다.", Toast.LENGTH_SHORT).show();
        }
    };

    private void updatePreview() throws CameraAccessException {
        m_previewBuilder = m_cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        m_previewBuilder.addTarget(m_surfaceHolder.getSurface());
        m_cameraDevice.createCaptureSession(Arrays.asList(m_surfaceHolder.getSurface(), m_imageReader.getSurface()), sessionPrevireStateCallback, null);
    }

    private CameraCaptureSession.StateCallback sessionPrevireStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
            m_captureSession = cameraCaptureSession;
            try {
                m_previewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                m_previewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                m_captureSession.setRepeatingRequest(m_previewBuilder.build(), null, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
            Toast.makeText(CameraActivity.this, "카메라 구성 실패", Toast.LENGTH_SHORT).show();
        }
    };

//    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
//        @Override
//        public void onImageAvailable(ImageReader reader) {
//
//            Image image = reader.acquireNextImage();
//            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
//            byte[] bytes = new byte[buffer.remaining()];
//            buffer.get(bytes);
//            final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//            new SaveImageTask().execute(bitmap);
//        }
//    };

//    private class SaveImageTask extends AsyncTask<Bitmap, Void, Void> {
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//
//            Toast.makeText(MainActivity.this, "사진을 저장하였습니다.", Toast.LENGTH_SHORT).show();
//        }
//
//        @Override
//        protected Void doInBackground(Bitmap... data) {
//
//            Bitmap bitmap = null;
//            try {
//                bitmap = getRotatedBitmap(data[0], mDeviceRotation);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            insertImage(getContentResolver(), bitmap, ""+System.currentTimeMillis(), "");
//
//            return null;
//        }
//    }

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
        Log.d("segeuru.com", "TextureView Width : " + viewWidth + " TextureView Height : " + viewHeight);
        m_surfaceView.setLayoutParams(new FrameLayout.LayoutParams(viewWidth, viewHeight));
    }

//    private void InitTextureView() {
//        m_textureView = findViewById(R.id.texture);
//        m_textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
//            @Override
//            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
//                openCamera();
//                Log.d("segeuru.com", "onSurfaceTextureAvailable");
//            }
//
//            @Override
//            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
//                Log.d("segeuru.com", "onSurfaceTextureSizeChanged");
//            }
//
//            @Override
//            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
//                Log.d("segeuru.com", "onSurfaceTextureDestroyed");
//                return false;
//            }
//
//            @Override
//            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
//                Log.d("segeuru.com", "onSurfaceTextureUpdated");
//            }
//        });
//    }

//    @SuppressLint("MissingPermission")
//    private void openCamera() {
//        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
//        try {
//            String[] cameraIdArray = cameraManager.getCameraIdList();
//            Log.d("segeuru.com", "MMM cameraIds = " + Arrays.deepToString(cameraIdArray));
//
//            String mainCameraId = cameraIdArray[0];
//            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(mainCameraId);
//            Log.d("segeuru.com", "x => " + cameraCharacteristics);
//
//            StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//            Size[] sizesForStream = map.getOutputSizes(SurfaceTexture.class);
//            Log.d("segeuru.com", "MMM sizesForStream = " + Arrays.deepToString(sizesForStream));
//
//            // 가장 큰 사이즈부터 들어있다
//            m_previewSize = sizesForStream[0];
//            Log.d("segeuru.com", m_previewSize.toString());
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
//                    Log.d("segeuru.com", "MMM onConfigureFailed");
//                }
//            }, null);
//        } catch (CameraAccessException e) {
//            Log.d("segeuru.com", "MMM showCameraPreview ", e);
//        }
//    }
//
//    private void updatePreview() {
//        try {
//            m_cameraSession.setRepeatingRequest(m_captureRequestBuilder.build(), null, null);
//        } catch (CameraAccessException e) {
//            Log.d("segeuru.com", "MMM updatePreview", e);
//        }
//    }

}
