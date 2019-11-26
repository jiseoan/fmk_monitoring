package com.segeuru.soft.monitering;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadActivity extends AppCompatActivity {

    private final String DEBUG_TAG = "segeuru.com";
    private ProgressBar m_progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        m_progressBar =findViewById(R.id.download_progressBar);
        m_progressBar.setProgress(0);

        findViewById(R.id.btn_runDownload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String fileURL = "http://doc.raonworks.com/wp-content/uploads/2019/11/video.mp4";
                File file = new File(MoniteringApp.APP_STORE_PATH, "video.mp4");

                if(file.exists()) {
                    //already exist.
                    Toast.makeText(DownloadActivity.this, "같은 파일이 존재합니다.", Toast.LENGTH_LONG).show();
                } else {
                    //download new.
                    final DownloadFileTask downloadFileTask = new DownloadFileTask(DownloadActivity.this);
                    downloadFileTask.execute(fileURL);
                }
            }
        });


    }

    private class DownloadFileTask extends AsyncTask<String, String, Long> {
        private Context m_context;
        private PowerManager.WakeLock m_wakeLock;

        public DownloadFileTask(Context context) {
            m_context = context;
        }

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
            int count;
            long fileSize = -1;
            InputStream inputStream = null;
            OutputStream outputStream = null;
            URLConnection connection = null;
            File outputFile = null;

            Log.i(DEBUG_TAG, "doInBackground");

            try {
                URL url = new URL(params[0]); //it's url
                connection = url.openConnection();
                connection.connect();

                fileSize = connection.getContentLength();
                inputStream = new BufferedInputStream(url.openStream(), 8192);
                outputFile = new File(MoniteringApp.APP_STORE_PATH, "video.mp4");

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

                    if(fileSize > 0) {
                        int per = (int)(((float)downloadedSize/fileSize) * 100);
                        m_progressBar.setProgress(per);
//                        String str = "Downloaded " + downloadedSize + "KB / " + fileSize + "KB (" + (int)per + "%)";
//                        Log.i(DEBUG_TAG, Long.toString(downloadedSize));
                    }

                    outputStream.write(data, 0, count);
                }

                m_progressBar.setProgress(100);

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
            Toast.makeText(DownloadActivity.this, "다운로드가 완료되었습니다.", Toast.LENGTH_LONG).show();
        }
    }

}
