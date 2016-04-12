# Thirdpresence Ad SDK For Android

Thirdpresence Ad SDK is based on WebView and Thirdpresence HTML5 player.  

It provides VideoInterstitial ad unit implementation

## Minimum requirements

- Android Studio
- Android API level 15 (Android 4.0.3)
- Googl Play Services 8.0.4 (see http://developer.android.com/google/play-services/setup.html)

## Integration to an application

There are two options to integrate the SDK:
1. Direct Integration
2. Mediation with existing SDK (e.g. MoPub)

Available mediation plugins:
- MoPub interstitial
- MoPub rewarded video

### Adding library dependencies

#### jCenter hosted library
 
Check that jcenter is included in the repositories block and add required dependencies to the dependencies block:
```

repositories {
    jcenter()
}

dependencies {
	// SDK library
    compile 'com.thirdpresence.adsdk.sdk:thirdpresence-ad-sdk:1.1.0@aar'
    // External dependencies
    compile 'com.google.android.gms:play-services:8.4.0'
    // mediation library, include if using MoPub SDK
    compile 'com.thirdpresence.adsdk.mediation.mopub:thirdpresence-mopub-mediation:1.1.0@aar'

}
```

#### Manually building the SDK

- Open SDK project
- Make project and Build APK
- Copy thirdpresence-ad-sdk/build/outputs/aar/thirdpresence-ad-sdk-release.aar to your /libs/ folder in the application project
- Add the library to the your application build 
	- File -> New... -> New Module... -> Import .JAR/.AAR Package -> Next
	- Select thirdpresence-ad-sdk-release.aar file and give name for the module -> Finish
- Do the same for a mediation library if needed



### Direct integration:

Quick guide to start showing ads:

Add Internet permission to AndroidManifest.xml if not already exist:
<uses-permission android:name="android.permission.INTERNET"/> 

Implement VideoAd.Listener interface:
```
    public void onError(VideoAd.ErrorCode errorCode, String message) {
    	// Player error has occured
        if (mVideoInterstitial != null) {
            mVideoInterstitial.remove();
        }
    }

    public void onAdEvent(String eventName, String arg1, String arg2, String arg3) {
        if (eventName.equals(VideoAd.Events.AD_LOADED)) {
        	// An ad is loaded
      	} else if (eventName.equals(VideoAd.Events.AD_STOPPED))) {
            // Ad stopped 
            mVideoIntertitial.remove();
        } else if (eventName.equals(VideoAd.Events.AD_ERROR)) {
            // Showing an ad has failed
        } 
    }
```

Instantiate the ad unit:
```
    mVideoInterstitial = new VideoInterstitial();
```
Set the listener:  
``` 	 
    mVideoInterstitial.setListener(this);
```
Initialise the ad unit:
```
    Map<String, String> environment = new HashMap<>();
   
	environment.put(VideoAd.Environment.KEY_ACCOUNT, "<Thirdpresence account>");
	environment.put(VideoAd.Environment.KEY_PLACEMENT_ID, "<Thirdpresence placement id>");
   
    Map<String, String> params = new HashMap<>();
   
    params.put(VideoAd.Parameters.KEY_PUBLISHER, "<application name>");
    params.put(VideoAd.Parameters.KEY_APP_NAME, "<application name>");
    params.put(VideoAd.Parameters.KEY_APP_VERSION, "<application version>");
    params.put(VideoAd.Parameters.KEY_APP_STORE_URL, "<market store URL>");
               
    mVideoInterstitial.init(activity, environment, params, VideoAd.);
```        
Load an ad:
```        
    mVideoInterstitial.loadAd();
```
Display the ad:
```
	mVideoInterstitial.displayAd();
```
Close the ad unit and clean up:
```
	mVideoInterstitial.remove();
    mVideoInterstitial.setListener(null);
    mVideoInterstitial = null;
```

Check out Sample App for reference.

### MoPub mediation:

- Login to MoPub console
- Create Fullscreen Ad or Rewarded Video Ad ad unit
- Add new Custom Native Network
- Set Custom Event Class and Custom Event Class Data for the ad unit

| Ad Unit | Custom Event Class | Custom Event Class Data |
| --- | --- | --- |
| Fullscreen Ad | ThirdpresenceCustomEvent | { "account":"REPLACE_ME", "playerid":"REPLACE_ME", "appname":"REPLACE_ME", "appversion":"REPLACE_ME", "appstoreurl":"REPLACE_ME", "skipoffset":"REPLACE_ME"} |
| Rewarded Video | ThirdpresenceCustomEventRewardedVideo | { "account":"REPLACE_ME", "playerid":"REPLACE_ME", "appname":"REPLACE_ME", "appversion":"REPLACE_ME", "appstoreurl":"REPLACE_ME", "rewardtitle":"REPLACE_ME", "rewardamount":"REPLACE_ME"}  |

Replace placeholders with the actual data.

- Go to Segments
- Select the segment where you want to enable the network
- Enable the network you just created and set the CPM.
- Test the integration with the MoPub sample app

