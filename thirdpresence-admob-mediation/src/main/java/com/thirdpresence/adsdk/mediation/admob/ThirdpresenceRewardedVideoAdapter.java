package com.thirdpresence.adsdk.mediation.admob;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.mediation.MediationAdRequest;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.mediation.MediationRewardedVideoAdAdapter;
import com.google.android.gms.ads.reward.mediation.MediationRewardedVideoAdListener;
import com.thirdpresence.adsdk.sdk.RewardedVideo;
import com.thirdpresence.adsdk.sdk.VideoAd;

import java.util.Map;

/**
 *
 * ThirdpresenceRewardedVideoAdapter is an MediationRewardedVideoAdAdapter implementation
 * for Admob SDK that provides rewarded video mediation for Thirdpresence Ad SDK
 *
 */
public class ThirdpresenceRewardedVideoAdapter implements MediationRewardedVideoAdAdapter, VideoAd.Listener {

    private RewardedVideo mRewardedVideo;
    private String mRewardTitle;
    private int mRewardAmount;
    private MediationRewardedVideoAdListener mRewardedListener;

    private static final String PARAM_NAME_REWARD_TITLE = "rewardtitle";
    private static final String PARAM_NAME_REWARD_AMOUNT = "rewardamount";

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
            mRewardedListener = null;
        }
    }

    /**
     * From MediationRewardedVideoAdAdapter
     */
    @Override
    public void initialize(Context context, MediationAdRequest mediationAdRequest, String s, MediationRewardedVideoAdListener mediationRewardedVideoAdListener, Bundle bundle, Bundle bundle1) {

        clear();

        mRewardedListener = mediationRewardedVideoAdListener;

        Activity activity = null;
        if (context instanceof Activity) {
            activity = (Activity) context;
        }

        if (activity == null) {
            mRewardedListener.onInitializationFailed(this, AdRequest.ERROR_CODE_INTERNAL_ERROR);
            return;
        }

        Map<String, String> pubParams = ThirdpresenceCustomEventHelper.parseParamsString(s);
        Map<String, String> env = ThirdpresenceCustomEventHelper.setEnvironment(pubParams);
        Map<String, String> params = ThirdpresenceCustomEventHelper.setPlayerParameters(activity, pubParams);

        if (pubParams.containsKey(PARAM_NAME_REWARD_TITLE)) {
            mRewardTitle = pubParams.get(PARAM_NAME_REWARD_TITLE);
        } else {
            mRewardedListener.onInitializationFailed(this, AdRequest.ERROR_CODE_INVALID_REQUEST);
            return;
        }

        try {
            String reward = pubParams.get(PARAM_NAME_REWARD_AMOUNT);
            if (reward != null) {
                mRewardAmount = Integer.parseInt(reward);
            }
        } catch (NumberFormatException e) {
            mRewardedListener.onInitializationFailed(this, AdRequest.ERROR_CODE_INVALID_REQUEST);
            return;
        }

        mRewardedVideo = new RewardedVideo();
        mRewardedVideo.setListener(this);
        mRewardedVideo.init(activity, env, params, VideoAd.DEFAULT_TIMEOUT);
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
            mRewardedListener.onAdFailedToLoad(this, AdRequest.ERROR_CODE_INTERNAL_ERROR);
        }
    }

    /**
     * From MediationRewardedVideoAdAdapter
     */
    @Override
    public boolean isInitialized() {
        return mRewardedVideo != null && mRewardedVideo.isPlayerReady();
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

    }

    /**
     * From MediationRewardedVideoAdAdapter
     */
    @Override
    public void onResume() {

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
                mRewardedListener.onAdFailedToLoad(this, AdRequest.ERROR_CODE_INTERNAL_ERROR);
            } else if (eventName.equals(VideoAd.Events.AD_CLICKTHRU)) {
                mRewardedListener.onAdClicked(this);
            } else if (eventName.equals(VideoAd.Events.AD_VIDEO_COMPLETE)) {
                RewardItem reward = new RewardItem() {
                    @Override
                    public String getType() {
                        return mRewardTitle;
                    }

                    @Override
                    public int getAmount() {
                        return mRewardAmount;
                    }
                };
                mRewardedListener.onRewarded(this, reward);

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
            mRewardedListener.onInitializationFailed(this, admobErrorCode);
        }

    }
}
