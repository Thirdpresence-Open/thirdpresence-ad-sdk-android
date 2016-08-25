package com.thirdpresence.adsdk.mediation.unity;

import android.app.Activity;

import com.thirdpresence.adsdk.sdk.VideoAd;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by okkonen on 27/06/16.
 */
public class ThirdpresenceAdapterBase {

    private ThirdpresencePlayerActivity mPlayerActivity;

    private static final String SERVER = VideoAd.SERVER_TYPE_PRODUCTION;
    private static final String EXTRAS_KEY_SDK_NAME = "sdk-name";
    private static final String EXTRAS_KEY_SDK_VERSION = "sdk-version";
    private static final String EXTRAS_KEY_ACCOUNT = "account";
    private static final String EXTRAS_KEY_PLACEMENT_ID = "placementid";

    private static final String EXTRAS_KEY_APP_NAME = "appname";
    private static final String EXTRAS_KEY_APP_VERSION = "appversion";
    private static final String EXTRAS_KEY_APP_STORE_URL = "appstoreurl";
    private static final String EXTRAS_KEY_SKIP_OFFSET = "skipoffset";
    private static final String EXTRAS_KEY_BUNDLE_ID = "bundleid";
    private static final String EXTRAS_KEY_USER_GENDER = "gender";
    private static final String EXTRAS_KEY_USER_YOB = "yob";

    public static synchronized ThirdpresenceAdapterBase getInstance() {
        // Empty default implementation
        return null;
    }

    /**
     * Displays the interstitial ad. Called from ThirdpresencePlayerActivity
     */
    public void displayAd() {
        // Empty default implementation.
    }

    /**
     * Removes the ad. Called from ThirdpresencePlayerActivity
     */
    public void removeAd() {
        // Empty default implementation.
    }

    /**
     * Sets the player activity.
     */
    public void setPlayerActivity(Activity activity) {
        mPlayerActivity = (ThirdpresencePlayerActivity)activity;
    }

    /**
     * Finishes the player activity.
     */
    public void finishPlayerActivity() {
        if (mPlayerActivity != null) {
            if (!mPlayerActivity.isFinishing()) {
                mPlayerActivity.finish();
            }
            mPlayerActivity = null;
        }
    }


    /**
     * Sets the environment data
     *
     * @param extras params passed from Unity runtime
     * @return Environment data map
     *
     */
    public static Map<String, String> setEnvironment(Map<String, String> extras) {
        Map<String, String> env = new HashMap<>();
        env.put(VideoAd.Environment.KEY_EXT_SDK, extras.get(EXTRAS_KEY_SDK_NAME));
        env.put(VideoAd.Environment.KEY_EXT_SDK_VERSION, extras.get(EXTRAS_KEY_SDK_VERSION));
        env.put(VideoAd.Environment.KEY_SERVER, SERVER);
        env.put(VideoAd.Environment.KEY_ACCOUNT, extras.get(EXTRAS_KEY_ACCOUNT));
        env.put(VideoAd.Environment.KEY_PLACEMENT_ID, extras.get(EXTRAS_KEY_PLACEMENT_ID));
        return env;
    }

    /**
     * Sets the player parameters
     *
     * @param extras params passed from Unity runtime
     * @return VideoAd parameters map
     *
     */
    public static Map<String, String> setPlayerParameters(Map<String, String> extras) {
        Map<String, String> params = new HashMap<>();
        params.put(VideoAd.Parameters.KEY_PUBLISHER, extras.get(EXTRAS_KEY_APP_NAME));
        params.put(VideoAd.Parameters.KEY_APP_NAME, extras.get(EXTRAS_KEY_APP_NAME));
        params.put(VideoAd.Parameters.KEY_APP_VERSION, extras.get(EXTRAS_KEY_APP_VERSION));
        params.put(VideoAd.Parameters.KEY_APP_STORE_URL, extras.get(EXTRAS_KEY_APP_STORE_URL));
        params.put(VideoAd.Parameters.KEY_BUNDLE_ID, extras.get(EXTRAS_KEY_BUNDLE_ID));
        params.put(VideoAd.Parameters.KEY_SKIP_OFFSET, extras.get(EXTRAS_KEY_SKIP_OFFSET));
        params.put(VideoAd.Parameters.KEY_USER_GENDER, extras.get(EXTRAS_KEY_USER_GENDER));
        params.put(VideoAd.Parameters.KEY_USER_YOB, extras.get(EXTRAS_KEY_USER_YOB));
        return params;
    }
}


