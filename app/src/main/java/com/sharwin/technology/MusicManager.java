package com.sharwin.technology;

import android.content.Context;
import android.media.MediaPlayer;

import com.example.technology.R;


public class MusicManager {
    private static MediaPlayer mediaPlayer;

    public static void startBackgroundMusic(Context context) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, com.example.technology.R.raw.background);
            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(0.2f, 0.2f); // adjust volume if needed
            mediaPlayer.start();
        }
    }

    public static void stopBackgroundMusic() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public static void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public static void resumeMusic() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }
}
