package com.thirdpresence.adsdk.mediation.unity;

import android.app.Activity;

import com.thirdpresence.adsdk.sdk.VideoAdManager;
import com.thirdpresence.adsdk.sdk.VideoAd;

import java.util.Map;

/**
 *
 * ThirdpresenceInterstitialAdapter is an adapter that provides interstitial ad for
 * Unity plugin that allows to use Thirdpresence Ads in Unity apps.
 *
 */
public class ThirdpresenceInterstitialAdapter extends ThirdpresenceAdapterBase implements VideoAd.Listener {

    private static ThirdpresenceInterstitialAdapter mInstance = null;
    private String mPlacementId;
    private boolean mDisplaying = false;

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
            if (mInterstitialListener != null) {
                mInterstitialListener.onInterstitialFailed(VideoAd.ErrorCode.INVALID_STATE.getErrorCode(), "Activity is null");
            }
            return;
        }

        Map<String, String> env = setEnvironment(environment);
        Map<String, String> params = setPlayerParameters(playerParams);

        if (!environment.containsKey(EXTRAS_KEY_ACCOUNT)) {
            if (mInterstitialListener != null) {
                mInterstitialListener.onInterstitialFailed(VideoAd.ErrorCode.PLAYER_INIT_FAILED.getErrorCode(), "Account is not set");
            }
            return;
        }

        if (!environment.containsKey(EXTRAS_KEY_PLACEMENT_ID)) {
            if (mInterstitialListener != null) {
                mInterstitialListener.onInterstitialFailed(VideoAd.ErrorCode.PLAYER_INIT_FAILED.getErrorCode(), "Placement id is not set");
            }
            return;
        }

        mPlacementId = env.get(VideoAd.Environment.KEY_PLACEMENT_ID);

        VideoAd ad = VideoAdManager.getInstance().create(VideoAd.PLACEMENT_TYPE_INTERSTITIAL, mPlacementId);
        ad.init(activity, env, params, VideoAd.DEFAULT_TIMEOUT);
        ad.setListener(this);
        ad.loadAd();

    }

    /**
     * Launches an additional activity to display the ad.
     */
    public void showInterstitial() {
        VideoAd ad = VideoAdManager.getInstance().get(mPlacementId);
        if (ad != null && ad.isAdLoaded()) {
            mDisplaying = true;
            ad.displayAd(null, null);
        } else if (mInterstitialListener != null)  {
            mInterstitialListener.onInterstitialFailed(VideoAd.ErrorCode.INVALID_STATE.getErrorCode(), "An ad is not loaded");
        }
    }

    /**
     * Remove the interstitial ad.
     */
    public void removeInterstitial() {
        mDisplaying = false;
        VideoAdManager.getInstance().remove(mPlacementId);
    }

    /**
     * {@inheritDoc}
     */
    public void onPlayerReady() {}

    /**
     * {@inheritDoc}
     */
    public void onError(VideoAd.ErrorCode errorCode, String message) {
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
                mInterstitialListener.onInterstitialLoaded();
            } else if (eventName.equals(VideoAd.Events.AD_VIDEO_COMPLETE)) {
                mInterstitialListener.onInterstitialShown();
            } else if (eventName.equals(VideoAd.Events.AD_STOPPED)) {
                if (mDisplaying) {
                    mDisplaying = false;
                    mInterstitialListener.onInterstitialDismissed();
                }
            } else if (eventName.equals(VideoAd.Events.AD_ERROR)) {
                VideoAd ad = VideoAdManager.getInstance().get(mPlacementId);
                if (ad != null && ad.isAdLoaded()) {
                    mInterstitialListener.onInterstitialFailed(VideoAd.ErrorCode.PLAYBACK_FAILED.getErrorCode(), arg1);
                } else {
                    mInterstitialListener.onInterstitialFailed(VideoAd.ErrorCode.NO_FILL.getErrorCode(), arg1);
                }
            } else if (eventName.equals(VideoAd.Events.AD_CLICKTHRU)) {
                mInterstitialListener.onInterstitialClicked();
            }
        }
    }
}
