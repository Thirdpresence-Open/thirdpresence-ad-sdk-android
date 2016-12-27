package com.thirdpresence.adsdk.sdk;

import android.app.Activity;

import com.thirdpresence.adsdk.sdk.internal.TLog;
import com.thirdpresence.adsdk.sdk.internal.VideoPlayer;

import java.util.Map;

/**
 * <h1>VideoBanner</h1>
 *
 * VideoBanner is a class that allows a developer to create a video banner ad
 * units for an application. It uses Thirdpresence HTML5 ad video player that supports VAST and
 * VPAID video ads. Visit www.thirdpresence.com and wiki.thirdpresence.com for more information.
 *
 */
public class VideoBanner extends VideoAd {

    private final VideoPlayer mVideoPlayer = new VideoPlayer();

    /**
     * Constructor
     *
     * Not intended to use
     *
     */
    private VideoBanner() {
        super(VideoAd.PLACEMENT_TYPE_BANNER, VideoAd.DEFAULT_PLACEMENT_ID);
    }

    /**
     * Constructor
     *
     * @param placementId the placement id
     */
    public VideoBanner(String placementId) {
        super(VideoAd.PLACEMENT_TYPE_BANNER, placementId);
    }

    /**
     * Inits the ad unit
     *
     * @param activity The container activity where the interstitial is displayed
     * @param bannerView container view for the banner ad
     * @param environment Environment parameters.
     *                    @see VideoAd.Environment for details
     *                    Mandatory parameters: KEY_ACCOUNT and KEY_PLACEMENT_ID
     * @param params VideoAd parameters
     *               @see VideoAd.Parameters for details
     * @param timeout Timeout for setting up the player in milliseconds
     *
     */
    @Override
    public void init(Activity activity, BannerView bannerView, Map<String, String> environment, Map<String, String> params, long timeout) {
        TLog.d("Initialising " + this.getPlacementType());

        boolean displayImmediately = true;
        if (environment != null && environment.containsKey(Environment.KEY_BANNER_AUTO_DISPLAY)) {
            displayImmediately = parseBoolean(environment.get(Environment.KEY_BANNER_AUTO_DISPLAY), true);
        }

        mVideoPlayer.setDisplayImmediately(displayImmediately);
        mVideoPlayer.init(activity, bannerView, environment, params, timeout, getPlacementId(), getPlacementType());
    }

    /**
     * Sets listener for callback events
     *
     * @param listener An object implementing the interface
     *
     */
    @Override
    public void setListener(Listener listener) {
        mVideoPlayer.setListener(listener);
    }

    /**
     * Closes the ad unit and reset the player. Shall be called before loading a new ad.
     */
    @Override
    public void reset() {
        TLog.d("Reseting video player");
        mVideoPlayer.reset();
    }

    /**
     * Releases all resources. In order to load new ad init() shall be called
     */
    @Override
    public void remove() {
        TLog.d("Closing ad unit");
        mVideoPlayer.close();
    }

    /**
     * Loads an ad. Listener.onAdEvent() is called with AD_LOADED eventName when the ad is loaded
     */
    @Override
    public void loadAd() {
        TLog.d("Loading an ad");
        mVideoPlayer.loadAd();
    }

    /**
     * Resets and loads new ad. Listener.onAdEvent() is called with AD_LOADED eventName when the ad is loaded
     */
    @Override
    public void resetAndLoadAd() {
        reset();
        loadAd();
    }

    /**
     * Displays the ad
     */
    public void displayAd() {
        TLog.d("Trying to display an ad");
        mVideoPlayer.displayAdInCurrentActivity();
    }

    /**
     * Not implemented for banner.
     */
    public void displayAd(Activity activity, Runnable runnable) {
    }

    /**
     * Not implemented for banner.
     */
    public void displayAd(Runnable runnable) {
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
    @Override
    public boolean isAdLoaded() {
        return mVideoPlayer.isAdLoaded();
    }

    /**
     * Checks if the player is ready
     *
     * @return true if ready, false otherwise
     */
    @Override
    public boolean isPlayerReady() {
        return mVideoPlayer.isPlayerReady();
    }


    /**
     * Checks if the video has been completed
     *
     * @return true if completed, false otherwise
     */
    @Override
    public boolean isVideoCompleted() {
        return mVideoPlayer.isVideoCompleted();
    }
}
