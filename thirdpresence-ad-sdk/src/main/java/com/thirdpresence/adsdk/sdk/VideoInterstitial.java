package com.thirdpresence.adsdk.sdk;

import android.app.Activity;

import com.thirdpresence.adsdk.sdk.internal.TLog;
import com.thirdpresence.adsdk.sdk.internal.VideoPlayer;
import java.util.Map;

/**
 * <h1>VideoInterstitial</h1>
 *
 * VideoInterstitial is a class that allows a developer to create a video interstitial ad
 * units for an application. It uses Thirdpresence HTML5 ad video player that supports VAST and
 * VPAID video ads. Visit www.thirdpresence.com and wiki.thirdpresence.com for more information.
 *
 */
public class VideoInterstitial extends VideoAd {

    private final VideoPlayer mVideoPlayer = new VideoPlayer();

    /**
     * Constructor
     *
     * Use the constructor with the placementId as argument
     *
     * @deprecated
     */
    public VideoInterstitial() {
        super(PLACEMENT_TYPE_INTERSTITIAL, VideoAd.DEFAULT_PLACEMENT_ID);
    }

    /**
     * Constructor
     *
     * @param placementId the placement id
     *
     */
    public VideoInterstitial(String placementId) {
        super(PLACEMENT_TYPE_INTERSTITIAL, placementId);
    }

    /**
     * Constructor
     *
     *
     * @param placementType @see VideoAd for possible values
     * @param placementId the placement id
     *
     */
    public VideoInterstitial(String placementType, String placementId) {
        super(placementType, placementId);
    }

    /**
     * Sets listener for callback events
     *
     * @param listener An object implementing the interface
     *
     */
    public void setListener(VideoAd.Listener listener) {
        mVideoPlayer.setListener(listener);
    }

    /**
     * Inits the ad unit
     *
     * @param activity The container activity where the interstitial is displayed
     * @param environment Environment parameters.
     *                    @see VideoAd.Environment for details
     *                    Mandatory parameters: KEY_ACCOUNT and KEY_PLACEMENT_ID
     * @param params VideoAd parameters
     *               @see VideoAd.Parameters for details
     * @param timeout Timeout for setting up the player in milliseconds
     *
     */
    public void init(Activity activity,
                     Map<String, String> environment,
                     Map<String, String> params,
                     long timeout){
        TLog.d("Initialising " + this.getPlacementType());
        mVideoPlayer.init(activity, null, environment, params, timeout, getPlacementId(), getPlacementType());
    }

    /**
     * Closes the ad unit and reset the player. Shall be called before loading a new ad.
     */
    public void reset() {
        TLog.d("Reseting video player");
        mVideoPlayer.reset();
    }

    /**
     * Closes the ad unit and releases resources. In order to load new ad init() shall be called
     */
    public void remove() {
        TLog.d("Closing ad unit");
        mVideoPlayer.close();
    }

    /**
     * Loads an ad. Listener.onAdEvent() is called with AD_LOADED eventName when the ad is loaded
     */
    public void loadAd() {
        TLog.d("Loading an ad");
        mVideoPlayer.loadAd();
    }

    /**
     * Resets and loads new ad. Listener.onAdEvent() is called with AD_LOADED eventName when the ad is loaded
     */
    public void resetAndLoadAd() {
        reset();
        loadAd();
    }

    /**
     * Displays the ad in the current activity
     * Notice that the activity given in init() must be active when displaying the ad
     *
     * @deprecated
     */
    public void displayAd() {
        TLog.d("Trying to display an ad");
        mVideoPlayer.displayAdInCurrentActivity();
    }

    /**
     * Displays the ad in the given activity
     *
     * @param activity that displays the ad or null if using built-in activity
     * @param runnable to be executed when completed or null
     */
    public void displayAd(Activity activity, Runnable runnable) {
        TLog.d("Trying to display an ad with new activity");
        mVideoPlayer.displayAd(activity, runnable);
    }

    /**
     * Displays the ad
     *
     * @param runnable to be executed when completed or null
     */
    public void displayAd(Runnable runnable) {
        TLog.d("Trying to display an ad");
        mVideoPlayer.displayAd(null, runnable);
    }

    /**
     * Pauses the playing ad
     */
    public void pauseAd() {
        mVideoPlayer.pauseAd();
    }

    /**
     * Resumes the paused ad
     */
    public void resumeAd() {
        mVideoPlayer.resumeAd();
    }


    /**
     * Checks if an ad is loaded
     *
     * @return true if loaded, false otherwise
     */
    public boolean isAdLoaded() {
        return mVideoPlayer.isAdLoaded();
    }

    /**
     * Checks if the player is ready
     *
     * @return true if ready, false otherwise
     */
    public boolean isPlayerReady() {
        return mVideoPlayer.isPlayerReady();
    }

    /**
     * Checks if the video has been completed
     *
     * @return true if completed, false otherwise
     */
    public boolean isVideoCompleted() {
        return mVideoPlayer.isVideoCompleted();
    }

    /**
     * Updates the activity the ad view is located in
     *
     * @param newActivity activity to switch to
     *
     * @deprecated                 
     */
    public void switchActivity(Activity newActivity) {
        TLog.d("Switching an activity");
        mVideoPlayer.switchActivity(newActivity);}
}
