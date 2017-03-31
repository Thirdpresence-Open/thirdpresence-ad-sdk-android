using UnityEditor;
using System;

public class Autobuild  {
	public static string AndroidSdkRoot {
		get { return EditorPrefs.GetString("AndroidSdkRoot"); }
		set { EditorPrefs.SetString("AndroidSdkRoot", value); }
	}

	public static string JdkRoot {
		get { return EditorPrefs.GetString("JdkPath"); }
		set { EditorPrefs.SetString("JdkPath", value); }
	}

	// This requires Unity 5.3 or later
	public static string AndroidNdkRoot {
		get { return EditorPrefs.GetString("AndroidNdkRoot"); }
		set { EditorPrefs.SetString("AndroidNdkRoot", value); }
	}

	static void SetToolPaths() {
		AndroidSdkRoot = Environment.GetEnvironmentVariable("ANDROID_HOME");
		JdkRoot = Environment.GetEnvironmentVariable("JAVA_HOME");
	}
}
