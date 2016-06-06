package com.thirdpresence.adsdk.mediation.unity;

import com.thirdpresence.adsdk.sdk.VideoAd;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * Helper class for creating environment data and player parameters
 *
 */
public class ThirdpresenceAdapterHelper {

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

    private ThirdpresenceAdapterHelper() {}

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
        return params;
    }

}
