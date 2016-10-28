package com.thirdpresence.adsdk.sdk;

/**
 * <h1>RewardedVideo</h1>
 *
 * @see com.thirdpresence.adsdk.sdk.VideoInterstitial for details
 *
 */
public class RewardedVideo extends VideoInterstitial {

    /**
     * Constructor
     */
    public RewardedVideo() {
        super(PLACEMENT_TYPE_REWARDED_VIDEO, "");
    }

    /**
     * Constructor
     */
    public RewardedVideo(String placementId) {
        super(PLACEMENT_TYPE_REWARDED_VIDEO, placementId);
    }

}
