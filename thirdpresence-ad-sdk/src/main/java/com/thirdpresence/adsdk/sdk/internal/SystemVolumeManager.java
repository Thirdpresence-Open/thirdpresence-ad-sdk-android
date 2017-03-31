package com.thirdpresence.adsdk.sdk.internal;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;

/**
 * <h1>SystemVolumeManager</h1>
 *
 * SystemVolumeManager is a class for monitoring system volume changes
 *
 */
public class SystemVolumeManager extends ContentObserver {
    private final AudioManager mAudioManager;
    private final Context mApplicationContext;
    private final int mAudioStream;
    private ChangeListener mChangeListener;
    private int mVolume;

    /**
     * An interface for volume change events.
     */
    public interface ChangeListener {
        /**
         * Callback function to be called when volume change
         *
         * @param volume value of volume in scale 0 to 1
         */
        void onVolumeChanged(float volume);
    }

    /**
     * Constructor
     *
     * @param appContext the application context
     * @param audioStream @see AudioManager for audio stream constants
     */
    SystemVolumeManager(Context appContext, int audioStream) {
        super(new Handler());
        mApplicationContext = appContext;
        mAudioManager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
        mAudioStream = audioStream;
        mVolume = mAudioManager.getStreamVolume(audioStream);
    }

    /**
     * Sets the change listener
     */
    public void setListener(ChangeListener listener) {
        mChangeListener = listener;
    }

    /**
     * Starts observing volume changes
     */
    public void startObserving() {
        mApplicationContext.getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, this);
    }

    /**
     * Stops observing volume changes
     */
    public void stopObserving() {
        mApplicationContext.getContentResolver().unregisterContentObserver(this);
    }

    /**
     * Gets current volume
     */
    public float getVolume() {
        float volume = (float) mAudioManager.getStreamVolume (mAudioStream);
        float maxVolume = (float) mAudioManager.getStreamMaxVolume(mAudioStream);
        float vol = volume > 0 ? volume / maxVolume  : 0;
        return vol;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        int currentVolume = mAudioManager.getStreamVolume(mAudioStream);
        if (currentVolume != mVolume) {
            mVolume = currentVolume;
            if (mChangeListener != null) {
                mChangeListener.onVolumeChanged(getVolume());
            }
        }
    }

}
