package com.thirdpresence.adsdk.sdk.internal;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import com.thirdpresence.adsdk.sdk.VideoAd;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

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

    private boolean mActivityRunning = false;
    private boolean mPlayerReady = false;
    private boolean mPlayerLoading = false;
    private boolean mInitialised = false;
    private boolean mAdLoading = false;
    private boolean mAdLoaded = false;
    private boolean mAdLoadingPending = false;
    private boolean mAdDisplaying = false;
    private boolean mVideoClicked = false;
    private boolean mWebViewPaused = false;
    private boolean mOrientationChanged = false;
    private boolean mPendingLocationUpdate = false;

    private Object mWebAdTracker;

    private HandlerThread mLocationHandler;
    
    private int mOriginalOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

    private static final String LOCATION_PERMISSION_WARNING = "Location permission not granted. Consider adding ACCESS_COARSE_LOCATION permission to app's AndroidManifest.xml";
    private static final String LOCATION_PERMISSION_WARNING_V6 = LOCATION_PERMISSION_WARNING + "\n" + "Beginning in Android 6.0 the location permission must be explicitly granted by user while app is running";

    private static final long LOCATION_EXPIRATION_LIMIT_IN_SECONDS = 3600;

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

        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            throw new IllegalThreadStateException("init() is not called from UI thread");
        }

        mInitialised = true;

        mActivity = activity;
        mActivityRunning = true;
        mEnv = environment;
        mParams = params;
        mInitTimeout = timeout;
        mLoadTimeout = timeout;

        mActivity.getApplication().registerActivityLifecycleCallbacks(this);

        if (checkLocationPermissions(mActivity)) {
            Location loc = getLocation(mActivity);
            if (loc != null) {
                mParams.put(VideoAd.Parameters.KEY_GEO_LAT, String.valueOf(loc.getLatitude()));
                mParams.put(VideoAd.Parameters.KEY_GEO_LON, String.valueOf(loc.getLongitude()));
            }
        }

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
        mWebView.setBackAllowed(!VideoAd.parseBoolean(environment.get(VideoAd.Environment.KEY_DISABLE_BACK_BUTTON), false));

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
        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            throw new IllegalThreadStateException("reset() is not called from UI thread");
        }

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

        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            throw new IllegalThreadStateException("close() is not called from UI thread");
        }

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
                mWebView.stopLoading();
                mWebView.destroy();
                mWebView = null;
            }

            if (mWebAdTracker != null) {
                mWebAdTracker = null;
            }

            mActivity = null;
            mInitialised = false;
        }

        if (mLocationHandler != null) {
            mLocationHandler.quit();
            mLocationHandler = null;
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
        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            throw new IllegalThreadStateException("loadAd() is not called from UI thread");
        }

        if (mContainer == null || mWebView == null) {
            if (mListener != null) {
                TLog.d("The ad unit is not initialised");
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
                                TLog.d("Timeout occured while loading an ad");
                                mListener.onError(VideoAd.ErrorCode.NETWORK_TIMEOUT, "Timeout occured while loading an ad");
                            }
                            if (mWebView != null) {
                                mWebView.stopLoading();
                            }
                            mAdLoading = false;

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
        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            throw new IllegalThreadStateException("displayAd() is not called from UI thread");
        }

        if (!mActivityRunning) {
            throw new IllegalStateException("Trying to display an ad while the containing activity is not running");
        }


        if (mContainer != null && mWebView != null) {
            if (mAdLoaded) {
                if (!mAdDisplaying) {
                    mAdDisplaying = true;
                    mOriginalOrientation = mActivity.getRequestedOrientation();
                    if (VideoAd.parseBoolean(mEnv.get(VideoAd.Environment.KEY_FORCE_LANDSCAPE), false)) {
                        mOrientationChanged = true;
                        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    } else if (VideoAd.parseBoolean(mEnv.get(VideoAd.Environment.KEY_FORCE_PORTRAIT), false)) {
                        mOrientationChanged = true;
                        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }
                    mContainer.setVisibility(View.VISIBLE);

                    mContainer.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mWebView != null) {
                                mWebView.displayAd();
                            }
                        }
                    }, 100);

                }
            } else {
                mAdLoaded = false;
                if (mListener != null ) {
                    TLog.d("An ad not available yet");
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
                    TLog.d("Player failure: account not set");
                    mListener.onError(VideoAd.ErrorCode.PLAYER_INIT_FAILED, "Cannot init the player. Account not set");
                }
            } else if (!mEnv.containsKey(VideoAd.Environment.KEY_ACCOUNT)) {
                if (mListener != null ) {
                    TLog.d("Player failure: placement id not set");
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
                mPlayerLoading = true;
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
        mPlayerLoading = false;
        mVideoClicked = false;
        
        if (mWebView != null && mWebViewPaused){
            mWebView.onResume();
            mWebView.resumeTimers();
            mWebViewPaused = false;
        }

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
                if (mActivity != null) {
                    mActivity.runOnUiThread(runnable);
                }
            }
        }, timeout);
    }

    /**
     * This function moves the player container to new activity, in case the application
     * does not allow playing the video in it's own activity.
     */
    public void switchActivity(Activity newActivity) {
        ViewGroup vg = (ViewGroup)mContainer.getParent();
        vg.setLayoutTransition(null);
        vg.removeView(mContainer);
        vg.invalidate();
        ViewGroup root = (ViewGroup) newActivity.getWindow().getDecorView().getRootView();
        root.addView(mContainer);

        mActivity = newActivity;
        mActivityRunning = true;
    }

    /**
     * Helper function for checking if permissions for locations are granted
     *
     * @param context
     * @return true if either ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION
     * permission is available, otherwise false
     */
    public boolean checkLocationPermissions(Context context) {

        boolean permissionAvailable = false;
        int permission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (permission == PackageManager.PERMISSION_GRANTED) {
            permissionAvailable = true;
        } else {
            permission = ContextCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permission == PackageManager.PERMISSION_GRANTED) {
                permissionAvailable = true;
            } else {
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
                    TLog.w(LOCATION_PERMISSION_WARNING_V6);
                } else {
                    TLog.w(LOCATION_PERMISSION_WARNING);
                }
            }
        }

        return permissionAvailable;
    }

    /**
     * Helper function for getting a Location object
     *
     * @param c context
     * @return Location object containing the location of the device or null
     */
    private Location getLocation(Context c) {
        Location loc = null;

        try {
            LocationManager lm = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
            List<String> providers = lm.getProviders(true);
            String bestProvider = null;
            if (providers != null && !providers.isEmpty()) {
                if (providers.contains(LocationManager.GPS_PROVIDER)) {
                    //noinspection ResourceType
                    loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    bestProvider = LocationManager.GPS_PROVIDER;
                }
                if (loc == null && providers.contains(LocationManager.NETWORK_PROVIDER)) {
                    //noinspection ResourceType
                    loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    bestProvider = LocationManager.NETWORK_PROVIDER;
                }
                if (loc == null && providers.contains(LocationManager.PASSIVE_PROVIDER)) {
                    //noinspection ResourceType
                    loc = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                    bestProvider = LocationManager.PASSIVE_PROVIDER;
                }
            }

            bestProvider = LocationManager.GPS_PROVIDER;
            if (bestProvider != null) {
                // Start location update if most recent update older than the expiration limit
                long expiration = 0;
                long locTime = 0;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    locTime = loc.getElapsedRealtimeNanos();
                    expiration = SystemClock.elapsedRealtimeNanos() -
                            TimeUnit.NANOSECONDS.convert(LOCATION_EXPIRATION_LIMIT_IN_SECONDS, TimeUnit.SECONDS);
                }

                if (locTime < expiration) {

                    if (mLocationHandler == null) {
                        mLocationHandler = new HandlerThread("TPR Ad SDK Location handler thread");
                        mLocationHandler.start();
                    }
                    LocationListener listener = new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            if (location != null) {
                                final Location newLocation = location;
                                mActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mPlayerReady) {
                                            updateLocationToPlayer(newLocation);
                                        } else if (mPlayerLoading) {
                                            mPendingLocationUpdate = true;
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onStatusChanged(String s, int i, Bundle bundle) {
                        }

                        @Override
                        public void onProviderEnabled(String s) {
                        }

                        @Override
                        public void onProviderDisabled(String s) {
                        }
                    };

                    //noinspection ResourceType
                    lm.requestSingleUpdate(bestProvider, listener, mLocationHandler.getLooper());
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return loc;
    }

    /**
     * Updates the geo location to the player
     *
     * @param location object to hold geo coordinates
     */
    private void updateLocationToPlayer(Location location) {
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            TLog.d("Updating geo location: " + latitude + "," + longitude);

            if (mWebView != null) {
                mWebView.updateLocation(String.valueOf(latitude), String.valueOf(longitude));
            }
        }
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
            if (success) {
                TLog.i("MOAT SDK enabled");
            } else {
                TLog.i("MOAT SDK failed to initialize");
            }
        } catch (Exception e) {
            // MOAT SDK not available, will continue without tracking
            success = true;
            TLog.i("MOAT SDK is not available");
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
        mPlayerLoading = false;
        mPlayerReady = true;

        if (mInitTimeoutTimer != null) {
            mInitTimeoutTimer.cancel();
            mInitTimeoutTimer = null;
        }

        if (mPendingLocationUpdate) {
            Location location = getLocation(mActivity);
            if (location != null) {
                updateLocationToPlayer(location);
            }
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
        TLog.d("Network failure " + statusCode + ":" + description);
        mListener.onError(VideoAd.ErrorCode.NETWORK_FAILURE, description);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPlayerFailure(VideoAd.ErrorCode errorCode, String errorText) {
        TLog.d("Player failure " + errorCode + ":" + errorText);
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
        TLog.d("An ad event occurred: " + eventName + ":" + arg1 + ":" + arg2 + ":" + arg3);
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
        if (activity == mActivity) {
            TLog.d("Activity started");
            mActivityRunning = true;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityResumed(Activity activity) {
        if (activity == mActivity) {
            TLog.d("Activity resumed");
            mActivityRunning = true;

            if(mWebView != null && mWebViewPaused){
                mWebView.onResume();
                mWebView.resumeTimers();
                mWebViewPaused = false;
                mWebView.resumeAd();
            }

            mVideoClicked = false;

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityPaused(Activity activity) {
        if (activity == mActivity) {
            TLog.d("Activity paused");
            mActivityRunning = false;

            if(mWebView != null && mAdDisplaying){
                mWebView.pauseAd();
                mWebView.onPause();
                mWebView.pauseTimers();
                mWebViewPaused = true;
            }

            if (mVideoClicked && mListener != null) {
                mListener.onAdEvent(VideoAd.Events.AD_LEFT_APPLICATION, null, null, null);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityStopped(Activity activity) {
        if (activity == mActivity) {
            TLog.d("Activity stopped");
            mActivityRunning = false;
        }
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
        if (activity == mActivity) {
            TLog.d("Activity destroyed");
            mActivityRunning = false;
            close();
        }
    }
}
