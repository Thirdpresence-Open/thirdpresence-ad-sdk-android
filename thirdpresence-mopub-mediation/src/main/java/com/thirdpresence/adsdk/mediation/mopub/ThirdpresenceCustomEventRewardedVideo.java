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
import com.thirdpresence.adsdk.sdk.internal.TLog;
import java.util.Map;

/**
 *
 * ThirdpresenceCustomEventRewardedVideo is CustomEventRewardedVideo implementation for MoPub SDK that
 * provides rewarded video mediation for Thirdpresence Ad SDK
 *
 */
public class ThirdpresenceCustomEventRewardedVideo extends CustomEventRewardedVideo implements MediationSettings, VideoAd.Listener, LifecycleListener {

    private RewardedVideo mRewardedVideo;
    private String mPlacementId = null;
    private String mRewardTitle = MoPubReward.NO_REWARD_LABEL;
    private int mRewardAmount = MoPubReward.NO_REWARD_AMOUNT;
    private CustomEventRewardedVideoListener mRewardedListener;
    private boolean mAdLoaded = false;
    public class RewardedVideoListener implements CustomEventRewardedVideoListener {}

    /**
     * From CustomEventRewardedVideoListener
     */
    @Override
    protected boolean checkAndInitializeSdk(@NonNull Activity launcherActivity, @NonNull Map<String, Object> localExtras, @NonNull Map<String, String> serverExtras) throws Exception {
        onInvalidate();

        mRewardedListener = new RewardedVideoListener();
        mRewardTitle = ThirdpresenceCustomEventHelper.parseRewardTitle(serverExtras);
        mRewardAmount = ThirdpresenceCustomEventHelper.parseRewardAmount(serverExtras);

        Map<String, String> env = ThirdpresenceCustomEventHelper.setEnvironment(serverExtras);
        Map<String, String> params = ThirdpresenceCustomEventHelper.setPlayerParameters(launcherActivity, serverExtras);

        mPlacementId = env.get(VideoAd.Environment.KEY_PLACEMENT_ID);

        if (mPlacementId == null) {
            TLog.e("Placement id is null");
            return false;
        }

        mRewardedVideo = new RewardedVideo(mPlacementId);
        mRewardedVideo.setListener(this);
        mRewardedVideo.init(launcherActivity, env, params, VideoAd.DEFAULT_TIMEOUT);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadWithSdkInitialized(@NonNull Activity activity, @NonNull Map<String, Object> localExtras, @NonNull Map<String, String> serverExtras) throws Exception {
        if (mRewardedVideo != null && mPlacementId != null) {
            mRewardedVideo.loadAd();
        } else {
            MoPubRewardedVideoManager.onRewardedVideoLoadFailure(
                ThirdpresenceCustomEventRewardedVideo.class,
                    mPlacementId,
                MoPubErrorCode.NETWORK_INVALID_STATE);
        }
    }

    /**
     * From CustomEventRewardedVideoListener
     */
    @Override
    protected boolean hasVideoAvailable() {
        return mRewardedVideo != null && mRewardedVideo.isAdLoaded();
    }

    /**
     * From CustomEventRewardedVideoListener
     */
    @Override
    protected void showVideo() {
        if (mRewardedVideo != null) {
            mRewardedVideo.displayAd();
        } else {
            MoPubRewardedVideoManager.onRewardedVideoPlaybackError(ThirdpresenceCustomEventRewardedVideo.class, mPlacementId, MoPubErrorCode.VIDEO_PLAYBACK_ERROR);
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
        return mPlacementId;
    }

    /**
     * From CustomEventRewardedVideoListener
     */
    @Override
    public void onInvalidate() {
        if (mRewardedVideo != null) {
            mRewardedVideo.remove();
            mRewardedVideo.setListener(null);
            mRewardedVideo = null;
        }
        mPlacementId = null;
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
        if (mPlacementId != null ) {
            if (eventName.equals(VideoAd.Events.AD_LOADED)) {
                mAdLoaded = true;
                MoPubRewardedVideoManager.onRewardedVideoLoadSuccess(
                    ThirdpresenceCustomEventRewardedVideo.class,
                        mPlacementId);
            } else if (eventName.equals(VideoAd.Events.AD_VIDEO_START)) {
                MoPubRewardedVideoManager.onRewardedVideoStarted(
                    ThirdpresenceCustomEventRewardedVideo.class,
                        mPlacementId);
            } else if (eventName.equals(VideoAd.Events.AD_VIDEO_COMPLETE)) {
                MoPubReward reward;
                reward = MoPubReward.success(mRewardTitle, mRewardAmount);
                MoPubRewardedVideoManager.onRewardedVideoCompleted(
                    ThirdpresenceCustomEventRewardedVideo.class,
                    null, // Can't deduce the zoneId from this object.
                    reward);
            } else if (eventName.equals(VideoAd.Events.AD_STOPPED)) {
                mAdLoaded = false;
                if (mRewardedVideo != null) {
                    mRewardedVideo.remove();
                }
                MoPubRewardedVideoManager.onRewardedVideoClosed(ThirdpresenceCustomEventRewardedVideo.class, mPlacementId);
                mPlacementId = null;
            } else if (eventName.equals(VideoAd.Events.AD_ERROR)) {
                if (mAdLoaded) {
                    MoPubRewardedVideoManager.onRewardedVideoPlaybackError(ThirdpresenceCustomEventRewardedVideo.class, mPlacementId, MoPubErrorCode.VIDEO_PLAYBACK_ERROR);
                } else {
                    MoPubRewardedVideoManager.onRewardedVideoLoadFailure(ThirdpresenceCustomEventRewardedVideo.class, mPlacementId, MoPubErrorCode.NO_FILL);
                }
            } else if (eventName.equals(VideoAd.Events.AD_CLICKTHRU)) {
                MoPubRewardedVideoManager.onRewardedVideoClicked(
                    ThirdpresenceCustomEventRewardedVideo.class,
                        mPlacementId);

            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onError(VideoAd.ErrorCode errorCode, String message) {
        if (mPlacementId != null) {
            MoPubErrorCode moPubErrorCode = ThirdpresenceCustomEventHelper.mapErrorCode(errorCode);

            if (mAdLoaded) {
                MoPubRewardedVideoManager.onRewardedVideoPlaybackError(
                        ThirdpresenceCustomEventRewardedVideo.class,
                        mPlacementId,
                        moPubErrorCode);
            } else {
                MoPubRewardedVideoManager.onRewardedVideoLoadFailure(
                        ThirdpresenceCustomEventRewardedVideo.class,
                        mPlacementId,
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
