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
import android.util.Log;

import com.thirdpresence.adsdk.sampleapp.test.BuildConfig;
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
public class BannerActivityTest {

    private UiDevice mDevice;

    private static final String SAMPLE_APP_PACKAGE = "com.thirdpresence.adsdk.sampleapp";
    private static final String STAGING_SERVER_NAME = "staging";
    private static final String STATUS_FIELD_DESC = "status field";

    private static final int LAUNCH_TIMEOUT = 5000;
    private static final int LOAD_TIMEOUT = 20000;
    private static final int DISPLAY_TIMEOUT = 35000;

    private static final String STATE_TEXT_DISPLAYING = "DISPLAYING";
    private static final String STATE_TEXT_STOPPED = "STOPPED";

    private static final String TEXT_ALLOW = "Allow";
    private static final String TEXT_DENY = "Deny";
    private static final String TEXT_NEVER_ASK_AGAIN = "Never ask again";
    private static final String TEXT_OK = "OK";

    @Rule
    public ActivityTestRule<BannerActivity> mActivityTestRule = new ActivityTestRule<>(BannerActivity.class, true, false);

    @Before
    public void setUp() {

        mDevice = UiDevice.getInstance(getInstrumentation());

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

        try {
            allowCurrentPermission(mDevice);
        } catch (UiObjectNotFoundException e) {
            // ignore
        }
    }

    @Test
    public void displayBannerTest() {

        Intent i = new Intent();
        if (STAGING_SERVER_NAME.equals(BuildConfig.SERVER_NAME)) {
            i.putExtra("use_staging_server", true);
        }
        mActivityTestRule.launchActivity(i);

        UiObject2 statusText = mDevice.findObject(By.desc(STATUS_FIELD_DESC));

        assertThat(mDevice, notNullValue());
        assertThat(statusText, notNullValue());

        statusText.wait(Until.textEquals(STATE_TEXT_DISPLAYING), LOAD_TIMEOUT);

        assertThat(statusText.getText(), is(equalTo(STATE_TEXT_DISPLAYING)));

        statusText.wait(Until.textEquals(STATE_TEXT_STOPPED), DISPLAY_TIMEOUT);

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
