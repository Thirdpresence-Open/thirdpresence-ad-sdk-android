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
        env.put(VideoAd.Environment.KEY_ACCOUNT, serverExtras.get("account"));
        env.put(VideoAd.Environment.KEY_PLACEMENT_ID, serverExtras.get("playerid"));
        env.put(VideoAd.Environment.KEY_DISABLE_BACK_BUTTON, serverExtras.get("disablebackbutton"));
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
        params.put(VideoAd.Parameters.KEY_PUBLISHER, serverExtras.get("appname"));
        params.put(VideoAd.Parameters.KEY_APP_NAME, serverExtras.get("appname"));
        params.put(VideoAd.Parameters.KEY_APP_VERSION, serverExtras.get("appversion"));
        params.put(VideoAd.Parameters.KEY_APP_STORE_URL, serverExtras.get("appstoreurl"));
        params.put(VideoAd.Parameters.KEY_BUNDLE_ID, context.getPackageName());
        params.put(VideoAd.Parameters.KEY_SKIP_OFFSET, serverExtras.get("skipoffset"));
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
