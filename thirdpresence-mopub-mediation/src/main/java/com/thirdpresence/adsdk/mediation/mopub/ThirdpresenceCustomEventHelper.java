package com.thirdpresence.adsdk.mediation.mopub;

import android.content.Context;

import com.mopub.common.MoPub;
import com.mopub.mobileads.MoPubErrorCode;
import com.thirdpresence.adsdk.sdk.VideoAd;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * Helper class for creating environment data and player parameters
 *
 */
public class ThirdpresenceCustomEventHelper {

    private static final String SERVER = VideoAd.SERVER_TYPE_PRODUCTION;
    private static final String SDK_NAME = "mopub";
    private static final String EXTRAS_KEY_ACCOUNT = "account";
    private static final String EXTRAS_KEY_PLACEMENT_ID = "placementid";
    private static final String EXTRAS_KEY_DISABLE_BACK = "disablebackbutton";

    private static final String EXTRAS_KEY_APP_NAME = "appname";
    private static final String EXTRAS_KEY_APP_VERSION = "appversion";
    private static final String EXTRAS_KEY_APP_STORE_URL = "appstoreurl";
    private static final String EXTRAS_KEY_SKIP_OFFSET = "skipoffset";



    private ThirdpresenceCustomEventHelper() {}

    /**
     * Sets the environment data
     *
     * @param serverExtras MoPub server extras
     * @return Environment data map
     *
     */
    public static Map<String, String> setEnvironment(Map<String, String> serverExtras) {
        Map<String, String> env = new HashMap<>();
        env.put(VideoAd.Environment.KEY_EXT_SDK, SDK_NAME);
        env.put(VideoAd.Environment.KEY_EXT_SDK_VERSION, MoPub.SDK_VERSION);
        env.put(VideoAd.Environment.KEY_SERVER, SERVER);
        env.put(VideoAd.Environment.KEY_ACCOUNT, serverExtras.get(EXTRAS_KEY_ACCOUNT));
        env.put(VideoAd.Environment.KEY_PLACEMENT_ID, serverExtras.get(EXTRAS_KEY_PLACEMENT_ID));
        env.put(VideoAd.Environment.KEY_DISABLE_BACK_BUTTON, serverExtras.get(EXTRAS_KEY_DISABLE_BACK));
        return env;
    }

    /**
     * Sets the player parameters
     *
     * @param context context
     * @param serverExtras MoPub server extras
     * @return VideoAd parameters map
     *
     */
    public static Map<String, String> setPlayerParameters(Context context, Map<String, String> serverExtras) {
        Map<String, String> params = new HashMap<>();
        params.put(VideoAd.Parameters.KEY_PUBLISHER, serverExtras.get(EXTRAS_KEY_APP_NAME));
        params.put(VideoAd.Parameters.KEY_APP_NAME, serverExtras.get(EXTRAS_KEY_APP_NAME));
        params.put(VideoAd.Parameters.KEY_APP_VERSION, serverExtras.get(EXTRAS_KEY_APP_VERSION));
        params.put(VideoAd.Parameters.KEY_APP_STORE_URL, serverExtras.get(EXTRAS_KEY_APP_STORE_URL));
        params.put(VideoAd.Parameters.KEY_BUNDLE_ID, context.getPackageName());
        params.put(VideoAd.Parameters.KEY_SKIP_OFFSET, serverExtras.get(EXTRAS_KEY_SKIP_OFFSET));
        return params;
    }

    /**
     * Maps Thirdpresence error codes to MoPub error codes.
     *
     * @param errorCode Thirdpresence error code
     * @return MoPub error code
     *
     */
    public static MoPubErrorCode mapErrorCode(VideoAd.ErrorCode errorCode) {
        MoPubErrorCode moPubErrorCode;
        switch (errorCode) {
            case NETWORK_FAILURE:
                moPubErrorCode = MoPubErrorCode.NO_CONNECTION;
                break;
            case PLAYER_INIT_FAILED:
                moPubErrorCode = MoPubErrorCode.INTERNAL_ERROR;
                break;
            case AD_NOT_READY:
                moPubErrorCode = MoPubErrorCode.NETWORK_INVALID_STATE;
                break;
            case NETWORK_TIMEOUT:
                moPubErrorCode = MoPubErrorCode.NETWORK_TIMEOUT;
                break;
            case NO_FILL:
                moPubErrorCode = MoPubErrorCode.NO_FILL;
                break;
            default:
                moPubErrorCode = MoPubErrorCode.UNSPECIFIED;
        }

        return moPubErrorCode;
    }

}
