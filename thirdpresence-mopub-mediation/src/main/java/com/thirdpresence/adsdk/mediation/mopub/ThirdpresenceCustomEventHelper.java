package com.thirdpresence.adsdk.mediation.mopub;

import android.content.Context;

import com.mopub.common.MoPub;
import com.mopub.common.MoPubReward;
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

    // Keys for server extras
    private static final String EXTRAS_KEY_ACCOUNT = "account";
    private static final String EXTRAS_KEY_PLACEMENT_ID = "placementid";
    private static final String EXTRAS_KEY_DISABLE_BACK = "disablebackbutton";
    private static final String EXTRAS_KEY_APP_NAME = "appname";
    private static final String EXTRAS_KEY_APP_VERSION = "appversion";
    private static final String EXTRAS_KEY_SKIP_OFFSET = "skipoffset";
    private static final String EXTRAS_KEY_REWARD_TITLE = "rewardtitle";
    private static final String EXTRAS_KEY_REWARD_AMOUNT = "rewardamount";

    // Keys for local extras
    public static final String LOCAL_EXTRAS_KEY_USER_GENDER = "tpr_gender";
    public static final String LOCAL_EXTRAS_KEY_USER_YOB = "tpr_yob";
    public static final String LOCAL_EXTRAS_KEY_USER_KEYWORDS = "tpr_keywords";

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
        env.put(VideoAd.Environment.KEY_REWARD_TITLE, serverExtras.get(EXTRAS_KEY_REWARD_TITLE));
        env.put(VideoAd.Environment.KEY_REWARD_AMOUNT, serverExtras.get(EXTRAS_KEY_REWARD_AMOUNT));
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
    public static Map<String, String> setPlayerParameters(Context context, Map<String, Object> localExtras, Map<String, String> serverExtras) {
        Map<String, String> params = new HashMap<>();

        params.put(VideoAd.Parameters.KEY_BUNDLE_ID, context.getPackageName());

        for (String key : serverExtras.keySet()) {
            switch (key) {
                case EXTRAS_KEY_APP_NAME:
                    params.put(VideoAd.Parameters.KEY_PUBLISHER, serverExtras.get(EXTRAS_KEY_APP_NAME));
                    params.put(VideoAd.Parameters.KEY_APP_NAME, serverExtras.get(EXTRAS_KEY_APP_NAME));
                    break;
                case EXTRAS_KEY_APP_VERSION:
                    params.put(VideoAd.Parameters.KEY_APP_VERSION, serverExtras.get(EXTRAS_KEY_APP_VERSION));
                    break;
                case EXTRAS_KEY_SKIP_OFFSET:
                    params.put(VideoAd.Parameters.KEY_SKIP_OFFSET, serverExtras.get(EXTRAS_KEY_SKIP_OFFSET));
                    break;
                default:
            }
        }

        for (String key : localExtras.keySet()) {
            switch (key) {
                case LOCAL_EXTRAS_KEY_USER_GENDER:
                    params.put(VideoAd.Parameters.KEY_USER_GENDER, (String)localExtras.get(LOCAL_EXTRAS_KEY_USER_GENDER));
                    break;
                case LOCAL_EXTRAS_KEY_USER_YOB:
                    params.put(VideoAd.Parameters.KEY_USER_YOB, (String)localExtras.get(LOCAL_EXTRAS_KEY_USER_YOB));
                    break;
                case LOCAL_EXTRAS_KEY_USER_KEYWORDS:
                    params.put(VideoAd.Parameters.KEY_KEYWORDS, (String)localExtras.get(LOCAL_EXTRAS_KEY_USER_KEYWORDS));
                    break;
                default:

            }
        }

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
            default:
                moPubErrorCode = MoPubErrorCode.NO_FILL;
                break;
        }

        return moPubErrorCode;
    }

    /**
     * Parses reward title from publisher params
     *
     * @param serverExtras MoPub publisher parameters
     * @return reward title or NO_REWARD_LABEL if not found
     *
     */
    public static String parseRewardTitle(Map<String, String> serverExtras) {

        String title = null;
        if (serverExtras.containsKey(EXTRAS_KEY_REWARD_TITLE)) {
            title = serverExtras.get(EXTRAS_KEY_REWARD_TITLE);
        }

        if (title == null){
            title = MoPubReward.NO_REWARD_LABEL;
        }
        return title;
    }

    /**
     * Parses reward amount from publisher params
     *
     * @param serverExtras MoPub publisher parameters
     * @return reward amount or NO_REWARD_AMOUNT if not found
     *
     */
    public static int parseRewardAmount(Map<String, String> serverExtras) {
        int amount = MoPubReward.NO_REWARD_AMOUNT;
        try {
            String reward = serverExtras.get(EXTRAS_KEY_REWARD_AMOUNT);
            if (reward != null) {
                amount = Integer.parseInt(reward);
            }
        } catch (NumberFormatException e) {
            amount = MoPubReward.NO_REWARD_AMOUNT;
        }
        return amount;
    }

}
