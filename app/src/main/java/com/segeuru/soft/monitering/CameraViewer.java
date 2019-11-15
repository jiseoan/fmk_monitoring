package com.segeuru.soft.monitering;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
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
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CameraViewer extends AppCompatActivity {

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private static final String DEBUG_TAG = "segeuru.com";
    private String m_cameraId;
    private Size m_imageDimension;
    private CameraDevice m_cameraDevice;
    private CameraCharacteristics m_cameraCharacteristics;
    private ImageReader m_imageReader;
    private CaptureRequest.Builder m_captureRequestBuilder;
    private CameraCaptureSession m_captureSession;

    private TextureView m_textureViewer;
    private Surface m_surface;
    private Button m_btnTakePicture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_viewer);

        m_textureViewer = findViewById(R.id.texture);
        assert m_textureViewer != null;

        m_textureViewer.setSurfaceTextureListener(m_textureListener);
        m_btnTakePicture = findViewById(R.id.btn_takepicture);
        assert m_btnTakePicture != null;

        m_btnTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void openCamera() {
        Log.d(DEBUG_TAG, "open camera now");
        CameraManager cameraManager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);

        try {
            Log.d(DEBUG_TAG, Arrays.toString(cameraManager.getCameraIdList()));
            m_cameraId = cameraManager.getCameraIdList()[0];
            m_cameraCharacteristics = cameraManager.getCameraCharacteristics(m_cameraId);
            StreamConfigurationMap map = m_cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;

            m_imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            Log.d(DEBUG_TAG, m_imageDimension.toString());

            Size[] jpegSizes = null;
            if(null != m_cameraCharacteristics) {
                jpegSizes = m_cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
                Log.d(DEBUG_TAG, Arrays.toString(jpegSizes));
            }

            int width = 640;
            int height = 480;
            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }

            m_imageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            m_imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {
                    Image image = null;
                    try {
                        image = m_imageReader.acquireNextImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);

                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        new saveThread().execute(bitmap);

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if(null != image) image.close();
                    }
                }
            }, null);

            cameraManager.openCamera(m_cameraId, m_stateCallback, null);

        } catch(CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void createCameraPreview() {

        SurfaceTexture texture = m_textureViewer.getSurfaceTexture();
        assert texture != null;

        try {
            texture.setDefaultBufferSize(m_imageDimension.getWidth(), m_imageDimension.getHeight());
            m_surface = new Surface(texture);
            m_captureRequestBuilder = m_cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            m_captureRequestBuilder.addTarget(m_surface);
            m_cameraDevice.createCaptureSession(Arrays.asList(m_surface, m_imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if(null == m_cameraDevice) return;
                    m_captureSession = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(CameraViewer.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        if(null == m_cameraDevice) {
            Log.e(DEBUG_TAG, "update preview error");
            return;
        }

        try {
            m_captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            m_captureSession.setRepeatingRequest(m_captureRequestBuilder.build(), null, null);
        } catch(CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void takePicture() {
        Log.d(DEBUG_TAG, "take a picture");
        if(null == m_cameraDevice) {
            Log.e(DEBUG_TAG, "cemeraDevuce is null");
            return;
        }

        try {
            final CaptureRequest.Builder captureBuilder = m_cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(m_imageReader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            m_captureSession.capture(captureBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    UnlockFocus();
                }
            }, null);

        } catch(CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void UnlockFocus() {
        try {
            m_captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            m_captureSession.setRepeatingRequest(m_captureRequestBuilder.build(), null, null);
        } catch(CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private class saveThread extends AsyncTask<Bitmap, Void, Void> {
        @Override
        protected Void doInBackground(Bitmap... data) {

            //orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            Log.d(DEBUG_TAG, String.format("device rotaion is %d", rotation));

            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "test picture");
            values.put(MediaStore.Images.Media.DISPLAY_NAME, "---- test ----");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.ORIENTATION, ORIENTATIONS.get(rotation));
            values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());

            ContentResolver contentResolver = getContentResolver();
            Uri uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            Bitmap bitmap = data[0];
            //bitmap = rotateImage(bitmap, 90);
            OutputStream imageOut = null;
            try {
                imageOut = contentResolver.openOutputStream(uri);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, imageOut);

            } catch(IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(CameraViewer.this, "사진을 저장하였습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private final TextureView.SurfaceTextureListener m_textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            openCamera();
            Log.d(DEBUG_TAG, "onSurfaceTextureAvailable");
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
            Log.d(DEBUG_TAG, "onSurfaceTextureSizeChanged");
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            Log.d(DEBUG_TAG, "onSurfaceTextureDestroyed");
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

    private final CameraDevice.StateCallback m_stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            Log.d(DEBUG_TAG, "onOpened");
            m_cameraDevice = cameraDevice;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            Log.d(DEBUG_TAG, "onDisconnected");
            m_cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            Log.d(DEBUG_TAG, "onError");
            m_cameraDevice.close();
            m_cameraDevice = null;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(DEBUG_TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(DEBUG_TAG, "onPause");
    }
}

