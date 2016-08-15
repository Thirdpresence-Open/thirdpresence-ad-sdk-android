
using System;
using UnityEngine;
using System.Collections;
using System.Collections.Generic;

#if UNITY_ANDROID

public delegate void ThirdpresenceInterstitialLoaded();
public delegate void ThirdpresenceInterstitialShown();
public delegate void ThirdpresenceInterstitialDismissed();
public delegate void ThirdpresenceInterstitialFailed(int errorCode, string errorText);
public delegate void ThirdpresenceInterstitialClicked();


public delegate void ThirdpresenceRewardedVideoLoaded();
public delegate void ThirdpresenceRewardedVideoShown();
public delegate void ThirdpresenceRewardedVideoDismissed();
public delegate void ThirdpresenceRewardedVideoFailed(int errorCode, string errorText);
public delegate void ThirdpresenceRewardedVideoClicked();
public delegate void ThirdpresenceRewardedVideoCompleted(string rewardTitle, int rewardAmount);
public delegate void ThirdpresenceRewardedVideoAdLeftApplication();

public class ThirdpresenceAdsAndroid
{
	private static AndroidJavaObject interstitialPlugin = null;
	private static AndroidJavaObject rewardedVideoPlugin = null;
	private static AndroidJavaObject activityContext = null;

	static ThirdpresenceAdsAndroid()
	{
		if( Application.platform != RuntimePlatform.Android )
			return;
		
		using(AndroidJavaClass activityClass = new AndroidJavaClass("com.unity3d.player.UnityPlayer")) {
			activityContext = activityClass.GetStatic<AndroidJavaObject>("currentActivity");
		}
	}

	public static event ThirdpresenceInterstitialLoaded OnThirdpresenceInterstitialLoaded;
	public static event ThirdpresenceInterstitialShown OnThirdpresenceInterstitialShown;
	public static event ThirdpresenceInterstitialDismissed OnThirdpresenceInterstitialDismissed;
	public static event ThirdpresenceInterstitialFailed OnThirdpresenceInterstitialFailed;
	public static event ThirdpresenceInterstitialClicked OnThirdpresenceInterstitialClicked;

	public class InterstitialListener : AndroidJavaProxy
    {
        public InterstitialListener () : base("com.thirdpresence.adsdk.mediation.unity.ThirdpresenceInterstitialAdapter$InterstitialListener") {}

        public void onInterstitialLoaded() {
			interstitialLoaded = true;
            if (OnThirdpresenceInterstitialLoaded != null) {
                OnThirdpresenceInterstitialLoaded ();
            }
        }

        public void onInterstitialShown() {
			interstitialLoaded = false;
            if (OnThirdpresenceInterstitialShown != null) {
                OnThirdpresenceInterstitialShown ();
            }
        }

        public void onInterstitialDismissed() {
			interstitialLoaded = false;
            if (OnThirdpresenceInterstitialDismissed != null) {
                OnThirdpresenceInterstitialDismissed ();
            }
        }

        public void onInterstitialFailed(int errorCode, string errorText) {
			interstitialLoaded = false;
            if (OnThirdpresenceInterstitialFailed != null) {
                OnThirdpresenceInterstitialFailed (errorCode, errorText);
            }
        }

        public void onInterstitialClicked() {
            if (OnThirdpresenceInterstitialClicked != null) {
                OnThirdpresenceInterstitialClicked ();
            }
        }
    }

	public static void InitInterstitial(Dictionary<string, string> environment, Dictionary<string, string> playerParams, long timeout) 
	{
        if( Application.platform != RuntimePlatform.Android )
            return;

		using (var pluginClass = new AndroidJavaClass ("com.thirdpresence.adsdk.mediation.unity.ThirdpresenceInterstitialAdapter")) {
			interstitialPlugin = pluginClass.CallStatic<AndroidJavaObject> ("getInstance");
			interstitialPlugin.Call ("setListener", new InterstitialListener ());
		}

		activityContext.Call ("runOnUiThread", new AndroidJavaRunnable (() => {
			using (AndroidJavaObject envMap = new AndroidJavaObject ("java.util.HashMap")) {

				foreach(KeyValuePair<string, string> entry in environment)
					AddToHashMap(envMap, entry.Key, entry.Value);

				using (AndroidJavaObject playerMap = new AndroidJavaObject ("java.util.HashMap")) {
					foreach(KeyValuePair<string, string> entry in playerParams)
						AddToHashMap(playerMap, entry.Key, entry.Value);
					interstitialPlugin.Call ("initInterstitial", activityContext, envMap, playerMap, timeout);
				}
			}
		}));
	}
		
	public static void ShowInterstitial()
	{
		if( Application.platform != RuntimePlatform.Android )
			return;

		activityContext.Call ("runOnUiThread", new AndroidJavaRunnable (() => {
			interstitialPlugin.Call ("showInterstitial");
		}));
	}

	public static void RemoveInterstitial()
	{
		if( Application.platform != RuntimePlatform.Android )
			return;

		interstitialLoaded = false;
		activityContext.Call ("runOnUiThread", new AndroidJavaRunnable (() => {
			interstitialPlugin.Call ("removeInterstitial");
		}));
	}

	private static bool interstitialLoaded = false;
	public static bool InterstitialLoaded
	{
		get
		{
			return interstitialLoaded;
		}
	}

	public static event ThirdpresenceRewardedVideoLoaded OnThirdpresenceRewardedVideoLoaded;
	public static event ThirdpresenceRewardedVideoShown OnThirdpresenceRewardedVideoShown;
	public static event ThirdpresenceRewardedVideoDismissed OnThirdpresenceRewardedVideoDismissed;
	public static event ThirdpresenceRewardedVideoFailed OnThirdpresenceRewardedVideoFailed;
	public static event ThirdpresenceRewardedVideoClicked OnThirdpresenceRewardedVideoClicked;
    public static event ThirdpresenceRewardedVideoCompleted OnThirdpresenceRewardedVideoCompleted;
    public static event ThirdpresenceRewardedVideoAdLeftApplication OnThirdpresenceRewardedAdLeftApplication;

	public class RewardedVideoListener : AndroidJavaProxy
	{
		public RewardedVideoListener () : base("com.thirdpresence.adsdk.mediation.unity.ThirdpresenceRewardedVideoAdapter$RewardedVideoListener") {}

		public void onRewardedVideoLoaded() {
			rewardedVideoLoaded = true;
			if (OnThirdpresenceRewardedVideoLoaded != null) {
				OnThirdpresenceRewardedVideoLoaded ();
			}
		}

		public void onRewardedVideoShown() {
			rewardedVideoLoaded = false;
			if (OnThirdpresenceRewardedVideoShown != null) {
				OnThirdpresenceRewardedVideoShown ();
			}
		}

		public void onRewardedVideoDismissed() {
			rewardedVideoLoaded = false;
			if (OnThirdpresenceRewardedVideoDismissed != null) {
				OnThirdpresenceRewardedVideoDismissed ();
			}
		}

		public void onRewardedVideoFailed(int errorCode, string errorText) {
			rewardedVideoLoaded = false;
			if (OnThirdpresenceRewardedVideoFailed != null) {
				OnThirdpresenceRewardedVideoFailed (errorCode, errorText);
			}
		}

		public void onRewardedVideoClicked() {
			if (OnThirdpresenceRewardedVideoClicked != null) {
				OnThirdpresenceRewardedVideoClicked ();
			}
		}

        public void onRewardedVideoCompleted(string rewardTitle, int rewardAmount) {
            if (OnThirdpresenceRewardedVideoCompleted != null) {
                OnThirdpresenceRewardedVideoCompleted (rewardTitle, rewardAmount);
            }
        }

        public void onRewardedVideoAdLeftApplication() {
            if (OnThirdpresenceRewardedAdLeftApplication != null) {
                OnThirdpresenceRewardedAdLeftApplication ();
            }
        }

	}

	public static void InitRewardedVideo(Dictionary<string, string> environment, Dictionary<string, string> playerParams, long timeout)
	{
        if( Application.platform != RuntimePlatform.Android )
            return;

		using (var pluginClass = new AndroidJavaClass ("com.thirdpresence.adsdk.mediation.unity.ThirdpresenceRewardedVideoAdapter")) {
			rewardedVideoPlugin = pluginClass.CallStatic<AndroidJavaObject> ("getInstance");
			rewardedVideoPlugin.Call ("setListener", new RewardedVideoListener ());
		}

		activityContext.Call ("runOnUiThread", new AndroidJavaRunnable (() => {
			using (AndroidJavaObject envMap = new AndroidJavaObject ("java.util.HashMap")) {

				foreach(KeyValuePair<string, string> entry in environment)
					AddToHashMap(envMap, entry.Key, entry.Value);

				using (AndroidJavaObject playerMap = new AndroidJavaObject ("java.util.HashMap")) {
					foreach(KeyValuePair<string, string> entry in playerParams)
						AddToHashMap(playerMap, entry.Key, entry.Value);
					rewardedVideoPlugin.Call ("initRewardedVideo", activityContext, envMap, playerMap, timeout);
				}
			}
		}));
	}

    public static void ShowRewardedVideo()
    {
        if( Application.platform != RuntimePlatform.Android )
            return;

        activityContext.Call ("runOnUiThread", new AndroidJavaRunnable (() => {
            rewardedVideoPlugin.Call ("showRewardedVideo");
        }));
    }

    public static void RemoveRewardedVideo()
    {
        if( Application.platform != RuntimePlatform.Android )
            return;

		rewardedVideoLoaded = false;
        activityContext.Call ("runOnUiThread", new AndroidJavaRunnable (() => {
            rewardedVideoPlugin.Call ("removeRewardedVideo");
        }));
    }

	private static bool rewardedVideoLoaded = false;
	public static bool RewardedVideoLoaded {
		get {
			return rewardedVideoLoaded;
		}
	}

	private static void AddToHashMap(AndroidJavaObject map, String k, String v) {
		IntPtr putMethod = AndroidJNIHelper.GetMethodID(
			map.GetRawClass(), "put",
			"(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
		
		using (AndroidJavaObject key = new AndroidJavaObject ("java.lang.String", k)) {
			using (AndroidJavaObject value = new AndroidJavaObject ("java.lang.String", v)) {
				object[] args = new object[2];
				args [0] = key;
				args [1] = value;
				AndroidJNI.CallObjectMethod(
					map.GetRawObject(),
					putMethod, 
					AndroidJNIHelper.CreateJNIArgArray(args));
			}
		}
	}
}
	
#endif


