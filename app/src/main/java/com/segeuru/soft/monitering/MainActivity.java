package com.segeuru.soft.monitering;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int REQUEST_PERMISSION_CODE = 1000;
    private String[] PERMISSIONS = { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE };
    private DBHelper m_dbHlper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = findViewById(R.id.btn_webview);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, WebviewActivity.class);
                startActivity(intent);
            }
        });

        btn = findViewById(R.id.btn_camera);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CameraViewer.class);
                startActivity(intent);
            }
        });

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!hasPermissions(PERMISSIONS))
                requestPermissions(PERMISSIONS, REQUEST_PERMISSION_CODE);
        } else {
            finish();
        }

        Log.d("segeuru.com", MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString());
    }

    private boolean hasPermissions(String[] permissions) {
        int result;
        for(String perms : permissions) {
            result = ContextCompat.checkSelfPermission(this, perms);
            if(result == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode) {
            case REQUEST_PERMISSION_CODE:
                if(grantResults.length > 0) {
                    boolean result = true;
                    result &= grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    result &= grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if(!result) {
                        Toast.makeText(this, "접근권한이 거부되었습니다.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
                break;
        }
    }


}
