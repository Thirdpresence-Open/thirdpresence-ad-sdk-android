# Thirdpresence Ad SDK for Android - Unity plugin

The Thirdpresence Ad SDK Unity plugin provides means to display interstitial and rewarded video ads on a Unity application.

## Minimum requirements

- Unity SDK 5 or newer
- Android API level 16 (Android 4.1)

## Getting Unity plugin package

The pre-built plugin is available here:
https://thirdpresence-ad-tags.s3.amazonaws.com/sdk/plugins/unity/1.5.7/thirdpresence-ad-sdk-android.unitypackage   

The plugin is built with the following tools:

- Unity SDK 2017.2.0f3
- Java JDK 1.8
- Android 8.0 (Api level 26)

Another option is to manually build the SDK and the Unity plugin. All the source code is available in this repository.
In that case, local.properties must be updated to inform builds tools on the path of the Unity Editor. For example:
```  
 unity.editor=/Applications/Unity/Unity.app/Contents/MacOS/Unity
``` 

## Importing plugin package

- Open an application project in the Unity Editor
- Select Assets -> Import Package -> Custom Package from the main menu
- Locate the Unity plugin package file and open it
- Import all files, and the plugin is available in the project

## Build settings

Set the build system to Gradle (new) instead of the internal (default).

Additionally ensure following settings in the Player settings:

- Select Settings for Android (Android icon)
- Set minimum API level in the Other settings at least to 16
- Specify key store and key for the signing in the Publishing settings

## Integration 

To start getting ads, the ThirdpresenceAdsAndroid singleton object must be initialised in a Unity script:
``` 
 #if UNITY_ANDROID
 	using TPR = ThirdpresenceAdsAndroid;
 #endif
  
```
The plugin supports interstitial and rewarded video ad units. 

### Interstitial

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
            
    // In order to get more targeted ads you shall provide user's gender and year of birth
    playerParams.Add ("gender", "male");
    playerParams.Add ("yob", "1975");
                
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
The events below are available for the interstitial ad unit:
 
| Event | Description | 
| --- | --- |
| OnThirdpresenceInterstitialLoaded | Interstitial ad has been loaded |
| OnThirdpresenceInterstitialShown | Interstitial ad has been displayed |
| OnThirdpresenceInterstitialDismissed | Interstitial ad has been dismissed |
| OnThirdpresenceInterstitialFailed | Interstitial ad has failed to load |
| OnThirdpresenceInterstitialClicked | Interstitial ad has been clicked |

### Rewarded video

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
    playerParams.Add ("bundleid", Application.bundleIdentifier);
                        
    // In order to get more targeted ads you shall provide user's gender and year of birth
    playerParams.Add ("gender", "male");
    playerParams.Add ("yob", "1975");
    
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
The events below are available for the rewarded video ad unit:

| Event | Description | 
| --- | --- |
| OnThirdpresenceRewardedVideoLoaded | Rewarded video ad has been loaded |
| OnThirdpresenceRewardedVideoShown | Rewarded video ad has been displayed |
| OnThirdpresenceRewardedVideoDismissed | Rewarded video ad has been dismissed |
| OnThirdpresenceRewardedVideoFailed | Rewarded video ad has failed to load |
| OnThirdpresenceRewardedVideoClicked | Rewarded video ad has been clicked |
| OnThirdpresenceRewardedVideoCompleted | Rewarded video ad has been completed  |
| OnThirdpresenceRewardedVideoAdLeftApplication | Rewarded video ad has opened an another app |

