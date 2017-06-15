package com.thirdpresence.adsdk.mediation.unity;

import android.app.Activity;
import com.thirdpresence.adsdk.sdk.VideoAdManager;
import com.thirdpresence.adsdk.sdk.VideoAd;

import java.util.Map;

/**
 *
 * ThirdpresenceRewardedVideoAdapter is an adapter that is used by Thirdpresence Unity plugin
 * for Unity apps.
 *
 * Thirdpresence Unity plugin does not support Rewarded ads yet.
 *
 */
public class ThirdpresenceRewardedVideoAdapter extends ThirdpresenceAdapterBase implements VideoAd.Listener {

    private static ThirdpresenceRewardedVideoAdapter mInstance = null;
    private String mPlacementId;
    private String mRewardTitle = null;
    private int mRewardAmount = -1;
    private boolean mDisplaying = false;

    /**
     * A listener implemented in Unity via {@code AndroidJavaProxy} to receive ad events.
     */
    public interface RewardedVideoListener {
        void onRewardedVideoLoaded();
        void onRewardedVideoShown();
        void onRewardedVideoDismissed();
        void onRewardedVideoFailed(int errorCode, String errorText);
        void onRewardedVideoClicked();
        void onRewardedVideoCompleted(String rewardTitle, int rewardAmount);
        void onRewardedVideoAdLeftApplication();
    }
    private RewardedVideoListener mRewardedVideoListener;

    /**
     * Private constructor. Use getInstance() instead.
     */
    private ThirdpresenceRewardedVideoAdapter() {}

    /**
     * Gets singleton instance of the ThirdpresenceInterstitialAdapter
     */
    public static synchronized ThirdpresenceAdapterBase getInstance() {
        if (mInstance == null) {
            mInstance = new ThirdpresenceRewardedVideoAdapter();
        }
        return mInstance;
    }

    /**
     * Set listener for rewarded video events.
     */
    public void setListener(RewardedVideoListener listener) {
        mRewardedVideoListener = listener;
    }

    /**
     * Inits the rewarded video ad unit
     *
     * @param activity the ad is loaded from
     * @param environment parameters
     * @param playerParams parameters
     * @param timeout is default timeout for initialising player and retrieving an ad
     *
     */
    public void initRewardedVideo(Activity activity,
                                  Map<String, String> environment,
                                  Map<String, String> playerParams,
                                  long timeout) {
        removeRewardedVideo();

        if (activity == null) {
            if (mRewardedVideoListener != null) {
                mRewardedVideoListener.onRewardedVideoFailed(VideoAd.ErrorCode.INVALID_STATE.getErrorCode(), "Activity is null");
            }
            return;
        }

        if (environment.containsKey(EXTRAS_KEY_REWARD_TITLE)) {
            mRewardTitle = environment.get(EXTRAS_KEY_REWARD_TITLE);
        } else {
            mRewardTitle = null;
        }

        try {
            String reward = environment.get(EXTRAS_KEY_REWARD_AMOUNT);
            if (reward != null) {
                mRewardAmount = Integer.parseInt(reward);
            }
        } catch (NumberFormatException e) {
            mRewardAmount = -1;
        }

        if (mRewardTitle == null || mRewardAmount < 0) {
            if (mRewardedVideoListener != null) {
                mRewardedVideoListener.onRewardedVideoFailed(VideoAd.ErrorCode.INVALID_STATE.getErrorCode(), "Rewarded video title or amount not properly set");
            }
            return;
        }


        if (!environment.containsKey(EXTRAS_KEY_ACCOUNT)) {
            if (mRewardedVideoListener != null) {
                mRewardedVideoListener.onRewardedVideoFailed(VideoAd.ErrorCode.PLAYER_INIT_FAILED.getErrorCode(), "Account is not set");
            }
            return;
        }

        if (!environment.containsKey(EXTRAS_KEY_PLACEMENT_ID)) {
            if (mRewardedVideoListener != null) {
                mRewardedVideoListener.onRewardedVideoFailed(VideoAd.ErrorCode.PLAYER_INIT_FAILED.getErrorCode(), "Placement id is not set");
            }
        }

        Map<String, String> env = setEnvironment(environment);
        Map<String, String> params = setPlayerParameters(playerParams);

        mPlacementId = env.get(VideoAd.Environment.KEY_PLACEMENT_ID);

        VideoAd ad = VideoAdManager.getInstance().create(VideoAd.PLACEMENT_TYPE_INTERSTITIAL, mPlacementId);
        ad.init(activity, env, params, VideoAd.DEFAULT_TIMEOUT);
        ad.setListener(this);
        ad.loadAd();
    }

    /**
     * Shows the loaded rewarded videoad
     */
    public void showRewardedVideo() {
        VideoAd ad = VideoAdManager.getInstance().get(mPlacementId);
        if (ad != null && ad.isAdLoaded()) {
            mDisplaying = true;
            ad.displayAd(null, null);
        } else if (mRewardedVideoListener != null) {
            mRewardedVideoListener.onRewardedVideoFailed(VideoAd.ErrorCode.INVALID_STATE.getErrorCode(), "Player is not loaded.");
        }
    }

    /**
     * Removes the rewarded video ad unit
     */
    public void removeRewardedVideo() {
        mDisplaying = false;
        VideoAdManager.getInstance().remove(mPlacementId);
    }

    /**
     * {@inheritDoc}
     */
    public void onPlayerReady() {
    }

    /**
     * {@inheritDoc}
     */
    public void onAdEvent(String eventName, String arg1, String arg2, String arg3) {
        if (mRewardedVideoListener != null) {
            if (eventName.equals(VideoAd.Events.AD_LOADED)) {
                mRewardedVideoListener.onRewardedVideoLoaded();
            } else if (eventName.equals(VideoAd.Events.AD_VIDEO_START)) {
                mRewardedVideoListener.onRewardedVideoShown();
            } else if (eventName.equals(VideoAd.Events.AD_VIDEO_COMPLETE)) {
                mRewardedVideoListener.onRewardedVideoCompleted(mRewardTitle, mRewardAmount);
            } else if (eventName.equals(VideoAd.Events.AD_STOPPED)) {
                mRewardedVideoListener.onRewardedVideoDismissed();
                if (mDisplaying) {
                    mDisplaying = false;
                    mRewardedVideoListener.onRewardedVideoDismissed();
                }
            } else if (eventName.equals(VideoAd.Events.AD_ERROR)) {
                VideoAd ad = VideoAdManager.getInstance().get(mPlacementId);
                if (ad != null && ad.isAdLoaded()) {
                    mRewardedVideoListener.onRewardedVideoFailed(VideoAd.ErrorCode.PLAYBACK_FAILED.getErrorCode(), arg1);
                } else {
                    mRewardedVideoListener.onRewardedVideoFailed(VideoAd.ErrorCode.NO_FILL.getErrorCode(), arg1);
                }
            } else if (eventName.equals(VideoAd.Events.AD_CLICKTHRU)) {
                mRewardedVideoListener.onRewardedVideoClicked();
            } else if (eventName.equals(VideoAd.Events.AD_LEFT_APPLICATION)) {
                mRewardedVideoListener.onRewardedVideoAdLeftApplication();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onError(VideoAd.ErrorCode errorCode, String message) {
        removeRewardedVideo();
        if (mRewardedVideoListener != null) {
            mRewardedVideoListener.onRewardedVideoFailed(errorCode.getErrorCode(), message);
        }
    }
}