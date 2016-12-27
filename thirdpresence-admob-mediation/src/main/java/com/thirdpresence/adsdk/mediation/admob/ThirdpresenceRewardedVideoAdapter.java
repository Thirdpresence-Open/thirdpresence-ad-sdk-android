package com.thirdpresence.adsdk.mediation.admob;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.mediation.MediationAdRequest;
import com.google.android.gms.ads.mediation.OnContextChangedListener;
import com.google.android.gms.ads.reward.mediation.MediationRewardedVideoAdAdapter;
import com.google.android.gms.ads.reward.mediation.MediationRewardedVideoAdListener;
import com.thirdpresence.adsdk.sdk.RewardedVideo;
import com.thirdpresence.adsdk.sdk.VideoAd;
import com.thirdpresence.adsdk.sdk.internal.TLog;
import java.util.Map;

/**
 *
 * ThirdpresenceRewardedVideoAdapter is an MediationRewardedVideoAdAdapter implementation
 * for Admob SDK that provides rewarded video mediation for Thirdpresence Ad SDK
 *
 */
public class ThirdpresenceRewardedVideoAdapter implements MediationRewardedVideoAdAdapter, VideoAd.Listener, OnContextChangedListener {

    private RewardedVideo mRewardedVideo;
    private RewardData mRewardData;
    private MediationRewardedVideoAdListener mRewardedListener;
    private Activity mActivity;
    /**
     * Default constructor
    */
    public ThirdpresenceRewardedVideoAdapter() {}

    /**
     * Closes the interstitial and releases all resources
     */
    private void clear() {
        if (mRewardedVideo != null) {
            mRewardedVideo.remove();
            mRewardedVideo.setListener(null);
            mRewardedVideo = null;
        }
        mRewardedListener = null;
        mRewardData = null;
    }

    /**
     * From MediationRewardedVideoAdAdapter
     */
    @Override
    public void initialize(Context context,
                           MediationAdRequest mediationAdRequest,
                           String unused,
                           MediationRewardedVideoAdListener mediationRewardedVideoAdListener,
                           Bundle serverParameters,
                           Bundle mediationExtras) {

        clear();

        mRewardedListener = mediationRewardedVideoAdListener;

        if (mActivity == null && context instanceof Activity) {
            mActivity = (Activity) context;
        }

        if (mActivity == null) {
            TLog.e("Activity is null");
            mRewardedListener.onInitializationFailed(this, AdRequest.ERROR_CODE_INVALID_REQUEST);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (mActivity.isDestroyed()) {
                TLog.e("Activity is destroyed");
                mRewardedListener.onInitializationFailed(this, AdRequest.ERROR_CODE_INVALID_REQUEST);
                return;
            }
        }

        String publisherParams = serverParameters.getString(CUSTOM_EVENT_SERVER_PARAMETER_FIELD);
        if (publisherParams == null) {
            TLog.e("Publisher parameters not set");
            mRewardedListener.onInitializationFailed(this, AdRequest.ERROR_CODE_INVALID_REQUEST);
            return;
        }

        Map<String, String> pubParams = ThirdpresenceCustomEventHelper.parseParamsString(publisherParams);

        TLog.d("Publisher parameters: " + pubParams);

        mRewardData = ThirdpresenceCustomEventHelper.parseRewardData(pubParams);
        if (mRewardData.getType() == null) {
            TLog.e("Reward title not set");
            mRewardedListener.onInitializationFailed(this, AdRequest.ERROR_CODE_INVALID_REQUEST);
            return;
        }

        if (mRewardData.getAmount() == -1) {
            TLog.e("Reward amount not set");
            mRewardedListener.onInitializationFailed(this, AdRequest.ERROR_CODE_INVALID_REQUEST);
            return;
        }

        Map<String, String> env = ThirdpresenceCustomEventHelper.setEnvironment(pubParams);
        Map<String, String> params = ThirdpresenceCustomEventHelper.setPlayerParameters(mActivity,
                mediationAdRequest,
                pubParams);

        String placementId = env.get(VideoAd.Environment.KEY_PLACEMENT_ID);

        if (placementId == null) {
            TLog.e("Placement id not set");
            mRewardedListener.onInitializationFailed(this, AdRequest.ERROR_CODE_INVALID_REQUEST);
            return;
        }

        mRewardedVideo = new RewardedVideo(placementId);
        mRewardedVideo.setListener(this);
        mRewardedVideo.init(mActivity, env, params, VideoAd.DEFAULT_TIMEOUT);
    }

    /**
     * From MediationRewardedVideoAdAdapter
     */
    @Override
    public void loadAd(MediationAdRequest mediationAdRequest, Bundle bundle, Bundle bundle1) {
        if (mRewardedVideo != null) {
            mRewardedVideo.loadAd();
        }
    }

    /**
     * From MediationRewardedVideoAdAdapter
     */
    @Override
    public void showVideo() {
        if (mRewardedVideo != null && mRewardedVideo.isAdLoaded()) {
            mRewardedVideo.displayAd();
        }
        else if (mRewardedListener != null) {
            Log.w(TLog.LOG_TAG, "Ad is not loaded");
        }
    }

    /**
     * From MediationRewardedVideoAdAdapter
     */
    @Override
    public boolean isInitialized() {
        return (mRewardedVideo != null && mRewardedVideo.isPlayerReady());
     }

    /**
     * From MediationRewardedVideoAdAdapter
     */
    @Override
    public void onDestroy() {
        clear();
    }

    /**
     * From MediationRewardedVideoAdAdapter
     */
    @Override
    public void onPause() {
        mRewardedVideo.pauseAd();
    }

    /**
     * From MediationRewardedVideoAdAdapter
     */
    @Override
    public void onResume() {
        mRewardedVideo.resumeAd();
    }

    /**
     * From OnContextChangedListener
     */
    @Override
    public void onContextChanged (Context newContext) {
        if (newContext instanceof Activity) {
            mActivity = (Activity) newContext;
            TLog.d("New activity: " + mActivity.getTitle());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAdEvent(String eventName, String arg1, String arg2, String arg3) {
        if (mRewardedListener != null ) {
            if (eventName.equals(VideoAd.Events.AD_LOADED)) {
                mRewardedListener.onAdLoaded(this);
            } else if (eventName.equals(VideoAd.Events.AD_STARTED)) {
                mRewardedListener.onAdOpened(this);
            } else if (eventName.equals(VideoAd.Events.AD_VIDEO_START)) {
                mRewardedListener.onVideoStarted(this);
            } else if (eventName.equals(VideoAd.Events.AD_STOPPED)) {
                if (mRewardedVideo != null) {
                    mRewardedVideo.remove();
                }
                mRewardedListener.onAdClosed(this);
            } else if (eventName.equals(VideoAd.Events.AD_ERROR)) {
                mRewardedListener.onAdFailedToLoad(this, AdRequest.ERROR_CODE_NO_FILL);
            } else if (eventName.equals(VideoAd.Events.AD_CLICKTHRU)) {
                mRewardedListener.onAdClicked(this);
            } else if (eventName.equals(VideoAd.Events.AD_VIDEO_COMPLETE)) {
                TLog.w("Reward earned: " + mRewardData.toString());
                mRewardedListener.onRewarded(this, mRewardData);
            } else if (eventName.equals(VideoAd.Events.AD_LEFT_APPLICATION)) {
                mRewardedListener.onAdLeftApplication(this);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPlayerReady() {
        if (mRewardedListener != null ) {
            mRewardedListener.onInitializationSucceeded(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onError(VideoAd.ErrorCode errorCode, String message) {
        if (mRewardedVideo != null) {
            mRewardedVideo.remove();
        }
        if (mRewardedListener != null) {
            int admobErrorCode = ThirdpresenceCustomEventHelper.mapErrorCode(errorCode);
            mRewardedListener.onAdFailedToLoad(this, admobErrorCode);
        }

    }
}
