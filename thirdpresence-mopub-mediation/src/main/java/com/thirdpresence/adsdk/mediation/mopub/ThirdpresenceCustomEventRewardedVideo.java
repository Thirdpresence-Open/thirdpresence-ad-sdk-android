package com.thirdpresence.adsdk.mediation.mopub;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mopub.common.LifecycleListener;
import com.mopub.common.MediationSettings;
import com.mopub.common.MoPubReward;
import com.mopub.mobileads.CustomEventRewardedVideo;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubRewardedVideoManager;
import com.thirdpresence.adsdk.sdk.RewardedVideo;
import com.thirdpresence.adsdk.sdk.VideoAd;
import com.thirdpresence.adsdk.sdk.VideoInterstitial;

import java.util.Map;

/**
 *
 * ThirdpresenceCustomEventRewardedVideo is CustomEventRewardedVideo implementation for MoPub SDK that
 * provides rewarded video mediation for Thirdpresence Ad SDK
 *
 */
public class ThirdpresenceCustomEventRewardedVideo extends CustomEventRewardedVideo implements MediationSettings, VideoAd.Listener, LifecycleListener {

    private RewardedVideo mInterstitial;
    private String mPlayerId = null;
    private String mRewardTitle = MoPubReward.NO_REWARD_LABEL;
    private int mRewardAmount = MoPubReward.NO_REWARD_AMOUNT;
    private CustomEventRewardedVideoListener mRewardedListener;
    private boolean mAdLoaded = false;
    public class RewardedVideoListener implements CustomEventRewardedVideoListener {}

    private static final String EXTRAS_KEY_REWARD_TITLE = "rewardtitle";
    private static final String EXTRAS_KEY_REWARD_AMOUNT = "rewardamount";

    /**
     * From CustomEventRewardedVideoListener
     */
    @Override
    protected boolean checkAndInitializeSdk(@NonNull Activity launcherActivity, @NonNull Map<String, Object> localExtras, @NonNull Map<String, String> serverExtras) throws Exception {
        onInvalidate();

        mRewardedListener = new RewardedVideoListener();

        Map<String, String> env = ThirdpresenceCustomEventHelper.setEnvironment(serverExtras);
        Map<String, String> params = ThirdpresenceCustomEventHelper.setPlayerParameters(launcherActivity, serverExtras);

        mPlayerId = env.get(VideoAd.Environment.KEY_PLACEMENT_ID);

        if (serverExtras.containsKey(EXTRAS_KEY_REWARD_TITLE)) {
            mRewardTitle = serverExtras.get(EXTRAS_KEY_REWARD_TITLE);
        } else {
            mRewardTitle = MoPubReward.NO_REWARD_LABEL;
        }

        try {
            String reward = serverExtras.get(EXTRAS_KEY_REWARD_AMOUNT);
            if (reward != null) {
                mRewardAmount = Integer.parseInt(reward);
            }
        } catch (NumberFormatException e) {
            mRewardAmount = MoPubReward.NO_REWARD_AMOUNT;
        }

        mInterstitial = new RewardedVideo();
        mInterstitial.setListener(this);
        mInterstitial.init(launcherActivity, env, params, VideoAd.DEFAULT_TIMEOUT);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadWithSdkInitialized(@NonNull Activity activity, @NonNull Map<String, Object> localExtras, @NonNull Map<String, String> serverExtras) throws Exception {
        if (mInterstitial != null && mPlayerId != null) {
            mInterstitial.loadAd();
        } else {
            MoPubRewardedVideoManager.onRewardedVideoLoadFailure(
                ThirdpresenceCustomEventRewardedVideo.class,
                mPlayerId,
                MoPubErrorCode.NETWORK_INVALID_STATE);
        }
    }

    /**
     * From CustomEventRewardedVideoListener
     */
    @Override
    protected boolean hasVideoAvailable() {
        return mInterstitial != null && mInterstitial.isAdLoaded();
    }

    /**
     * From CustomEventRewardedVideoListener
     */
    @Override
    protected void showVideo() {
        if (mInterstitial != null) {
            mInterstitial.displayAd();
        } else {
            MoPubRewardedVideoManager.onRewardedVideoPlaybackError(ThirdpresenceCustomEventRewardedVideo.class, mPlayerId, MoPubErrorCode.VIDEO_PLAYBACK_ERROR);
        }
    }

    /**
     * From CustomEventRewardedVideoListener
     */
    @Nullable
    @Override
    protected CustomEventRewardedVideoListener getVideoListenerForSdk() {
        return mRewardedListener;
    }

    /**
     * From CustomEventRewardedVideoListener
     */
    @Nullable
    @Override
    protected LifecycleListener getLifecycleListener() {
        return this;
    }

    /**
     * From CustomEventRewardedVideoListener
     */
    @NonNull
    @Override
    protected String getAdNetworkId() {
        return mPlayerId;
    }

    /**
     * From CustomEventRewardedVideoListener
     */
    @Override
    public void onInvalidate() {
        if (mInterstitial != null) {
            mInterstitial.remove();
            mInterstitial.setListener(null);
            mInterstitial = null;
        }
        mPlayerId = null;
        mAdLoaded = false;
    }

    /**
     * {@inheritDoc}
     */
    public void onPlayerReady() {}

    /**
     * {@inheritDoc}
     */
    public void onAdEvent(String eventName, String arg1, String arg2, String arg3) {
        if (mPlayerId != null ) {
            if (eventName.equals(VideoAd.Events.AD_LOADED)) {
                mAdLoaded = true;
                MoPubRewardedVideoManager.onRewardedVideoLoadSuccess(
                    ThirdpresenceCustomEventRewardedVideo.class,
                    mPlayerId);
            } else if (eventName.equals(VideoAd.Events.AD_VIDEO_START)) {
                MoPubRewardedVideoManager.onRewardedVideoStarted(
                    ThirdpresenceCustomEventRewardedVideo.class,
                    mPlayerId);
            } else if (eventName.equals(VideoAd.Events.AD_VIDEO_COMPLETE)) {
                MoPubReward reward;
                reward = MoPubReward.success(mRewardTitle, mRewardAmount);
                MoPubRewardedVideoManager.onRewardedVideoCompleted(
                    ThirdpresenceCustomEventRewardedVideo.class,
                    null, // Can't deduce the zoneId from this object.
                    reward);
            } else if (eventName.equals(VideoAd.Events.AD_STOPPED)) {
                mAdLoaded = false;
                if (mInterstitial != null) {
                    mInterstitial.remove();
                }
                MoPubRewardedVideoManager.onRewardedVideoClosed(ThirdpresenceCustomEventRewardedVideo.class, mPlayerId);
                mPlayerId = null;
            } else if (eventName.equals(VideoAd.Events.AD_ERROR)) {
                if (mAdLoaded) {
                    MoPubRewardedVideoManager.onRewardedVideoPlaybackError(ThirdpresenceCustomEventRewardedVideo.class, mPlayerId, MoPubErrorCode.VIDEO_PLAYBACK_ERROR);
                } else {
                    MoPubRewardedVideoManager.onRewardedVideoLoadFailure(ThirdpresenceCustomEventRewardedVideo.class, mPlayerId, MoPubErrorCode.NO_FILL);
                }
            } else if (eventName.equals(VideoAd.Events.AD_CLICKTHRU)) {
                MoPubRewardedVideoManager.onRewardedVideoClicked(
                    ThirdpresenceCustomEventRewardedVideo.class,
                    mPlayerId);

            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onError(VideoAd.ErrorCode errorCode, String message) {
        if (mPlayerId != null) {
            MoPubErrorCode moPubErrorCode = ThirdpresenceCustomEventHelper.mapErrorCode(errorCode);

            if (mAdLoaded) {
                MoPubRewardedVideoManager.onRewardedVideoPlaybackError(
                        ThirdpresenceCustomEventRewardedVideo.class,
                        mPlayerId,
                        moPubErrorCode);
            } else {
                MoPubRewardedVideoManager.onRewardedVideoLoadFailure(
                        ThirdpresenceCustomEventRewardedVideo.class,
                        mPlayerId,
                        moPubErrorCode);
            }
        }
    }

    /**
     * From LifecycleListener
     */
    @Override
    public void onCreate(@NonNull Activity activity) {}

    /**
     * From LifecycleListener
     */
    @Override
    public void onStart(@NonNull Activity activity) {}

    /**
     * From LifecycleListener
     */
    @Override
    public void onPause(@NonNull Activity activity) {}

    /**
     * From LifecycleListener
     */
    @Override
    public void onResume(@NonNull Activity activity) {}

    /**
     * From LifecycleListener
     */
    @Override
    public void onRestart(@NonNull Activity activity) {}

    /**
     * From LifecycleListener
     */
    @Override
    public void onStop(@NonNull Activity activity) {}

    /**
     * From LifecycleListener
     */
    @Override
    public void onDestroy(@NonNull Activity activity) {
        onInvalidate();
    }

    /**
     * From LifecycleListener
     */
    @Override
    public void onBackPressed(@NonNull Activity activity) {}

}
