package com.segeuru.soft.monitering;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Arrays;

public class CameraViewer extends AppCompatActivity {

    private static final String DEBUG_TAG = "segeuru.com";
    private String m_cameraId;
    private Size m_imageDimension;
    private CameraDevice m_cameraDevice;
    private CaptureRequest.Builder m_captureRequestBuilder;
    private CameraCaptureSession m_captureSession;
    private Handler m_handler;
    private HandlerThread m_handlerThread;

    private TextureView m_textureViewer;
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
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(m_cameraId);
            StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;

            m_imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            Log.d(DEBUG_TAG, m_imageDimension.toString());

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
            Surface surface = new Surface(texture);
            m_captureRequestBuilder = m_cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            m_captureRequestBuilder.addTarget(surface);
            m_cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
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
            m_captureSession.setRepeatingRequest(m_captureRequestBuilder.build(), null, m_handler);
        } catch(CameraAccessException e) {
            e.printStackTrace();
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
