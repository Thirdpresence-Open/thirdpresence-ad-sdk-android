package com.thirdpresence.adsdk.sdk.internal;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
//import android.net.ConnectivityManager;
import android.os.Build;

import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.thirdpresence.adsdk.sdk.BuildConfig;
import com.thirdpresence.adsdk.sdk.VideoAd;

import org.json.JSONObject;
import java.net.URLEncoder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import java.util.Map;

/**
 *
 * VideoWebView is an extended WebView to be used to display Thirdpresence HTML5 video player
 *
 */
public class VideoWebView extends WebView {

    private Activity mActivity;
    private Listener mListener;

    private boolean mPlayerPageLoaded = false;
    private boolean mBackAllowed = false;
    private boolean mDeadlockCleared = false;

    private final static String JS_API_NAME = "ThirdpresenceNative";
    private final static String PLAYER_URL_BASE = "//d1c13tt6n7tja5.cloudfront.net/tags/[KEY_SERVER]/sdk/LATEST/sdk_player.v3.html?";

    private final static String EVENT_NAME_PLAYER_READY = "PlayerReady";
    private final static String EVENT_NAME_PLAYER_ERROR = "PlayerError";

    /**
     * Callback interface for WebView events.
     */
    public interface Listener {

        /**
         * Called when player is loaded and ready to be getting further actions
         */
        void onPlayerReady();

        /**
         * Called when loading or initialising the player has failed
         *
         * @param errorCode @see com.thirdpresence.adsdk.sdk.VideoAd.ErrorCode
         * @param errorText human-readable error message
         *
         */
        void onPlayerFailure(VideoAd.ErrorCode errorCode, String errorText);

        /**
         * Called when network error has occured
         *
         * @param statusCode HTTP status code
         * @param description status description
         *
         */
        void onNetworkError(int statusCode, String description);

        /**
         * Called when a player event has occured
         *
         * @param eventName name of the event
         * @param arg1 an event-specific argument
         * @param arg2 an event-specific argument
         * @param arg3 an event-specific argument
         * @see com.thirdpresence.adsdk.sdk.VideoAd.Events
         *
         */
        void onAdEvent(String eventName, String arg1, String arg2, String arg3);

        /**
         * Called when user tries to open URL. Typically occurs when an ad has a landing page and
         * user clicks the video. Typically the URL is opened in the browser or PlayStore app.
         */
        void onOpenURLIntercepted(String url);
    }

    /**
     * Implementation of the web interface that is used for receiving events from HTML5 player.
     */
    public class WebAppInterface {
        private Context mContext;
        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void onPlayerEvent(final String eventName, final String arg1, final String arg2, final String arg3) {
            mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    if (mListener != null) {
                        if (eventName.contentEquals(EVENT_NAME_PLAYER_READY)) {
                            mPlayerPageLoaded = true;
                            mListener.onPlayerReady();
                        } else if (eventName.contentEquals(EVENT_NAME_PLAYER_ERROR)) {
                            mListener.onPlayerFailure(VideoAd.ErrorCode.PLAYER_INIT_FAILED, arg1);
                        } else {
                            mListener.onAdEvent(eventName, arg1, arg2, arg3);
                        }
                    }
                }
            });
        }
    }

    /**
     * Overridden WebViewClient to handle WebView errors
     */
    public class ThirdpresenceWebViewClient extends WebViewClient {

        @SuppressWarnings("deprecation")
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                mListener.onNetworkError(errorCode, description);
            }
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && error != null) {
                mListener.onNetworkError(error.getErrorCode(), error.getDescription().toString() );
            }
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            super.onReceivedHttpError(view, request, errorResponse);
            if (!mPlayerPageLoaded) {
                String message = null;
                if (errorResponse != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        message = errorResponse.getReasonPhrase();
                    }
                }
                if (message == null) {
                    message = "HTTP failure when loading the player";
                }

                mListener.onPlayerFailure(VideoAd.ErrorCode.PLAYER_INIT_FAILED, message);
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (!isConnected()) {
                TLog.e("Device is not connected to Internet");
            }

            if (mPlayerPageLoaded && !shallHandleURLInWebView(url)) {
                if (mListener != null) {
                    mListener.onOpenURLIntercepted(url);
                }
                return true;
            }
            return super.shouldOverrideUrlLoading(view, url);
        }
    }

    /**
     * Overridden WebChromeClient. Empty implementation for now.
     */
    public class ThirdpresenceWebChromeClient extends WebChromeClient {}

    /**
     * Constructor
     */
    @SuppressLint({"AddJavascriptInterface", "SetJavaScriptEnabled"})
    public VideoWebView(Context context) {
        super(context.getApplicationContext());

        if (!mDeadlockCleared) {
            clearWebViewDeadlock(getContext());
            mDeadlockCleared = true;
        }

        mActivity = (Activity) context;
        mPlayerPageLoaded = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (0 != (mActivity.getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE)) {
                WebView.setWebContentsDebuggingEnabled(true);
            }
        }

        WebSettings webSettings = getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        String userAgent = getUserAgent(context);
        webSettings.setUserAgentString(userAgent);
        webSettings.setAllowFileAccess(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webSettings.setMediaPlaybackRequiresUserGesture(false);
        }

        /*
         * Disabling file access and content access prevents advertising creatives from
         * detecting the presence of, or reading, files on the device filesystem.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            webSettings.setAllowContentAccess(false);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            webSettings.setAllowFileAccessFromFileURLs(false);
            webSettings.setAllowUniversalAccessFromFileURLs(false);
        }

        setWebViewClient(new ThirdpresenceWebViewClient());
        setWebChromeClient(new ThirdpresenceWebChromeClient());
        clearCache(true);
        addJavascriptInterface(new WebAppInterface(context), JS_API_NAME);
    }

    /**
     * Overridden destroy() method to verify that the webview is not used after destroyed.
     */
    @Override
    public void destroy() {
        mPlayerPageLoaded = false;
        super.destroy();
    }

    /**
     * Overridden dispatchKeyEventPreIme() to intercept BACK key.
     */
    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && !isBackAllowed()) {
            return true;
        }
        return super.dispatchKeyEventPreIme(event);
    }

    /**
     * Sets listener
     *
     * @param listener listener object
     *
     */
    public void setListener(Listener listener) {
        mListener = listener;
    }

    /**
     * Sets listener
     *
     * @param allow true if back shall be allowed
     *
     */
    public void setBackAllowed(boolean allow) {
        mBackAllowed = allow;
    }

    /**
     * Check if back is allowed
     *
     * @return true if allowed
     *
     */
    public boolean isBackAllowed() {
        return mBackAllowed;
    }

    /**
     * Loads and inits HTML5 player
     *
     * @param environment environment parameters
     * @param playerParams player parameters
     *
     */
    public void initPlayer(Map<String, String> environment,  Map<String, String> playerParams) {
        String customization = "";
        try {
            JSONObject json = new JSONObject(playerParams);
            String jsonString = json.toString();
            if (jsonString != null) {
                customization = URLEncoder.encode(jsonString, "UTF-8");
            }
        } catch (Exception e) {
            customization = null;
        }

        String server = environment.get(VideoAd.Environment.KEY_SERVER);
        String account = environment.get(VideoAd.Environment.KEY_ACCOUNT);
        String playerId = environment.get(VideoAd.Environment.KEY_PLACEMENT_ID);

        String versionString = BuildConfig.VERSION_NAME + "." + BuildConfig.VERSION_CODE;
        if (environment.containsKey(VideoAd.Environment.KEY_EXT_SDK)) {
            versionString += "," + environment.get(VideoAd.Environment.KEY_EXT_SDK);
            if (environment.containsKey(VideoAd.Environment.KEY_EXT_SDK_VERSION)) {
                versionString += "," + environment.get(VideoAd.Environment.KEY_EXT_SDK);
            }
        }

        if (account == null) {
            mListener.onPlayerFailure(VideoAd.ErrorCode.PLAYER_INIT_FAILED, "Cannot init the player. Account not set");
        } else if (playerId == null) {
            mListener.onPlayerFailure(VideoAd.ErrorCode.PLAYER_INIT_FAILED, "Cannot init the player. VideoAd id not set");
        } else if (customization == null) {
            mListener.onPlayerFailure(VideoAd.ErrorCode.PLAYER_INIT_FAILED, "Cannot init the player. Invalid customization parameters");
        } else {

            String protocol = VideoAd.parseBoolean(environment.get(VideoAd.Environment.KEY_FORCE_SECURE_HTTP), false) ? "https:" : "http:";

            String url = protocol
                        + PLAYER_URL_BASE.replace("[KEY_SERVER]", server)
                        + "env=" + server
                        + "&adsdk=" + versionString
                        + "&cid=" + account
                        + "&playerid=" + playerId
                        + "&customization=" + customization;
            TLog.d("Loading player: " + url);
            loadUrl(url);
        }
    }

    /**
     * Loads an ad
     */
    public void loadAd() {
        if (mPlayerPageLoaded) {
            callJSFunction("loadAd", null, null);
        } else {
            mListener.onPlayerFailure(VideoAd.ErrorCode.AD_NOT_READY, "VideoAd is not ready");
        }
    }

    /**
     * Displays an ad
     */
    public void displayAd() {
        if (mPlayerPageLoaded) {
            callJSFunction("startAd", null, null);
        } else {
            mListener.onPlayerFailure(VideoAd.ErrorCode.AD_NOT_READY, "VideoAd is not ready");
        }
    }

    /**
     * Pauses the loaded ad
     */
    public void pauseAd() {
        if (mPlayerPageLoaded) {
            callJSFunction("pauseAd", null, null);
        } else {
            mListener.onPlayerFailure(VideoAd.ErrorCode.AD_NOT_READY, "VideoAd is not ready");
        }
    }
    /**
     * Resumes the loaded  ad
     */
    public void resumeAd() {
        if (mPlayerPageLoaded) {
            callJSFunction("resumeAd", null, null);
        } else {
            mListener.onPlayerFailure(VideoAd.ErrorCode.AD_NOT_READY, "VideoAd is not ready");
        }
    }


    /**
     * Updates geo location to the player
     *
     * @param latitude coordinates
     * @param longitude coordinates
     */
    public void updateLocation(String latitude, String longitude) {
        if (mPlayerPageLoaded) {
            callJSFunction("updateLocation", latitude, longitude);
        }
    }

    /**
     * Calls a function in the player JavaScript API
     *
     * @param function name of the JavaScript function
     * @param arg1 first argument for the JavaScript function
     * @param arg2 seconds argument for the JavaScript function
     */
    private void callJSFunction(String function, String arg1, String arg2) {
        String a1 = null;
        String a2 = null;
        if (arg1 != null) {
            a1 = "\"" + arg1 + "\"";
        }
        if (arg2 != null) {
            a2 = "\"" + arg2 + "\"";
        }
        loadUrl("javascript:" + function + "(" + arg1 + "," +  arg2 + ");");
    }

    /**
     * Gets the browser user-agent
     *
     * @param context context
     * @return user-agent string
     *
     */
    private static String getUserAgent(@NonNull final Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return WebSettings.getDefaultUserAgent(context);
        } else {
            try {
                final Class<?> webSettingsClassicClass = Class.forName("android.webkit.WebSettingsClassic");
                final Constructor<?> constructor = webSettingsClassicClass.getDeclaredConstructor(Context.class, Class.forName("android.webkit.WebViewClassic"));
                constructor.setAccessible(true);
                final Method method = webSettingsClassicClass.getMethod("getUserAgentString");
                return (String) method.invoke(constructor.newInstance(context, null));
            } catch (final Exception e) {
                return new WebView(context).getSettings()
                        .getUserAgentString();
            }
        }
    }

    /**
     * Checks whether the web view shall open the URL instead of external app.
     *
     * @param url to be checked
     * @return true if the URL shall be opened in web view
     *
     */
    private boolean shallHandleURLInWebView(@NonNull String url) {
        return url.startsWith("javascript:") || url.startsWith("about:") || url.startsWith("blob:");
    }

    /**
     * Checks whether the device is connected to Internet
     *
     * @return true if connection available
     *
     */
    private boolean isConnected() {
        boolean isConnected = false;
        /*
        if (mActivity != null) {
            ConnectivityManager cm =
                    (ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (cm != null) {
                if (cm.getActiveNetworkInfo() != null
                        && cm.getActiveNetworkInfo().isAvailable()
                        && cm.getActiveNetworkInfo().isConnected()) {
                    isConnected = true;
                }
            }
        }
        */
        return isConnected;
    }

    /**
     * Copied from MoPub SDK
     *
     * This fixes https://code.google.com/p/android/issues/detail?id=63754,
     * which occurs on KitKat device. When a WebView containing an HTML5 video is
     * is destroyed it can deadlock the WebView thread until another hardware accelerated WebView
     * is added to the view hierarchy and restores the GL context. Since we need to use WebView
     * before adding it to the view hierarchy, this method clears the deadlock by adding a
     * separate invisible WebView.
     *
     * This potential deadlock must be cleared anytime you attempt to access a WebView that
     * is not added to the view hierarchy.
     *
     * @param context context
     *
     */
    private void clearWebViewDeadlock(@NonNull final Context context) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            // Create an invisible webview
            final WebView webView = new WebView(context.getApplicationContext());
            webView.setBackgroundColor(Color.TRANSPARENT);

            // For the deadlock to be cleared, we must load content and add to the view hierarchy. Since
            // we don't have an activity context, we'll use a system window.
            webView.loadDataWithBaseURL(null, "", "text/html", "UTF-8", null);
            final WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.width = 1;
            params.height = 1;
            // Unlike other system window types TYPE_TOAST doesn't require extra permissions
            params.type = WindowManager.LayoutParams.TYPE_TOAST;
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
            params.format = PixelFormat.TRANSPARENT;
            params.gravity = Gravity.START | Gravity.TOP;
            final WindowManager windowManager =
                    (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

            windowManager.addView(webView, params);
        }
    }
}
