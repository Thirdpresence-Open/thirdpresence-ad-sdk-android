package com.thirdpresence.adsdk.sdk;

import android.app.Activity;
import java.util.Map;

/**
 * <h1>VideoAd</h1>
 *
 * VideoAd is a base class for all video ad classes. Do not use directly.
 */
public abstract class VideoAd {

    /**
     * Callback interface for player events.
     */
    public interface Listener {
        /**
         * This callback is called on any ad event that is originated from the ad player
         *
         * @param eventName event name @see Events for possible events
         * @param arg1 an event specific argument
         * @param arg2 an event specific argument
         * @param arg3 an event specific argument
         *
         */
        void onAdEvent(String eventName, String arg1, String arg2, String arg3);

        /**
         * This callback is called when the player is ready to load ads
         */
        void onPlayerReady();

        /**
         * This callback is called when an error occurs
         *
         * @param errorCode @see ErrorCode
         * @param message Human-readable error message
         *
         */
        void onError(ErrorCode errorCode, String message);

    }

    /**
     * VideoAd events. These event are compliant with IAB VPAID 2.0 specification.
     */
    public class Events {
        /**
         * Ad loaded
         */
        public final static String AD_LOADED = "AdLoaded";
        /**
         * Ad started
         */
        public final static String AD_STARTED = "AdStarted";
        /**
         * Ad stopped
         */
        public final static String AD_STOPPED = "AdStopped";
        /**
         * Ad skipped
         */
        public final static String AD_SKIPPED = "AdSkipped";
        /**
         * Ad paused
         */
        public final static String AD_PAUSED = "AdPaused";
        /**
         * Ad playing
         */
        public final static String AD_PLAYING = "AdPlaying";
        /**
         * Ad error. arg1 contains error text
         */
        public final static String AD_ERROR = "AdError";
        /**
         * Ad clicked, arg1 contains url to be opened
         */
        public final static String AD_CLICKTHRU = "AdClickThru";
        /**
         * Ad impression
         */
        public final static String AD_IMPRESSION = "AdImpression";
        /**
         * Video started
         */
        public final static String AD_VIDEO_START = "AdVideoStart";
        /**
         * Video first quartile
         */
        public final static String AD_VIDEO_FIRST_QUARTILE = "AdVideoFirstQuartile";
        /**
         * Video midpoint
         */
        public final static String AD_VIDEO_MIDPOINT = "AdVideoMidpoint";
        /**
         * Video third queartile
         */
        public final static String AD_VIDEO_THIRD_QUARTILE = "AdVideoThirdQuartile";
        /**
         * Video complete
         */
        public final static String AD_VIDEO_COMPLETE = "AdVideoComplete";
        /**
         * Fallback ad displayed
         */
        public final static String AD_FALLBACK_DISPLAYED = "AdFallbackDisplayed";
        /**
         * Ad left the application, for example, due opening a landing page
         */
        public final static String AD_LEFT_APPLICATION = "AdLeftApplication";

    }
    /**
     * Environment class contains keys for setting up the placement and the player
     *
     */
    public class Environment {
        /**
         * External mediator SDK
         */
        public final static String KEY_EXT_SDK = "sdk";
        /**
         * External mediator SDK version
         */
        public final static String KEY_EXT_SDK_VERSION = "sdk_version";
        /**
         * Thirdpresence server environment
         */
        public final static String KEY_SERVER = "server";
        /**
         * Thirdpresence account name
         */
        public final static String KEY_ACCOUNT = "account";
        /**
         * Thirdpresence ad placement id
         */
        public final static String KEY_PLACEMENT_ID = "playerid";
        /**
         * Disable BACK button while ad is playing
         *
         * @deprecated
         */
        public final static String KEY_DISABLE_BACK_BUTTON = "disablebackbutton";
        /**
         * Force ad placement to landscape orientation
         */
        public final static String KEY_FORCE_LANDSCAPE = "forcelandscape";
        /**
         * Force ad placement to portrait orientation
         */
        public final static String KEY_FORCE_PORTRAIT = "forceportrait";
        /**
         * Force secure HTTP requests
         */
        public final static String KEY_FORCE_SECURE_HTTP = "forcehttps";
    }

    /**
     * Production server
     */
    public final static String SERVER_TYPE_PRODUCTION = "production";

    /**
     * Staging server
     */
    public final static String SERVER_TYPE_STAGING = "staging";

    /**
     * Parameters contains keys for available parameters that can be passed to player
     *
     */
    public class Parameters {
        /**
         * Application name
         */
        public final static String KEY_APP_NAME = "appname";
        /**
         * Application version
         */
        public final static String KEY_APP_VERSION = "appversion";
        /**
         * Application market place URL
         */
        public final static String KEY_APP_STORE_URL = "appstoreurl";
        /**
         * Title of the reward
         */
        public final static String KEY_REWARD_TITLE = "rewardtitle";
        /**
         * Amount of the reward
         */
        public final static String KEY_REWARD_AMOUNT = "rewardamount";
        /**
         * Skip offset in seconds
         */
        public final static String KEY_SKIP_OFFSET = "closedelaymax";
        /**
         * Ad placement type, @see AdPlacementType
         */
        public final static String KEY_AD_PLACEMENT = "adplacement";
        /**
         * Publisher name.
         */
        public final static String KEY_PUBLISHER = "publisher";
        /**
         * Bundle ID (automatically determined)
         */
        public final static String KEY_BUNDLE_ID = "bundleid";
        /**
         * Advertising ID (automatically determined)
         */
        public final static String KEY_DEVICE_ID = "deviceid";
        /**
         * Publisher name.
         */
        public final static String KEY_VAST_URL = "vast_url";
        /**
         * Location: latitude
         */
        public final static String KEY_GEO_LAT = "geo_lat";
        /**
         * Location: longitude
         */
        public final static String KEY_GEO_LON = "geo_lon";
        /**
         * Location: country
         */
        public final static String KEY_GEO_COUNTRY = "country";
        /**
         * Location: region
         */
        public final static String KEY_GEO_REGION = "region";
        /**
         * Location: city
         */
        public final static String KEY_GEO_CITY = "city";
        /**
         * User Gender
         */
        public final static String KEY_USER_GENDER = "gender";
        /**
         * User year of birth
         */
        public final static String KEY_USER_YOB = "yob";
    }

    /**
     * Ad Placement type interstitial ad
     */
    public final static String PLACEMENT_TYPE_INTERSTITIAL = "interstitial";

    /**
     * Ad Placement type rewarded type
     */
    public final static String PLACEMENT_TYPE_REWARDED_VIDEO = "rewardedvideo";

    /**
     * Thirdpresence error codes
     */
    public enum ErrorCode {
        /**
         * Network failure
         */
        NETWORK_FAILURE(1),
        /**
         * Network timeout
         */
        NETWORK_TIMEOUT(2),
        /**
         * VideoAd init failed
         */
        PLAYER_INIT_FAILED(3),
        /**
         * No fill for the ad placement
         */
        NO_FILL(4),
        /**
         * Ad not yet ready to be displayed
         */
        AD_NOT_READY(5),
        /**
         * Method called on invalid state
         */
        INVALID_STATE(6),
        /**
         * Unknown error
         */
        UNKNOWN(9999);

        int errorCode = 0;

        ErrorCode(int errorCode) {
            this.errorCode = errorCode;
        }

        public int getErrorCode() {
            return errorCode;
        }
    }

    public final static long DEFAULT_TIMEOUT = 20000;
    public final static String DEFAULT_PLACEMENT_ID = "default_placement_id";

    private final String mPlacementType;
    private final String mPlacementId;

    private VideoAd() {
        mPlacementType = "";
        mPlacementId = "";
    }

    protected VideoAd(String placementType, String placementId) {
        mPlacementType = placementType;
        mPlacementId = placementId;
    }

    /**
     * Gets the placement type
     */
    public String getPlacementType() {
        return mPlacementType;
    }

    /**
     * Gets the placement id
     */
    public String getPlacementId() {
        return mPlacementId;
    }

    /**
     * Sets listener for callback events
     *
     * @param listener An object implementing the interface
     *
     */
    public abstract void setListener(Listener listener);

    /**
     * Inits the ad unit
     * @param activity The container activity where the interstitial is displayed
     * @param environment Environment parameters.
     *                    @see VideoAd.Environment for details
     *                    Mandatory parameters: KEY_ACCOUNT and KEY_PLACEMENT_ID
     * @param params VideoAd parameters
     *               @see VideoAd.Parameters for details
     * @param timeout Timeout for setting up the player in milliseconds
     */
    public abstract void init(Activity activity,
                     Map<String, String> environment,
                     Map<String, String> params,
                     long timeout);

    /**
     * Closes the ad unit resets the ad unit
     */
    public abstract void reset();

    /**
     * Closes the ad unit and releases resources.
     */
    public abstract void remove();

    /**
     * Loads an ad. Listener.onAdEvent() is called with AD_LOADED eventName when the ad is loaded
     */
    public abstract void loadAd();

    /**
     * Resets and laods new ad. Listener.onAdEvent() is called with AD_LOADED eventName when the ad is loaded
     */
    public abstract void resetAndLoadAd();
    /**
     * Displays the ad in the current activity
     *
     * @deprecated
     */
    public abstract void displayAd();

    /**
     * Displays the ad in the built-in activity
     *
     * @param runnable to be executed when completed
     */
    public abstract void displayAd(Runnable runnable);

    /**
     * Display the ad in the given activity
     *
     * @param activity that displays the ad or null if using built-in activity
     * @param runnable to be executed when completed or null
     */
    public abstract void displayAd(Activity activity, Runnable runnable);
    /**
     * Checks if an ad is loaded
     *
     * @return true if loaded, false otherwise
     */
    public abstract boolean isAdLoaded();

    /**
     * Checks if the player is ready
     *
     * @return true if ready, false otherwise
     */
    public abstract boolean isPlayerReady();

    /**
     * Helper function for parsing boolean from a string
     *
     * @param booleanString string that contains boolean
     * @param defaultVal defaultVal if boolean cannot be parse
     * @return boolean parsed from the string or defaultVal
     *
     */
    public static boolean parseBoolean(String booleanString, boolean defaultVal) {
        boolean ret = defaultVal;
        try {
            if (booleanString != null) {
                ret = Boolean.parseBoolean(booleanString);
            }
        } catch (NumberFormatException e) {
            ret = defaultVal;
        }
        return ret;
    }
}
