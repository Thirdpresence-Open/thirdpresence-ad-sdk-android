# Thirdpresence Ad SDK for Android

Thirdpresence Ad SDK provides an API to display interstitial and rewarded video ads in an application.

## Minimum requirements

- Android Studio
- Android API level 15 (Android 4.0.3)
- Google Play Services Ads 9.6.1 (optional, but highly recommended)
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
    compile 'com.thirdpresence.adsdk.sdk:thirdpresence-ad-sdk:1.4.0@aar'
    // Google Play Services 
    compile 'com.google.android.gms:play-services-ads:9.6.1'
    // Google Support libraries
    compile 'com.android.support:support-compat:24.2.1'
    compile 'com.android.support:support-core-utils:24.2.1'
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

Example code for initialising the placement and displaying an ad:
```
    // Call initInterstitial() as soon as there is an activity available.
    public static void initInterstitial(Activity activity, String placementID) {
        HashMap<String, String> environment = new HashMap<>();

        environment.put(VideoAd.Environment.KEY_ACCOUNT, ACCOUNT);
        environment.put(VideoAd.Environment.KEY_PLACEMENT_ID, placementID);

        HashMap<String, String> params = new HashMap<>();
        params.put(VideoAd.Parameters.KEY_PUBLISHER, "My Application");
        params.put(VideoAd.Parameters.KEY_APP_NAME, "My Application"));
        params.put(VideoAd.Parameters.KEY_APP_VERSION, "1.0");
        params.put(VideoAd.Parameters.KEY_APP_STORE_URL, "https://play.google.com/store/apps/details?id=com.thirdpresence.adsdk.demo");

        // In order to get more targeted ads you shall provide user's gender and year of birth
        // You can use e.g. Google+ API or Facebook Graph API
        // https://developers.google.com/android/reference/com/google/android/gms/plus/model/people/package-summary
        // https://developers.facebook.com/docs/android/graph/
        params.put(VideoAd.Parameters.KEY_USER_GENDER, "male");
        params.put(VideoAd.Parameters.KEY_USER_YOB, "1970");

        // VideoAdManager is a helper class for getting access to the VideoAd instance
        VideoAd ad = VideoAdManager.getInstance().create(VideoAd.PLACEMENT_TYPE_INTERSTITIAL, placementID);
        ad.init(activity, environment, params, VideoAd.DEFAULT_TIMEOUT);
        ad.loadAd();
    }

    // When going to next level in the app check if an ad is available
    public void goToNextLevel(String placementId) {
        final VideoAd ad = VideoAdManager.getInstance().get(placementId);
        if (ad != null && ad.isAdLoaded()) {
            ad.displayAd(new Runnable() {
                @Override
                public void run() {
                    // Close the placement and load a new ad
                    ad.resetAndLoadAd();
                    startNextLevel();
                }
             });
        } else {
             startNextLevel();
        }
    }
    
    
```

See [Sample App](../thirdpresence-ad-sdk-demo#thirdpresence-ad-sdk-for-android---sample-application) and
[Demo App](../thirdpresence-ad-sdk-demo#thirdpresence-ad-sdk-for-android---demo-application) for a complete reference. 

### API reference

See Thirdpresence Ad SDK [JavaDoc](http://s3.amazonaws.com/thirdpresence-ad-tags/sdk/javadoc/android/1.4.0/index.html)


