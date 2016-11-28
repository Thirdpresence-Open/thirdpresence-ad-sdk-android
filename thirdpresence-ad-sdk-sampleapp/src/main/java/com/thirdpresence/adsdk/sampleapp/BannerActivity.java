package com.thirdpresence.adsdk.sampleapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.thirdpresence.adsdk.sdk.BannerView;
import com.thirdpresence.adsdk.sdk.VideoAd;
import com.thirdpresence.adsdk.sdk.VideoAdManager;
import com.thirdpresence.adsdk.sdk.internal.TLog;
import com.thirdpresence.sampleapp.R;

import java.util.HashMap;

import static com.thirdpresence.adsdk.sdk.VideoAd.Events.AD_ERROR;
import static com.thirdpresence.adsdk.sdk.VideoAd.Events.AD_LOADED;
import static com.thirdpresence.adsdk.sdk.VideoAd.Events.AD_STARTED;
import static com.thirdpresence.adsdk.sdk.VideoAd.Events.AD_STOPPED;

public class BannerActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_CHECK = 1;
    private String mAccountName;
    private String mPlacementId;
    private EditText mStatusField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mStatusField = (EditText) findViewById(R.id.statusField);
        mStatusField.setText("IDLE");

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

        Button reloadButton = (Button) findViewById(R.id.reloadButton);
        reloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadAd();
            }
        });
        loadAd();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VideoAdManager.getInstance().remove(mPlacementId);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String permissions[],
            @NonNull int[] grantResults) {

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
            } else if (eventName.equals(AD_STARTED)) {
                mStatusField.setText("DISPLAYING");
            } else if (eventName.equals(AD_STOPPED)) {
                mStatusField.setText("STOPPED");
            } else if (eventName.equals(AD_ERROR)) {
                Toast.makeText(BannerActivity.this, "An error occured: " + arg1, Toast.LENGTH_SHORT).show();
                mStatusField.setText("ERROR");
            }
        }

        @Override
        public void onPlayerReady() {
        }

        @Override
        public void onError(VideoAd.ErrorCode errorCode, String message) {
            Toast.makeText(BannerActivity.this, message, Toast.LENGTH_SHORT).show();
            mStatusField.setText("ERROR");
        }
    }

    /*
     * This method demonstrates how to initialize a banner ad placement.
     */
    private void loadAd() {
        // Remove previous ad instance if already initialized
        VideoAdManager.getInstance().clear();

        TextView accountField = (TextView) findViewById(R.id.accountField);
        mAccountName = accountField.getText().toString();

        TextView placementField = (TextView) findViewById(R.id.placementField);
        mPlacementId = placementField.getText().toString();

        TextView vastTagField = (TextView) findViewById(R.id.vastTagField);
        String vastTag = vastTagField.getText().toString();

        HashMap<String, String> environment = new HashMap<>();

        environment.put(VideoAd.Environment.KEY_ACCOUNT, mAccountName);
        environment.put(VideoAd.Environment.KEY_PLACEMENT_ID, mPlacementId);

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

        // This is used to test external VAST tags with the player
        if (vastTag != null && vastTag.length() > 0) {
            params.put(VideoAd.Parameters.KEY_VAST_URL, vastTag);
        }

        // When Google Play Services is available it is used to retrieves Google Advertiser ID.
        // Otherwise device ID (e.g. ANDROID_ID) shall be passed from the app.
        // params.put(VideoAd.Parameters.KEY_DEVICE_ID, "<ANDROID_ID>");

        BannerView bannerView = (BannerView)findViewById(R.id.bannerView);

        VideoAd ad = VideoAdManager.getInstance().create(VideoAd.PLACEMENT_TYPE_BANNER, mPlacementId);
        ad.setListener(new BannerActivity.SampleAppListener());
        ad.init(this, bannerView, environment, params, VideoAd.DEFAULT_TIMEOUT);
        ad.loadAd();

        mStatusField.setText("LOADING");
    }

}
