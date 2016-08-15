# Thirdpresence Ad SDK For Android

Thirdpresence Ad SDK is based on a WebView and the Thirdpresence HTML5 player.  

It provides implementations for an interstitial video and rewarded video ad units. 

## Minimum requirements

- Android Studio
- Android API level 15 (Android 4.0.3)
- Google Play Services 8.0.4 (optional)
    - used for getting Google Advertising ID
    - for more information, see http://developer.android.com/google/play-services/setup.html

## Integration to an application

There are three different ways to integrate the SDK with your app:

1. Direct Integration
2. Plugin for Mopub or Admob mediation (rewarded video not yet available from Admob) 
3. Plugin for Unity3d

In all cases, you will need to download the appropriate SDK/plugin, add it to your app project and compile a new version of the app.

### Adding library dependencies

#### jCenter hosted library

Thirdpresence Ad SDK
[ ![Download](https://api.bintray.com/packages/thirdpresence/thirdpresence-ad-sdk-android/com.thirdpresence.adsdk.sdk/images/download.svg) ](https://bintray.com/thirdpresence/thirdpresence-ad-sdk-android/com.thirdpresence.adsdk.sdk/_latestVersion)

Mopub mediation
[ ![Download](https://api.bintray.com/packages/thirdpresence/thirdpresence-ad-sdk-android/com.thirdpresence.adsdk.mediation.mopub/images/download.svg) ](https://bintray.com/thirdpresence/thirdpresence-ad-sdk-android/com.thirdpresence.adsdk.mediation.mopub/_latestVersion)

Admob mediation
[ ![Download](https://api.bintray.com/packages/thirdpresence/thirdpresence-ad-sdk-android/com.thirdpresence.adsdk.mediation.admob/images/download.svg) ](https://bintray.com/thirdpresence/thirdpresence-ad-sdk-android/com.thirdpresence.adsdk.mediation.admob/_latestVersion)

Check that jcenter is included in the repositories block and add required dependencies to the dependencies block:
```
repositories {
    jcenter()
    // Backup repository if libraries not available from jcenter
    // maven { url 'http://dl.bintray.com/thirdpresence/thirdpresence-ad-sdk-android' }
}

dependencies {
	// SDK library
    compile 'com.thirdpresence.adsdk.sdk:thirdpresence-ad-sdk:1.2.5@aar'
    // mediation library, include if using MoPub SDK
    compile 'com.thirdpresence.adsdk.mediation.mopub:thirdpresence-mopub-mediation:1.2.5@aar'
    // mediation library, include if using Admob SDK
    compile 'com.thirdpresence.adsdk.mediation.admob:thirdpresence-admob-mediation:1.2.5    @aar'
    // Google Play Services mandatory for Admob mediation, otherwise optional but recommended
    compile 'com.google.android.gms:play-services:8.4.0'
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


### Direct integration

A quick guide to start showing ads on an application:

Add Internet permission to AndroidManifest.xml if it doesn't already exist:
```
<uses-permission android:name="android.permission.INTERNET"/> 
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
```

Example code for displaying an ad:
```
public class MyActivity extends AppCompatActivity implements VideoAd.Listener {

    private VideoInterstitial mVideoInterstitial;
    private boolean mAdLoaded = false;
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ...
        initAndLoadAd();
    }
    
    // Initialise ad unit as soon as possible
    private void initAndLoadAd() {
        // Instantiate the ad unit
        mVideoInterstitial = new VideoInterstitial();

        // Set the listener   	 
        mVideoInterstitial.setListener(this);

        // The data needed for the ad unit and the player is passed with two Map objects        
        Map<String, String> environment = new HashMap<>();
       
        // For the testing purposes use account name "sdk-demo" and placementid "sa7nvltbrn".
        environment.put(VideoAd.Environment.KEY_ACCOUNT, "<Thirdpresence account>");
        environment.put(VideoAd.Environment.KEY_PLACEMENT_ID, "<Thirdpresence placement id>");
       
        Map<String, String> params = new HashMap<>();
       
        params.put(VideoAd.Parameters.KEY_PUBLISHER, "<application name>");
        params.put(VideoAd.Parameters.KEY_APP_NAME, "<application name>");
        params.put(VideoAd.Parameters.KEY_APP_VERSION, "<application version>");
        params.put(VideoAd.Parameters.KEY_APP_STORE_URL, "<market store URL>");
                   
        // When Google Play Services is available it is used to retrieves Google Advertiser ID.
        // Otherwise device ID (e.g. ANDROID_ID) shall be passed from the app.
        // params.put(VideoAd.Parameters.KEY_DEVICE_ID, "<ANDROID_ID>");
    
        // Initialise the interstitial and load an ad
        mVideoInterstitial.init(activity, environment, params, VideoAd.DEFAULT_TIMEOUT);
        mVideoInterstitial.loadAd();
    }
    
    // Call displayAd() when the ad shall be shown
    private void displayAd() {
        if (mAdLoaded) {
	        mVideoInterstitial.displayAd();
	    }
	}
	
	// Release reserved resources when the ad unit is no longer needed
	private void cleanUp() {
	    if (mVideoInterstitial != null) {
		    mVideoInterstitial.remove();
            mVideoInterstitial.setListener(null);
            mVideoInterstitial = null;
        }
	}

    // From VideoAd.Listener
    public void onError(VideoAd.ErrorCode errorCode, String message) {
        // Player error has occured
        mAdLoaded = false;
        if (mVideoInterstitial != null) {
            mVideoInterstitial.remove();
        }
    }
    
    // From VideoAd.Listener
    public void onAdEvent(String eventName, String arg1, String arg2, String arg3) {
        if (eventName.equals(VideoAd.Events.AD_LOADED)) {
            mAdLoaded = true;
        } else if (eventName.equals(VideoAd.Events.AD_STOPPED))) {
            // Ad stopped 
            if (mVideoInterstitial != null) {
                mVideoIntertitial.remove();
            }
            mAdLoaded = false;
        } else if (eventName.equals(VideoAd.Events.AD_ERROR)) {
            // Showing an ad has failed
            mAdLoaded = false;
        } 
    }
}
```

Check out the Sample App code for a complete reference. 

### MoPub mediation

- Login to the MoPub console
- Create a Fullscreen Ad or Rewarded Video Ad ad unit or use an existing ad unit in one of your apps
- Create a new Custom Native Network (see detailed instructions here https://dev.twitter.com/mopub/ui-setup/network-setup-custom-native)
- Set Custom Event Class and Custom Event Class Data for the ad unit as follows:

| Ad Unit | Custom Event Class | Custom Event Class Data |
| --- | --- | --- |
| Fullscreen Ad | com.thirdpresence.adsdk.mediation.mopub. ThirdpresenceCustomEvent | { "account":"REPLACE_ME", "placementid":"REPLACE_ME", "appname":"REPLACE_ME", "appversion":"REPLACE_ME", "appstoreurl":"REPLACE_ME", "skipoffset":"REPLACE_ME"} |
| Rewarded Video | com.thirdpresence.adsdk.mediation.mopub. ThirdpresenceCustomEventRewardedVideo | { "account":"REPLACE_ME", "placementid":"REPLACE_ME", "appname":"REPLACE_ME", "appversion":"REPLACE_ME", "appstoreurl":"REPLACE_ME", "rewardtitle":"REPLACE_ME", "rewardamount":"REPLACE_ME"}  |

**Replace all the REPLACE_ME placeholders with actual values!**

The Custom Event Method field should be left blank.

For testing purposes you can use the account name "sdk-demo" and placementid "sa7nvltbrn".

- Go to the Segments tab on the Mopub console
- Select the segment where you want to enable the Thirdpresence custom native network
- Enable the network for this segment and set the CPM.
- Test the integration with the MoPub sample app, remember to include the Thirdpresence plugin in your project.

### Admob mediation

- Login to the Admob console
- Create new Interstitial ad unit for video if not exists
- In the ad units list, click "x ad source(s)" link on the Mediation column of the interstitial ad unit
- Click New ad network button
- Click "+ Custom event" button
- Fill the form:

| Field | Value |
| --- | --- |
| Class Name | com.thirdpresence.adsdk.mediation.admob.ThirdpresenceCustomEventInterstitial |
| Label | Thirdpresence |
| Parameter | account:REPLACE_ME,placementid:REPLACE_ME |

**Replace REPLACE_ME placeholders with actual values!**

For the testing purposes use account name "sdk-demo" and placementid "sa7nvltbrn".

- Click Continue button
- Give eCPM for the Thirdpresence ad network
- Save changes and the integration is ready

### Unity plugin

The Thirdpresence Ad SDK Unity plugin is compatible with Unity 5 or newer.

Get the Thirdpresence Ad SDK Unity plugin and import to your Unity project. 

The plugin can be downloaded from:
http://s3.amazonaws.com/thirdpresence-ad-tags/sdk/plugins/unity/1.2.5/thirdpresence-ad-sdk.unitypackage
 
In order to start getting ads the ThirdpresenceAdsAndroid singleton object needs to be initialised in an Unity script:
``` 
 #if UNITY_ANDROID
 	using TPR = ThirdpresenceAdsAndroid;
 #endif
  
```
The plugin supports interstitial and rewarded video ad units. 

An example for loading and displaying an interstitial ad:
``` 
// Initialise ad unit as soon as the app is ready to load an ad
private void initInterstitial() {

    // Subscribe to ad events and implement needed event handler methods.
    // See a list below for a full list of available events.
    TPR.OnThirdpresenceInterstitialLoaded -= InterstitialLoaded;
    TPR.OnThirdpresenceInterstitialLoaded += InterstitialLoaded;
    TPR.OnThirdpresenceInterstitialFailed -= InterstitialFailed;
    TPR.OnThirdpresenceInterstitialFailed += InterstitialFailed;
 
    // Create dictionary objects that hold the data needed to initialise the ad unit and the player.
    Dictionary<string, string> environment = new Dictionary<string, string>();
    environment.Add ("account", "REPLACE_ME"); // For the testing purposes use account name "sdk-demo" 
    environment.Add ("placementid", "REPLACE_ME"); // For the testing purposes use placement id "sa7nvltbrn". 
    environment.Add ("sdk-name", "Unity" + Application.platform);
    environment.Add ("sdk-version", Application.unityVersion);
 
    Dictionary<string, string> playerParams = new Dictionary<string, string>();
    playerParams.Add ("appname", Application.productName);
    playerParams.Add ("appversion", Application.version);
    playerParams.Add ("appstoreurl", "REPLACEME");
    playerParams.Add ("bundleid", Application.bundleIdentifier);
        
    long timeoutMs = 10000;

    // Initialise the interstitial
    TPR.initInterstitial (environment, playerParams, timeoutMs);
}	
    
// When an ad is loaded the event handler method is called
private void InterstitialLoaded() {
    // interstitial loaded
}

// When an ad load is failed the error handler method is called
private void InterstitialFailed(int errorCode, string errorText) {
    // failed to load interstitial ad
}

// Call showInterstitial when the ad shall be displayed 
private void showAd() {
    // InterstitialLoaded property can be used to check if the ad is loaded
    if (TPR.InterstitialLoaded) {
        TPR.showInterstitial ();
    }
}
```
Following events are available for the interstitial ad unit:
 
| Event | Description | 
| --- | --- |
| OnThirdpresenceInterstitialLoaded | Interstitial ad has been loaded |
| OnThirdpresenceInterstitialShown | Interstitial ad has been displayed |
| OnThirdpresenceInterstitialDismissed | Interstitial ad has been dismissed |
| OnThirdpresenceInterstitialFailed | Interstitial ad has failed to load |
| OnThirdpresenceInterstitialClicked | Interstitial ad has been clicked |

An example for loading and displaying a rewarded video ad:
``` 
// Initialise ad unit as soon as the app is ready to load an ad
private void initRewardedVideo() {

    // Subscribe to ad events and implement needed event handler methods.
    // See a list below for a full list of available events.
    TPR.OnThirdpresenceRewardedVideoLoaded -= RewardedVideoLoaded;
    TPR.OnThirdpresenceRewardedVideoLoaded += RewardedVideoLoaded;
    TPR.OnThirdpresenceRewardedVideoFailed -= RewardedVideoFailed;
    TPR.OnThirdpresenceRewardedVideoFailed += RewardedVideoFailed;
    TPR.OnThirdpresenceRewardedVideoCompleted -= RewardedVideoCompleted;
    TPR.OnThirdpresenceRewardedVideoCompleted += RewardedVideoCompleted;
            
    // Create dictionary objects that hold the data needed to initialise the ad unit and the player.
    Dictionary<string, string> environment = new Dictionary<string, string>();
    environment.Add ("account", "REPLACE_ME"); // For the testing purposes use account name "sdk-demo" 
    environment.Add ("placementid", "REPLACE_ME"); // For the testing purposes use placement id "sa7nvltbrn". 
    environment.Add ("sdk-name", "Unity" + Application.platform);
    environment.Add ("sdk-version", Application.unityVersion);

    // rewardtitle can be used as a virtual currency name. rewardamount is the amount of currency gained.
    environment.Add ("rewardtitle", "my-money");
    environment.Add ("rewardamount", "100");
 
    Dictionary<string, string> playerParams = new Dictionary<string, string>();
    playerParams.Add ("appname", Application.productName);
    playerParams.Add ("appversion", Application.version);
    playerParams.Add ("appstoreurl", "REPLACEME");
    playerParams.Add ("bundleid", Application.bundleIdentifier);
        
    long timeoutMs = 10000;

    // Initialise the rewarded video
    TPR.initRewardedVideo (environment, playerParams, timeoutMs);
}	
    
// When an ad is loaded the event handler method is called
private void RewardedVideoLoaded() {
}

// When the ad load is failed the error handler method is called
private void RewardedVideoFailed(int errorCode, string errorText) {
    // failed to load rewarded video ad
}

// When the user has watched the video the completed handler is called
private void RewardedVideoCompleted(string rewardTitle, int rewardAmount) {
    // User has earned the reward
}

// Call showRewardedVideo when the ad shall be displayed 
private void showAd() {
    // RewardedVideoLoaded property can be used to check if the ad is loaded
    if (TPR.RewardedVideoLoaded) {
        TPR.showRewardedVideo ();
    }
}
```
Following events are available for the rewarded video ad unit:

| Event | Description | 
| --- | --- |
| OnThirdpresenceRewardedVideoLoaded | Rewarded video ad has been loaded |
| OnThirdpresenceRewardedVideoShown | Rewarded video ad has been displayed |
| OnThirdpresenceRewardedVideoDismissed | Rewarded video ad has been dismissed |
| OnThirdpresenceRewardedVideoFailed | Rewarded video ad has failed to load |
| OnThirdpresenceRewardedVideoClicked | Rewarded video ad has been clicked |
| OnThirdpresenceRewardedVideoCompleted | Rewarded video ad has been completed  |
| OnThirdpresenceRewardedVideoAdLeftApplication | Rewarded video ad has opened an another app |

