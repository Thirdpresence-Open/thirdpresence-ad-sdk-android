package com.thirdpresence.adsdk.sampleapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.HashMap;

import com.thirdpresence.adsdk.sdk.VideoAd;
import com.thirdpresence.adsdk.sdk.VideoInterstitial;
import com.thirdpresence.adsdk.sdk.internal.TLog;
import com.thirdpresence.sampleapp.R;

public class MainActivity extends AppCompatActivity implements VideoAd.Listener {

    private VideoInterstitial mInterstitial;

    private static final int LOCATION_PERMISSION_CHECK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

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

        // Create VideoInterstitial object
        mInterstitial = new VideoInterstitial();
        mInterstitial.setListener(this);

        Button initButton = (Button) findViewById(R.id.initButton);
        initButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Remove previous interstitial first to allow re-init
                mInterstitial.remove();

                TextView accountField = (TextView) findViewById(R.id.accountField);
                String mAccountName = accountField.getText().toString();

                TextView placementField = (TextView) findViewById(R.id.placementField);
                String mPlacementId = placementField.getText().toString();

                TextView vastTagField = (TextView) findViewById(R.id.vastTagField);
                String vastTag = vastTagField.getText().toString();

                HashMap<String, String> environment = new HashMap<>();

                environment.put(VideoAd.Environment.KEY_ACCOUNT, mAccountName);
                environment.put(VideoAd.Environment.KEY_PLACEMENT_ID, mPlacementId);
                environment.put(VideoAd.Environment.KEY_SERVER, VideoAd.SERVER_TYPE_PRODUCTION);

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

                try {
                    mInterstitial.init(MainActivity.this, environment, params, VideoAd.DEFAULT_TIMEOUT);
                } catch (IllegalStateException e) {
                    Log.e("SampleApp", e.toString());
                    Toast.makeText(MainActivity.this, "Init failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button loadButton = (Button) findViewById(R.id.loadButton);
        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Load an ad
                mInterstitial.loadAd();
            }
        });

        Button displayButton = (Button) findViewById(R.id.displayButton);
        displayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Display and if loaded
                if (mInterstitial.isAdLoaded()) {
                    mInterstitial.displayAd();
                }
                else {
                    Toast.makeText(MainActivity.this, "Ad not ready yet", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onAdEvent(String eventName, String arg1, String arg2, String arg3) {
        if (eventName.equals(VideoAd.Events.AD_STOPPED)) {
            mInterstitial.reset();
        }
        else if (eventName.equals(VideoAd.Events.AD_LOADED)) {
            Toast.makeText(MainActivity.this, "Ad loaded", Toast.LENGTH_SHORT).show();
        }
        else if (eventName.equals(VideoAd.Events.AD_ERROR)) {
            Log.e("SampleApp", "Error occured: " + arg1);
            Toast.makeText(MainActivity.this, "Loading ad failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPlayerReady() {
        Log.d("SampleApp", "Player initialised");
        Toast.makeText(MainActivity.this, "Initialized", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(VideoAd.ErrorCode errorCode, String message) {
        Log.e("SampleApp", "Error occured: " + message);
        Toast.makeText(MainActivity.this, "Loading player failed: " + message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_CHECK: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("SampleApp", "Location permission granted");
                } else {
                    Log.d("SampleApp", "Location permission denied");
                }
                return;
            }
        }
    }
}
