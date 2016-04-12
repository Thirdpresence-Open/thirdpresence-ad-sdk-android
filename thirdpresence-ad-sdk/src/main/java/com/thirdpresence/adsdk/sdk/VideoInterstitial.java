package com.thirdpresence.adsdk.sdk;

import android.app.Activity;
import com.thirdpresence.adsdk.sdk.internal.VideoPlayer;
import java.util.Map;

/**
 * <h1>VideoInterstitial</h1>
 *
 * VideoInterstitial is a class that allows a developer to create a video interstitial ad
 * units for an application. It uses Thirdpresence HTML5 ad video player that supports VAST and
 * VPAID video ads. Visit www.thirdpresence.com and wiki.thirdpresence.com for more information.
 *
 * @author  Marko Okkonen
 * @version 1.0
 * @since   2016-03-31
 *
 */
public class VideoInterstitial extends VideoAd{

    public final static long DEFAULT_TIMEOUT = 3000;

    private final VideoPlayer mVideoPlayer = new VideoPlayer();

    /**
     * Constructor
     */
    public VideoInterstitial() {
        super(PLACEMENT_TYPE_INTERSTITIAL);
    }

    /**
     * Constructor
     */
    public VideoInterstitial(String placementType) {
        super(placementType);
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
     * @param activity The container activity where the intertitial is displayed
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
        super.init();
        mVideoPlayer.init(activity, environment, params, timeout);
    }

    /**
     * Closes the ad unit and reset the player. Shall be called before loading a new ad.
     */
    public void reset() {
        mVideoPlayer.reset();
    }

    /**
     * Closes the ad unit and releases resources. In order to load new ad init() shall be called
     */
    public void remove() {
        mVideoPlayer.close();
    }

    /**
     * Loads an ad. Listener.onAdEvent() is called with AD_LOADED eventName when the ad is loaded
     */
    public void loadAd() {
        mVideoPlayer.loadAd();
    }

    /**
     * Display the ad view and starts playing the video
     */
    public void displayAd() {
        mVideoPlayer.displayAd();
    }

    /**
     * Checks if an ad is loaded
     *
     * @return true if loaded, false otherwise
     */
    public boolean isAdLoaded() {
        return mVideoPlayer.isAdLoaded();
    }


}
