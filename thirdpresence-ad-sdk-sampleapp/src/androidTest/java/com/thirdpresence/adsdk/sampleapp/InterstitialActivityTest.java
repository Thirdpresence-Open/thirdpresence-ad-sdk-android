package com.thirdpresence.adsdk.sampleapp;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.SdkSuppress;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.Until;

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
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)
public class InterstitialActivityTest {

    private UiDevice mDevice;

    private static final String STAGING_SERVER_NAME = "staging";
    private static final String STATUS_FIELD_DESC = "status field";

    private static final int LAUNCH_TIMEOUT = 5000;
    private static final int INIT_TIMEOUT = 10000;
    private static final int LOAD_TIMEOUT = 10000;
    private static final int PLAYER_INIT_TIMEOUT = 1000;
    private static final int DISPLAY_TIMEOUT = 45000;

    private static final String STATE_TEXT_READY = "READY";
    private static final String STATE_TEXT_LOADED = "LOADED";

    @Rule
    public ActivityTestRule<InterstitialActivity> mActivityTestRule = new ActivityTestRule<>(InterstitialActivity.class, true, false);

    @Before
    public void setUp() {

        mDevice = UiDevice.getInstance(getInstrumentation());

        // To close any active dialog
        mDevice.pressBack();

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

        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(InterstitialActivity.class.getName(), null, false);

        Intent i = new Intent();
        if (STAGING_SERVER_NAME.equals(BuildConfig.SERVER_NAME)) {
            i.putExtra("use_staging_server", true);
        }
        mActivityTestRule.launchActivity(i);

        InterstitialActivity interstitialActivity = (InterstitialActivity) activityMonitor.waitForActivityWithTimeout(LAUNCH_TIMEOUT);
        assertNotNull(interstitialActivity);

        HelperFunctions.waitFor(2000);

        try {
            HelperFunctions.allowCurrentPermission(mDevice);
        } catch (UiObjectNotFoundException e) {
            // ignore
            System.out.println("\n\nPermission dialog not found");
        }
    }

    @Test
    public void displayInterstitialTest() {
        HelperFunctions.waitFor(2000);

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
            HelperFunctions.confirmFullscreen(mDevice);
        } catch (UiObjectNotFoundException e) {
            // ignore
        }
        
        InterstitialActivity interstitialActivity = (InterstitialActivity) activityMonitor2.waitForActivityWithTimeout(DISPLAY_TIMEOUT);

        assertNotNull(interstitialActivity);

        HelperFunctions.waitFor(2000);

        statusText = mDevice.findObject(By.desc(STATUS_FIELD_DESC));

        assertNotNull(statusText);

        statusText.wait(Until.textEquals(STATE_TEXT_READY), INIT_TIMEOUT);

        assertThat(statusText.getText(), is(equalTo(STATE_TEXT_READY)));
    }

}
