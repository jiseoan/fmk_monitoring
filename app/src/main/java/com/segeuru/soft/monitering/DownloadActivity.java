package com.segeuru.soft.monitering;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;

public class DownloadActivity extends AppCompatActivity {

    private final String DEBUG_TAG = "segeuru.com";
    private String m_mimeType;
    private String m_url;
    private String m_filename;
    //private ProgressBar m_progressBar;
    private MediaController m_mediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

//        m_progressBar =findViewById(R.id.download_progressBar);
//        m_progressBar.setProgress(0);

        Intent intent = getIntent();
        m_mimeType = intent.getStringExtra("mime_type");
        m_url = intent.getStringExtra("url");

        Uri uri= Uri.parse(m_url);
        File file= new File(uri.getPath());
        m_filename = file.getName();

        final VideoView videoView = findViewById(R.id.videoView);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                    @Override
                    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                        //add media controller
                        m_mediaController = new MediaController(DownloadActivity.this);
                        videoView.setMediaController(m_mediaController);

                        //and set its position on screen
                        m_mediaController.setAnchorView(videoView);
                    }
                });
            }
        });

        videoView.setVisibility(View.GONE);

        ((TextView)findViewById(R.id.title)).setText(intent.getStringExtra("title"));
        findViewById(R.id.imageView).setVisibility(View.GONE);
        findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        findViewById(R.id.btn_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        findViewById(R.id.btn_download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //download now.
                final DownloadFileTask downloadFileTask = new DownloadFileTask(DownloadActivity.this);
                downloadFileTask.execute(m_url);
            }
        });

        downloadMedia();
    }

    private int getFileSize(String fileURL) {

        int fileSize = 0;
        try {
            @SuppressLint("StaticFieldLeak") AsyncTask<String, Void, Integer> asyncTask = new AsyncTask<String, Void, Integer>() {
                @Override
                protected Integer doInBackground(String... urls) {
                    int fileSize = 0;
                    try {
                        URL url = new URL(urls[0]); //it's url
                        URLConnection connection = url.openConnection();
                         fileSize = connection.getContentLength();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return fileSize;
                }
            };

            fileSize = asyncTask.execute(fileURL).get();

        } catch(Exception e) {
            e.printStackTrace();
        }

        return fileSize;
    }

    private void downloadMedia() {
        final String fileURL = m_url;
        File file = new File(MoniteringApp.APP_STORE_PATH, m_filename);

        if(file.exists()) {
            //already exist.
            playVideo();
        } else {
            findViewById(R.id.download_status).setVisibility(View.VISIBLE);
            int filesize = getFileSize(fileURL);
            setDownloadText(0, filesize);
        }
    }

    private void setDownloadText(int received, int total) {

        float receivedMB = received != 0 ? (float)received / 1000000 : 0f;
        float totalMB = total != 0 ? (float)total / 1000000 : 0f;
        DecimalFormat formatMB = new DecimalFormat("#.#");

        ((TextView)findViewById(R.id.txt_download)).setText(formatMB.format(receivedMB) + "MB/" + formatMB.format(totalMB) + "MB");
    }

    private void playVideo() {
        //동영상일경우
        findViewById(R.id.download_status).setVisibility(View.GONE);
        findViewById(R.id.videoView).setVisibility(View.GONE);
        findViewById(R.id.imageView).setVisibility(View.GONE);

        if(m_mimeType.compareTo("video") == 0) {
            findViewById(R.id.videoView).setVisibility(View.VISIBLE);
            final VideoView videoView = findViewById(R.id.videoView);
            videoView.setVideoPath(MoniteringApp.APP_STORE_PATH + "/" + m_filename);
            //videoView.setRotation(90);
            videoView.seekTo(0);
            videoView.start();
        } else {
        //이미지일 경우
            findViewById(R.id.imageView).setVisibility(View.VISIBLE);
            File imgFile = new  File(MoniteringApp.APP_STORE_PATH + "/" + m_filename);
            if(imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getPath());
                ImageView imageView = findViewById(R.id.imageView);
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    private class DownloadFileTask extends AsyncTask<String, String, Long> {
        private Context m_context;
        private PowerManager.WakeLock m_wakeLock;

        public DownloadFileTask(Context context) {
            m_context = context;
        }

        int count;
        long fileSize = -1;
        File outputFile = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        URLConnection connection = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            Log.i(DEBUG_TAG, "onPreExecute");
//            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
//            m_wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
//            m_wakeLock.acquire();
        }

        @Override
        protected Long doInBackground(String... params) {
            Log.i(DEBUG_TAG, "doInBackground");

            try {
                URL url = new URL(params[0]); //it's url
                connection = url.openConnection();
                connection.connect();

                fileSize = connection.getContentLength();
                inputStream = new BufferedInputStream(url.openStream(), 8192);
                outputFile = new File(MoniteringApp.APP_STORE_PATH, m_filename);

                outputStream = new FileOutputStream(outputFile);

                byte data[] = new byte[1024];
                long downloadedSize = 0;
                while((count = inputStream.read(data)) != -1) {
                    if(isCancelled()) {
                        //user cancel.
                        inputStream.close();
                        return Long.valueOf(-1);
                    }

                    downloadedSize += count;
                    final int ui_recieved = (int)downloadedSize;
                    final int ui_total = (int)fileSize;

                    if(fileSize > 0) {
//                        int per = (int)(((float)downloadedSize/fileSize) * 100);
//                        m_progressBar.setProgress(per);
//                        String str = "Downloaded " + downloadedSize + "KB / " + fileSize + "KB (" + (int)per + "%)";
//                        Log.i(DEBUG_TAG, Long.toString(downloadedSize));

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setDownloadText(ui_recieved, ui_total);
                            }
                        });
                    }

                    outputStream.write(data, 0, count);
                }

//                m_progressBar.setProgress(100);

                outputStream.flush();
                outputStream.close();
                outputStream = null;

                inputStream.close();
                inputStream = null;

            } catch(Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (null != outputStream) outputStream.close();
                    if (null != inputStream) inputStream.close();
                } catch(Exception e) {
                    e.printStackTrace();
                }

//                m_wakeLock.release();
            }

            return fileSize;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);

            Log.i(DEBUG_TAG, "onPostExecute");
            Log.i(DEBUG_TAG, m_mimeType);
            playVideo();

            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + outputFile.getAbsolutePath())));
            Toast.makeText(DownloadActivity.this, "다운로드가 완료되었습니다.", Toast.LENGTH_LONG).show();
        }
    }

}
