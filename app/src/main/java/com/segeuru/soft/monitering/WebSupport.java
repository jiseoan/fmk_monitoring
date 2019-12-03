package com.segeuru.soft.monitering;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class WebSupport {
    private final String DEBUG_TAG = "segeuru.com";
    private Context m_context;
    private ArrayList<String> m_fileNames;

    public WebSupport() {
        m_fileNames = new ArrayList<>();
        m_fileNames.add("040.jpg");
        m_fileNames.add("045.jpg");
    }

    private void uploadImage(Activity activity) {
        final String SERVER_URL = "http://test.raonworks.com/upload.php";
        final String boundary = "*****";
        final String twoHyphens = "--";
        final String lineEnd = "\r\n";
        //final String fileName = "040.jpg";

        URL url = null;
        byte[] buffer;
        int bytesRead, bytesAvailable, bufferSize;
        int maxBufferSize = 1024 * 1024;
        HttpURLConnection httpConn = null;

//        File sourceFile = new File(MoniteringApp.APP_STORE_PATH + "/" + fileName);
//        if(!sourceFile.exists()) {
//            Log.i(DEBUG_TAG, "not exists");
//            return;
//        }

        try {
            // Open a HTTP  connection to  the URL
            url = new URL(SERVER_URL);
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setDoInput(true); // Allow Inputs
            httpConn.setDoOutput(true); // Allow Outputs
            httpConn.setUseCaches(false); // Don't use a Cached Copy
            httpConn.setRequestMethod("POST");
            httpConn.setRequestProperty("Connection", "Keep-Alive");
            httpConn.setRequestProperty("Charset", "UTF-8");
            httpConn.setRequestProperty("ENCTYPE", "multipart/form-data");
            httpConn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            //httpConn.setRequestProperty("uploaded_file", fileName);

            DataOutputStream dos = new DataOutputStream(httpConn.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + lineEnd);

            for(int i=0;i<m_fileNames.size();++i) {
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file[]\";filename=\"" + m_fileNames.get(i) + "\"" + lineEnd);
                dos.writeBytes(lineEnd);

                File sourceFile = new File(MoniteringApp.APP_STORE_PATH + "/" + m_fileNames.get(i));
                if (!sourceFile.exists()) {
                    Log.i(DEBUG_TAG, "not exists");
                    return;
                }
                FileInputStream fileInputStream = new FileInputStream(sourceFile);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                // send multipart form data necesssary after file data...
                fileInputStream.close();
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                //dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            }

            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                int serverResponseCode = httpConn.getResponseCode();
                String serverResponseMessage = httpConn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

//                if (serverResponseCode == 200) {
//
//                    activity.runOnUiThread(new Runnable() {
//                        public void run() {
//
//                        String msg = "File Upload Completed.\n\n See uploaded file here : \n\n"
//                                +" http://www.androidexample.com/media/uploads/"
//                                +uploadFileName;
//
//                        messageText.setText(msg);
//                        Toast.makeText(UploadToServer.this, "File Upload Complete.",
//                                Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                }

                //close the streams //
                dos.flush();
                dos.close();

//            int responseCode = httpConn.getResponseCode();
//            Log.i("segeuru.com", Integer.toString(responseCode));

        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    public void uploadImages(Activity activity) {
        new uploadAsyncTask().execute(activity);
    }

    private class uploadAsyncTask extends AsyncTask<Activity, Activity, Long> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Long doInBackground(Activity... urls) {
            uploadImage(urls[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
        }
    }

}
