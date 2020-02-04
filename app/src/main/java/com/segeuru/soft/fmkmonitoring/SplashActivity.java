package com.segeuru.soft.fmkmonitoring;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SplashActivity extends AppCompatActivity {
    boolean isNeedUpdate;
    VersionCheck versionCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        final Context mContext = this;
        versionCheck = new VersionCheck(mContext);

        final Animation anim = AnimationUtils.loadAnimation(this, R.anim.alpha_anim);
        final ImageView imageView = findViewById(R.id.imageView_logo);
        anim.setFillAfter(true);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // 버전 체크
                isNeedUpdate = versionCheck.isNeedUpdate();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isNeedUpdate)
                {
                    versionCheck.showUpdateDialog();
                    return;
                }
                else
                {
                    if (versionCheck.remoteVersion.equals(""))
                    {
                        versionCheck.showCheckFailDialog();
                        return;
                    }
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(SplashActivity.this, WebviewActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        finish();
                    }
                }, 0);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        imageView.startAnimation(anim);
    }

}
