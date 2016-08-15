package com.thirdpresence.adsdk.mediation.unity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * ThirdpresencePlayerActivity is an activity that displays the player on Unity apps.
 *
 */
public class ThirdpresencePlayerActivity extends Activity {

    public static final String ADAPTER_CLASS_EXTRAS_KEY = "adapter_class_key";
    private ThirdpresenceAdapterBase mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thirdpresence_unity_plugin);

        hideNavigationBar();

        String adapterClassName = this.getIntent().getStringExtra(ADAPTER_CLASS_EXTRAS_KEY);

        try {
            Class<?> adapterClass = Class.forName(adapterClassName);
            Method createMethod = adapterClass.getMethod("getInstance");
            mAdapter = (ThirdpresenceAdapterBase) createMethod.invoke(null);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Adapter class not found");
        }

        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {

                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    hideNavigationBar();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAdapter != null) {
            mAdapter.setPlayerActivity(this);
            mAdapter.displayAd();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideNavigationBar();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isFinishing() && mAdapter != null) {
            mAdapter.finishPlayerActivity();
        }
        mAdapter = null;
    }

    private void hideNavigationBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE;
        decorView.setSystemUiVisibility(uiOptions);
    }


}