package com.thirdpresence.adsdk.demo;

import android.app.Activity;
import com.thirdpresence.adsdk.sdk.VideoAd;
import com.thirdpresence.adsdk.sdk.VideoAdManager;

import java.util.HashMap;

/**
 * Helper class for initialising an ad placement
 */
public class Ads {
    public static final String ACCOUNT = "sdk-demo";
    public static final String PLACEMENT_ID_1 = "sa7nvltbrn";
    public static final String PLACEMENT_ID_2 = "nhilpfoz6b";

    public static final String APP_NAME = "Photo Demo";
    public static final String APP_VERSION = "1.0";
    public static final String STORE_URL = "https://play.google.com/store/apps/details?id=com.thirdpresence.adsdk.demo";

    public static void initInterstitial(Activity activity, String placementID) {
        HashMap<String, String> environment = new HashMap<>();

        environment.put(VideoAd.Environment.KEY_ACCOUNT, ACCOUNT);
        environment.put(VideoAd.Environment.KEY_PLACEMENT_ID, placementID);
        environment.put(VideoAd.Environment.KEY_SERVER, VideoAd.SERVER_TYPE_PRODUCTION);
        environment.put(VideoAd.Environment.KEY_DISABLE_BACK_BUTTON, Boolean.TRUE.toString());

        HashMap<String, String> params = new HashMap<>();
        params.put(VideoAd.Parameters.KEY_PUBLISHER, APP_NAME);
        params.put(VideoAd.Parameters.KEY_APP_NAME, APP_NAME);
        params.put(VideoAd.Parameters.KEY_APP_VERSION, APP_VERSION);
        params.put(VideoAd.Parameters.KEY_APP_STORE_URL, STORE_URL);

        // In order to get more targeted ads you shall provide user's gender and year of birth
        // You can use e.g. Google+ API or Facebook Graph API
        // https://developers.google.com/android/reference/com/google/android/gms/plus/model/people/package-summary
        // https://developers.facebook.com/docs/android/graph/
        // params.put(VideoAd.Parameters.KEY_USER_GENDER, userGender);
        // params.put(VideoAd.Parameters.KEY_USER_YOB, "userYearOfBirth);

        VideoAd ad = VideoAdManager.getInstance().create(VideoAd.PLACEMENT_TYPE_INTERSTITIAL, placementID);
        ad.init(activity, environment, params, VideoAd.DEFAULT_TIMEOUT);
        ad.loadAd();
    }
}
