package com.thirdpresence.adsdk.mediation.mopub;

import android.app.Activity;
import android.content.Context;
import com.mopub.mobileads.CustomEventBanner;
import com.mopub.mobileads.MoPubErrorCode;
import com.thirdpresence.adsdk.sdk.BannerView;
import com.thirdpresence.adsdk.sdk.VideoAd;
import com.thirdpresence.adsdk.sdk.VideoAdManager;
import com.thirdpresence.adsdk.sdk.VideoBanner;
import com.thirdpresence.adsdk.sdk.internal.TLog;
import java.util.Map;

/**
 *
 * ThirdpresenceCustomEventBanner is CustomEvent implementation for MoPub SDK that provides banner ad
 * mediation for Thirdpresence Ad SDK
 *
 */
public class ThirdpresenceCustomEventBanner extends CustomEventBanner implements VideoAd.Listener {

    private String mPlacementId;
    private BannerView mBannerView;
    private boolean mAdLoaded;
    private CustomEventBannerListener mBannerListener;

    /**
     * Default constructor
     */
    public ThirdpresenceCustomEventBanner() {
    }

    /**
     * From CustomEventBanner
     */
    @Override
    protected void loadBanner(Context context,
                              CustomEventBannerListener customEventBannerListener,
                              Map<String, Object> localExtras,
                              Map<String, String> serverExtras) {


        onInvalidate();

        mBannerListener = customEventBannerListener;

        Activity activity = null;
        if (context instanceof Activity) {
            activity = (Activity) context;
        }

        if (activity == null) {
            mBannerListener.onBannerFailed(MoPubErrorCode.UNSPECIFIED);
            TLog.e("Activity is null");
            return;
        }

        Map<String, String> env = ThirdpresenceCustomEventHelper.setEnvironment(serverExtras);
        Map<String, String> params = ThirdpresenceCustomEventHelper.setPlayerParameters(activity, serverExtras);

        mPlacementId = env.get(VideoAd.Environment.KEY_PLACEMENT_ID);

        if (mPlacementId == null) {
            mBannerListener.onBannerFailed(MoPubErrorCode.UNSPECIFIED);
            TLog.e("Placement id is null");
            return;
        }

        mBannerView = new BannerView(context);
        VideoBanner banner = (VideoBanner)VideoAdManager.getInstance().create(VideoAd.PLACEMENT_TYPE_BANNER, mPlacementId);
        banner.setListener(this);
        banner.init(activity, mBannerView, env, params, VideoAd.DEFAULT_TIMEOUT);
        banner.loadAd();
    }

    /**
     * From CustomEventBanner
     */
    @Override
    protected void onInvalidate() {
        if (mPlacementId != null) {
            VideoAdManager.getInstance().remove(mPlacementId);
            mPlacementId = null;
        }

        mBannerView = null;
        mAdLoaded = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAdEvent(String eventName, String arg1, String arg2, String arg3) {
        if (mBannerListener != null ) {
            if (eventName.equals(VideoAd.Events.AD_LOADED)) {
                mAdLoaded = true;
                mBannerListener.onBannerLoaded(mBannerView);
            } else if (eventName.equals(VideoAd.Events.AD_ERROR)) {
                if (mAdLoaded) {
                    mBannerListener.onBannerFailed(MoPubErrorCode.INTERNAL_ERROR);
                    TLog.e("An error occurred while playing");
                } else {
                    mBannerListener.onBannerFailed(MoPubErrorCode.NO_FILL);
                    TLog.e("An error occurred while loading an ad");
                }
            } else if (eventName.equals(VideoAd.Events.AD_CLICKTHRU)) {
                mBannerListener.onBannerClicked();
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
            MoPubErrorCode moPubErrorCode = ThirdpresenceCustomEventHelper.mapErrorCode(errorCode);
            mBannerListener.onBannerFailed(moPubErrorCode);
        }
    }
}
