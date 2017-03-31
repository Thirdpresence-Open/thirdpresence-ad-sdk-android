package com.thirdpresence.adsdk.sampleapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.HashMap;

import com.thirdpresence.adsdk.sdk.VideoAd;
import com.thirdpresence.adsdk.sdk.VideoAdManager;
import com.thirdpresence.adsdk.sdk.internal.TLog;
import com.thirdpresence.sampleapp.R;

import static com.thirdpresence.adsdk.sdk.VideoAd.Events.AD_ERROR;
import static com.thirdpresence.adsdk.sdk.VideoAd.Events.AD_LOADED;

public class RewardedVideoActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_CHECK = 1;
    private String mAccountName;
    private String mPlacementId;
    private EditText mStatusField;
    private EditText mRewardField;
    private boolean mUseStagingServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewarded_video);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mStatusField = (EditText) findViewById(R.id.statusField);
        mStatusField.setText("IDLE");

        mRewardField = (EditText) findViewById(R.id.rewardField);
        mRewardField.setText("");

        mUseStagingServer = getIntent().getBooleanExtra("use_staging_server", false);
        if (mUseStagingServer) {
            TextView placementField = (TextView) findViewById(R.id.placementField);
            placementField.setText(R.string.staging_rewarded_video_placement_id);
        }

        // Enable console logs for the SDK
        TLog.enabled = true;

        // Access to the location data is optional but highly recommended.
        // Android 6.0 requires user to grant access to location services.
        // Therefore the app shall request permission from the user before
        // initialising the interstitial
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
            if (checkSelfPermission(
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATION_PERMISSION_CHECK);
            }
        }

        Button initButton = (Button) findViewById(R.id.initButton);
        initButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initAd();
            }
        });

        Button loadButton = (Button) findViewById(R.id.loadButton);
        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadAd();
            }
        });

        Button displayButton = (Button) findViewById(R.id.displayButton);
        displayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAd();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_CHECK: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("SampleApp", "Location permission granted");
                } else {
                    Log.d("SampleApp", "Location permission denied");
                }
            }
        }
    }

    /*
     * An example VideoAd listener implementation.
     * This allows to listen video player events.
     * See VideoAd.Events for all event names..
     */
    class SampleAppListener implements VideoAd.Listener {
        @Override
        public void onAdEvent(String eventName, String arg1, String arg2, String arg3) {
            if (eventName.equals(AD_LOADED)) {
                mStatusField.setText("LOADED");
            } else if (eventName.equals(AD_ERROR)) {
                Toast.makeText(RewardedVideoActivity.this, "An error occured: " + arg1, Toast.LENGTH_SHORT).show();
                mStatusField.setText("ERROR");
            }
        }

        @Override
        public void onPlayerReady() {
            mStatusField.setText("READY");
        }

        @Override
        public void onError(VideoAd.ErrorCode errorCode, String message) {
            Toast.makeText(RewardedVideoActivity.this, message, Toast.LENGTH_SHORT).show();
            mStatusField.setText("ERROR");
        }
    }

    /*
     * This method demonstrates how to initialize an interstitial ad placement.
     */
    private void initAd() {
        // Remove previous ad instance if already initialized
        VideoAdManager.getInstance().clear();

        TextView accountField = (TextView) findViewById(R.id.accountField);
        mAccountName = accountField.getText().toString();

        TextView placementField = (TextView) findViewById(R.id.placementField);
        mPlacementId = placementField.getText().toString();

        HashMap<String, String> environment = new HashMap<>();

        environment.put(VideoAd.Environment.KEY_ACCOUNT, mAccountName);
        environment.put(VideoAd.Environment.KEY_PLACEMENT_ID, mPlacementId);
        environment.put(VideoAd.Environment.KEY_REWARD_TITLE, "credits");
        environment.put(VideoAd.Environment.KEY_REWARD_AMOUNT, "10");

        if (mUseStagingServer) {
            environment.put(VideoAd.Environment.KEY_SERVER, VideoAd.SERVER_TYPE_STAGING);
        }

        HashMap<String, String> params = new HashMap<>();
        params.put(VideoAd.Parameters.KEY_PUBLISHER, "Thirdpresence Sample App");
        params.put(VideoAd.Parameters.KEY_APP_NAME, "Thirdpresence Sample App");
        params.put(VideoAd.Parameters.KEY_APP_VERSION, "1.0");
        params.put(VideoAd.Parameters.KEY_APP_STORE_URL, "https://play.google.com/store/apps/details?id=com.thirdpresence.adsdk.sampleapp");

        // In order to get more targeted ads you shall provide user's gender and year of birth
        // You can use e.g. Google+ API or Facebook Graph API
        // https://developers.google.com/android/reference/com/google/android/gms/plus/model/people/package-summary
        // https://developers.facebook.com/docs/android/graph/
        params.put(VideoAd.Parameters.KEY_USER_GENDER, "male");
        params.put(VideoAd.Parameters.KEY_USER_YOB, "1970");

        // When Google Play Services is available it is used to retrieves Google Advertiser ID.
        // Otherwise device ID (e.g. ANDROID_ID) shall be passed from the app.
        // params.put(VideoAd.Parameters.KEY_DEVICE_ID, "<ANDROID_ID>");

        VideoAd ad = VideoAdManager.getInstance().create(VideoAd.PLACEMENT_TYPE_REWARDED_VIDEO, mPlacementId);
        ad.setListener(new SampleAppListener());
        ad.init(RewardedVideoActivity.this, environment, params, VideoAd.DEFAULT_TIMEOUT);
        mStatusField.setText("INITIALIZING");
        mRewardField.setText("");

    }

    /*
     * This method demonstrates how to load an ad
     */
    private void loadAd() {
        VideoAd ad = VideoAdManager.getInstance().get(mPlacementId);
        if (ad != null) {
            ad.loadAd();
            mRewardField.setText("");
            mStatusField.setText("LOADING");
        } else {
            Toast.makeText(RewardedVideoActivity.this, "Ad not initialized yet", Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * This method demonstrates how to display an ad.
     *
     * The Runnable is executed when displaying ad is completed and allows
     * to do further actions in the app, such as loading a new activity
     */
    private void displayAd() {
        final VideoAd ad = VideoAdManager.getInstance().get(mPlacementId);
        if (ad != null) {
            if (ad.isAdLoaded()) {
                ad.displayAd(null, new Runnable() {
                    @Override
                    public void run() {
                        // Reward is earned if video has been completed
                        if (ad.isVideoCompleted()) {
                            String rewardString = ad.getRewardAmount() + " " + ad.getRewardTitle();
                            mRewardField.setText(rewardString);
                            mStatusField.setText("COMPLETED");
                        }

                        // Call reset to close the ad placement
                        ad.reset();

                    }
                });
                mStatusField.setText("DISPLAYING");
            } else {
                Toast.makeText(RewardedVideoActivity.this, "Ad not ready yet", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(RewardedVideoActivity.this, "Ad not initialized yet", Toast.LENGTH_SHORT).show();
        }
    }
}
