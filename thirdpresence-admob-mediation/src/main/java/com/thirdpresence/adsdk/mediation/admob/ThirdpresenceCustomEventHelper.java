package com.thirdpresence.adsdk.mediation.admob;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.mediation.MediationAdRequest;
import com.thirdpresence.adsdk.sdk.VideoAd;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.google.android.gms.ads.AdRequest.GENDER_FEMALE;
import static com.google.android.gms.ads.AdRequest.GENDER_MALE;

/**
 *
 * Helper class for creating environment data and player parameters
 *
 */
public class ThirdpresenceCustomEventHelper {

    private static final String SERVER = VideoAd.SERVER_TYPE_PRODUCTION;
    private static final String SDK_NAME = "admob";

    private static final String PARAM_NAME_ACCOUNT = "account";
    private static final String PARAM_NAME_PLACEMENT_ID = "placementid";
    private static final String PARAM_NAME_DISABLE_BACK = "disablebackbutton";
    private static final String PARAM_NAME_SKIP_OFFSET = "skipoffset";
    private static final String PARAM_NAME_REWARD_TITLE = "rewardtitle";
    private static final String PARAM_NAME_REWARD_AMOUNT = "rewardamount";

    private static final String GOOGLE_PLAY_URL_BASE = "http://play.google.com/store/apps/details?id=";

    private ThirdpresenceCustomEventHelper() {}

    /**
     * Sets the environment data
     *
     * @param params map of Admob parameters
     *
     * @return Environment data map
     *
     */
    public static Map<String, String> setEnvironment(Map<String, String> params) {
        Map<String, String> env = new HashMap<>();
        env.put(VideoAd.Environment.KEY_EXT_SDK, SDK_NAME);
        env.put(VideoAd.Environment.KEY_EXT_SDK_VERSION, "unknown");
        env.put(VideoAd.Environment.KEY_SERVER, SERVER);

        if (params.containsKey(PARAM_NAME_ACCOUNT)) {
            env.put(VideoAd.Environment.KEY_ACCOUNT, params.get(PARAM_NAME_ACCOUNT));
        }
        if (params.containsKey(PARAM_NAME_PLACEMENT_ID)) {
            env.put(VideoAd.Environment.KEY_PLACEMENT_ID, params.get(PARAM_NAME_PLACEMENT_ID));
        }
        if (params.containsKey(PARAM_NAME_DISABLE_BACK)) {
            env.put(VideoAd.Environment.KEY_DISABLE_BACK_BUTTON, params.get(PARAM_NAME_DISABLE_BACK));
        }
        if (params.containsKey(PARAM_NAME_REWARD_TITLE)) {
            env.put(VideoAd.Environment.KEY_REWARD_TITLE, params.get(PARAM_NAME_REWARD_TITLE));
        }
        if (params.containsKey(PARAM_NAME_REWARD_AMOUNT)) {
            env.put(VideoAd.Environment.KEY_REWARD_AMOUNT, params.get(PARAM_NAME_REWARD_AMOUNT));
        }

        return env;
    }

    /**
     * Sets the player parameters
     *
     * @param activity the container activity
     * @param params map of Admob parameters
     *
     * @return VideoAd parameters map
     *
     */
    public static Map<String, String> setPlayerParameters(Activity activity, MediationAdRequest adRequest, Map<String, String> params) {
        Map<String, String> playerParams = new HashMap<>();

        String packageName = activity.getPackageName();
        playerParams.put(VideoAd.Parameters.KEY_BUNDLE_ID, packageName);

        ApplicationInfo info = activity.getApplicationInfo();
        if (info.labelRes > 0) {
            String appname = activity.getString(info.labelRes);
            playerParams.put(VideoAd.Parameters.KEY_PUBLISHER, appname);
            playerParams.put(VideoAd.Parameters.KEY_APP_NAME, appname);

            try {
                PackageInfo pinfo = activity.getPackageManager().getPackageInfo(packageName, 0);
                String versionName = pinfo.versionName;
                playerParams.put(VideoAd.Parameters.KEY_APP_VERSION, versionName);
            } catch (PackageManager.NameNotFoundException e) {}
        }

        if (params.containsKey(PARAM_NAME_SKIP_OFFSET)) {
            playerParams.put(VideoAd.Parameters.KEY_SKIP_OFFSET, params.get(PARAM_NAME_SKIP_OFFSET));
        }

        int gender = adRequest.getGender();
        if (gender == GENDER_MALE) {
            playerParams.put(VideoAd.Parameters.KEY_USER_GENDER, VideoAd.GENDER_MALE);
        } else if (gender == GENDER_FEMALE) {
            playerParams.put(VideoAd.Parameters.KEY_USER_GENDER, VideoAd.GENDER_FEMALE);
        }

        Date birthday = adRequest.getBirthday();
        if (birthday != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
            String yob = formatter.format(birthday);
            if (yob != null) {
                playerParams.put(VideoAd.Parameters.KEY_USER_YOB, yob);
            }
        }

        Set<String> keywordSet = adRequest.getKeywords();
        if (keywordSet != null && !keywordSet.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            Iterator<String> iterator = keywordSet.iterator();
            while (iterator.hasNext()) {
                sb.append(iterator.next());
                if (iterator.hasNext()) {
                    sb.append(',');
                }
            }
            playerParams.put(VideoAd.Parameters.KEY_KEYWORDS, sb.toString());
        }

        return playerParams;
    }

    /**
     * Maps Thirdpresence error codes to Admob error codes.
     *
     * @param errorCode Thirdpresence error code
     * @return Admob error code
     *
     */
    public static int mapErrorCode(VideoAd.ErrorCode errorCode) {
        int admobErrorCode;
        switch (errorCode) {
            case NETWORK_FAILURE:
            case NETWORK_TIMEOUT:
                admobErrorCode = AdRequest.ERROR_CODE_NETWORK_ERROR;
                break;
            case INVALID_STATE:
                admobErrorCode = AdRequest.ERROR_CODE_INTERNAL_ERROR;
                break;
            default:
            case NO_FILL:
                admobErrorCode = AdRequest.ERROR_CODE_NO_FILL;
                break;
        }

        return admobErrorCode;
    }

    /**
     * Parses parameters string from Admob
     *
     * @param paramString Admob publisher parameters string
     * @return map of parameters
     *
     */
    public static Map<String, String> parseParamsString(String paramString) {
        Map<String, String> map = new HashMap<>();
        if (paramString != null) {
            String[] params = paramString.split(",");
            for (String param : params) {
                String[] kvp = param.split(":");
                if (kvp.length == 2 && kvp[0] != null && kvp[1] != null) {
                    map.put(kvp[0], kvp[1]);
                }
            }
        }
        return map;
    }

    /**
     * Parses reward data from publisher params
     *
     * @param params Admob publisher parameters
     * @return reward data
     *
     */
    public static RewardData parseRewardData(Map<String, String> params) {
        String title = null;
        int amount = -1;

        if (params.containsKey(PARAM_NAME_REWARD_TITLE)) {
            title = params.get(PARAM_NAME_REWARD_TITLE);
        }

        if (params.containsKey(PARAM_NAME_REWARD_AMOUNT)) {
            try {
                String reward = params.get(PARAM_NAME_REWARD_AMOUNT);
                if (reward != null) {
                    amount = Integer.parseInt(reward);
                }
            } catch (NumberFormatException e) {
                amount = -1;
            }
        }

        return new RewardData(title, amount);
    }

}

