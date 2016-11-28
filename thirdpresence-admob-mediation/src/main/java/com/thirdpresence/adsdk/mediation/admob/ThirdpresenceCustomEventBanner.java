package com.thirdpresence.adsdk.mediation.admob;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.mediation.MediationAdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventBanner;
import com.google.android.gms.ads.mediation.customevent.CustomEventBannerListener;
import com.thirdpresence.adsdk.sdk.BannerView;
import com.thirdpresence.adsdk.sdk.VideoAd;
import com.thirdpresence.adsdk.sdk.VideoAdManager;
import com.thirdpresence.adsdk.sdk.VideoBanner;
import com.thirdpresence.adsdk.sdk.internal.TLog;

import java.util.Map;

import static com.google.android.gms.ads.AdRequest.ERROR_CODE_INTERNAL_ERROR;
import static com.google.android.gms.ads.AdRequest.ERROR_CODE_NO_FILL;

/**
 *
 * ThirdpresenceCustomEventBanner is CustomEvent implementation for Admob SDK that provides banner ad
 * mediation for Thirdpresence Ad SDK
 *
 */
public class ThirdpresenceCustomEventBanner  implements CustomEventBanner, VideoAd.Listener {

    private CustomEventBannerListener mBannerListener;
    private BannerView mBannerView;
    private String mPlacementId;
    private Boolean mAdLoaded;

    /**
     * Default constructor
     */
    public ThirdpresenceCustomEventBanner() {

    }
    /**
     * From CustomEventBanner
     */
    @Override
    public void requestBannerAd(Context context,
                                CustomEventBannerListener customEventBannerListener,
                                String publisherData,
                                AdSize adSize,
                                MediationAdRequest mediationAdRequest,
                                Bundle customEventExtras) {

        onDestroy();

        mBannerListener = customEventBannerListener;

        Activity activity = null;
        if (context instanceof Activity) {
            activity = (Activity) context;
        }

        if (activity == null) {
            TLog.e("Activity is null");
            mBannerListener.onAdFailedToLoad(ERROR_CODE_INTERNAL_ERROR);
            return;
        }

        Map<String, String> pubParams = ThirdpresenceCustomEventHelper.parseParamsString(publisherData);
        Map<String, String> env = ThirdpresenceCustomEventHelper.setEnvironment(pubParams);
        Map<String, String> params = ThirdpresenceCustomEventHelper.setPlayerParameters(activity, pubParams);

        mPlacementId = env.get(VideoAd.Environment.KEY_PLACEMENT_ID);

        if (mPlacementId == null) {
            TLog.e("Placement id is null");
            mBannerListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INVALID_REQUEST);
            return;
        }

        Bundle bannerParams = new Bundle();
        bannerParams.putInt(BannerView.PARAM_KEY_AD_WIDTH, adSize.getWidth());
        bannerParams.putInt(BannerView.PARAM_KEY_AD_HEIGHT, adSize.getHeight());
        mBannerView = new BannerView(context, bannerParams);

        VideoBanner banner = (VideoBanner)VideoAdManager.getInstance().create(VideoAd.PLACEMENT_TYPE_BANNER, mPlacementId);
        banner.setListener(this);
        banner.init(activity, mBannerView, env, params, VideoAd.DEFAULT_TIMEOUT);
        banner.loadAd();
    }

    /**
     * From CustomEventBanner
     */
    @Override
    public void onDestroy() {
        if (mPlacementId != null) {
            VideoAdManager.getInstance().remove(mPlacementId);
            mPlacementId = null;
        }

        mBannerView = null;
        mAdLoaded = false;
        mBannerListener = null;
    }

    /**
     * From CustomEventBanner
     */
    @Override
    public void onPause() {
        if (mPlacementId != null) {
            VideoAd ad = VideoAdManager.getInstance().get(mPlacementId);
            ad.pauseAd();
        }
    }

    /**
     * From CustomEventBanner
     */
    @Override
    public void onResume() {
        if (mPlacementId != null) {
            VideoAd ad = VideoAdManager.getInstance().get(mPlacementId);
            ad.resumeAd();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAdEvent(String eventName, String arg1, String arg2, String arg3) {
        if (mBannerListener != null ) {
            if (eventName.equals(VideoAd.Events.AD_LOADED)) {
                mAdLoaded = true;
                mBannerListener.onAdLoaded(mBannerView);
            } else if (eventName.equals(VideoAd.Events.AD_ERROR)) {
                if (mAdLoaded) {
                    mBannerListener.onAdFailedToLoad(ERROR_CODE_INTERNAL_ERROR);
                    TLog.e("An error occurred while playing");
                } else {
                    mBannerListener.onAdFailedToLoad(ERROR_CODE_NO_FILL);
                    TLog.e("An error occurred while loading an ad");
                }
            } else if (eventName.equals(VideoAd.Events.AD_CLICKTHRU)) {
                mBannerListener.onAdClicked();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPlayerReady() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onError(VideoAd.ErrorCode errorCode, String message) {
        TLog.e("An error occurred, code : " + errorCode + ", message: " + message);
        mAdLoaded = false;

        if (mBannerListener != null) {
            mBannerListener.onAdFailedToLoad(ThirdpresenceCustomEventHelper.mapErrorCode(errorCode));
        }
    }
}
