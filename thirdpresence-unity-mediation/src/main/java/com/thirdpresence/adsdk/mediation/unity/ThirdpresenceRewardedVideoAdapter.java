package com.thirdpresence.adsdk.mediation.unity;

import android.app.Activity;
import android.content.Intent;

import com.thirdpresence.adsdk.sdk.RewardedVideo;
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
    private RewardedVideo mRewardedVideo;
    private String mRewardTitle = null;
    private int mRewardAmount = -1;
    private Activity mUnityActivity;

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

    private static final String EXTRAS_KEY_REWARD_TITLE = "rewardtitle";
    private static final String EXTRAS_KEY_REWARD_AMOUNT = "rewardamount";

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
            mRewardedVideoListener.onRewardedVideoFailed(VideoAd.ErrorCode.INVALID_STATE.getErrorCode(), "Activity is null");
            return;
        }

        mUnityActivity = activity;

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
            mRewardedVideoListener.onRewardedVideoFailed(VideoAd.ErrorCode.INVALID_STATE.getErrorCode(), "Rewarded video title or amount not properly set");
            return;
        }

        Map<String, String> env = setEnvironment(environment);
        Map<String, String> params = setPlayerParameters(playerParams);

        mRewardedVideo = new RewardedVideo();
        mRewardedVideo.setListener(this);
        mRewardedVideo.init(activity, env, params, VideoAd.DEFAULT_TIMEOUT);
        mRewardedVideo.loadAd();
    }

    /**
     * Shows the loaded rewarded videoad
     */
    public void showRewardedVideo() {
        if (mRewardedVideo != null && mRewardedVideo.isAdLoaded()) {
            Intent i = new Intent(mUnityActivity, ThirdpresencePlayerActivity.class);
            i.putExtra(ThirdpresencePlayerActivity.ADAPTER_CLASS_EXTRAS_KEY, this.getClass().getName());
            mUnityActivity.startActivity(i);
        } else if (mRewardedVideoListener != null) {
            mRewardedVideoListener.onRewardedVideoFailed(VideoAd.ErrorCode.INVALID_STATE.getErrorCode(), "Player is not loaded.");
        }
    }

    /**
     * Removes the rewarded video ad unit
     */
    public void removeRewardedVideo() {
        if (mRewardedVideo != null) {
            mRewardedVideo.remove();
            mRewardedVideo.setListener(null);
            mRewardedVideo = null;
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
     * Displays the ad. This is used by ThirdpresencePlayerActivity
     */
    public void displayAd() {
        if (mRewardedVideo != null) {
            mRewardedVideo.displayAd();
        } else if (mRewardedVideoListener != null) {
            mRewardedVideoListener.onRewardedVideoFailed(VideoAd.ErrorCode.INVALID_STATE.getErrorCode(), "Player is not loaded.");
        }
    }


    /**
     * Removes the ad
     */
    public void removeAd() {
        super.removeAd();
        if (mRewardedVideo != null) {
            mRewardedVideo.remove();
        }
    }

    /**
     * Sets the player activity.
     */
    public void setPlayerActivity(Activity activity) {
        super.setPlayerActivity(activity);
        mRewardedVideo.switchActivity(activity);
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
                removeAd();
                finishPlayerActivity();
                mRewardedVideoListener.onRewardedVideoDismissed();
            } else if (eventName.equals(VideoAd.Events.AD_ERROR)) {
                mRewardedVideoListener.onRewardedVideoFailed(VideoAd.ErrorCode.NO_FILL.getErrorCode(), "No ad available");
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
        removeAd();
        if (mRewardedVideoListener != null) {
            mRewardedVideoListener.onRewardedVideoFailed(errorCode.getErrorCode(), message);
        }

        finishPlayerActivity();

    }
}