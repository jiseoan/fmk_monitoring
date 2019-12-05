package com.segeuru.soft.monitering;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class WebSupport {
    private final String DEBUG_TAG = "segeuru.com";
    private Context m_context;
    private WebviewActivity m_webviewActivity;
    private ArrayList<String> m_fileNames;

    public WebSupport(WebviewActivity webviewActivity) {
        m_webviewActivity = webviewActivity;
    }

    public void uploadImages(String json) {
        m_fileNames = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            for(int i=0;i<jsonArray.length();++i) {
                JSONObject jsonObject = (JSONObject)jsonArray.get(i);
                m_fileNames.add(jsonObject.getString("filename"));
                //Log.i(DEBUG_TAG, jsonObject.getString("filename"));
            }
        } catch(Exception e) {
            e.printStackTrace();
            return;
        }

        new uploadAsyncTask().execute();
    }

    private void uploadImagesAsync() {
        final String SERVER_URL = "http://test.raonworks.com/upload.php";
        final String boundary = "*****";
        final String twoHyphens = "--";
        final String lineEnd = "\r\n";

        URL url = null;
        byte[] buffer;
        int bytesRead, bytesAvailable, bufferSize;
        int maxBufferSize = 1024 * 1024;
        HttpURLConnection httpConn = null;

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

            DataOutputStream dataOutputStream = new DataOutputStream(httpConn.getOutputStream());

            //BEGIN - form datas
            dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
            dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"email\""+ lineEnd);
            dataOutputStream.writeBytes(lineEnd);
            dataOutputStream.writeBytes("devhong@segeuru.com");
            dataOutputStream.writeBytes(lineEnd);
            dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);

            //BEGIN - media datas
            for(int i=0;i<m_fileNames.size();++i) {
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file[]\";filename=\"" + m_fileNames.get(i) + "\"" + lineEnd);
                dataOutputStream.writeBytes(lineEnd);

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
                    dataOutputStream.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                // send multipart form data necesssary after file data...
                fileInputStream.close();
                dataOutputStream.writeBytes(lineEnd);
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                //dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            }

            dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

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
            dataOutputStream.flush();
            dataOutputStream.close();

//            int responseCode = httpConn.getResponseCode();
//            Log.i("segeuru.com", Integer.toString(responseCode));

        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    private class uploadAsyncTask extends AsyncTask<Activity, Activity, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Activity... activities) {
            uploadImagesAsync();
            return 14;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            m_webviewActivity.javaScriptCallback("uploadedImages", "", "");
        }
    }

}
