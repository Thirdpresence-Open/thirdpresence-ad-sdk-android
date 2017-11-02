package com.thirdpresence.adsdk.sampleapp;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.Until;

import com.thirdpresence.adsdk.sampleapp.test.BuildConfig;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)
public class BannerActivityTest {

    private UiDevice mDevice;

    private static final String STAGING_SERVER_NAME = "staging";
    private static final String STATUS_FIELD_DESC = "status field";

    private static final int LOAD_TIMEOUT = 20000;
    private static final int DISPLAY_TIMEOUT = 45000;

    private static final String STATE_TEXT_DISPLAYING = "DISPLAYING";
    private static final String STATE_TEXT_STOPPED = "STOPPED";

    @Rule
    public ActivityTestRule<BannerActivity> mActivityTestRule = new ActivityTestRule<>(BannerActivity.class, true, false);

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

        Intent i = new Intent();
        if (STAGING_SERVER_NAME.equals(BuildConfig.SERVER_NAME)) {
            i.putExtra("use_staging_server", true);
        }
        mActivityTestRule.launchActivity(i);

        HelperFunctions.waitFor(2000);

        try {
            HelperFunctions.allowCurrentPermission(mDevice);
        } catch (UiObjectNotFoundException e) {
            // ignore
        }
    }

    @Test
    public void displayBannerTest() {

        HelperFunctions.waitFor(2000);

        UiObject2 statusText = mDevice.findObject(By.desc(STATUS_FIELD_DESC));

        assertThat(mDevice, notNullValue());
        assertThat(statusText, notNullValue());

        statusText.wait(Until.textEquals(STATE_TEXT_DISPLAYING), LOAD_TIMEOUT);

        assertThat(statusText.getText(), is(equalTo(STATE_TEXT_DISPLAYING)));

        statusText.wait(Until.textEquals(STATE_TEXT_STOPPED), DISPLAY_TIMEOUT);

    }
}
