package com.thirdpresence.adsdk.mediation.mopub;

import android.app.Activity;
import android.content.Context;

import com.thirdpresence.adsdk.sdk.VideoAd;
import com.thirdpresence.adsdk.sdk.VideoInterstitial;
import com.mopub.mobileads.CustomEventInterstitial;
import com.mopub.mobileads.MoPubErrorCode;
import com.thirdpresence.adsdk.sdk.internal.TLog;

import java.util.Map;

/**
 *
 * ThirdpresenceCustomEvent is CustomEvent implementation for MoPub SDK that provides interstitial ad
 * mediation for Thirdpresence Ad SDK
 *
 */
public class ThirdpresenceCustomEvent extends CustomEventInterstitial implements VideoAd.Listener {

    private CustomEventInterstitialListener mInterstitialListener;
    private VideoInterstitial mVideoInterstitial;

    private static boolean mAdLoaded = false;

    /**
     * From CustomEventInterstitial
     */
    @Override
    protected void loadInterstitial(Context context,
                                    CustomEventInterstitialListener interstitialListener,
                                    Map<String, Object> localExtras, Map<String, String> serverExtras) {
        onInvalidate();

        mInterstitialListener = interstitialListener;

        Activity activity = null;
        if (context instanceof Activity) {
            activity = (Activity) context;
        }

        if (activity == null) {
            mInterstitialListener.onInterstitialFailed(MoPubErrorCode.UNSPECIFIED);
            TLog.e("activity is null");
            return;
        }

        Map<String, String> env = ThirdpresenceCustomEventHelper.setEnvironment(serverExtras);
        Map<String, String> params = ThirdpresenceCustomEventHelper.setPlayerParameters(activity, localExtras, serverExtras);

        String placementId = env.get(VideoAd.Environment.KEY_PLACEMENT_ID);

        if (placementId == null) {
            mInterstitialListener.onInterstitialFailed(MoPubErrorCode.UNSPECIFIED);
            TLog.e("Placement id is null");
            return;
        }

        mVideoInterstitial = new VideoInterstitial(placementId);
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
            mInterstitialListener.onInterstitialFailed(MoPubErrorCode.UNSPECIFIED);
        }
    }

    /**
     * From CustomEventInterstitial
     */
    @Override
    public void onInvalidate() {
        mAdLoaded = false;
        if (mVideoInterstitial != null) {
            mVideoInterstitial.remove();
            mVideoInterstitial.setListener(null);
            mVideoInterstitial = null;
        }
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
            MoPubErrorCode moPubErrorCode = ThirdpresenceCustomEventHelper.mapErrorCode(errorCode);
            mInterstitialListener.onInterstitialFailed(moPubErrorCode);
        }
        TLog.e("An error occurred, code : " + errorCode + ", message: " + message);
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
                mAdLoaded = false;
                if (mVideoInterstitial != null) {
                    mVideoInterstitial.remove();
                }
                mInterstitialListener.onInterstitialDismissed();
            } else if (eventName.equals(VideoAd.Events.AD_ERROR)) {
                if (mAdLoaded) {
                    mInterstitialListener.onInterstitialFailed(MoPubErrorCode.VIDEO_PLAYBACK_ERROR);
                    TLog.e("An error occurred while playing");
                } else {
                    mInterstitialListener.onInterstitialFailed(MoPubErrorCode.NO_FILL);
                    TLog.e("An error occurred while loading an ad");
                }
            } else if (eventName.equals(VideoAd.Events.AD_CLICKTHRU)) {
                mInterstitialListener.onInterstitialClicked();
            }
        }
    }
}
