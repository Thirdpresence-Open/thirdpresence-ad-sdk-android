package com.thirdpresence.adsdk.sdk;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * <h1>VideoAdManager</h1>
 *
 * VideoAdManager is a singleton class that can be used to create and get access to
 * VideoAd instances. VideoAd instances are separated by placementId. The app can
 * have multiple ad placements and they can have different ads loaded at the same
 * time. However, beware that it may consume significant amount of memory to have
 * several VideoAd instances and ad loaded in each of them.
 *
 */
public class VideoAdManager {

    private Map<String, VideoAd> mVideoAds;
    private static VideoAdManager ourInstance = new VideoAdManager();

    /**
     * Private constructor
     */
    private VideoAdManager() {
        mVideoAds = new HashMap<String, VideoAd>();
    }

    /**
     * Gets singleton instance of the VideoAdManager
     *
     * @return VideoAdManager singleton instance
     */
    public static VideoAdManager getInstance() {
        return ourInstance;
    }

    /**
     * Creates VideoAd instance
     * @param placementType type of the placement. The value can be either
     *                      VideoAd.PLACEMENT_TYPE_INTERSTITIAL or
     *                      VideoAd.PLACEMENT_TYPE_REWARDED_VIDEO
     * @param placementId id of the placement
     * @return VideoAd instance of the specified placement type
     */
    public VideoAd create(String placementType, String placementId) {
        if (mVideoAds.containsKey(placementId)) {
            return mVideoAds.get(placementId);
        }

        if (placementType == null) {
            throw new IllegalArgumentException("placementType cannot be null");
        } else if (placementId == null) {
            throw new IllegalArgumentException("placementId cannot be null");
        }

        VideoAd videoAd;
        if (placementType.contentEquals(VideoAd.PLACEMENT_TYPE_INTERSTITIAL)) {
            videoAd = new VideoInterstitial(placementId);
        } else if (placementType.contentEquals(VideoAd.PLACEMENT_TYPE_REWARDED_VIDEO)) {
            videoAd = new RewardedVideo(placementId);
        } else if (placementType.contentEquals(VideoAd.PLACEMENT_TYPE_BANNER)) {
            videoAd = new VideoBanner(placementId);
        } else {
            throw new IllegalArgumentException(placementType + " is not valid placement type");
        }

        if (videoAd != null) {
            mVideoAds.put(placementId, videoAd);
        } else {
            throw new IllegalStateException("Could not create VideoAd object");
        }

        return videoAd;
    }

    /**
     * Gets VideoAd instance by placement id
     * @param placementId id of the placement
     * @return VideoAd instance or null if not found
     */
    public VideoAd get(String placementId) {
        if (mVideoAds.containsKey(placementId)) {
            return mVideoAds.get(placementId);
        }
        return null;
    }

    /**
     * Removes VideoAd instance by placement id
     * @param placementId id of the placement
     */
    public void remove(String placementId) {
        if (mVideoAds.containsKey(placementId)) {
            VideoAd ad = mVideoAds.get(placementId);
            ad.setListener(null);
            ad.remove();
            mVideoAds.remove(placementId);
        }
    }

    /**
     * Removes all VideoAd instances
     */
    public void clear() {
        Iterator it = mVideoAds.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<?,?> pair = (Map.Entry)it.next();
            VideoAd ad = (VideoAd)pair.getValue();
            ad.setListener(null);
            ad.remove();
            it.remove();
        }
    }
}
