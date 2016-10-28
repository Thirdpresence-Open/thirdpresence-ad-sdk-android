# Thirdpresence Ad SDK for Android - Admob mediation

Thirdpresence Ad SDK Admob mediation enables displaying Thirdpresence video ads in an application that has the Admob SDK integrated.

https://firebase.google.com/docs/admob/

## Minimum requirements

- Android Studio
- Android API level 15 (Android 4.0.3)
- Google Play Services Ads 9.6.1

## Importing SDK libraries

### jCenter hosted library

Thirdpresence Ad SDK
[ ![Download](https://api.bintray.com/packages/thirdpresence/thirdpresence-ad-sdk-android/com.thirdpresence.adsdk.sdk/images/download.svg) ](https://bintray.com/thirdpresence/thirdpresence-ad-sdk-android/com.thirdpresence.adsdk.sdk/_latestVersion)

Admob mediation
[ ![Download](https://api.bintray.com/packages/thirdpresence/thirdpresence-ad-sdk-android/com.thirdpresence.adsdk.mediation.admob/images/download.svg) ](https://bintray.com/thirdpresence/thirdpresence-ad-sdk-android/com.thirdpresence.adsdk.mediation.admob/_latestVersion)

Modify the build.gradle file to include the required libraries:
```
repositories {
    jcenter()
    // Backup repository if libraries not available from jcenter
    // maven { url 'http://dl.bintray.com/thirdpresence/thirdpresence-ad-sdk-android' }
}

dependencies {
	// SDK library and admob mediation plugin
    compile 'com.thirdpresence.adsdk.sdk:thirdpresence-ad-sdk:1.4.0@aar'
    compile 'com.thirdpresence.adsdk.mediation.admob:thirdpresence-admob-mediation:1.4.0@aar'
    // Google Play Services 
    compile 'com.google.android.gms:play-services-ads:9.6.1'
    // Google Support libraries
    compile 'com.android.support:support-compat:24.2.1'
    compile 'com.android.support:support-core-utils:24.2.1'
}
```

### Manually building SDK libraries

If manual building of the SDK is preferred, see [Thirdpresence Ad SDK - Manually building the SDK] (../thirdpresence-ad-sdk/#manually-building-the-sdk) 

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
- Create a new Interstitial ad unit for video, if one does not exist
- In the ad units list, click the "x ad source(s)" link on the Mediation column of the interstitial ad unit
- Click New ad network button
- Click the "+ Custom event" button
- Fill in the form:

| Field | Value |
| --- | --- |
| Class Name | com.thirdpresence.adsdk.mediation.admob.ThirdpresenceCustomEventInterstitial |
| Label | Thirdpresence |
| Parameter | account:REPLACE_ME,placementid:REPLACE_ME,gender:REPLACE_ME,yob:REPLACE_ME |

**Replace REPLACE_ME placeholders with actual values!**

For testing purposes, use account name "sdk-demo" and placementid "sa7nvltbrn".
Provide the user's gender and yob (year of birth) to get more targeted ads. Leave them empty if the information is not available.

- Click the Continue button
- Give eCPM for the Thirdpresence ad network
- Save changes, and the integration is ready
