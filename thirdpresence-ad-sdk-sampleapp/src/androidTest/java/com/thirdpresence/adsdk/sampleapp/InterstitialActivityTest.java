package com.thirdpresence.adsdk.sampleapp;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.wifi.WifiManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.SdkSuppress;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;

import com.thirdpresence.adsdk.sdk.internal.PlayerActivity;
import com.thirdpresence.sampleapp.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)
public class InterstitialActivityTest {

    private UiDevice mDevice;

    private static final String SAMPLE_APP_PACKAGE
            = "com.thirdpresence.adsdk.sampleapp";

    private static final String STATUS_FIELD_DESC
            = "status field";

    private static final int LAUNCH_TIMEOUT = 10000;
    private static final int INIT_TIMEOUT = 21000;
    private static final int LOAD_TIMEOUT = 10000;
    private static final int PLAYER_INIT_TIMEOUT = 1000;
    private static final int DISPLAY_TIMEOUT = 35000;

    private static final String STATE_TEXT_READY = "READY";
    private static final String STATE_TEXT_LOADED = "LOADED";

    private static final String TEXT_ALLOW = "Allow";
    private static final String TEXT_DENY = "Deny";
    private static final String TEXT_NEVER_ASK_AGAIN = "Never ask again";
    private static final String TEXT_OK = "OK";

    @Rule
    public ActivityTestRule<InterstitialActivity> mActivityTestRule = new ActivityTestRule<>(InterstitialActivity.class);

    @Before
    public void startMainActivityFromHomeScreen() {
        // Initialize UiDevice instance
        mDevice = UiDevice.getInstance(getInstrumentation());

        // Start from the home screen
        mDevice.pressHome();

        // Wait for launcher
        final String launcherPackage = getLauncherPackageName();
        assertThat(launcherPackage, notNullValue());
        mDevice.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), LAUNCH_TIMEOUT);

        Context context = InstrumentationRegistry.getContext();

        // This is needed for AWS Device Farm to verify that Wifi is enabled before running tests
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Launch the blueprint app
        final Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(SAMPLE_APP_PACKAGE);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);    // Clear out any previous instances
        context.startActivity(intent);

        // Wait for the app to appear
        mDevice.wait(Until.hasObject(By.pkg(SAMPLE_APP_PACKAGE).depth(0)), LAUNCH_TIMEOUT);

        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(InterstitialActivity.class.getName(), null, false);

        mActivityTestRule.launchActivity(new Intent());

        InterstitialActivity interstitialActivity = (InterstitialActivity) activityMonitor.waitForActivityWithTimeout(PLAYER_INIT_TIMEOUT);
        assertNotNull(interstitialActivity);

        try {
            allowCurrentPermission(mDevice);
        } catch (UiObjectNotFoundException e) {
            // ignore
        }
    }

    @Test
    public void displayInterstitialTest() {

        UiObject2 statusText = mDevice.findObject(By.desc(STATUS_FIELD_DESC));

        assertThat(mDevice, notNullValue());
        assertThat(statusText, notNullValue());

        ViewInteraction initButton = onView(withId(R.id.initButton));
        initButton.perform(click());

        statusText.wait(Until.textEquals(STATE_TEXT_READY), INIT_TIMEOUT);

        assertThat(statusText.getText(), is(equalTo(STATE_TEXT_READY)));

        onView(withId(R.id.loadButton)).perform(click());

        statusText.wait(Until.textEquals(STATE_TEXT_LOADED), LOAD_TIMEOUT);

        assertThat(statusText.getText(), is(equalTo(STATE_TEXT_LOADED)));

        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(PlayerActivity.class.getName(), null, false);
        Instrumentation.ActivityMonitor activityMonitor2 = getInstrumentation().addMonitor(InterstitialActivity.class.getName(), null, false);

        onView(withId(R.id.displayButton)).perform(click());

        PlayerActivity nextActivity = (PlayerActivity) activityMonitor.waitForActivityWithTimeout(PLAYER_INIT_TIMEOUT);

        assertNotNull(nextActivity);

        try {
            confirmFullscreen(mDevice);
        } catch (UiObjectNotFoundException e) {
            // ignore
        }
        
        InterstitialActivity interstitialActivity = (InterstitialActivity) activityMonitor2.waitForActivityWithTimeout(DISPLAY_TIMEOUT);

        assertNotNull(interstitialActivity);

        statusText = mDevice.findObject(By.desc(STATUS_FIELD_DESC));

        assertNotNull(statusText);

        statusText.wait(Until.textEquals(STATE_TEXT_READY), INIT_TIMEOUT);

        assertThat(statusText.getText(), is(equalTo(STATE_TEXT_READY)));
    }

    /**
     * Uses package manager to find the package name of the device launcher. Usually this package
     * is "com.android.launcher" but can be different at times. This is a generic solution which
     * works on all platforms.`
     */
    private String getLauncherPackageName() {
        // Create launcher Intent
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);

        // Use PackageManager to get the launcher package name
        PackageManager pm = InstrumentationRegistry.getContext().getPackageManager();
        ResolveInfo resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return resolveInfo.activityInfo.packageName;
    }

    public static void allowCurrentPermission(UiDevice device) throws UiObjectNotFoundException {
        UiObject allowButton = device.findObject(new UiSelector().text(TEXT_ALLOW));
        allowButton.click();
    }

    public static void denyCurrentPermission(UiDevice device) throws UiObjectNotFoundException {
        UiObject denyButton = device.findObject(new UiSelector().text(TEXT_DENY));
        denyButton.click();
    }

    public static void denyCurrentPermissionPermanently(UiDevice device) throws UiObjectNotFoundException {
        UiObject neverAskAgainCheckbox = device.findObject(new UiSelector().text(TEXT_NEVER_ASK_AGAIN));
        neverAskAgainCheckbox.click();
        denyCurrentPermission(device);
    }

    public static void grantPermission(UiDevice device, String permissionTitle) throws UiObjectNotFoundException {
        UiObject permissionEntry = device.findObject(new UiSelector().text(permissionTitle));
        permissionEntry.click();
    }

    public static void confirmFullscreen(UiDevice device) throws UiObjectNotFoundException {
        UiObject okButton = device.findObject(new UiSelector().text(TEXT_OK));
        okButton.click();
    }
}
