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
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.moat.analytics.mobile.trdp.MoatAnalytics;
import com.moat.analytics.mobile.trdp.MoatFactory;
import com.moat.analytics.mobile.trdp.WebAdTracker;

import com.thirdpresence.adsdk.sdk.VideoAd;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static com.thirdpresence.adsdk.sdk.VideoAd.Environment.KEY_MOAT_AD_TRACKING;

/**
 * <h1>VideoPlayer</h1>
 *
 * VideoPlayer class creates and WebView and loads Thirdpresence HTML5 ad player on it.
 * It provides means to load and display video ads.
 *
 */
public class VideoPlayer implements Application.ActivityLifecycleCallbacks, SystemVolumeManager.ChangeListener {

    private VideoAd.Listener mListener;

    private Activity mActivity;
    private Activity mActiveActivity = null;

    private Application mApplication;
    private RelativeLayout mContainer;
    private VideoWebView mWebView;

    private Map<String,String> mEnv;
    private Map<String,String> mParams;

    private long mInitTimeout = VideoAd.DEFAULT_TIMEOUT;
    private long mLoadTimeout = VideoAd.DEFAULT_TIMEOUT;

    private Timer mInitTimeoutTimer;
    private Timer mLoadTimeoutTimer;

    private String mDeviceId;
    private String mPlacementId;
    private String mPlacementType;

    private boolean mUsingPlayerActivity = false;
    private boolean mAdLoadingPending = false;
    private boolean mVideoCompleted = false;
    private boolean mVideoClicked = false;
    private boolean mWebViewPaused = false;
    private boolean mOrientationChanged = false;
    private boolean mPendingLocationUpdate = false;
    private boolean mDisplayImmediately = false;

    private WebAdTracker mWebAdTracker;

    private HandlerThread mLocationHandler;
    private LocationListener mLocationListener;

    private SystemVolumeManager mSystemVolumeManager;

    private int mOriginalOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

    private static final String LOCATION_PERMISSION_WARNING = "Location permission not granted. Consider adding ACCESS_COARSE_LOCATION permission to app's AndroidManifest.xml";
    private static final String LOCATION_PERMISSION_WARNING_V6 = LOCATION_PERMISSION_WARNING + "\n" + "Beginning in Android 6.0 the location permission must be explicitly granted by user while app is running";

    private static final long LOCATION_EXPIRATION_LIMIT_IN_SECONDS = 3600;

    public enum State {
        IDLE,
        INITIALISING,
        INITIALIZED,
        LOADING,
        LOADED,
        DISPLAYING,
        STOPPED,
        ERROR
    }
    private State mPlayerState = State.IDLE;

    class StateChangeRunnableInfo {
        public Runnable runOnStateChange;
        public State previousState;
        public State nextState;

        public StateChangeRunnableInfo(Runnable runnable, State fromState, State toState)  {
            runOnStateChange = runnable;
            previousState = fromState;
            nextState = toState;
        }
    }

    private ArrayList<StateChangeRunnableInfo> mRunnables = new ArrayList<>();

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
     * Sets the player to display the ad immediately and not waiting displayAd() call.
     *
     * @param enabled true if ad to be displayed immediately
     *
     */
    public void setDisplayImmediately(boolean enabled) {
        mDisplayImmediately = enabled;
    }

    /**
     * Inits the player
     *
     * @param activity The container activity where the intertitial is displayed
     * @param environment Environment parameters
     *                    @see com.thirdpresence.adsdk.sdk.VideoAd.Environment for details
     *                    Mandatory parameters: KEY_ACCOUNT and KEY_PLACEMENT_ID
     * @param params VideoAd parameters
     *                  @see com.thirdpresence.adsdk.sdk.VideoAd.Parameters for details
     * @param timeout Timeout for setting up the player in milliseconds
     * @param placementId placement id of the placement the player is attached to
     */
    public void init(Activity activity,
                     ViewGroup rootLayout,
                     Map<String, String> environment,
                     Map<String, String> params,
                     long timeout,
                     String placementId,
                     String placementType) {

        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            throw new IllegalThreadStateException("init() is not called from UI thread");
        }

        if (mPlayerState == State.INITIALISING) {
            TLog.w("already initialising");
            return;
        }

        changeState(State.INITIALISING);

        if (mContainer != null) {
            close();
        }

        mActivity = activity;
        mActiveActivity = activity;
        mApplication = activity.getApplication();
        mPlacementId = placementId;
        mPlacementType = placementType;
        mEnv = environment;
        mParams = params;
        mInitTimeout = timeout;
        mLoadTimeout = timeout;

        mApplication.registerActivityLifecycleCallbacks(this);

        mSystemVolumeManager = new SystemVolumeManager(mApplication, AudioManager.STREAM_MUSIC);
        mSystemVolumeManager.setListener(this);
        mSystemVolumeManager.startObserving();

        if (checkLocationPermissions(mApplication)) {
            Location loc = getLocation(mApplication);
            if (loc != null) {
                mParams.put(VideoAd.Parameters.KEY_GEO_LAT, String.valueOf(loc.getLatitude()));
                mParams.put(VideoAd.Parameters.KEY_GEO_LON, String.valueOf(loc.getLongitude()));
            }
        }
        
        if (!environment.containsKey(VideoAd.Environment.KEY_SERVER)) {
            mEnv.put(VideoAd.Environment.KEY_SERVER, VideoAd.SERVER_TYPE_PRODUCTION);
        }

        if (!params.containsKey(VideoAd.Parameters.KEY_BUNDLE_ID)) {
            mParams.put(VideoAd.Parameters.KEY_BUNDLE_ID, mApplication.getPackageName());
        }

        startAdTrackingAnalytics();

        mWebView = new VideoWebView(mApplication);
        mWebView.setHandler(mHandler);
        mWebView.setBackAllowed(!VideoAd.parseBoolean(environment.get(VideoAd.Environment.KEY_DISABLE_BACK_BUTTON), false));
        mWebView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (mPlacementType != VideoAd.PLACEMENT_TYPE_BANNER && (mPlayerState == State.STOPPED || mPlayerState == State.ERROR)) {
                    reset();
                    return true;
                }
                return false;
            }
        });

        ViewGroup root = rootLayout != null ? rootLayout : (ViewGroup) activity.getWindow().getDecorView().getRootView();

        mContainer = new RelativeLayout(mApplication);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        mContainer.addView(mWebView, layoutParams);
        mContainer.setVisibility(View.GONE);
        mContainer.setFocusable(true);
        mContainer.setFocusableInTouchMode(true);
        mContainer.requestFocus();

        root.addView(mContainer, layoutParams);

        AdInfoRetriever retriever = new AdInfoRetriever();
        retriever.setContext(mApplication);
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
            resetState(false);
            initPlayer();
        }
    }

    /**
     * Closes the ad view and releases resources.
     */
    public void close() {
        TLog.d("Releasing resources");

        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            throw new IllegalThreadStateException("close() is not called from UI thread");
        }

        if (mLocationListener != null) {
            LocationManager lm = (LocationManager) mApplication.getSystemService(Context.LOCATION_SERVICE);
            //noinspection ResourceType
            lm.removeUpdates(mLocationListener);
            mLocationListener = null;
        }

        if (mLocationHandler != null) {
            mLocationHandler.quit();
            mLocationHandler = null;
        }

        if (mSystemVolumeManager != null) {
            mSystemVolumeManager.setListener(null);
            mSystemVolumeManager.stopObserving();
            mSystemVolumeManager = null;
        }

        if (mApplication != null) {
            mApplication.unregisterActivityLifecycleCallbacks(this);
            mApplication = null;
        }

        resetState(true);

        if (mContainer != null) {
            if (mContainer.getVisibility() == View.VISIBLE) {
                mContainer.setVisibility(View.GONE);
            }
            ((ViewGroup) mContainer.getParent()).removeView(mContainer);
            mContainer.removeAllViews();
            mContainer = null;
        }

        if (mWebView != null) {
            mWebView.setHandler(null);
            mWebView.stopLoading();
            mWebView.destroy();
            mWebView = null;
        }

        mHandler.removeCallbacksAndMessages(null);

        mActivity = null;
        TLog.d("Player cleaned up");
    }

    /**
     * Loads an ad. Listener.onAdEvent() is called with AD_LOADED eventName when the ad is loaded
     */
    public void loadAd() {
        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            throw new IllegalThreadStateException("loadAd() is not called from UI thread");
        }

        if (mContainer == null || mWebView == null || mPlayerState == State.ERROR) {
            changeState(State.ERROR);
            if (mListener != null) {
                TLog.d("The ad unit is not initialised");
                mListener.onError(VideoAd.ErrorCode.INVALID_STATE, "The ad unit is not initialised");
            }
        } else if (mPlayerState == State.LOADING) {
            TLog.w("Already loading an ad");
        } else if (mPlayerState == State.INITIALISING) {
            TLog.d("Loading pending");
            mAdLoadingPending = true;
        } else if (mPlayerState == State.INITIALIZED) {
            TLog.d("Start loading ");
            mAdLoadingPending = false;
            changeState(State.LOADING);
            mLoadTimeoutTimer = new Timer();
            setTimeout(mLoadTimeoutTimer, mLoadTimeout, new Runnable() {
                @Override
                public void run() {
                    if (mPlayerState == State.LOADING) {
                        changeState(State.ERROR);
                        if (mListener != null) {
                            TLog.d("Timeout occurred while loading an ad");
                            mListener.onError(VideoAd.ErrorCode.NETWORK_TIMEOUT, "Timeout occurred while loading an ad");
                        }
                        if (mWebView != null) {
                            mWebView.stopLoading();
                        }
                    }
                }
            });
            mWebView.loadAd();

        } else {
            TLog.w("Invalid state, load request ignored");
        }
    }

    /**
     * Display the ad view and starts playing the video
     */
    public void displayAdInCurrentActivity() {
        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            throw new IllegalThreadStateException("displayAd() is not called from UI thread");
        }

        if (mPlayerState == State.DISPLAYING) {
            TLog.w("Already displaying an ad. Display request ignored");
            return;
        }

        displayAdInternal();
    }

    /**
     * Display the ad view and starts playing the video
     *
     * @param activity update the current activity where the ad is played
     * @param runnable to be executed after complete
     */
    public void displayAd(Activity activity, Runnable runnable) {
        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            throw new IllegalThreadStateException("displayAd() is not called from UI thread");
        }

        if (mPlayerState == State.DISPLAYING) {
            TLog.w("displayAd() ignored as called while already displaying an ad.");
            return;
        }

        if (mPlayerState == State.LOADED) {
            addStateChangeRunnable(State.DISPLAYING, null, runnable);
            if (activity != null) {
                switchActivity(activity);
                displayAdInternal();
            } else if (mActiveActivity != null) {
                mUsingPlayerActivity = true;
                Intent i = new Intent();
                i.setClass(mApplication, PlayerActivity.class);
                i.putExtra(PlayerActivity.PLACEMENT_ID_EXTRA_KEY, mPlacementId);
                mActiveActivity.startActivity(i);
            } else {
                TLog.w("Could not display player activity");
                changeState(State.ERROR);
            }
        } else {
            if (mListener != null) {
                TLog.d("Invalid state, ad is not loaded.");
                mListener.onError(VideoAd.ErrorCode.AD_NOT_READY, "No ad available yet.");
            }

            if (runnable != null) {
                if (mHandler != null)
                    mHandler.post(runnable);
            }
        }
    }

    /**
     * Displays the ad view and starts playing the video
     */
    private void displayAdInternal() {

        if (mActivity == null || mActivity != mActiveActivity) {
            if (mListener != null) {
                mListener.onError(VideoAd.ErrorCode.INVALID_STATE, "Activity is not active");
            }
            return;
        }

        if (mContainer == null) {
            if (mListener != null) {
                mListener.onError(VideoAd.ErrorCode.INVALID_STATE, "Container view does not exist");
            }
            return;
        }

        changeState(State.DISPLAYING);

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
                    startAdTracking();
                    mWebView.updateVolume(mSystemVolumeManager.getVolume());
                    mWebView.displayAd();
                }
            }
        }, 100);
    }

    /**
     * Pauses the playing ad
     */
    public void pauseAd() {
        mWebView.pauseAd();
    }

    /**
     * Resumes the paused ad
     */
    public void resumeAd() {
        mWebView.resumeAd();
    }

    /**
     * Checks if an ad is loaded
     *
     * @return true if loaded, false otherwise
     */
    public boolean isAdLoaded() {
        return mPlayerState == State.LOADED;
    }

    /**
     * Checks if the player is ready
     *
     * @return true if ready, false otherwise
     */
    public boolean isPlayerReady() {
        return mPlayerState != State.IDLE && mPlayerState != State.INITIALISING && mPlayerState != State.ERROR;
    }

    /**
     * Checks if the video has been completed
     *
     * @return true if completed, false otherwise
     */
    public boolean isVideoCompleted() {
        return mVideoCompleted;
    }

    /**
     * Inits the video player
     */
    private void initPlayer() {
        if (mWebView != null) {

            if (!mEnv.containsKey(VideoAd.Environment.KEY_ACCOUNT)) {
                changeState(State.ERROR);
                if (mListener != null ) {
                    TLog.d("Player failure: account not set");
                    mListener.onError(VideoAd.ErrorCode.PLAYER_INIT_FAILED, "Cannot init the player. Account not set");
                }
            } else if (!mEnv.containsKey(VideoAd.Environment.KEY_ACCOUNT)) {
                changeState(State.ERROR);
                if (mListener != null ) {
                    TLog.d("Player failure: placement id not set");
                    mListener.onError(VideoAd.ErrorCode.PLAYER_INIT_FAILED,  "Cannot init the player. Placement id not set");
                }
            } else {

                mInitTimeoutTimer = new Timer();
                setTimeout(mInitTimeoutTimer, mInitTimeout, new Runnable() {
                    @Override
                    public void run() {
                        changeState(State.ERROR);
                        close();

                        if (mListener != null) {
                            mListener.onError(VideoAd.ErrorCode.NETWORK_TIMEOUT, "Timeout occurred while initialising the player");
                        }
                    }
                });

                if (!mParams.containsKey(VideoAd.Parameters.KEY_DEVICE_ID) && mDeviceId != null) {
                    mParams.put(VideoAd.Parameters.KEY_DEVICE_ID, mDeviceId);
                }

                createAdTrackers();

                mWebView.initPlayer(mEnv, mParams, mPlacementType);
                changeState(State.INITIALISING);
            }
        }
    }

    /**
     * Resets all state variables
     */
    private void resetState(boolean closing) {
        TLog.d("resetState");
        if (mActivity != null) {
            int currentOrientation = mActivity.getRequestedOrientation();
            if (mOrientationChanged && currentOrientation != mOriginalOrientation) {
                mActivity.setRequestedOrientation(mOriginalOrientation);
            }

            if (mUsingPlayerActivity && mActivity instanceof PlayerActivity && !mActivity.isFinishing()) {
                mActivity.finish();
            }
        }

        if (closing) {
            changeState(State.IDLE);
        } else {
            changeState(State.INITIALIZED);
        }

        mAdLoadingPending = false;
        mUsingPlayerActivity = false;
        mVideoClicked = false;
        mVideoCompleted = false;

        stopAdTracking();

        if (mWebView != null && mWebViewPaused){
            mWebView.onResume();
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
                mHandler.post(runnable);
            }
        }, timeout);
    }

    /**
     * This function moves the player container to new activity, in case the application
     * does not allow playing the video in it's own activity.
     */
    public void switchActivity(Activity newActivity) {
        if (mContainer != null && newActivity != null) {
            ViewGroup vg = (ViewGroup) mContainer.getParent();
            vg.setLayoutTransition(null);
            vg.removeView(mContainer);
            vg.invalidate();
            ViewGroup root = (ViewGroup) newActivity.getWindow().getDecorView().getRootView();
            root.addView(mContainer);
            mActivity = newActivity;
        } else {
            mActivity = null;
        }
    }

    /**
     * Helper function for checking if permissions for locations are granted
     *
     * @param context the application context
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

            if (bestProvider != null) {
                // Start location update if most recent update older than the expiration limit
                long expiration = 0;
                long locTime = 0;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    locTime = (loc != null) ? loc.getElapsedRealtimeNanos() : 0;
                    expiration = SystemClock.elapsedRealtimeNanos() -
                            TimeUnit.NANOSECONDS.convert(LOCATION_EXPIRATION_LIMIT_IN_SECONDS, TimeUnit.SECONDS);
                }

                if (locTime < expiration) {

                    if (mLocationHandler == null) {
                        mLocationHandler = new HandlerThread("TPR Ad SDK Location handler thread");
                        mLocationHandler.start();

                        mLocationListener = new LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {
                                if (location != null) {
                                    final Location newLocation = location;

                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (mPlayerState != State.INITIALISING) {
                                                mPendingLocationUpdate = true;
                                            } else if (mPlayerState != State.IDLE && mPlayerState != State.ERROR) {
                                                updateLocationToPlayer(newLocation);
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
                        lm.requestSingleUpdate(bestProvider, mLocationListener, mLocationHandler.getLooper());

                    }
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
     * AsyncTask for retrieving the advertising ID of the device
     */
    private class AdInfoRetriever extends AsyncTask<Void, Void, Void> {

        private Context mContext;
        public void setContext(Context context) {
            mContext = context;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // Using Google Play Services is optional
                Class<?> idClientClass = Class.forName("com.google.android.gms.ads.identifier.AdvertisingIdClient");
                Method getIdInfoMethod = idClientClass.getMethod("getAdvertisingIdInfo", Context.class);
                Object idInfo = getIdInfoMethod.invoke(null, mContext);
                Method getIdMethod = idInfo.getClass().getMethod("getId", (Class<?>[]) null);
                mDeviceId = (String) getIdMethod.invoke(idInfo, (Object[]) null);
                TLog.d("Device ID retrieved: " + mDeviceId);

            } catch (Exception e) {
                // Google Play Services not available
                TLog.d("Google play services not available");
                mDeviceId = null;
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    initPlayer();
                 }
            });

            return null;
        }
    }

    /**
     * Setup ad tracking services
     */
    private void startAdTrackingAnalytics() {
        // MOAT
        if (VideoAd.parseBoolean(mEnv.get(KEY_MOAT_AD_TRACKING), true)) {
            MoatAnalytics.getInstance().start(mApplication);
            TLog.d("MOAT ad tracking enabled");
        }
    }

    /**
     * Create ad trackers
     */
    private void createAdTrackers() {
        // MOAT
        if (VideoAd.parseBoolean(mEnv.get(KEY_MOAT_AD_TRACKING), true)) {
            MoatFactory factory = MoatFactory.create();
            mWebAdTracker = factory.createWebAdTracker(mWebView);
            TLog.d("MOAT ad tracker created");
        }
    }

    /**
     * Start Ad tracking
     */
    private void startAdTracking() {
        if (mWebAdTracker != null) {
            mWebAdTracker.startTracking();
            TLog.d("MOAT ad tracking started");
        }
    }

    /**
     * Stop Ad tracking
     */
    private void stopAdTracking() {
        if (mWebAdTracker != null) {
            mWebAdTracker.stopTracking();
            mWebAdTracker = null;
            TLog.d("MOAT ad tracking stopped");
        }
    }



    /**
     * Changes the player state and executes state change runnables
     *
     * @param newState the state changing to
     */
    private void changeState(State newState) {
        if (mPlayerState == newState) return;

        TLog.i("Change state " + mPlayerState + " > " + newState);
        Iterator<StateChangeRunnableInfo> i = mRunnables.iterator();
        while (i.hasNext()) {
            StateChangeRunnableInfo info = i.next();
            if (info.previousState == mPlayerState) {
                i.remove();
                if (info.nextState == null || info.nextState == newState) {
                    mHandler.postDelayed(info.runOnStateChange, 1);
                }
            }
        }
        mPlayerState = newState;
    }

    /**
     * Adds new state change runnable
     *
     * @param fromState the state, which exit will trigger the runnable
     * @param toState if given will cause runnable to execute if moving to this state.
     * @param runnable to be executed
     */
    private void addStateChangeRunnable(State fromState, State toState, Runnable runnable) {
        StateChangeRunnableInfo info = new StateChangeRunnableInfo(runnable, fromState, toState);
        mRunnables.add(info);
    }

    /**
     * Message handler
     */
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            Bundle data = msg.getData();

            switch (msg.what) {
                case VideoWebView.MSG_TYPE_PLAYER_READY: {

                    changeState(State.INITIALIZED);

                    if (mInitTimeoutTimer != null) {
                        mInitTimeoutTimer.cancel();
                        mInitTimeoutTimer = null;
                    }

                    if (mPendingLocationUpdate) {
                        Location location = getLocation(mApplication);
                        if (location != null) {
                            updateLocationToPlayer(location);
                        }
                    }

                    if (mListener != null) {
                        mListener.onPlayerReady();
                    }

                    if (mAdLoadingPending) {
                        loadAd();
                    }

                    break;
                }
                case VideoWebView.MSG_TYPE_PLAYER_EVENT: {
                    if (data != null) {
                        String args[] = data.getStringArray(VideoWebView.MSG_DATA_KEY_PLAYER_EVENT_DETAILS);

                        TLog.d("An ad event occurred: " + args[0] + ":" + args[1] + ":" + args[2] + ":" + args[3]);
                        if (args[0] != null) {
                            if (args[0].equals(VideoAd.Events.AD_LOADED)) {
                                if (mLoadTimeoutTimer != null) {
                                    mLoadTimeoutTimer.cancel();
                                    mLoadTimeoutTimer = null;
                                }
                                changeState(State.LOADED);

                                if (mDisplayImmediately) {
                                    displayAdInCurrentActivity();
                                }
                            } else if (args[0].equals(VideoAd.Events.AD_STOPPED)) {
                                if (mPlayerState == State.DISPLAYING) {
                                    changeState(State.STOPPED);
                                }
                            } else if (args[0].equals(VideoAd.Events.AD_VIDEO_COMPLETE)) {
                                mVideoCompleted = true;
                            }

                            if (mListener != null) {
                                mListener.onAdEvent(args[0], args[1], args[2], args[3]);
                            }

                        }
                    }
                    break;
                }
                case VideoWebView.MSG_TYPE_PLAYER_ERROR: {
                    if (data != null) {
                        int errorCode = data.getInt(VideoWebView.MSG_DATA_KEY_ERROR_CODE);
                        String errorMessage = data.getString(VideoWebView.MSG_DATA_KEY_ERROR_MESSAGE);

                        TLog.d("Player failure " + errorCode + ":" + errorMessage);

                        changeState(State.ERROR);

                        close();

                        if (mListener != null) {
                            mListener.onError(VideoAd.ErrorCode.PLAYER_INIT_FAILED, errorMessage);
                        }
                    }
                    break;
                }
                case VideoWebView.MSG_TYPE_NETWORK_ERROR: {
                    if (data != null) {
                        int errorCode = data.getInt(VideoWebView.MSG_DATA_KEY_ERROR_CODE);
                        String errorMessage = data.getString(VideoWebView.MSG_DATA_KEY_ERROR_MESSAGE);

                        TLog.d("Network error: " + errorCode + ":" + errorMessage);

                        changeState(State.ERROR);

                        close();

                        if (mListener != null) {
                            mListener.onError(VideoAd.ErrorCode.NETWORK_FAILURE, errorMessage);
                        }
                    }
                    break;
                }
                case VideoWebView.MSG_TYPE_URL_INTERCEPTED: {
                    if (data != null && mActivity != null) {
                        try {
                            mVideoClicked = true;
                            String url = data.getString(VideoWebView.MSG_DATA_KEY_URL);
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            mActivity.startActivity(browserIntent);

                        } catch (Exception e) {
                            mVideoClicked = false;
                        }
                    }
                    break;
                }
                default:

            }
        }
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        TLog.d("Activity created: " + activity.getClass().getSimpleName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityStarted(Activity activity) {
        TLog.d("Activity started: " + activity.getClass().getSimpleName());
        mActiveActivity = activity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityResumed(Activity activity) {
        TLog.d("Activity resumed: " + activity.getClass().getSimpleName());
        mActiveActivity = activity;
        if (activity == mActivity) {
            if (mWebView != null && mWebViewPaused) {
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
        TLog.d("Activity paused: " + activity.getClass().getSimpleName());
        if (activity == mActiveActivity) {
            mActiveActivity = null;
        }

        if (activity == mActivity) {
            if(mWebView != null && mPlayerState == State.DISPLAYING){
                mWebView.pauseAd();
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
        TLog.d("Activity stopped: " + activity.getClass().getSimpleName());
        if (activity == mActiveActivity) {
            mActiveActivity = null;
        }

        if (!mUsingPlayerActivity && activity == mActivity && mPlayerState == State.DISPLAYING && !mVideoClicked) {
            TLog.w("Activity stopped while displaying an ad");
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
        TLog.d("Activity destroyed: " + activity.getClass().getSimpleName());
        if (activity == mActiveActivity) {
            mActiveActivity = null;
        }
        if (activity == mActivity) {
            if (!(activity instanceof PlayerActivity)) {
                reset();
            }

            mActivity = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onVolumeChanged(float volume) {
        if (mWebView != null) {
            mWebView.updateVolume(volume);
        }
    }
}
