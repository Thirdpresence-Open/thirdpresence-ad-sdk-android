package com.thirdpresence.adsdk.mediation.unity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

/**
 *
 * ThirdpresencePlayerActivity is an activity that displays the player on Unity apps.
 *
 */
public class ThirdpresencePlayerActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thirdpresence_unity_plugin);

        hideNavigationBar();

        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {

                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    hideNavigationBar();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        ThirdpresenceInterstitialAdapter.getInstance().setPlayerActivity(this);
        ThirdpresenceInterstitialAdapter.getInstance().displayAd();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideNavigationBar();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isFinishing()) {
            ThirdpresenceInterstitialAdapter.getInstance().removeInterstitial();
        }
    }

    private void hideNavigationBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE;
        decorView.setSystemUiVisibility(uiOptions);
    }


}