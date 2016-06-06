
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

public class ThirdpresenceAdsAndroid
{
	public static event ThirdpresenceInterstitialLoaded OnThirdpresenceInterstitialLoaded;
	public static event ThirdpresenceInterstitialShown OnThirdpresenceInterstitialShown;
	public static event ThirdpresenceInterstitialDismissed OnThirdpresenceInterstitialDismissed;
	public static event ThirdpresenceInterstitialFailed OnThirdpresenceInterstitialFailed;
	public static event ThirdpresenceInterstitialClicked OnThirdpresenceInterstitialClicked;

	private static AndroidJavaObject tprPlugin = null;
	private static AndroidJavaObject activityContext = null;

	public class ThirdpresenceAdsAndroidListener : AndroidJavaProxy
	{
		public ThirdpresenceAdsAndroidListener () : base("com.thirdpresence.adsdk.mediation.unity.ThirdpresenceInterstitialAdapter$InterstitialListener") {}

		public void onInterstitialLoaded() {
			if (OnThirdpresenceInterstitialLoaded != null) {
				OnThirdpresenceInterstitialLoaded ();
			}
		}

		public void onInterstitialShown() {
			if (OnThirdpresenceInterstitialShown != null) {
				OnThirdpresenceInterstitialShown ();
			}
		}

		public void onInterstitialDismissed() {
			if (OnThirdpresenceInterstitialDismissed != null) {
				OnThirdpresenceInterstitialDismissed ();
			}
		}

		public void onInterstitialFailed(int errorCode, string errorText) {
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

	static ThirdpresenceAdsAndroid()
	{
		if( Application.platform != RuntimePlatform.Android )
			return;
		
		using(AndroidJavaClass activityClass = new AndroidJavaClass("com.unity3d.player.UnityPlayer")) {
			activityContext = activityClass.GetStatic<AndroidJavaObject>("currentActivity");
		}

		using (var pluginClass = new AndroidJavaClass ("com.thirdpresence.adsdk.mediation.unity.ThirdpresenceInterstitialAdapter")) {
			tprPlugin = pluginClass.CallStatic<AndroidJavaObject> ("getInstance");
			tprPlugin.Call ("setListener", new ThirdpresenceAdsAndroidListener ());
		}
	}
	
	public static void initInterstitial(Dictionary<string, string> environment, Dictionary<string, string> playerParams, long timeout) 
	{
        if( Application.platform != RuntimePlatform.Android )
            return;

		activityContext.Call ("runOnUiThread", new AndroidJavaRunnable (() => {
			using (AndroidJavaObject envMap = new AndroidJavaObject ("java.util.HashMap")) {

				foreach(KeyValuePair<string, string> entry in environment)
					addToHashMap(envMap, entry.Key, entry.Value);

				using (AndroidJavaObject playerMap = new AndroidJavaObject ("java.util.HashMap")) {
					foreach(KeyValuePair<string, string> entry in playerParams)
						addToHashMap(playerMap, entry.Key, entry.Value);
					tprPlugin.Call ("initInterstitial", activityContext, envMap, playerMap, timeout);
				}
			}
		}));
	}
		
	public static void showInterstitial()
	{
		if( Application.platform != RuntimePlatform.Android )
			return;

		activityContext.Call ("runOnUiThread", new AndroidJavaRunnable (() => {
			tprPlugin.Call ("showInterstitial");
		}));
	}

	public static void removeInterstitial()
	{
		if( Application.platform != RuntimePlatform.Android )
			return;

		activityContext.Call ("runOnUiThread", new AndroidJavaRunnable (() => {
			tprPlugin.Call ("removeInterstitial");
		}));
	}

	private static void addToHashMap(AndroidJavaObject map, String k, String v) {
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


