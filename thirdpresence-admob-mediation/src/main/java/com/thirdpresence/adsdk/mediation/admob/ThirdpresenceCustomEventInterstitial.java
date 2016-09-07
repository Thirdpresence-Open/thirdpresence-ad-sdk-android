package com.thirdpresence.adsdk.mediation.admob;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.mediation.MediationAdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventInterstitial;
import com.google.android.gms.ads.mediation.customevent.CustomEventInterstitialListener;

import com.thirdpresence.adsdk.sdk.VideoAd;
import com.thirdpresence.adsdk.sdk.VideoInterstitial;

import java.util.Map;

/**
 *
 * ThirdpresenceCustomEventInterstitial is CustomEvent implementation for Admob SDK that provides
 * interstitial ad mediation for Thirdpresence Ad SDK
 *
 */
public class ThirdpresenceCustomEventInterstitial implements CustomEventInterstitial, VideoAd.Listener {

    private CustomEventInterstitialListener mInterstitialListener;
    private VideoInterstitial mVideoInterstitial;
    private Boolean mAdLoaded;

    /**
     * Default constructor
     */
    public ThirdpresenceCustomEventInterstitial() {}

     /**
     * Closes the interstitial and releases all resources
     */
    private void clear() {
        mAdLoaded = false;
        if (mVideoInterstitial != null) {
            mVideoInterstitial.remove();
            mVideoInterstitial.setListener(null);
            mVideoInterstitial = null;
        }
    }

    /**
     * From CustomEventInterstitial
     */
    @Override
    public void requestInterstitialAd(Context context,
                                      CustomEventInterstitialListener customEventInterstitialListener,
                                      String publisherData,
                                      MediationAdRequest mediationAdRequest,
                                      Bundle bundle) {
        clear();

        mInterstitialListener = customEventInterstitialListener;

        Activity activity = null;
        if (context instanceof Activity) {
            activity = (Activity) context;
        }

        if (activity == null) {
            mInterstitialListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INTERNAL_ERROR);
            return;
        }

        Map<String, String> pubParams = ThirdpresenceCustomEventHelper.parseParamsString(publisherData);
        Map<String, String> env = ThirdpresenceCustomEventHelper.setEnvironment(pubParams);
        Map<String, String> params = ThirdpresenceCustomEventHelper.setPlayerParameters(activity, pubParams);

        mVideoInterstitial = new VideoInterstitial();
        mVideoInterstitial.setListener(this);
        mVideoInterstitial.init(activity, env, params, VideoInterstitial.DEFAULT_TIMEOUT);
        mVideoInterstitial.loadAd();
    }

    /**
     * From CustomEventInterstitial
     */
    @Override
    public void showInterstitial() {
        if (mAdLoaded && mVideoInterstitial != null) {
            mVideoInterstitial.displayAd();
        } else if (mInterstitialListener != null)  {
            mInterstitialListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INTERNAL_ERROR);
        }
    }

    /**
     * From CustomEventInterstitial
     */
    @Override
    public void onDestroy() {
        clear();
    }

    /**
     * From CustomEventInterstitial
     */
    @Override
    public void onPause() {
    }

    /**
     * From CustomEventInterstitial
     */
    @Override
    public void onResume() {
    }

    /**
     * {@inheritDoc}
     */
    public void onPlayerReady() {}

    /**
     * {@inheritDoc}
     */
    public void onError(VideoAd.ErrorCode errorCode, String message) {
        mAdLoaded = false;
        if (mVideoInterstitial != null) {
            mVideoInterstitial.remove();
        }
        if (mInterstitialListener != null) {
            int admobErrorCode = ThirdpresenceCustomEventHelper.mapErrorCode(errorCode);
           mInterstitialListener.onAdFailedToLoad(admobErrorCode);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onAdEvent(String eventName, String arg1, String arg2, String arg3) {
        if (mInterstitialListener != null ) {
            if (eventName.equals(VideoAd.Events.AD_LOADED)) {
                mAdLoaded = true;
                mInterstitialListener.onAdLoaded();
            } else if (eventName.equals(VideoAd.Events.AD_STARTED)) {
                mInterstitialListener.onAdOpened();
            } else if (eventName.equals(VideoAd.Events.AD_STOPPED)) {
                mAdLoaded = false;
                if (mVideoInterstitial != null) {
                    mVideoInterstitial.remove();
                }
                mInterstitialListener.onAdClosed();
            } else if (eventName.equals(VideoAd.Events.AD_ERROR)) {
                mInterstitialListener.onAdFailedToLoad(AdRequest.ERROR_CODE_NO_FILL);
            } else if (eventName.equals(VideoAd.Events.AD_CLICKTHRU)) {
                mInterstitialListener.onAdClicked();
            } else if (eventName.equals(VideoAd.Events.AD_LEFT_APPLICATION)) {
                mInterstitialListener.onAdLeftApplication();
            }
        }
    }
}

