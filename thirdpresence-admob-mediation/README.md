# Thirdpresence Ad SDK for Android - Admob mediation

Thirdpresence Ad SDK Admob mediation enables displaying Thirdpresence video ads in an application that has the Admob SDK integrated.

https://firebase.google.com/docs/admob/

## Minimum requirements

- Android Studio
- Android API level 16 (Android 4.1)
- Google Play Services Ads 10.2.1

## Importing SDK libraries

### jCenter hosted library

Thirdpresence Ad SDK
[ ![Download](https://api.bintray.com/packages/thirdpresence/thirdpresence-ad-sdk-android/com.thirdpresence.adsdk.sdk/images/download.svg) ](https://bintray.com/thirdpresence/thirdpresence-ad-sdk-android/com.thirdpresence.adsdk.sdk/_latestVersion)

Admob mediation
[ ![Download](https://api.bintray.com/packages/thirdpresence/thirdpresence-ad-sdk-android/com.thirdpresence.adsdk.mediation.admob/images/download.svg) ](https://bintray.com/thirdpresence/thirdpresence-ad-sdk-android/com.thirdpresence.adsdk.mediation.admob/_latestVersion)

Modify the build.gradle file to include the required libraries:
```
repositories {
    google()
    jcenter()
    // Backup repository if libraries not available from jcenter
    // maven { url 'http://dl.bintray.com/thirdpresence/thirdpresence-ad-sdk-android' }
}

dependencies {
	// SDK library and admob mediation plugin
    compile 'com.thirdpresence.adsdk.sdk:thirdpresence-ad-sdk:1.5.7@aar'
    compile 'com.thirdpresence.adsdk.mediation.admob:thirdpresence-admob-mediation:1.5.7@aar'
    // Google Play Services 
    compile 'com.google.android.gms:play-services-ads:10.2.1'
    // Google Support libraries
    compile 'com.android.support:support-compat:25.3.1'
    compile 'com.android.support:support-core-utils:25.3.1'
}
```

### Manually building SDK libraries

If manual building of the SDK is preferred, see [Thirdpresence Ad SDK - Manually building the SDK](../thirdpresence-ad-sdk/#manually-building-the-sdk) 

## Application permissions

Add the required permissions to AndroidManifest.xml, if they do not already exist.
The location permission is optional, but highly recommended to get a higher fill rate.
```
<uses-permission android:name="android.permission.INTERNET"/> 
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>

```

## Creating an ad unit

- Login to the Admob console
- Create a new ad unit for video, if one does not exist. Following ad units are supported:
    - Banner (Automatic refresh shall be "no refresh" or min. 60 seconds)
    - Interstitial
    - Rewarded interstitial 
- In the ad units list, click the "x ad source(s)" link on the Mediation column of the interstitial ad unit
- Click the New ad network button
- Click the "+ Custom event" button
- Fill in the form:

[ Ad Unit | Class Name | Parameter |
| --- | --- | --- |
| Banner | com.thirdpresence.adsdk.mediation.admob.ThirdpresenceCustomEventBanner | account:REPLACE_ME,placementid:REPLACE_ME |
| Interstitial | com.thirdpresence.adsdk.mediation.admob.ThirdpresenceCustomEventInterstitial | account:REPLACE_ME,placementid:REPLACE_ME |
| Rewarded interstitial | com.thirdpresence.adsdk.mediation.admob.ThirdpresenceRewardedVideoAdapter | account:REPLACE_ME,placementid:REPLACE_ME,rewardtitle:REPLACE_ME;rewardamount:REPLACE_ME |

**Replace REPLACE_ME placeholders with actual values!**

For the rewarded video the reward title and reward amount values are mandatory.

For testing purposes, use account name "sdk-demo" and following placement ids:
 
| Ad Unit | Placement Id | 
| --- | --- | 
| Banner | zhlwlm9280 | 
| Interstitial | sa7nvltbrn |
| Rewarded interstitial | nhdfxqclej |

- Click the Continue button
- Give eCPM for the Thirdpresence ad network
- Save changes, and the integration is ready

