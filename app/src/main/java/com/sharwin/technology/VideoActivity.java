package com.sharwin.technology;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.technology.R;

public class VideoActivity extends AppCompatActivity {




    VideoView videoView;
    Button skipButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video); // <-- This layout must have a VideoView with id "videoView"
        MusicManager.pauseMusic(); // Pause background music when video starts

        videoView = findViewById(R.id.videoView); // This will work only if videoView exists in activity_video.xml




        skipButton = findViewById(R.id.skipButton);

        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.videoplayback;
        Uri uri = Uri.parse(videoPath);
        videoView.setVideoURI(uri);
         // line 33


// Optional debug controls
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        videoView.setOnPreparedListener(mp -> {
            Log.d("VideoActivity", "Video ready. Starting now.");
            videoView.start();
        });

        videoView.setOnCompletionListener(mp -> {

            startActivity(new Intent(this, ColorDetectorActivity.class));
            finish();
        });

        skipButton.setOnClickListener(v -> {

            startActivity(new Intent(this, ColorDetectorActivity.class));
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}

