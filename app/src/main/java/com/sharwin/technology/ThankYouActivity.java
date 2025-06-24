package com.sharwin.technology;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.technology.R;

public class ThankYouActivity extends AppCompatActivity {

    // UI Elements
    Button playAgainButton, exitButton, shareButton;
    TextView thankYouMessage, congratsSubtitle, scoreValue;
    ImageView celebrationIcon;
    CardView thankYouCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thank_you);

        // Initialize views
        initializeViews();

        // Set up content
        setupContent();

        // Set up click listeners
        setupClickListeners();

        // Start entrance animations
        startEntranceAnimations();
    }

    private void initializeViews() {
        // Text views
        thankYouMessage = findViewById(R.id.thankYouMessage);
        congratsSubtitle = findViewById(R.id.congratsSubtitle);
        scoreValue = findViewById(R.id.scoreValue);

        // Buttons
        playAgainButton = findViewById(R.id.playAgainButton);
        exitButton = findViewById(R.id.exitButton);
        shareButton = findViewById(R.id.shareButton);

        // Other UI elements
        celebrationIcon = findViewById(R.id.celebrationIcon);
        thankYouCard = findViewById(R.id.thankYouCard);
    }

    private void setupContent() {
        // Get score/data from intent if passed from previous activity
        Intent intent = getIntent();
        int finalScore = intent.getIntExtra("final_score", 100);
        String playerName = intent.getStringExtra("player_name");

        // Set dynamic content
        if (playerName != null && !playerName.isEmpty()) {
            congratsSubtitle.setText("Great job, " + playerName + "!");
        } else {
            congratsSubtitle.setText("You did amazing!");
        }

        // Display score
        scoreValue.setText(finalScore + "%");

        // Change message based on score
        if (finalScore >= 90) {
            thankYouMessage.setText("🏆 OUTSTANDING! 🏆");
        } else if (finalScore >= 70) {
            thankYouMessage.setText("🎉 WELL DONE! 🎉");
        } else {
            thankYouMessage.setText("🎉 THANK YOU! 🎉");
        }
    }

    private void setupClickListeners() {
        // Play Again Button
        playAgainButton.setOnClickListener(v -> {
            animateButtonClick(playAgainButton);

            // Delay to show animation before transitioning
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(ThankYouActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();

                // Add transition animation
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }, 200);
        });

        // Share Button
        shareButton.setOnClickListener(v -> {
            animateButtonClick(shareButton);
            shareAchievement();
        });

        // Exit Button
        exitButton.setOnClickListener(v -> {
            animateButtonClick(exitButton);

            // Delay to show animation before exiting
            new Handler().postDelayed(() -> {
                finishAffinity(); // Close all activities
            }, 200);
        });
    }

    private void shareAchievement() {
        Intent intent = getIntent();
        int finalScore = intent.getIntExtra("final_score", 100);

        String shareText = "🎉 I just completed the game with a score of " + finalScore + "%! " +
                "Can you beat my score? 🏆 #GameChallenge";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My Game Achievement!");

        startActivity(Intent.createChooser(shareIntent, "Share your achievement"));
    }

    private void startEntranceAnimations() {
        // Hide elements initially
        celebrationIcon.setAlpha(0f);
        celebrationIcon.setScaleX(0.3f);
        celebrationIcon.setScaleY(0.3f);
        thankYouCard.setAlpha(0f);
        thankYouCard.setTranslationY(100f);
        playAgainButton.setAlpha(0f);
        shareButton.setAlpha(0f);
        exitButton.setAlpha(0f);

        // Animate celebration icon
        ObjectAnimator iconAlpha = ObjectAnimator.ofFloat(celebrationIcon, "alpha", 0f, 1f);
        ObjectAnimator iconScaleX = ObjectAnimator.ofFloat(celebrationIcon, "scaleX", 0.3f, 1.2f, 1f);
        ObjectAnimator iconScaleY = ObjectAnimator.ofFloat(celebrationIcon, "scaleY", 0.3f, 1.2f, 1f);

        AnimatorSet iconAnimator = new AnimatorSet();
        iconAnimator.playTogether(iconAlpha, iconScaleX, iconScaleY);
        iconAnimator.setDuration(800);
        iconAnimator.setInterpolator(new BounceInterpolator());

        // Animate card
        ObjectAnimator cardAlpha = ObjectAnimator.ofFloat(thankYouCard, "alpha", 0f, 1f);
        ObjectAnimator cardTranslation = ObjectAnimator.ofFloat(thankYouCard, "translationY", 100f, 0f);

        AnimatorSet cardAnimator = new AnimatorSet();
        cardAnimator.playTogether(cardAlpha, cardTranslation);
        cardAnimator.setDuration(600);
        cardAnimator.setInterpolator(new DecelerateInterpolator());

        // Animate buttons with stagger
        animateButtonEntrance(playAgainButton, 400);
        animateButtonEntrance(shareButton, 600);
        animateButtonEntrance(exitButton, 800);

        // Start animations in sequence
        iconAnimator.start();

        new Handler().postDelayed(() -> cardAnimator.start(), 300);
    }

    private void animateButtonEntrance(Button button, long delay) {
        new Handler().postDelayed(() -> {
            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(button, "alpha", 0f, 1f);
            ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(button, "scaleX", 0.8f, 1f);
            ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(button, "scaleY", 0.8f, 1f);

            AnimatorSet buttonAnimator = new AnimatorSet();
            buttonAnimator.playTogether(alphaAnimator, scaleXAnimator, scaleYAnimator);
            buttonAnimator.setDuration(300);
            buttonAnimator.setInterpolator(new DecelerateInterpolator());
            buttonAnimator.start();
        }, delay);
    }

    private void animateButtonClick(Button button) {
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.95f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.95f);
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(button, "scaleX", 0.95f, 1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(button, "scaleY", 0.95f, 1f);

        AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.playTogether(scaleDownX, scaleDownY);
        scaleDown.setDuration(100);

        AnimatorSet scaleUp = new AnimatorSet();
        scaleUp.playTogether(scaleUpX, scaleUpY);
        scaleUp.setDuration(100);

        AnimatorSet buttonClickAnimator = new AnimatorSet();
        buttonClickAnimator.playSequentially(scaleDown, scaleUp);
        buttonClickAnimator.start();
    }

    @Override
    public void onBackPressed() {
        // Animate exit when back button is pressed
        animateButtonClick(exitButton);
        new Handler().postDelayed(() -> {
            super.onBackPressed();
        }, 200);
    }
}