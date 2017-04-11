# Thirdpresence Ad SDK for Android

Thirdpresence Ad SDK provides an API to display banner, interstitial and rewarded video ads in an application.

## Minimum requirements

- Android Studio
- Android API level 16 (Android 4.1)
- Google Play Services Ads 10.2.1 (optional, but highly recommended)
    - used for getting Google Advertising ID
    - for more information, see http://developer.android.com/google/play-services/setup.html

## Adding library dependencies

### jCenter hosted library

Thirdpresence Ad SDK
[ ![Download](https://api.bintray.com/packages/thirdpresence/thirdpresence-ad-sdk-android/com.thirdpresence.adsdk.sdk/images/download.svg) ](https://bintray.com/thirdpresence/thirdpresence-ad-sdk-android/com.thirdpresence.adsdk.sdk/_latestVersion)

Check that jcenter is included in the repositories block, and add the required dependencies to the dependencies block:
```
repositories {
    jcenter()
    // Backup repository if libraries not available from jcenter
    // maven { url 'http://dl.bintray.com/thirdpresence/thirdpresence-ad-sdk-android' }
}

dependencies {
	// SDK library
    compile 'com.thirdpresence.adsdk.sdk:thirdpresence-ad-sdk:1.5.4@aar'
    // Google Play Services 
    compile 'com.google.android.gms:play-services-ads:10.2.1'
    // Google Support libraries
    compile 'com.android.support:support-compat:25.3.1'
    compile 'com.android.support:support-core-utils:25.3.1'
    compile 'com.android.support:support-annotations:25.3.1'
}
```

### Manually building the SDK

Instead of using prebuilt jCenter libraries, the SDK can also be built manually. All source code is available in this repository.

- Create a new project in Android Studio from the content of this repository
- Make project and Build APK
- Copy thirdpresence-ad-sdk/build/outputs/aar/thirdpresence-ad-sdk-release.aar to your /libs/ folder in the application project
- Add the library to your application build 
	- File -> New... -> New Module... -> Import .JAR/.AAR Package -> Next
	- Select the thirdpresence-ad-sdk-release.aar file, and name the module -> Finish
- Do the same for the mediation library, if you are using SDK mediation (such as MoPub or Admob)

## Integration

Add the required permissions to AndroidManifest.xml if they do not already exist.
The location permission is optional, but highly recommended to get a higher fill rate.
```
<uses-permission android:name="android.permission.INTERNET"/> 
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
```

As of Android 6.0 (API level 23), users grant permissions to apps while the app is running, not when they install the app. 
Thirdpresence Ad SDK does not request permissions from the user, but it must be done by the app.
For details, see Google Android documentation:
https://developer.android.com/training/permissions/requesting.html

Add PlayerActivity to the AndroidManifest.xml
```
        <activity android:name="com.thirdpresence.adsdk.sdk.internal.PlayerActivity"
            android:theme="@android:style/Theme.NoTitleBar.Fullscreen"
            android:hardwareAccelerated="true"
            android:configChanges="orientation|screenSize|keyboardHidden"></activity>
                        
```

### Interstitial and rewarded video ad placements

An interstitial and rewarded video ad placements are initialised and displayed in similar fashion. There are three methods to be called: init(), loadAd() and displayAd().
The ad unit is closed by calling reset() or completely removed by calling remove()*[]: 

An example code for initialising and displaying an interstitial ad placement:
```
    // Call initInterstitial() as soon as there is an activity available.
    public static void initInterstitial(Activity activity, String placementID) {
        HashMap<String, String> environment = new HashMap<>();

        environment.put(VideoAd.Environment.KEY_ACCOUNT, ACCOUNT);
        environment.put(VideoAd.Environment.KEY_PLACEMENT_ID, placementID);

        // For rewarded video the reward title (e.g. name of virtual credit) and reward amount must be defined
        environment.put(VideoAd.Environment.KEY_REWARD_TITLE, "credits");
        environment.put(VideoAd.Environment.KEY_REWARD_AMOUNT, "10");

        HashMap<String, String> params = new HashMap<>();
        params.put(VideoAd.Parameters.KEY_PUBLISHER, "My Application");
        params.put(VideoAd.Parameters.KEY_APP_NAME, "My Application"));
        params.put(VideoAd.Parameters.KEY_APP_VERSION, "1.0");

        // In order to get more targeted ads you shall provide user's gender and year of birth
        // You can use e.g. Google+ API or Facebook Graph API
        // https://developers.google.com/android/reference/com/google/android/gms/plus/model/people/package-summary
        // https://developers.facebook.com/docs/android/graph/
        params.put(VideoAd.Parameters.KEY_USER_GENDER, "male");
        params.put(VideoAd.Parameters.KEY_USER_YOB, "1970");
        
        // VideoAdManager is a helper class for getting access to the VideoAd instance
        // For rewarded video use the VideoAd.PLACEMENT_TYPE_REWARDED_VIDEO
        VideoAd ad = VideoAdManager.getInstance().create(VideoAd.PLACEMENT_TYPE_INTERSTITIAL, placementID);
        ad.init(activity, environment, params, VideoAd.DEFAULT_TIMEOUT);
        ad.loadAd();
    }

    // For the interstitial and rewarded video ad placements displayAd() must be explicitly called. 
    // For example, when going to next level in the app.
    public void goToNextLevel(String placementId) {
        final VideoAd ad = VideoAdManager.getInstance().get(placementId);
        if (ad != null && ad.isAdLoaded()) {
            ad.displayAd(new Runnable() {
                @Override
                public void run() {
                    // Close the placement 
                    ad.reset();
                    startNextLevel();
                }
             });
        } else {
             startNextLevel();
        }
    }
    
    
```

### Banner ad placement

In order to display videoa ad in an banner ad placement a BannerView needs to be added to the layout. This can be done either by defining the view in the layout XML or creating the view programmatically. 
Example of defining BannerView in the layout XML:
```
    <com.thirdpresence.adsdk.sdk.BannerView
        xmlns:tpr="http://schemas.android.com/apk/res-auto"
        android:id="@+id/bannerView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        tpr:adWidth="300"
        tpr:adHeight="250"/>
```

Example of instantiating BannerView programmatically:
```
        Bundle bannerParams = new Bundle();
        bannerParams.putInt(BannerView.PARAM_KEY_AD_WIDTH, 300);
        bannerParams.putInt(BannerView.PARAM_KEY_AD_HEIGHT, 250);
        mBannerView = new BannerView(context, bannerParams);
```

The banner ad can be initialized, for example, in the Activity's onCreate() method.
```
        HashMap<String, String> environment = new HashMap<>();

        environment.put(VideoAd.Environment.KEY_ACCOUNT, ACCOUNT);
        environment.put(VideoAd.Environment.KEY_PLACEMENT_ID, placement);
        // This is only needed if banner is in ScrollView or similar
        environment.put(VideoAd.Environment.KEY_BANNER_AUTO_DISPLAY, Boolean.FALSE.toString());

        HashMap<String, String> params = new HashMap<>();
        params.put(VideoAd.Parameters.KEY_PUBLISHER, "Thirdpresence Sample App");
        params.put(VideoAd.Parameters.KEY_APP_NAME, "Thirdpresence Sample App");
        params.put(VideoAd.Parameters.KEY_APP_VERSION, "1.0");
 
        // In order to get more targeted ads you shall provide user's gender and year of birth
        // You can use e.g. Google+ API or Facebook Graph API
        // https://developers.google.com/android/reference/com/google/android/gms/plus/model/people/package-summary
        // https://developers.facebook.com/docs/android/graph/
        params.put(VideoAd.Parameters.KEY_USER_GENDER, "male");
        params.put(VideoAd.Parameters.KEY_USER_YOB, "1970");

        // When Google Play Services is available it is used to retrieves Google Advertiser ID.
        // Otherwise device ID (e.g. ANDROID_ID) shall be passed from the app.
        // params.put(VideoAd.Parameters.KEY_DEVICE_ID, "<ANDROID_ID>");

        BannerView bannerView = (BannerView)findViewById(R.id.bannerView);

        VideoAd ad = VideoAdManager.getInstance().create(VideoAd.PLACEMENT_TYPE_BANNER, mPlacementId);
        ad.setListener(new BannerActivity.SampleAppListener());
        
        ad.init(this, bannerView, environment, params, VideoAd.DEFAULT_TIMEOUT);
        
        // Video is displayed as soon as it is loaded
        ad.loadAd();
        
```

In case the banner view is in an ScrollView or similar where it might be not visible at the time the view is loaded then the ad should not be displayed before the view is actually visible. 
By default the ad is displayed automatically right after it is loaded. Set false value for environment map with key KEY_BANNER_AUTO_DISPLAY. Then when the view is visible call VideoBanner.displayAd();

See Sample App for detailed examples for each placement type.

[Sample App](../thirdpresence-ad-sdk-demo#thirdpresence-ad-sdk-for-android---sample-application)
[Demo App](../thirdpresence-ad-sdk-demo#thirdpresence-ad-sdk-for-android---demo-application)

### API reference

See Thirdpresence Ad SDK [JavaDoc](https://thirdpresence-ad-tags.s3.amazonaws.com/sdk/javadoc/android/1.5.4/index.html)


