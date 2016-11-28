package com.thirdpresence.adsdk.sdk.internal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
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
 * <h1>VideoWebView</h1>
 *
 * VideoWebView is an extended WebView to be used to display Thirdpresence HTML5 video player
 *
 */
public class VideoWebView extends WebView {

    private Context mApplication;
    private Handler mHandler;

    private String mPlayerUrl;

    private boolean mPlayerPageLoaded = false;
    private boolean mBackAllowed = false;

    private int mInitRetryCount;

    private final static int INIT_RETRY_COUNT_MAX = 5;
    private final static int INIT_RETRY_DELAY_MS = 1000;


    private final static String JS_API_NAME = "ThirdpresenceNative";
    private final static String PLAYER_URL_BASE = "//d1c13tt6n7tja5.cloudfront.net/tags/[KEY_SERVER]/sdk/LATEST/sdk_player.v3.html?";

    private final static String EVENT_NAME_PLAYER_READY = "PlayerReady";
    private final static String EVENT_NAME_PLAYER_ERROR = "PlayerError";

    public final static int MSG_TYPE_PLAYER_READY = 101;
    public final static int MSG_TYPE_PLAYER_EVENT = 102;
    public final static int MSG_TYPE_PLAYER_ERROR = 103;
    public final static int MSG_TYPE_NETWORK_ERROR = 104;
    public final static int MSG_TYPE_URL_INTERCEPTED = 105;

    public final static String MSG_DATA_KEY_PLAYER_EVENT_DETAILS = "PlayerEventDetails";
    public final static String MSG_DATA_KEY_ERROR_CODE = "ErrorCode";
    public final static String MSG_DATA_KEY_ERROR_MESSAGE = "ErrorMessage";
    public final static String MSG_DATA_KEY_URL = "URL";

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
            if (eventName != null) {
                String a1 = arg1 != null ? arg1 : "";
                String a2 = arg2 != null ? arg2 : "";
                String a3 = arg3 != null ? arg3 : "";

                if (eventName.contentEquals(EVENT_NAME_PLAYER_READY)) {
                    mPlayerPageLoaded = true;
                    sendEmptyMessage(MSG_TYPE_PLAYER_READY);
                } else if (eventName.contentEquals(EVENT_NAME_PLAYER_ERROR)) {
                    sendErrorMessage(MSG_TYPE_PLAYER_ERROR, VideoAd.ErrorCode.PLAYER_INIT_FAILED.getErrorCode(), a1);
                } else {
                    Bundle data = new Bundle();
                    String args[] = {eventName, a1, a2, a3};
                    data.putStringArray(MSG_DATA_KEY_PLAYER_EVENT_DETAILS, args);
                    sendMessage(MSG_TYPE_PLAYER_EVENT, data);
                }
            }
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
            if (view == VideoWebView.this) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    if (!mPlayerPageLoaded) {
                        if (mInitRetryCount <= INIT_RETRY_COUNT_MAX) {
                            mInitRetryCount++;
                            TLog.w("Network error ignored: " + errorCode + ":" + description + ". Trying to retry.");
                            retryInitPlayer();
                        } else {
                            String message = description != null ? description : "Unknown error";
                            sendErrorMessage(MSG_TYPE_NETWORK_ERROR, errorCode, message);
                        }
                    } else {
                        TLog.w("Network error ignored: " + errorCode + ":" + description);
                    }
                }
            }
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            if (view == VideoWebView.this) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && error != null) {
                    CharSequence description = error.getDescription();
                    String message = description != null ? description.toString() : "Unknown error";
                    if (!mPlayerPageLoaded && mPlayerUrl != null && mInitRetryCount <= INIT_RETRY_COUNT_MAX) {
                        mInitRetryCount++;
                        TLog.w("Network error ignored: " + error.getErrorCode() + ":" + message + ". Trying to retry.");
                        retryInitPlayer();
                        return;
                    }

                    if (!mPlayerPageLoaded || request.isForMainFrame()) {
                        sendErrorMessage(MSG_TYPE_NETWORK_ERROR, error.getErrorCode(), message);
                    } else {
                        TLog.w("Network error ignored: " + error.getErrorCode() + ":" + message);
                    }
                }
            }
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            super.onReceivedHttpError(view, request, errorResponse);
            if (view == VideoWebView.this) {
                if (!mPlayerPageLoaded) {
                    String message = null;
                    int errorCode = -1;
                    if (errorResponse != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            message = "HTTP: " +  errorResponse.getReasonPhrase();
                            errorCode = errorResponse.getStatusCode();
                        }
                    }

                    if (message == null) {
                        message = "HTTP: Unknonw failure when loading the player";
                    }

                    if (mPlayerUrl != null && mInitRetryCount <= INIT_RETRY_COUNT_MAX) {
                        mInitRetryCount++;
                        TLog.w("Network error ignored: " + errorCode + ":" + message + ". Trying to retry.");
                        retryInitPlayer();
                    } else {
                        sendErrorMessage(MSG_TYPE_PLAYER_ERROR, errorCode , message);
                    }
                }
            }
        }

        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (view == VideoWebView.this) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    if (!isConnected()) {
                        TLog.e("Device is not connected to Internet");
                    }

                    if (mPlayerPageLoaded && url != null && !shallHandleURLInWebView(url)) {
                        Bundle data = new Bundle();
                        data.putString(MSG_DATA_KEY_URL, url);
                        sendMessage(MSG_TYPE_URL_INTERCEPTED, data);
                        return true;
                    }
                }
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if (view == VideoWebView.this) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (!isConnected()) {
                        TLog.e("Device is not connected to Internet");
                    }

                    Uri url = request.getUrl();

                    if (mPlayerPageLoaded && url != null) {
                        String urlString = url.toString();
                        if (urlString != null &&  !shallHandleURLInWebView(urlString)) {
                            Bundle data = new Bundle();
                            data.putString(MSG_DATA_KEY_URL, urlString);
                            sendMessage(MSG_TYPE_URL_INTERCEPTED, data);
                            return true;
                        }
                    }
                }
            }
            return super.shouldOverrideUrlLoading(view, request);
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
        super(context);

        mApplication = context;
        mPlayerPageLoaded = false;
        mInitRetryCount = 0;
        mPlayerUrl = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (0 != (mApplication.getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE)) {
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
        mPlayerUrl = null;
        mInitRetryCount = 0;
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
     * Sets handler which handles messages on the main thread
     *
     * @param handler handler object
     *
     */
    public void setHandler(Handler handler) {
        mHandler = handler;
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
    public void initPlayer(Map<String, String> environment,  Map<String, String> playerParams, String placementType) {
        String customization = "";
        mPlayerUrl = null;
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
            sendErrorMessage(MSG_TYPE_PLAYER_ERROR,
                    VideoAd.ErrorCode.PLAYER_INIT_FAILED.getErrorCode(),
                    "Cannot init the player. Account not set");
        } else if (playerId == null) {
            sendErrorMessage(MSG_TYPE_PLAYER_ERROR,
                    VideoAd.ErrorCode.PLAYER_INIT_FAILED.getErrorCode(),
                    "Cannot init the player. VideoAd id not set");
        } else if (customization == null) {
            sendErrorMessage(MSG_TYPE_PLAYER_ERROR,
                    VideoAd.ErrorCode.PLAYER_INIT_FAILED.getErrorCode(),
                    "Cannot init the player. Invalid customization parameters");
        } else {

            String protocol = VideoAd.parseBoolean(environment.get(VideoAd.Environment.KEY_FORCE_SECURE_HTTP), false) ? "https:" : "http:";

            mPlayerUrl = protocol
                        + PLAYER_URL_BASE.replace("[KEY_SERVER]", server)
                        + "env=" + server
                        + "&adsdk=" + versionString
                        + "&cid=" + account
                        + "&playerid=" + playerId
                        + "&type=" + placementType
                        + "&customization=" + customization;
            TLog.d("Loading player: " + mPlayerUrl);
            loadUrl(mPlayerUrl);
        }
    }

    private void retryInitPlayer() {
        if (mHandler != null) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mPlayerUrl != null) {
                        loadUrl(mPlayerUrl);
                    }
                }
            }, INIT_RETRY_DELAY_MS);
        }
    }

    /**
     * Loads an ad
     */
    public void loadAd() {
        if (mPlayerPageLoaded) {
            callJSFunction("loadAd", null, null);
        } else {
            sendErrorMessage(MSG_TYPE_PLAYER_ERROR,
                    VideoAd.ErrorCode.INVALID_STATE.getErrorCode(),
                    "Player is not ready");
        }
    }

    /**
     * Displays an ad
     */
    public void displayAd() {
        if (mPlayerPageLoaded) {
            callJSFunction("startAd", null, null);
        } else {
            sendErrorMessage(MSG_TYPE_PLAYER_ERROR,
                    VideoAd.ErrorCode.INVALID_STATE.getErrorCode(),
                    "Player is not ready");
        }
    }

    /**
     * Pauses the loaded ad
     */
    public void pauseAd() {
        if (mPlayerPageLoaded) {
            callJSFunction("pauseAd", null, null);
        } else {
            sendErrorMessage(MSG_TYPE_PLAYER_ERROR,
                    VideoAd.ErrorCode.INVALID_STATE.getErrorCode(),
                    "Player is not ready");
        }
    }
    /**
     * Resumes the loaded  ad
     */
    public void resumeAd() {
        if (mPlayerPageLoaded) {
            callJSFunction("resumeAd", null, null);
        } else {
            sendErrorMessage(MSG_TYPE_PLAYER_ERROR,
                    VideoAd.ErrorCode.INVALID_STATE.getErrorCode(),
                    "Player is not ready");
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

    private void sendEmptyMessage(int what) {
        if (mHandler != null) {
            mHandler.sendEmptyMessage(what);
        }
    }

    private void sendMessage(int what, Bundle data) {
        if (mHandler != null) {
            Message msg = Message.obtain(mHandler, what);
            msg.setData(data);
            mHandler.sendMessage(msg);
        }
    }

    private void sendErrorMessage(int what, int code, String message) {
        if (mHandler != null && message != null) {
            Message msg = Message.obtain(mHandler, what);
            Bundle data = new Bundle();
            data.putInt(MSG_DATA_KEY_ERROR_CODE, code);
            data.putString(MSG_DATA_KEY_ERROR_MESSAGE, message);
            msg.setData(data);
            mHandler.sendMessage(msg);
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
        loadUrl("javascript:" + function + "(" + a1 + "," +  a2 + ");");
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

        if (mApplication != null) {
            ConnectivityManager cm =
                    (ConnectivityManager) mApplication.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (cm != null) {
                if (cm.getActiveNetworkInfo() != null
                        && cm.getActiveNetworkInfo().isAvailable()
                        && cm.getActiveNetworkInfo().isConnected()) {
                    isConnected = true;
                }
            }
        }

        return isConnected;
    }
}
