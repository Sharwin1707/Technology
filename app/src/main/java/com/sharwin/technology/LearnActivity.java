package com.sharwin.technology;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.technology.R;

public class LearnActivity extends AppCompatActivity {

    Button tryNowButton;
    MediaPlayer narration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);
        MusicManager.pauseMusic();

        tryNowButton = findViewById(R.id.tryNowButton);

        // Play a fun narration when this activity starts
        narration = MediaPlayer.create(this, R.raw.robot_intro);
        narration.start();

        tryNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Stop audio before navigating
                if (narration != null && narration.isPlaying()) {
                    narration.stop();
                    narration.release();
                    narration = null;
                }
                // Go to ColorDetectorActivity
                Intent intent = new Intent(LearnActivity.this, VideoActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (narration != null && narration.isPlaying()) {
            narration.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (narration != null && !narration.isPlaying()) {
            narration.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (narration != null) {
            narration.release();
            narration = null;
        }
    }
}



