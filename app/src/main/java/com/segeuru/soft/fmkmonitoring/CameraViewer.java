package com.segeuru.soft.fmkmonitoring;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

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
    private ImageButton m_btnTakePicture;
    private String m_requestId;
    private ArrayList<String> m_filePath;
    private int m_needPic_count;
    private int m_currentPic_count;

    private TimerTask m_timerTask;
    private Timer m_timer;

    private int m_resolution_width = 640;
    private int m_resolution_height = 480;
    private float m_font_scale = 1;
    private MediaPlayer m_shutter_player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(DEBUG_TAG, "onCreate");
        setContentView(R.layout.activity_camera_viewer);

        m_filePath = new ArrayList<>();
        m_currentPic_count = 0;

        Intent intent = getIntent();
        m_requestId = intent.getStringExtra("request_id");
        if(m_requestId.compareTo("monitoring") == 0) {
            //monitoring.
            m_resolution_width = 960;
            m_resolution_height = 540;
        } else {
            //other is ad.
            m_resolution_width = 1440;
            m_resolution_height = 810;
        }

        m_shutter_player = MediaPlayer.create(this, R.raw.shutter);

        setSupportActionBar((Toolbar)findViewById(R.id.camera_toolbar));
        m_needPic_count = intent.getIntExtra("take_count", 0);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((TextView)findViewById(R.id.title)).setText(intent.getStringExtra("title"));
        ((TextView)findViewById(R.id.txt_address)).setText(intent.getStringExtra("address"));

        Date currentTime = Calendar.getInstance().getTime();
        String dateTime = new SimpleDateFormat("yyyy. MM. dd. a hh:mm", Locale.getDefault()).format(currentTime);
        ((TextView)findViewById(R.id.txt_datetime)).setText(dateTime);

        updateTakeCount();

        findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //촬영 중간에 취소
                Intent intent = new Intent();
                intent.putExtra("request_id", m_requestId);
                intent.putExtra("pics", "[]");
                setResult(WebviewActivity.REQUEST_CODE, intent);
                finish();
            }
        });

        m_textureViewer = findViewById(R.id.texture);
        assert m_textureViewer != null;

        m_textureViewer.setSurfaceTextureListener(m_textureListener);
        m_btnTakePicture = findViewById(R.id.btn_takepicture);
        assert m_btnTakePicture != null;

        m_btnTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_shutter_player.start();
                takePicture();
            }
        });
        setMessage(intent.getStringExtra("frontMessage"), 2000);


    }

    //메세지 출력 타이머
    private void setMessage(String message, int time) {
        if(null != m_timer) {
            m_timer.cancel();
        }

        final TextView textView = findViewById(R.id.txt_message);
        textView.setText(message);
        textView.setVisibility(View.VISIBLE);

        m_timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setVisibility(View.GONE);
                        m_timer = null;
                    }
                });
            }
        };

        m_timer = new Timer();
        m_timer.schedule(m_timerTask, time);
    }

    private void updateTakeCount() {
        if(m_needPic_count > 1)
            setMessage(m_currentPic_count + "/" + m_needPic_count, 2000);
    }

    @SuppressLint("MissingPermission")
    private void openCamera() {
        //Log.i(DEBUG_TAG, "open camera now");
        CameraManager cameraManager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);

        try {
            //Log.i(DEBUG_TAG, Arrays.toString(cameraManager.getCameraIdList()));
            m_cameraId = cameraManager.getCameraIdList()[0];
            m_cameraCharacteristics = cameraManager.getCameraCharacteristics(m_cameraId);
            StreamConfigurationMap map = m_cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;

            m_imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            //Log.i(DEBUG_TAG, m_imageDimension.toString());

            Size[] jpegSizes = null;
            if(null != m_cameraCharacteristics) {
                jpegSizes = m_cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
                Log.i(DEBUG_TAG, Arrays.toString(jpegSizes));
            }

            ArrayList<Size> jpegList = new ArrayList<>(Arrays.asList(jpegSizes));
            Collections.reverse(jpegList);

            int width = 640;
            int height = 480;
            for(Size size:jpegList) {
                if(size.getWidth() >= m_resolution_width && size.getHeight() >= m_resolution_height )
                {
                    width = size.getWidth();
                    height = size.getHeight();
                    break;
                }
            }

//            if (jpegSizes != null && 0 < jpegSizes.length) {
//                width = jpegSizes[0].getWidth();
//                height = jpegSizes[0].getHeight();
//            }

            //Log.i(DEBUG_TAG, width + ":" + height);
            m_font_scale = (float)width / 480;

            m_imageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            Log.i(DEBUG_TAG, width + ":" + height);


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
                        save(bitmap);
                        //save(bytes);
                        //new saveThread().execute(bitmap);


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
        //Log.i(DEBUG_TAG, "take a picture");
        if(null == m_cameraDevice) {
            Log.e(DEBUG_TAG, "cemeraDevice is null");
            return;
        }

        try {
            final CaptureRequest.Builder captureBuilder = m_cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(m_imageReader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(getWindowManager().getDefaultDisplay().getRotation()));

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

    private void save(Bitmap bitmap) throws IOException {

        int padding = Math.round(10 * m_font_scale);
        int lineHeight = Math.round(10 * m_font_scale);
        final File file = new File(MoniteringApp.APP_STORE_PICTURE_PATH + "/" + System.currentTimeMillis() + ".jpg");
        OutputStream output = null;

        try {
//            Bitmap bitmapTmp = bitmap.copy(bitmap.getConfig(), true);
            Bitmap bitmapTmp = rotateImage(bitmap, 90);

            Paint paint = new Paint();
            paint.setTypeface(Typeface.DEFAULT);
            paint.setAntiAlias(true);
            paint.setDither(true);

            String txtAddress = ((TextView)findViewById(R.id.txt_address)).getText().toString();
            String txtDatetime = ((TextView)findViewById(R.id.txt_datetime)).getText().toString();

            Rect bound = new Rect();

            //calculate rectangle bound box
            Rect rectangleBound = new Rect();
            paint.setTextSize(Math.round(13 * m_font_scale));
            paint.getTextBounds(txtAddress, 0, txtAddress.length(), bound);
            rectangleBound.bottom = bound.height();

            paint.setTextSize(Math.round(13 * m_font_scale));
            paint.getTextBounds(txtAddress, 0, txtAddress.length(), bound);
            rectangleBound.bottom += bound.height();

            Canvas canvas = new Canvas(bitmapTmp);
            canvas.drawBitmap(bitmapTmp, 0, 0, paint);

            //draw text
            paint.setColor(Color.WHITE);
            paint.setTextSize(Math.round(13 * m_font_scale));
            paint.getTextBounds(txtAddress, 0, txtAddress.length(), bound);
            canvas.drawText(txtAddress, ((m_imageReader.getHeight() - bound.width()) * 0.5f), bound.height() + padding, paint);

            paint.setTextSize(Math.round(13 * m_font_scale));
            paint.getTextBounds(txtDatetime, 0, txtDatetime.length(), bound);
            canvas.drawText(txtDatetime, ((m_imageReader.getHeight() - bound.width()) * 0.5f), (bound.height() * 2) + padding + lineHeight, paint);

            output = new FileOutputStream(file);
            bitmapTmp.compress(Bitmap.CompressFormat.JPEG, 60, output);

//            ExifInterface exif = new ExifInterface(file.getPath());
//            exif.setAttribute(ExifInterface.TAG_ORIENTATION, "90");
//            exif.saveAttributes();

        } finally {
            if (null != output) {
                output.close();

//                ExifInterface exif = new ExifInterface(file.getPath());
//                Log.i(DEBUG_TAG, exif.getAttribute(ExifInterface.TAG_ORIENTATION));

                String filePath = "file://" + file.getAbsolutePath();
                m_filePath.add(filePath);

                //scan gallery.
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(filePath)));
                Toast.makeText(CameraViewer.this, "사진을 저장하였습니다.", Toast.LENGTH_SHORT).show();

                JSONArray jsonArray = new JSONArray();
                for (int i=0; i < m_filePath.size(); i++) {
                    JSONObject json = new JSONObject();
                    try {
                        json.put("filepath", m_filePath.get(i));
                        jsonArray.put(json);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                m_currentPic_count++;
                updateTakeCount();

                if(m_currentPic_count >= m_needPic_count) {
                    Intent intent = new Intent();
                    intent.putExtra("request_id", m_requestId);
                    intent.putExtra("pics", jsonArray.toString());
                    setResult(WebviewActivity.REQUEST_CODE, intent);
                    finish();
                }
            }
        }
    }

    private void save(byte[] bytes) throws IOException {
        final File file = new File(MoniteringApp.APP_STORE_PICTURE_PATH + "/" + System.currentTimeMillis() + ".jpg");
        //Log.i(DEBUG_TAG, file.getAbsolutePath());

        OutputStream output = null;
        try {
            output = new FileOutputStream(file);
            output.write(bytes);

        } finally {
            if (null != output) {
                output.close();

                //scan gallery.
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file.getAbsolutePath())));
                Toast.makeText(CameraViewer.this, "사진을 저장하였습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class saveThread extends AsyncTask<Bitmap, Void, Void> {
        @Override
        protected Void doInBackground(Bitmap... data) {

            //orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            //Log.i(DEBUG_TAG, String.format("device rotaion is %d", rotation));

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
            //Log.i(DEBUG_TAG, "onSurfaceTextureAvailable");
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
            //Log.i(DEBUG_TAG, "onSurfaceTextureSizeChanged");
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            //Log.i(DEBUG_TAG, "onSurfaceTextureDestroyed");
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

    private final CameraDevice.StateCallback m_stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onClosed(@NonNull CameraDevice camera) {
            super.onClosed(camera);
        }

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            Log.i(DEBUG_TAG, "onOpened");
            m_cameraDevice = cameraDevice;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            Log.i(DEBUG_TAG, "onDisconnected");
            m_cameraDevice.close();
            m_cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            Log.i(DEBUG_TAG, "onError");
            m_cameraDevice.close();
            m_cameraDevice = null;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        //Log.i(DEBUG_TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Log.i(DEBUG_TAG, "onPause");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Log.i(DEBUG_TAG, "caneraView onActivityResult");
    }

    @Override
    protected void onDestroy() {
        Log.i(DEBUG_TAG, "onDestroy");
        super.onDestroy();
        try {
            m_captureSession.stopRepeating();
            m_captureSession.close();
            m_cameraDevice.close();
            m_cameraDevice = null;
        } catch (Exception e) {

        }
    }
}
