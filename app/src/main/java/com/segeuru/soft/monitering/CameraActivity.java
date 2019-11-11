package com.segeuru.soft.monitering;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Camera;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import java.util.Arrays;

public class CameraActivity extends AppCompatActivity {

    private CameraDevice m_camera;
    private TextureView m_textureView;
    private Size m_previewSize;
    private CaptureRequest.Builder m_captureRequestBuilder;
    private CameraCaptureSession m_cameraSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        InitTextureView();
    }

    private void InitTextureView() {
        m_textureView = findViewById(R.id.texture);
        m_textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                openCamera();
                Log.e("segeuru", "onSurfaceTextureAvailable");
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
                Log.e("segeuru", "onSurfaceTextureSizeChanged");
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                Log.e("segeuru", "onSurfaceTextureDestroyed");
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
                Log.e("segeuru", "onSurfaceTextureUpdated");
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void openCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] cameraIdArray = cameraManager.getCameraIdList();
            Log.e("segeuru", "MMM cameraIds = " + Arrays.deepToString(cameraIdArray));

            String mainCameraId = cameraIdArray[0];
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(mainCameraId);
            Log.e("segeuru", "x => " + cameraCharacteristics);

            StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size[] sizesForStream = map.getOutputSizes(SurfaceTexture.class);
            Log.e("segeuru", "MMM sizesForStream = " + Arrays.deepToString(sizesForStream));

            // 가장 큰 사이즈부터 들어있다
            m_previewSize = sizesForStream[0];
            Log.e("segeuru", m_previewSize.toString());

            cameraManager.openCamera(mainCameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice cameraDevice) {
                    m_camera = cameraDevice;
                    showCameraPreview();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice cameraDevice) {

                }

                @Override
                public void onError(@NonNull CameraDevice cameraDevice, int i) {
                    m_camera.close();
                    m_camera = null;
                }
            }, null);

        } catch(CameraAccessException e) {

        }
    }

    private void showCameraPreview() {
        try {
            SurfaceTexture texture = m_textureView.getSurfaceTexture();
            texture.setDefaultBufferSize(m_previewSize.getWidth(), m_previewSize.getHeight());
            Surface textureViewSurface = new Surface(texture);

            m_captureRequestBuilder = m_camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            m_captureRequestBuilder.addTarget(textureViewSurface);
            m_captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            m_camera.createCaptureSession(Arrays.asList(textureViewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    m_cameraSession = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Log.e("cklee", "MMM onConfigureFailed");
                }
            }, null);
        } catch (CameraAccessException e) {
            Log.e("cklee", "MMM showCameraPreview ", e);
        }
    }

    private void updatePreview() {
        try {
            m_cameraSession.setRepeatingRequest(m_captureRequestBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            Log.e("cklee", "MMM updatePreview", e);
        }
    }

}
