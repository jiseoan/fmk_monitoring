package com.segeuru.soft.monitering;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ExifInterface;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class CameraActivity extends AppCompatActivity {

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(ExifInterface.ORIENTATION_NORMAL, 0);
        ORIENTATIONS.append(ExifInterface.ORIENTATION_ROTATE_90, 90);
        ORIENTATIONS.append(ExifInterface.ORIENTATION_ROTATE_180, 180);
        ORIENTATIONS.append(ExifInterface.ORIENTATION_ROTATE_270, 270);
    }

    private CameraCaptureSession m_cameraCaptureSession;
    private ImageReader m_imageReader;
    private SurfaceView m_surfaceView;
    private SurfaceHolder m_surfaceHolder;
    private SensorManager m_sensorManager;
    private Sensor m_magnetometer;
    private Sensor m_accelerometer;
    private int m_DSI_height, m_DSI_width;
    private CameraDevice m_cameraDevice;
    private CaptureRequest.Builder m_previewBuilder;
    private CameraCaptureSession m_captureSession;
    private Handler m_handler;

    private CameraCaptureSession.StateCallback m_sessionStateCallback = new CameraCaptureSession.StateCallback() {
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

    private CameraCaptureSession.CaptureCallback m_sessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            m_cameraCaptureSession = session;
            unlockFocus();
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
            m_cameraCaptureSession = session;
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
        }
    };

    private CameraDevice.StateCallback m_deviceStateCallback = new CameraDevice.StateCallback() {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_camera);

        ImageButton button = findViewById(R.id.btn_take_photo);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });

        m_surfaceView = findViewById(R.id.surfaceView);
        m_sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        m_accelerometer = m_sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        m_magnetometer = m_sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

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
            m_imageReader.setOnImageAvailableListener(m_onImageAvailableListener, null);

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                return;

            cameraManager.openCamera(cameraId, m_deviceStateCallback, null);

        } catch(CameraAccessException e) {
            Toast.makeText(this, "카메라를 열지 못했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePreview() throws CameraAccessException {
        m_previewBuilder = m_cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        m_previewBuilder.addTarget(m_surfaceHolder.getSurface());
        m_cameraDevice.createCaptureSession(Arrays.asList(m_surfaceHolder.getSurface(), m_imageReader.getSurface()), m_sessionStateCallback, null);
    }

    private void takePicture() {
        try {
            CaptureRequest.Builder captureRequestBuilder = m_cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureRequestBuilder.addTarget(m_imageReader.getSurface());
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            //captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, mDeviceRotation);
            CaptureRequest captureRequest = captureRequestBuilder.build();
            m_captureSession.capture(captureRequest, m_sessionCaptureCallback, null);
        } catch(CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void unlockFocus() {
        try {
            // Reset the auto-focus trigger
            m_previewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            m_previewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            m_captureSession.capture(m_previewBuilder.build(), m_sessionCaptureCallback,null);

            // After this, the camera will go back to the normal state of preview.
            m_captureSession.setRepeatingRequest(m_previewBuilder.build(), m_sessionCaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private ImageReader.OnImageAvailableListener m_onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.d("segeuru.com", "capture a picture!");
            Image image = reader.acquireNextImage();
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            new SaveImageTask().execute(bitmap);
        }
    };

    private class SaveImageTask extends AsyncTask<Bitmap, Void, Void> {

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(CameraActivity.this, "사진을 저장하였습니다.", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Bitmap... data) {
            Bitmap bitmap = null;
            try {
                //bitmap = getRotatedBitmap(data[0], mDeviceRotation);
                bitmap = getRotatedBitmap(data[0], 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            insertImage(getContentResolver(), bitmap, "" + System.currentTimeMillis(), "");

            return null;
        }
    }

    public Bitmap getRotatedBitmap(Bitmap bitmap, int degrees) throws Exception {
        if(bitmap == null) return null;
        if (degrees == 0) return bitmap;

        Matrix m = new Matrix();
        m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
    }

    public static final String insertImage(ContentResolver cr,
                                           Bitmap source,
                                           String title,
                                           String description) {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, title);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, title);
        values.put(MediaStore.Images.Media.DESCRIPTION, description);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        // Add the date meta data to ensure the image is added at the front of the gallery
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());

        Uri url = null;
        String stringUrl = null;    /* value to be returned */

        try {
            url = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (source != null) {
                OutputStream imageOut = cr.openOutputStream(url);
                try {
                    source.compress(Bitmap.CompressFormat.JPEG, 50, imageOut);
                } finally {
                    imageOut.close();
                }

            } else {
                cr.delete(url, null, null);
                url = null;
            }
        } catch (Exception e) {
            if (url != null) {
                cr.delete(url, null, null);
                url = null;
            }
        }

        if (url != null) {
            stringUrl = url.toString();
        }

        return stringUrl;
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
        Log.d("segeuru.com", "TextureView Width : " + viewWidth + " TextureView Height : " + viewHeight);
        m_surfaceView.setLayoutParams(new FrameLayout.LayoutParams(viewWidth, viewHeight));
    }

}
