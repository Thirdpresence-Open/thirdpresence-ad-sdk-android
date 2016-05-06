package com.thirdpresence.adsdk.sdk.internal;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import com.thirdpresence.adsdk.sdk.VideoAd;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * VideoPlayer class creates and WebView and loads Thirdpresence HTML5 ad player on it.
 * It provides means to load and display video ads.
 *
 */
public class VideoPlayer implements VideoWebView.Listener, Application.ActivityLifecycleCallbacks {

    private VideoAd.Listener mListener;

    private Activity mActivity;
    private RelativeLayout mContainer;
    private VideoWebView mWebView;

    private Map<String,String> mEnv;
    private Map<String,String> mParams;

    private long mInitTimeout = VideoAd.DEFAULT_TIMEOUT;
    private long mLoadTimeout = VideoAd.DEFAULT_TIMEOUT;

    private Timer mInitTimeoutTimer;
    private Timer mLoadTimeoutTimer;

    private String mDeviceId;
    private boolean mPlayerReady = false;
    private boolean mInitialised = false;
    private boolean mAdLoading = false;
    private boolean mAdLoaded = false;
    private boolean mAdLoadingPending = false;
    private boolean mAdDisplaying = false;
    private boolean mVideoClicked = false;
    private boolean mOrientationChanged = false;

    private Object mWebAdTracker;

    private int mOriginalOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

    /**
     * Sets listener for callback events
     *
     * @param listener An object implementing the interface
     *
     */
    public void setListener(VideoAd.Listener listener) {
        mListener = listener;
    }

    /**
     * Inits the player
     *
     * @param activity The container activity where the intertitial is displayed
     * @param environment Environment parameters
     *                    @see com.thirdpresence.adsdk.sdk.VideoAd.Environment for details
     *                    Mandatory parameters: KEY_ACCOUNT and KEY_PLACEMENT_ID
     *
     * @param params VideoAd parameters
     *                  @see com.thirdpresence.adsdk.sdk.VideoAd.Parameters for details
     *
     * @param timeout Timeout for setting up the player in milliseconds
     *
     */
    public void init(Activity activity,
                     Map<String, String> environment,
                     Map<String, String> params,
                     long timeout) {

        if (mInitialised) {
            throw new IllegalStateException("Already initialised");
        }

        mInitialised = true;

        mActivity = activity;
        mEnv = environment;
        mParams = params;
        mLoadTimeout = timeout;

        mActivity.getApplication().registerActivityLifecycleCallbacks(this);

        if (mContainer != null) {
            close();
        }

        if (!environment.containsKey(VideoAd.Environment.KEY_SERVER)) {
            mEnv.put(VideoAd.Environment.KEY_SERVER, VideoAd.SERVER_TYPE_PRODUCTION);
        }

        if (!params.containsKey(VideoAd.Parameters.KEY_BUNDLE_ID)) {
            mParams.put(VideoAd.Parameters.KEY_BUNDLE_ID, activity.getApplicationContext().getPackageName());
        }

        mWebView = new VideoWebView(mActivity);
        mWebView.setListener(this);
        mWebView.setBackAllowed(!parseBoolean(environment.get(VideoAd.Environment.KEY_DISABLE_BACK_BUTTON), false));

        ViewGroup root = (ViewGroup) activity.getWindow().getDecorView().getRootView();

        mContainer = new RelativeLayout(activity);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        mContainer.addView(mWebView, layoutParams);
        mContainer.setVisibility(View.GONE);
        mContainer.setFocusable(true);
        mContainer.setFocusableInTouchMode(true);
        mContainer.requestFocus();

        root.addView(mContainer, layoutParams);

        AdInfoRetriever retriever = new AdInfoRetriever();
        retriever.setContext(activity);
        retriever.execute();
    }

    /**
     * Resets the state and re-init the players
     */
    public void reset() {
        if (mContainer != null) {
            mContainer.setVisibility(View.GONE);
            resetState();
            initPlayer();
        }
    }

    /**
     * Closes the ad view and releases resources.
     */
    public void close() {
        if (mActivity != null) {
            mActivity.getApplication().unregisterActivityLifecycleCallbacks(this);

            resetState();

            if (mContainer != null) {
                if (mContainer.getVisibility() == View.VISIBLE) {
                    mContainer.setVisibility(View.GONE);
                }
                ((ViewGroup) mContainer.getParent()).removeView(mContainer);
                mContainer.removeAllViews();
                mContainer = null;
            }
            if (mWebView != null) {
                mWebView.setListener(null);
                mWebView.destroy();
                mWebView = null;
            }

            if (mWebAdTracker != null) {
                mWebAdTracker = null;
            }

            mActivity = null;
            mInitialised = false;
        }
    }

    /**
     * Check if player is ready
     */
    public boolean isReady() {
        return mPlayerReady;
    }

    /**
     * Loads an ad. Listener.onAdEvent() is called with AD_LOADED eventName when the ad is loaded
     */
    public void loadAd() {
        if (mContainer == null || mWebView == null) {
            if (mListener != null) {
                mListener.onError(VideoAd.ErrorCode.INVALID_STATE, "The ad unit is not initialised");
            }
        } else if (mPlayerReady) {
            mAdLoadingPending = false;
            if (!mAdLoading) {
                mAdLoading = true;
                mLoadTimeoutTimer = new Timer();
                setTimeout(mLoadTimeoutTimer, mLoadTimeout, new Runnable() {
                    @Override
                    public void run() {
                        if (mAdLoading) {
                            if (mListener != null) {
                                mListener.onError(VideoAd.ErrorCode.NETWORK_TIMEOUT, "Timeout occured while loading an ad");
                            }
                        }
                    }
                });
                mWebView.loadAd();
            }
        } else {
            mAdLoadingPending = true;
        }
    }

    /**
     * Display the ad view and starts playing the video
     */
    public void displayAd() {
        if (mContainer != null && mWebView != null) {
            if (mAdLoaded) {
                if (!mAdDisplaying) {
                    mAdDisplaying = true;
                    mOriginalOrientation = mActivity.getRequestedOrientation();
                    if (parseBoolean(mEnv.get(VideoAd.Environment.KEY_FORCE_LANDSCAPE), false)) {
                        mOrientationChanged = true;
                        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    } else if (parseBoolean(mEnv.get(VideoAd.Environment.KEY_FORCE_PORTRAIT), false)) {
                        mOrientationChanged = true;
                        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }
                    mContainer.setVisibility(View.VISIBLE);
                    mWebView.displayAd();
                }
            } else {
                mAdLoaded = false;
                if (mListener != null ) {
                    mListener.onError(VideoAd.ErrorCode.AD_NOT_READY, "An ad not available yet.");
                }
            }
        }
    }

    /**
     * Checks if an ad is loaded
     *
     * @return true if loaded, false otherwise
     */
    public boolean isAdLoaded() {
        return mAdLoaded;
    }

    /**
     * Checks if the player is ready
     *
     * @return true if ready, false otherwise
     */
    public boolean isPlayerReady() {
        return mPlayerReady;
    }

    /**
     * Inits the video player
     */
    private void initPlayer() {
        if (mWebView != null) {

            if (!mEnv.containsKey(VideoAd.Environment.KEY_ACCOUNT)) {
                if (mListener != null ) {
                    mListener.onError(VideoAd.ErrorCode.PLAYER_INIT_FAILED, "Cannot init the player. Account not set");
                }
            } else if (!mEnv.containsKey(VideoAd.Environment.KEY_ACCOUNT)) {
                if (mListener != null ) {
                    mListener.onError(VideoAd.ErrorCode.PLAYER_INIT_FAILED,  "Cannot init the player. Placement id not set");
                }
            } else {

                mInitTimeoutTimer = new Timer();
                setTimeout(mInitTimeoutTimer, mInitTimeout, new Runnable() {
                    @Override
                    public void run() {
                        close();

                        if (mListener != null) {
                            mListener.onError(VideoAd.ErrorCode.NETWORK_TIMEOUT, "Timeout occured while initialising the player");
                        }

                    }
                });

                if (!mParams.containsKey(VideoAd.Parameters.KEY_DEVICE_ID) && mDeviceId != null) {
                    mParams.put(VideoAd.Parameters.KEY_DEVICE_ID, mDeviceId);
                }

                mParams.put(VideoAd.Parameters.KEY_AD_PLACEMENT, VideoAd.PLACEMENT_TYPE_INTERSTITIAL);
                mWebView.initPlayer(mEnv, mParams);
            }
        }
    }

    /**
     * Reset state variables
     */
    private void resetState() {
        if (mActivity != null) {
            int currentOrientation = mActivity.getRequestedOrientation();
            if (mOrientationChanged && currentOrientation != mOriginalOrientation) {
                mActivity.setRequestedOrientation(mOriginalOrientation);
            }
        }

        mAdLoadingPending = false;
        mAdLoading = false;
        mAdLoaded = false;
        mAdDisplaying = false;
        mPlayerReady = false;
        mVideoClicked = false;
        if (mInitTimeoutTimer != null) {
            mInitTimeoutTimer.cancel();
            mInitTimeoutTimer = null;
        }
        if (mLoadTimeoutTimer != null) {
            mLoadTimeoutTimer.cancel();
            mLoadTimeoutTimer = null;
        }
    }

    /**
     * Helper function for setting timeout timer and handle task in main thread
     *
     * @param timer timer to schedule
     * @param timeout timeout in milliseconds
     * @param runnable Runnable that will be run when timeout occurs
     *
     */
    private void setTimeout(Timer timer, long timeout, final Runnable runnable) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                mActivity.runOnUiThread(runnable);
            }
        }, timeout);
    }

    /**
     * Helper function for parsing boolean from a string
     *
     * @param booleanString string that contains boolean
     * @param defaultVal defaultVal if boolean cannot be parse
     * @return boolean parsed from the string or defaultVal
     *
     */
    private boolean parseBoolean(String booleanString, boolean defaultVal) {
        boolean ret = defaultVal;
        try {
            if (booleanString != null) {
                ret = Boolean.parseBoolean(booleanString);
            }
        } catch (NumberFormatException e) {
            ret = defaultVal;
        }
        return ret;
    }

    /**
     * AsyncTask for retrieve the advertising ID of the device
     */
    private class AdInfoRetriever extends AsyncTask<Void, Void, Void> {
        private Activity mActivity;
        public void setContext(Activity activity) {
            mActivity = activity;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // Using Google Play Services is optional
                Class<?> idClientClass = Class.forName("com.google.android.gms.ads.identifier.AdvertisingIdClient");
                Method getIdInfoMethod = idClientClass.getMethod("getAdvertisingIdInfo", Context.class);
                Object idInfo = getIdInfoMethod.invoke(null, mActivity);
                Method getIdMethod = idInfo.getClass().getMethod("getId", (Class<?>[]) null);
                mDeviceId = (String) getIdMethod.invoke(idInfo, (Object[]) null);
            } catch (Exception e) {
                // Google Play Services not available
                mDeviceId = null;
            }
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setAdTracker();
                }
            });
            return null;
        }
    }

    /**
     * Setup MOAT Ad tracker if SDK available
     */
    private void setAdTracker() {
        boolean success;
        try {
            // Using MOAT Ad tracker is optional
            Class<?> factoryClass = Class.forName("com.moat.analytics.mobile.MoatFactory");
            Method createMethod = factoryClass.getMethod("create", Activity.class);
            Object factory = createMethod.invoke(null, mActivity);
            Method createWebAdTrackerMethod = factory.getClass().getMethod("createWebAdTracker", WebView.class);
            mWebAdTracker = createWebAdTrackerMethod.invoke(factory, mWebView);
            Class<?> trackerClass = Class.forName("com.moat.analytics.mobile.WebAdTracker");
            Method trackMethod = trackerClass.getMethod("track", (Class<?>[]) null);
            success = (Boolean) trackMethod.invoke(mWebAdTracker, (Object[]) null);
        } catch (Exception e) {
            // MOAT SDK not available, will continue without tracking
            success = true;
        }

        if (success) {
            initPlayer();
        }
        else {
            mListener.onError(VideoAd.ErrorCode.PLAYER_INIT_FAILED, "Setting up ad tracker failed");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPlayerReady() {
        mPlayerReady = true;
        if (mInitTimeoutTimer != null) {
            mInitTimeoutTimer.cancel();
            mInitTimeoutTimer = null;
        }

        if (mListener != null ) {
            mListener.onPlayerReady();
        }

        if (mAdLoadingPending) {
            loadAd();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNetworkError(int statusCode, String description) {
        mListener.onError(VideoAd.ErrorCode.NETWORK_FAILURE, description);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPlayerFailure(VideoAd.ErrorCode errorCode, String errorText) {
        close();
        if (mListener != null ) {
            mListener.onError(VideoAd.ErrorCode.PLAYER_INIT_FAILED, errorText);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onOpenURLIntercepted(String url) {
        try {
            mVideoClicked = true;
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            mActivity.startActivity(browserIntent);


        } catch (android.content.ActivityNotFoundException e) {
            mVideoClicked = false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAdEvent(String eventName, String arg1, String arg2, String arg3) {

        if (eventName.equals(VideoAd.Events.AD_LOADED)) {
            if (mLoadTimeoutTimer != null) {
                mLoadTimeoutTimer.cancel();
                mLoadTimeoutTimer = null;
            }
            mAdLoaded = true;
        }

        if (mListener != null ) {
            mListener.onAdEvent(eventName, arg1, arg2, arg3);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityStarted(Activity activity) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityResumed(Activity activity) {
        if (activity == mActivity && mAdLoaded && mVideoClicked) {
            mVideoClicked = false;
            mWebView.displayAd();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityPaused(Activity activity) {
        if (activity == mActivity && mVideoClicked) {
            if (mListener != null ) {
                mListener.onAdEvent(VideoAd.Events.AD_LEFT_APPLICATION, null, null, null);
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityStopped(Activity activity) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityDestroyed(Activity activity) {
        close();
    }

}
