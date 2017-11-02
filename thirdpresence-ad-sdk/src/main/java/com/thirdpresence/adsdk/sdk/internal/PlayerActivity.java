package com.thirdpresence.adsdk.sdk.internal;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.thirdpresence.adsdk.sdk.R;
import com.thirdpresence.adsdk.sdk.VideoAd;
import com.thirdpresence.adsdk.sdk.VideoAdManager;

/**
 * <h1>PlayerActivity</h1>
 *
 * PlayerActivity is an activity that displays the ad view.
 *
 */
public class PlayerActivity extends Activity {

    public static final String PLACEMENT_ID_EXTRA_KEY = "com.thirdpresence.adsdk.sdk.placementid";

    private String mPlacementId;
    private boolean mDisplayingAd;

    private static final int NAVIGATION_BAR_HIDE_DELAY = 1000;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thirdpresence_player);

        hideNavigationBar();

        mPlacementId = this.getIntent().getStringExtra(PLACEMENT_ID_EXTRA_KEY);

        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {

                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            hideNavigationBar();
                        }
                    }, NAVIGATION_BAR_HIDE_DELAY);
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (mDisplayingAd) {
            VideoAd videoAd = VideoAdManager.getInstance().get(mPlacementId);
            videoAd.reset();
            mDisplayingAd = false;
        }
        mPlacementId = this.getIntent().getStringExtra(PLACEMENT_ID_EXTRA_KEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (mPlacementId != null) {
            if (!mDisplayingAd) {
                mDisplayingAd = true;
                VideoAd videoAd = VideoAdManager.getInstance().get(mPlacementId);
                videoAd.displayAd(this, new Runnable() {
                    @Override
                    public void run() {
                        mDisplayingAd = false;
                        if (!isFinishing()) {
                            TLog.w("PlayerActivity finishing");
                            finish();
                        } else {
                            TLog.w("PlayerActivity already finishing");
                        }
                    }
                });
            }
        } else {
            TLog.w("PlayerActivity started without placement id");
            finish();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onResume() {
        super.onResume();
        hideNavigationBar();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDisplayingAd) {
            // This should occur only if the activity is unexpectedly destroyed
            VideoAd videoAd = VideoAdManager.getInstance().get(mPlacementId);
            videoAd.remove();
        }
        mPlacementId = null;
        mDisplayingAd = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackPressed() {
        if (!mDisplayingAd) {
            finish();
        }
    }

    /**
     * Hides navigation bar
     */
    private void hideNavigationBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            } else {
                uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN;
            }
            decorView.setSystemUiVisibility(uiOptions);
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    /**
     * Gets current placement id
     */
    public String getPlacementId() {
        return mPlacementId;
    }

}