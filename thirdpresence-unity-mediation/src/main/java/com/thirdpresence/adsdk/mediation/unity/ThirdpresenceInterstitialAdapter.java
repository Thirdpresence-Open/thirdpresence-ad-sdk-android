package com.thirdpresence.adsdk.mediation.unity;

import android.app.Activity;
import android.content.Intent;

import com.thirdpresence.adsdk.sdk.VideoAd;
import com.thirdpresence.adsdk.sdk.VideoInterstitial;

import java.util.Map;

/**
 *
 * ThirdpresenceInterstitialAdapter is an adapter that provides interstitial ad for
 * Unity plugin that allows to use Thirdpresence Ads in Unity apps.
 *
 */
public class ThirdpresenceInterstitialAdapter extends ThirdpresenceAdapterBase implements VideoAd.Listener {

    private static ThirdpresenceInterstitialAdapter mInstance = null;
    private VideoInterstitial mVideoInterstitial;
    private static boolean mAdLoaded = false;

    private Activity mUnityActivity;

    /**
     * A listener is implemented in Unity via {@code AndroidJavaProxy} to receive ad events.
     */
    public interface InterstitialListener {
        void onInterstitialLoaded();
        void onInterstitialShown();
        void onInterstitialDismissed();
        void onInterstitialFailed(int errorCode, String errorText);
        void onInterstitialClicked();
    }
    private InterstitialListener mInterstitialListener;

    /**
     * Private constructor. Use getInstance() instead.
     */
    private ThirdpresenceInterstitialAdapter() {}

    /**
     * Gets singleton instance of the ThirdpresenceInterstitialAdapter
     */
    public static synchronized ThirdpresenceAdapterBase getInstance() {
        if (mInstance == null) {
            mInstance = new ThirdpresenceInterstitialAdapter();
        }
        return mInstance;
    }

    /**
     * Set listener for interstitial events.
     */
    public void setListener(InterstitialListener listener) {
        mInterstitialListener = listener;
    }

    /**
     * Inits the interstitial ad unit
     *
     * @param activity the ad is loaded from
     * @param environment parameters
     * @param playerParams parameters
     * @param timeout is default timeout for initialising player and retrieving an ad
     *
     */
    public void initInterstitial(Activity activity,
                                 Map<String, String> environment,
                                 Map<String, String> playerParams,
                                 long timeout) {
        removeInterstitial();

        if (activity == null) {
            mInterstitialListener.onInterstitialFailed(VideoAd.ErrorCode.INVALID_STATE.getErrorCode(), "Activity is null");
            return;
        }
        mUnityActivity = activity;

        Map<String, String> env = setEnvironment(environment);
        Map<String, String> params = setPlayerParameters(playerParams);

        mVideoInterstitial = new VideoInterstitial();
        mVideoInterstitial.setListener(this);
        mVideoInterstitial.init(activity, env, params, timeout > 0 ? timeout : VideoInterstitial.DEFAULT_TIMEOUT);
        mVideoInterstitial.loadAd();
    }

    /**
     * Launches an additional activity to display the ad.
     */
    public void showInterstitial() {
        if (mAdLoaded && mVideoInterstitial != null) {
            Intent i = new Intent(mUnityActivity, ThirdpresencePlayerActivity.class);
            i.putExtra(ThirdpresencePlayerActivity.ADAPTER_CLASS_EXTRAS_KEY, this.getClass().getName());
            mUnityActivity.startActivity(i);
        } else if (mInterstitialListener != null)  {
            mInterstitialListener.onInterstitialFailed(VideoAd.ErrorCode.INVALID_STATE.getErrorCode(), "An ad is not loaded");
        }
    }

    /**
     * Remove the interstitial ad.
     */
    public void removeInterstitial() {
        mAdLoaded = false;
        if (mVideoInterstitial != null) {
            mVideoInterstitial.remove();
            mVideoInterstitial.setListener(null);
            mVideoInterstitial = null;
        }
        finishPlayerActivity();
    }

    /**
     * Finish the player activity
     */
    public void finishPlayerActivity() {
        removeAd();
        super.finishPlayerActivity();
    }

    /**
     * Displays the interstitial ad. Called from ThirdpresencePlayerActivity
     */
    public void displayAd() {
        super.displayAd();
        if (mAdLoaded && mVideoInterstitial != null) {
            mVideoInterstitial.displayAd();
        }
    }

    /**
     * Removes the ad
     */
    public void removeAd() {
        super.removeAd();
        mAdLoaded = false;
        if (mVideoInterstitial != null) {
            mVideoInterstitial.remove();
        }
    }

    /**
     * Sets the player activity.
     */
    public void setPlayerActivity(Activity activity) {
        super.setPlayerActivity(activity);
        mVideoInterstitial.switchActivity(activity);
    }

    /**
     * {@inheritDoc}
     */
    public void onPlayerReady() {}

    /**
     * {@inheritDoc}
     */
    public void onError(VideoAd.ErrorCode errorCode, String message) {
        finishPlayerActivity();
        if (mInterstitialListener != null) {
            mInterstitialListener.onInterstitialFailed(VideoAd.ErrorCode.PLAYER_INIT_FAILED.getErrorCode(), message);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onAdEvent(String eventName, String arg1, String arg2, String arg3) {
        if (mInterstitialListener != null ) {
            if (eventName.equals(VideoAd.Events.AD_LOADED)) {
                mAdLoaded = true;
                mInterstitialListener.onInterstitialLoaded();
            } else if (eventName.equals(VideoAd.Events.AD_VIDEO_COMPLETE)) {
                mInterstitialListener.onInterstitialShown();
            } else if (eventName.equals(VideoAd.Events.AD_STOPPED)) {
                finishPlayerActivity();
                mInterstitialListener.onInterstitialDismissed();
            } else if (eventName.equals(VideoAd.Events.AD_ERROR)) {
                mInterstitialListener.onInterstitialFailed(VideoAd.ErrorCode.NO_FILL.getErrorCode(), arg1);
            } else if (eventName.equals(VideoAd.Events.AD_CLICKTHRU)) {
                mInterstitialListener.onInterstitialClicked();
            }
        }
    }
}
