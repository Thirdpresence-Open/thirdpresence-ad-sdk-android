package com.thirdpresence.adsdk.sampleapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.util.HashMap;

import com.thirdpresence.adsdk.sdk.VideoAd;
import com.thirdpresence.adsdk.sdk.VideoInterstitial;
import com.thirdpresence.sampleapp.R;

public class MainActivity extends AppCompatActivity implements VideoAd.Listener {

    private VideoInterstitial mInterstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        // Create VideoInterstitial object
        mInterstitial = new VideoInterstitial();
        mInterstitial.setListener(this);

        Button initButton = (Button) findViewById(R.id.initButton);
        initButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Init the ad unit

                HashMap<String, String> environment = new HashMap<>();
                environment.put(VideoAd.Environment.KEY_ACCOUNT, "sdk-demo");
                environment.put(VideoAd.Environment.KEY_PLACEMENT_ID, "sa7nvltbrn");

                environment.put(VideoAd.Environment.KEY_SERVER, VideoAd.SERVER_TYPE_PRODUCTION);

                HashMap<String, String> params = new HashMap<>();

                params.put(VideoAd.Parameters.KEY_PUBLISHER, "Thirdpresence Sample App");
                params.put(VideoAd.Parameters.KEY_APP_NAME, "Thirdpresence Sample App");
                params.put(VideoAd.Parameters.KEY_APP_VERSION, "1.0");
                params.put(VideoAd.Parameters.KEY_APP_STORE_URL, "https://play.google.com/store/apps/details?id=com.thirdpresence.adsdk.sampleapp");

                // When Google Play Services is available it is used to retrieves Google Advertiser ID.
                // Otherwise device ID (e.g. ANDROID_ID) shall be passed from the app.
                // params.put(VideoAd.Parameters.KEY_DEVICE_ID, "<ANDROID_ID>");

                try {
                    mInterstitial.init(MainActivity.this, environment, params, VideoAd.DEFAULT_TIMEOUT);
                    Toast.makeText(MainActivity.this, "Initialized", Toast.LENGTH_SHORT).show();
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
        mInterstitial.loadAd();
    }

    @Override
    public void onError(VideoAd.ErrorCode errorCode, String message) {
        Log.e("SampleApp", "Error occured: " + message);
        Toast.makeText(MainActivity.this, "Loading player failed: " + message, Toast.LENGTH_SHORT).show();
    }
}
