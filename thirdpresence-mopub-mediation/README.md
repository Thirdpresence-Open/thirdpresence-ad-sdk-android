# Thirdpresence Ad SDK for Android - MoPub mediation

Thirdpresence Ad SDK MoPub mediation enables displaying Thirdpresence video ads in an application that has the MoPub SDK integrated.

http://www.mopub.com/

## Minimum requirements

- Android Studio
- Android API level 16 (Android 4.1)
- Google Play Services Ads 10.2.1 (optional, but highly recommended)
    - used for getting the Google Advertising ID
    - for more information, see http://developer.android.com/google/play-services/setup.html
- MoPub SDK 4.5

## Importing SDK libraries

### jCenter hosted library

Thirdpresence Ad SDK
[ ![Download](https://api.bintray.com/packages/thirdpresence/thirdpresence-ad-sdk-android/com.thirdpresence.adsdk.sdk/images/download.svg) ](https://bintray.com/thirdpresence/thirdpresence-ad-sdk-android/com.thirdpresence.adsdk.sdk/_latestVersion)

Mopub mediation
[ ![Download](https://api.bintray.com/packages/thirdpresence/thirdpresence-ad-sdk-android/com.thirdpresence.adsdk.mediation.mopub/images/download.svg) ](https://bintray.com/thirdpresence/thirdpresence-ad-sdk-android/com.thirdpresence.adsdk.mediation.mopub/_latestVersion)

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
    compile 'com.thirdpresence.adsdk.mediation.mopub:thirdpresence-mopub-mediation:1.5.7@aar'
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

- Login to the MoPub console
- Create a Fullscreen Ad or Rewarded Video Ad ad unit, or use an existing ad unit in one of your apps
- Create a new Custom Native Network (for detailed instructions, see https://dev.twitter.com/mopub/ui-setup/network-setup-custom-native)
- Set Custom Event Class and Custom Event Class Data for the ad unit as follows:

| Ad Unit | Custom Event Class | Custom Event Class Data |
| --- | --- | --- |
| Medium Ad | com.thirdpresence.adsdk.mediation.mopub. ThirdpresenceCustomEventBanner | { "account":"REPLACE_ME", "placementid":"REPLACE_ME", "appname":"REPLACE_ME", "appversion":"REPLACE_ME"} |
| Fullscreen Ad | com.thirdpresence.adsdk.mediation.mopub. ThirdpresenceCustomEvent | { "account":"REPLACE_ME", "placementid":"REPLACE_ME", "appname":"REPLACE_ME", "appversion":"REPLACE_ME"} |
| Rewarded Video | com.thirdpresence.adsdk.mediation.mopub. ThirdpresenceCustomEventRewardedVideo | { "account":"REPLACE_ME", "placementid":"REPLACE_ME", "appname":"REPLACE_ME", "appversion":"REPLACE_ME", "rewardtitle":"REPLACE_ME", "rewardamount":"REPLACE_ME" }  |

The Custom Event Method field should be left blank.

**Replace all the REPLACE_ME placeholders with actual values!**

For the rewarded video the reward title and reward amount values are mandatory.

For testing purposes, use account name "sdk-demo" and following placement ids:
 
|  Ad Unit | Placement Id | 
| --- | --- |
| Medium ad | zhlwlm9280 | 
| Interstitial | sa7nvltbrn |
| Rewarded video | nhdfxqclej |

- Open the Segments tab on the Mopub console
- Select the segment where you want to enable the Thirdpresence custom native network
- Enable the network for this segment, and set the CPM
- Test the integration with the MoPub sample app. Remember to include the Thirdpresence plugin in your project.

## Passing user info and keywords

Thirdpresence can provide more targeted ads if user info and keywords are passed. In order to do that the data needs to be added to MoPub ad request.
An example for a Medium ad unit below. The interstitial unit is done similar way.
```
    String userGender = "male"; // allowed values are "male" or "female"
    String userYearOfBirth = "1976";
    String keywords = "advertising,sdk,programming"; // comma-separated string of keyworads

    Map<String,Object> extras = new HashMap<String,Object>();
    extras.put("tpr_gender", userGender);
    extras.put("tpr_yob", userYearOfBirth);
    extras.put("tpr_keywords", keywords);
    mMoPubView.setLocalExtras(extras);
    mMoPubView.loadAd();
        
```

An example for Rewarded video ad unit.
```
    String userGender = "male"; // allowed values are "male" or "female"
    String userYearOfBirth = "1976";
    String keywords = "advertising,sdk,programming"; // comma-separated string of keyworads

    final ThirdpresenceCustomEventRewardedVideo.ThirdpresenceMediationSettings mediationSettings =
           new ThirdpresenceCustomEventRewardedVideo.ThirdpresenceMediationSettings.Builder()
                 .gender(userGender)
                 .yearOfBirth(userYearOfBirth)
                 .keywords(keywords)
                 .build();

    MoPub.initializeRewardedVideo(getActivity(), mediationSettings);
```