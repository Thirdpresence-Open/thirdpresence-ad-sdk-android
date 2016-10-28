package com.thirdpresence.adsdk.mediation.unity;

import com.thirdpresence.adsdk.sdk.VideoAd;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * ThirdpresenceAdapterBase is a base class for video ad adapters
 *
 */
public class ThirdpresenceAdapterBase {

    private static final String SERVER = VideoAd.SERVER_TYPE_PRODUCTION;

    public static final String EXTRAS_KEY_SDK_NAME = "sdk-name";
    public static final String EXTRAS_KEY_SDK_VERSION = "sdk-version";
    public static final String EXTRAS_KEY_ACCOUNT = "account";
    public static final String EXTRAS_KEY_PLACEMENT_ID = "placementid";
    public static final String EXTRAS_KEY_REWARD_TITLE = "rewardtitle";
    public static final String EXTRAS_KEY_REWARD_AMOUNT = "rewardamount";

    public static final String EXTRAS_KEY_APP_NAME = "appname";
    public static final String EXTRAS_KEY_APP_VERSION = "appversion";
    public static final String EXTRAS_KEY_APP_STORE_URL = "appstoreurl";
    public static final String EXTRAS_KEY_SKIP_OFFSET = "skipoffset";
    public static final String EXTRAS_KEY_BUNDLE_ID = "bundleid";
    public static final String EXTRAS_KEY_USER_GENDER = "gender";
    public static final String EXTRAS_KEY_USER_YOB = "yob";

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


