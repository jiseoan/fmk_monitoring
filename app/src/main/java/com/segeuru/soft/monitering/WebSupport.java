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

public class WebSupport {
    private final String DEBUG_TAG = "segeuru.com";
    private Context m_context;

    private void uploadImage(Activity activity) {
        final String SERVER_URL = "http://test.raonworks.com/upload.php";
        final String boundary = "*****";
        final String twoHyphens = "--";
        final String lineEnd = "\r\n";
        final String fileName = "a.jpg";

        URL url = null;
        byte[] buffer;
        int bytesRead, bytesAvailable, bufferSize;
        int maxBufferSize = 1024 * 1024;
        HttpURLConnection httpConn = null;
        DataOutputStream dos = null;

        File sourceFile = new File(MoniteringApp.APP_STORE_PATH + "/" + fileName);
        if(!sourceFile.exists()) {
            Log.i(DEBUG_TAG, "not exists");
            return;
        }

        try {
            FileInputStream fileInputStream = new FileInputStream(sourceFile);
            url = new URL(SERVER_URL);

            // Open a HTTP  connection to  the URL
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setDoInput(true); // Allow Inputs
            httpConn.setDoOutput(true); // Allow Outputs
            httpConn.setUseCaches(false); // Don't use a Cached Copy
            httpConn.setRequestMethod("POST");
            httpConn.setRequestProperty("Connection", "Keep-Alive");
            httpConn.setRequestProperty("ENCTYPE", "multipart/form-data");
            httpConn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            httpConn.setRequestProperty("uploaded_file", fileName);

            dos = new DataOutputStream(httpConn.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + fileName + "\"" + lineEnd);
            dos.writeBytes(lineEnd);

            // create a buffer of  maximum size
            bytesAvailable = fileInputStream.available();

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            // send multipart form data necesssary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Responses from the server (code and message)
            int serverResponseCode = httpConn.getResponseCode();
            String serverResponseMessage = httpConn.getResponseMessage();

            Log.i("uploadFile", "HTTP Response is : "
                    + serverResponseMessage + ": " + serverResponseCode);

            if(serverResponseCode == 200){

                activity.runOnUiThread(new Runnable() {
                    public void run() {

//                        String msg = "File Upload Completed.\n\n See uploaded file here : \n\n"
//                                +" http://www.androidexample.com/media/uploads/"
//                                +uploadFileName;
//
//                        messageText.setText(msg);
//                        Toast.makeText(UploadToServer.this, "File Upload Complete.",
//                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            //close the streams //
            fileInputStream.close();
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
