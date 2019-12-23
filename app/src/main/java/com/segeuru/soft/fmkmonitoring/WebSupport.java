package com.segeuru.soft.fmkmonitoring;

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
import java.net.URLEncoder;
import java.util.ArrayList;

public class WebSupport {
    private final String DEBUG_TAG = "segeuru.com";
    private Context m_context;
    private WebviewActivity m_webviewActivity;
    private ArrayList<String> m_fileNames;
    private int m_serverResponseCode;
    private String m_serverResponseMessage;

    public WebSupport(WebviewActivity webviewActivity) {
        m_webviewActivity = webviewActivity;
    }

    public void uploadData(String jsonString) {
        new uploadAsyncTask().execute(jsonString);
    }

    private void uploadDataAsync(String jsonString) {

        Log.i(DEBUG_TAG, jsonString);

        String uploadURL = null;
        JSONObject params = null;
        JSONArray filePaths = null;
        try {
            JSONObject jsonRoot = new JSONObject(jsonString);

            //test, get url.
            uploadURL = jsonRoot.getString("url");
            Log.i(DEBUG_TAG, uploadURL);

            //test, list of params.
            params = jsonRoot;
            for(int i=0;i<params.length();++i) {
                Log.i(DEBUG_TAG, params.names().getString(i));
                Log.i(DEBUG_TAG, params.get(params.names().getString(i)).toString());
            }

            //list of file paths.
            m_fileNames = new ArrayList<>();
            if(jsonRoot.has("fileData")) {
                filePaths = (JSONArray) jsonRoot.get("fileData");
                for (int i = 0; i < filePaths.length(); ++i) {
                    JSONObject filePath = (JSONObject) filePaths.get(i);
                    m_fileNames.add(filePath.getString("filename"));
                    //Log.i(DEBUG_TAG, filePath.getString("path"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

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
            for(int i=0;i<params.length();++i) {
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes(String.format("Content-Disposition: form-data; name=\"%s\"" + lineEnd, params.names().getString(i)));
                dataOutputStream.writeBytes(lineEnd);
                //dataOutputStream.writeUTF(params.getString(params.names().getString(i)));
                dataOutputStream.writeBytes(URLEncoder.encode (params.getString(params.names().getString(i)), "UTF-8"));
                dataOutputStream.writeBytes(lineEnd);
            }

            dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);

            //BEGIN - media datas
            for(int i=0;i<m_fileNames.size();++i) {
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file[]\";filename=\"" + m_fileNames.get(i) + "\"" + lineEnd);
                dataOutputStream.writeBytes(lineEnd);

                File sourceFile = new File(MoniteringApp.APP_STORE_PICTURE_PATH + "/" + m_fileNames.get(i));
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
            m_serverResponseCode = httpConn.getResponseCode();
            m_serverResponseMessage = httpConn.getResponseMessage();

            Log.i("uploadFile", "HTTP Response is : "
                        + m_serverResponseMessage + ": " + m_serverResponseCode);

            //close the streams //
            dataOutputStream.flush();
            dataOutputStream.close();

        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    private class uploadAsyncTask extends AsyncTask<String, String, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(String... args) {
            uploadDataAsync(args[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            m_webviewActivity.javaScriptCallback("uploadData", m_serverResponseMessage, Integer.toString(m_serverResponseCode));
        }
    }

}
