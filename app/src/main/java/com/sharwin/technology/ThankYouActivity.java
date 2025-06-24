package com.sharwin.technology;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
    TextView gameScoreText, quizScoreText, totalScoreText; // New score display TextViews
    ImageView celebrationIcon;
    CardView thankYouCard;

    // Score variables
    private int gameScore = 0;
    private int quizScore = 0;
    private int totalScore = 0;
    private int totalQuestions = 0;
    private int correctAnswers = 0;

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

        // New score display TextViews (you'll need to add these to your layout)
        gameScoreText = findViewById(R.id.gameScoreText);
        quizScoreText = findViewById(R.id.quizScoreText);
        totalScoreText = findViewById(R.id.totalScoreText);

        // Buttons
        playAgainButton = findViewById(R.id.playAgainButton);
        exitButton = findViewById(R.id.exitButton);
        shareButton = findViewById(R.id.shareButton);

        // Other UI elements
        celebrationIcon = findViewById(R.id.celebrationIcon);
        thankYouCard = findViewById(R.id.thankYouCard);
    }

    private void setupContent() {
        // Get scores from intent (these match the parameters sent from QuizActivity)
        Intent intent = getIntent();
        gameScore = intent.getIntExtra("gameScore", 0);
        quizScore = intent.getIntExtra("quizScore", 0);
        totalScore = intent.getIntExtra("totalScore", 0);
        totalQuestions = intent.getIntExtra("totalQuestions", 6);
        correctAnswers = intent.getIntExtra("correctAnswers", 0);

        String playerName = intent.getStringExtra("player_name");

        // Set dynamic content
        if (playerName != null && !playerName.isEmpty()) {
            congratsSubtitle.setText("Great job, " + playerName + "!");
        } else {
            congratsSubtitle.setText("You did amazing!");
        }

        // Display detailed scores
        if (gameScoreText != null) {
            gameScoreText.setText("Game Score: " + gameScore);
        }
        if (quizScoreText != null) {
            quizScoreText.setText("Quiz Score: " + quizScore + " (" + correctAnswers + "/" + totalQuestions + " correct)");
        }
        if (totalScoreText != null) {
            totalScoreText.setText("Total Score: " + totalScore);
        }

        // Calculate percentage for the main score display
        int maxPossibleScore = gameScore + (totalQuestions * 10); // Assuming 10 points per question
        int percentage = maxPossibleScore > 0 ? (totalScore * 100) / maxPossibleScore : 0;
        scoreValue.setText(totalScore + " pts");

        // Change message based on performance - FIXED LOGIC
        if (correctAnswers == totalQuestions) { // Perfect score (all 6 correct)
            thankYouMessage.setText("🏆 PERFECT SCORE! 🏆");
        } else if (correctAnswers >= 5) { // 5 or more correct out of 6
            thankYouMessage.setText("🌟 EXCELLENT! 🌟");
        } else if (correctAnswers >= 3) { // 3 or 4 correct out of 6
            thankYouMessage.setText("🎉 WELL DONE! 🎉");
        } else { // 0-2 correct
            thankYouMessage.setText("🎉 THANK YOU FOR PLAYING! 🎉");
        }
    }

    private void setupClickListeners() {
        // Play Again Button
        playAgainButton.setOnClickListener(v -> {
            animateButtonClick(playAgainButton);

            // Delay to show animation before transitioning
            new Handler().postDelayed(() -> {
                // Go back to the drag and drop game (assuming that's your main game)
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
        String shareText = "🎉 I just completed the Color Learning Game! 🎨\n" +
                "🎮 Game Score: " + gameScore + "\n" +
                "📝 Quiz Score: " + quizScore + " (" + correctAnswers + "/" + totalQuestions + " correct)\n" +
                "🏆 Total Score: " + totalScore + " points\n" +
                "Can you beat my score? 🏆 #ColorLearningGame";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My Color Learning Game Achievement!");

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

        // Hide score TextViews initially
        if (gameScoreText != null) {
            gameScoreText.setAlpha(0f);
            gameScoreText.setTranslationX(-50f);
        }
        if (quizScoreText != null) {
            quizScoreText.setAlpha(0f);
            quizScoreText.setTranslationX(-50f);
        }
        if (totalScoreText != null) {
            totalScoreText.setAlpha(0f);
            totalScoreText.setTranslationX(-50f);
        }

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

        // Animate score displays with stagger
        animateScoreEntrance(gameScoreText, 500);
        animateScoreEntrance(quizScoreText, 700);
        animateScoreEntrance(totalScoreText, 900);

        // Animate buttons with stagger
        animateButtonEntrance(playAgainButton, 1100);
        animateButtonEntrance(shareButton, 1300);
        animateButtonEntrance(exitButton, 1500);

        // Start animations in sequence
        iconAnimator.start();

        new Handler().postDelayed(() -> cardAnimator.start(), 300);
    }

    private void animateScoreEntrance(TextView textView, long delay) {
        if (textView == null) return;

        new Handler().postDelayed(() -> {
            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(textView, "alpha", 0f, 1f);
            ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(textView, "translationX", -50f, 0f);

            AnimatorSet scoreAnimator = new AnimatorSet();
            scoreAnimator.playTogether(alphaAnimator, translationAnimator);
            scoreAnimator.setDuration(400);
            scoreAnimator.setInterpolator(new DecelerateInterpolator());
            scoreAnimator.start();
        }, delay);
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