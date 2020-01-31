package com.segeuru.soft.fmkmonitoring;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import androidx.appcompat.app.AlertDialog;

public class VersionCheck { // 버전 체크 클래스 [2020.01.30] - jhlee@falsto.com
    private final Context mContext;
    private final String DEBUG_TAG = "segeuru.com";

    public VersionCheck(Context context)
    {
        mContext = context;
    }

    // 업데이트가 필요한지 검사
    public boolean isNeedUpdate()
    {
        String currentVersion = getCurrentVersion();
        String remoteVersion = getRemoteVersionFromPlayStore();
        if (remoteVersion.equals(""))
            remoteVersion = getRemoteVersionFromRemoteConfig();
        if (!remoteVersion.equals("") && !currentVersion.equals(remoteVersion)) // 원격서버 버전이 존재하고 버전이 다르다면 업데이트 존재
            return true;
        // Log.d(DEBUG_TAG, "현재버전: " + currentVersion + " (서버버전: " + remoteVersion + ")");
        return false;
    }

    // 업데이트 창을 표시
    public void showUpdateDialog()
    {
        // 버전 업데이트 안내 팝업을 띄움
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("업데이트 안내");
        builder.setMessage("새로운 버전이 있습니다.");
        builder.setPositiveButton("스토어로 이동",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("market://details?id=" + mContext.getPackageName()));
                        mContext.startActivity(intent);
                    }
                });
        builder.setNegativeButton("종료",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                });
        builder.setCancelable(false);
        builder.show();
    }

    // 현재 버전정보 가져오기
    private String getCurrentVersion()
    {
        String versionName = "";
        try {
            PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            versionName = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    // 원격서버 버전정보 가져오기 (Google PlayStore 파싱)
    private String getRemoteVersionFromPlayStore()
    {
        String versionName = "";
        JsoupAsyncTask task = new JsoupAsyncTask();
        try {
            versionName = task.execute().get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Log.d(DEBUG_TAG, "구글플레이 스토어 버전: " + versionName);
        return versionName;
    }

    // 원격서버 버전정보 가져오기 (FirebaseRemoteConfig)
    private String getRemoteVersionFromRemoteConfig()
    {
        String versionName = "";
        // 구글플레이 서비스 설치 검사
        if (!checkGooglePlayServices()) return versionName;
        // FirebaseRemoteConfig 에서 값을 불러옴
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        remoteConfig.setConfigSettingsAsync(configSettings);
        versionName = remoteConfig.getString("versionName");
        // Log.d(DEBUG_TAG, "파이어베이스 원격설정 버전: " + versionName);
        return versionName;
    }

    // 플레이스토어 활성여부 검사
    private boolean checkGooglePlayServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(mContext);
        if (status != ConnectionResult.SUCCESS) {
            return false;
        }
        return true;
    }

    private class JsoupAsyncTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(Void... params) {
            String versionName = "";
            try {
                String packageName = mContext.getPackageName();
                String url = "https://play.google.com/store/apps/details?id=" + packageName;
                Document document = Jsoup
                        .connect(url)
                        .timeout(3000)
                        .get();
                Element element = document.select("span.htlgb div span.htlgb").get(3);
                versionName = element.text();
            }
            catch (Exception e)
            {
                // e.printStackTrace();
            }
            return versionName;
        }
    }
}
