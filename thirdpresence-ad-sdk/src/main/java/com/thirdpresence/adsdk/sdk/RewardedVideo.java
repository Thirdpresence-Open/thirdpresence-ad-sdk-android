package com.thirdpresence.adsdk.sdk;

import android.app.Activity;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Map;

/**
 * <h1>RewardedVideo</h1>
 *
 * @see com.thirdpresence.adsdk.sdk.VideoInterstitial for details
 *
 */
public class RewardedVideo extends VideoInterstitial {

    private String mRewardTitle;
    private Number mRewardAmount;

    /**
     * Constructor
     */
    @Deprecated
    public RewardedVideo() {
        super(PLACEMENT_TYPE_REWARDED_VIDEO, "");
    }

    /**
     * Constructor
     */
    public RewardedVideo(String placementId) {
        super(PLACEMENT_TYPE_REWARDED_VIDEO, placementId);
    }

    /**
     * Inits the ad unit
     *
     * @param activity The container activity where the interstitial is displayed
     * @param environment Environment parameters.
     *                    @see VideoAd.Environment for details
     *                    Mandatory parameters: KEY_ACCOUNT and KEY_PLACEMENT_ID
     * @param params VideoAd parameters
     *               @see VideoAd.Parameters for details
     * @param timeout Timeout for setting up the player in milliseconds
     *
     */
    @Override
    public void init(Activity activity,
                     Map<String, String> environment,
                     Map<String, String> params,
                     long timeout){
        mRewardTitle = null;
        mRewardAmount = 0;

        if (environment.containsKey(Environment.KEY_REWARD_TITLE)) {
            mRewardTitle = environment.get(Environment.KEY_REWARD_TITLE);
        } else {

            throw new AssertionError("Environment must contain reward title data");
        }

        if (environment.containsKey(Environment.KEY_REWARD_AMOUNT)) {
            String rewardAmountStr = environment.get(Environment.KEY_REWARD_AMOUNT);
            try {
                if (rewardAmountStr != null) {
                    mRewardAmount = NumberFormat.getInstance().parse(rewardAmountStr);
                }
            } catch (ParseException e) {
                throw new AssertionError("Reward amount must be numeric value");
            }
        } else {
            throw new AssertionError("Environment must contain reward amount data");
        }
        super.init(activity, environment, params, timeout);
    }

    /**
     * Gets reward title
     *
     * @return reward title
     */
    @Override
    public String getRewardTitle() {
        return mRewardTitle;
    }

    /**
     * Gets reward amount
     *
     * @return reward amount
     */
    @Override
    public Number getRewardAmount() {
        return mRewardAmount;
    }
}
