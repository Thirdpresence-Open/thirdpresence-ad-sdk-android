package com.thirdpresence.adsdk.sdk;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 *
 * BannerView is a custom view implementation for banner ad placement
 *
 */
public class BannerView extends RelativeLayout {

    private Bundle mParams;

    public static final String PARAM_KEY_AD_WIDTH = "adWidth";
    public static final String PARAM_KEY_AD_HEIGHT = "adHeight";

    /**
     * Constructor
     *
     * @param context Activity context
     */
    public BannerView(Context context) {
        super(context);
    }

    /**
     * Constructor
     *
     * @param context The context the view is running in
     * @param params Bundle object for defining banner style
     */
    public BannerView(Context context, Bundle params) {
        super(context);
        mParams = params;
    }

    /**
     * Constructor
     *
     * @param context The context the view is running in
     * @param attrs Attributes of the XML tag
     */
    public BannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            readParamsFromXML(attrs);
        }
    }

    /**
     * Constructor
     *
     * @param context The context the view is running in
     * @param attrs Attributes of the XML tag
     * @param defStyle An attribute in the current theme that contains a reference to a style resource that supplies default values for the view.
     */
    public BannerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (!isInEditMode()) {
            readParamsFromXML(attrs);
        }
    }

    /**
     * Constructor
     *
     * @param context The context the view is running in
     * @param attrs Attributes of the XML tag
     * @param defStyleAttr An attribute in the current theme that contains a reference to a style resource that supplies default values for the view.
     * @param defStyleRes A resource identifier of a style resource that supplies default values for the view, used only if defStyleAttr is 0 or can not be found in the theme.
     */
    @TargetApi(21)
    public BannerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if (!isInEditMode()) {
            readParamsFromXML(attrs);
        }
    }

    /**
     * From View
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (mParams != null && !mParams.isEmpty()) {
            ViewGroup.LayoutParams params = getLayoutParams();
            final float scale = getResources().getDisplayMetrics().density;
            if (mParams.containsKey(PARAM_KEY_AD_WIDTH)) {
                params.width = (int)(mParams.getInt(PARAM_KEY_AD_WIDTH) * scale);
            }
            if (mParams.containsKey(PARAM_KEY_AD_HEIGHT)) {
                params.height = (int)(mParams.getInt(PARAM_KEY_AD_HEIGHT) * scale);
            }
            setLayoutParams(params);
        }
    }

    /**
     * Reads attributes set in the layout XML
     *
     * @param attrs the attributes of the XML tag
     */
    private void readParamsFromXML(AttributeSet attrs) {
        mParams = new Bundle();
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.BannerView);
        int width = a.getInt(R.styleable.BannerView_adWidth, 0);
        int height = a.getInt(R.styleable.BannerView_adHeight, 0);
        final float scale = getResources().getDisplayMetrics().density;
        if (width > 0) {
            mParams.putInt(PARAM_KEY_AD_WIDTH, (int)(width * scale));
        }
        if (height > 0) {
            mParams.putInt(PARAM_KEY_AD_HEIGHT, (int)(height * scale));
        }
        a.recycle();
    }
}
