package com.thirdpresence.adsdk.sampleapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;

public class HelperFunctions {

    private static final String TEXT_ALLOW = "Allow";
    private static final String TEXT_DENY = "Deny";
    private static final String TEXT_NEVER_ASK_AGAIN = "Never ask again";
    private static final String TEXT_OK = "OK";

    public static void waitFor(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // ignore
        }
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
