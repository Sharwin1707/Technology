package com.sharwin.technology;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.technology.R;
import java.util.HashMap;
import java.util.Map;

public class DragMatchActivity extends AppCompatActivity {

    // UI Components - Images
    private ImageView imgBanana, imgSun, imgLemon, imgStar;           // Yellow items
    private ImageView imgApple, imgHeart, imgStrawberry, imgRose;     // Red items
    private ImageView imgLeaf, imgTree, imgGrass, imgCactus;         // Green items

    // UI Components - Cards
    private CardView bananaCard, sunCard, lemonCard, starCard;        // Yellow cards
    private CardView appleCard, heartCard, strawberryCard, roseCard;  // Red cards
    private CardView leafCard, treeCard, grassCard, cactusCard;      // Green cards

    // Drop zones
    private LinearLayout yellowBox, redBox, greenBox;

    // Score and progress UI
    private TextView scoreText, progressText;
    private TextView yellowCount, redCount, greenCount;
    private ProgressBar progressBar;

    // NEW: Instant feedback TextView
    private TextView feedbackText;

    // Game State
    private int score = 0;
    private int correctMatches = 0;
    private final int totalItems = 12;
    private boolean[] itemsMatched = new boolean[12]; // Track which items are matched

    // Color category counters
    private int yellowMatched = 0;
    private int redMatched = 0;
    private int greenMatched = 0;

    // NEW: Category celebration tracking
    private boolean yellowCategoryCelebrated = false;
    private boolean redCategoryCelebrated = false;
    private boolean greenCategoryCelebrated = false;

    // Animation and feedback
    private MediaPlayer correctSound, wrongSound;
    private Handler animationHandler = new Handler(Looper.getMainLooper());

    // NEW: Handler for instant feedback
    private Handler feedbackHandler = new Handler(Looper.getMainLooper());

    // NEW: Rapid match tracking
    private int rapidMatchCount = 0;
    private long lastMatchTime = 0;
    private static final long RAPID_MATCH_THRESHOLD = 1000; // 1 second

    // Item mapping for easier management
    private Map<String, Integer> itemIndexMap = new HashMap<>();
    private Map<String, String> itemColorMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.technology.R.layout.activity_drag_match);

        initializeItemMaps();
        initializeViews();
        initializeDragAndDrop();
        initializeSounds();
        startGameIntroAnimation();
    }

    private void initializeItemMaps() {
        // Map items to their indices and colors
        // Yellow items (0-3)
        itemIndexMap.put("banana", 0);
        itemIndexMap.put("sun", 1);
        itemIndexMap.put("lemon", 2);
        itemIndexMap.put("star", 3);

        // Red items (4-7)
        itemIndexMap.put("apple", 4);
        itemIndexMap.put("heart", 5);
        itemIndexMap.put("strawberry", 6);
        itemIndexMap.put("rose", 7);

        // Green items (8-11)
        itemIndexMap.put("leaf", 8);
        itemIndexMap.put("tree", 9);
        itemIndexMap.put("grass", 10);
        itemIndexMap.put("cactus", 11);

        // Map items to their correct colors
        itemColorMap.put("banana", "yellow");
        itemColorMap.put("sun", "yellow");
        itemColorMap.put("lemon", "yellow");
        itemColorMap.put("star", "yellow");

        itemColorMap.put("apple", "red");
        itemColorMap.put("heart", "red");
        itemColorMap.put("strawberry", "red");
        itemColorMap.put("rose", "red");

        itemColorMap.put("leaf", "green");
        itemColorMap.put("tree", "green");
        itemColorMap.put("grass", "green");
        itemColorMap.put("cactus", "green");
    }

    private void initializeViews() {
        // Yellow items
        imgBanana = findViewById(R.id.imgBanana);
        imgSun = findViewById(R.id.imgSun);
        imgLemon = findViewById(R.id.imgLemon);
        imgStar = findViewById(R.id.imgStar);

        bananaCard = findViewById(R.id.bananaCard);
        sunCard = findViewById(R.id.sunCard);
        lemonCard = findViewById(R.id.lemonCard);
        starCard = findViewById(R.id.starCard);

        // Red items
        imgApple = findViewById(R.id.imgApple);
        imgHeart = findViewById(R.id.imgHeart);
        imgStrawberry = findViewById(R.id.imgStrawberry);
        imgRose = findViewById(R.id.imgRose);

        appleCard = findViewById(R.id.appleCard);
        heartCard = findViewById(R.id.heartCard);
        strawberryCard = findViewById(R.id.strawberryCard);
        roseCard = findViewById(R.id.roseCard);

        // Green items
        imgLeaf = findViewById(R.id.imgLeaf);
        imgTree = findViewById(R.id.imgTree);
        imgGrass = findViewById(R.id.imgGrass);
        imgCactus = findViewById(R.id.imgCactus);

        leafCard = findViewById(R.id.leafCard);
        treeCard = findViewById(R.id.treeCard);
        grassCard = findViewById(R.id.grassCard);
        cactusCard = findViewById(R.id.cactusCard);

        // Drop zones
        yellowBox = findViewById(R.id.yellowBox);
        redBox = findViewById(R.id.redBox);
        greenBox = findViewById(R.id.greenBox);

        // Score and progress
        scoreText = findViewById(R.id.scoreText);
        progressText = findViewById(R.id.progressText);
        progressBar = findViewById(R.id.progressBar);

        // Color counters
        yellowCount = findViewById(R.id.yellowCount);
        redCount = findViewById(R.id.redCount);
        greenCount = findViewById(R.id.greenCount);

        // NEW: Initialize feedback text (try to find it, create if not exists)
        feedbackText = findViewById(R.id.feedbackText);
        if (feedbackText == null) {
            // Create programmatically if not in layout
            feedbackText = new TextView(this);
            feedbackText.setId(R.id.feedbackText);
            feedbackText.setTextSize(20);
            feedbackText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            feedbackText.setVisibility(View.INVISIBLE);
            feedbackText.setElevation(10f);
            feedbackText.setPadding(24, 12, 24, 12);
            feedbackText.setBackgroundResource(android.R.drawable.toast_frame);
            feedbackText.setTextColor(getResources().getColor(android.R.color.white));

            // Add to root layout - you might need to adjust this based on your layout
            findViewById(android.R.id.content).getRootView();
        }

        updateScoreDisplay();
    }

    private void initializeDragAndDrop() {
        // Yellow items
        setDragListener(imgBanana, bananaCard, "banana");
        setDragListener(imgSun, sunCard, "sun");
        setDragListener(imgLemon, lemonCard, "lemon");
        setDragListener(imgStar, starCard, "star");

        // Red items
        setDragListener(imgApple, appleCard, "apple");
        setDragListener(imgHeart, heartCard, "heart");
        setDragListener(imgStrawberry, strawberryCard, "strawberry");
        setDragListener(imgRose, roseCard, "rose");

        // Green items
        setDragListener(imgLeaf, leafCard, "leaf");
        setDragListener(imgTree, treeCard, "tree");
        setDragListener(imgGrass, grassCard, "grass");
        setDragListener(imgCactus, cactusCard, "cactus");

        // Set drop targets
        setDropListener(yellowBox, "yellow");
        setDropListener(redBox, "red");
        setDropListener(greenBox, "green");
    }

    private void initializeSounds() {
        try {
            // You'll need to add these sound files to res/raw/
            correctSound = MediaPlayer.create(this, R.raw.correct);
            wrongSound = MediaPlayer.create(this, R.raw.wrong);
        } catch (Exception e) {
            // Handle sound initialization errors gracefully
            e.printStackTrace();
        }
    }

    private void setDragListener(ImageView imageView, CardView cardView, String tag) {
        int itemIndex = itemIndexMap.get(tag);

        View.OnTouchListener touchListener = (v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN && !itemsMatched[itemIndex]) {
                // Create drag data
                ClipData data = ClipData.newPlainText("object", tag);
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(cardView);

                // Start drag with enhanced visual feedback
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    cardView.startDragAndDrop(data, shadowBuilder, new DragData(imageView, cardView, itemIndex), 0);
                } else {
                    cardView.startDrag(data, shadowBuilder, new DragData(imageView, cardView, itemIndex), 0);
                }

                // Add scaling animation when drag starts
                animateCardScale(cardView, 0.9f);

                return true;
            }
            return false;
        };

        imageView.setOnTouchListener(touchListener);
        cardView.setOnTouchListener(touchListener);

        // Set tags for identification
        imageView.setTag(tag);
        cardView.setTag(tag);
    }

    private void setDropListener(LinearLayout dropZone, String targetColor) {
        dropZone.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;

                case DragEvent.ACTION_DRAG_ENTERED:
                    // Visual feedback when drag enters the zone
                    animateDropZoneHighlight(dropZone, true);
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    // Remove highlight when drag exits
                    animateDropZoneHighlight(dropZone, false);
                    return true;

                case DragEvent.ACTION_DROP:
                    // Remove highlight
                    animateDropZoneHighlight(dropZone, false);

                    DragData dragData = (DragData) event.getLocalState();
                    if (dragData != null && dragData.cardView != null && dragData.cardView.getTag() != null) {
                        String draggedTag = (String) dragData.cardView.getTag();
                        String itemColor = itemColorMap.get(draggedTag);

                        if (itemColor != null && itemColor.equals(targetColor)) {
                            handleCorrectMatch(dragData, dropZone, targetColor);
                        } else {
                            handleIncorrectMatch(dragData);
                        }
                    }
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                    // Reset any remaining highlights
                    animateDropZoneHighlight(dropZone, false);
                    return true;
            }
            return false;
        });
    }

    // Enhanced DragMatchActivity with point deduction for wrong answers

    private void handleCorrectMatch(DragData dragData, LinearLayout dropZone, String color) {
        // Mark item as matched
        itemsMatched[dragData.itemIndex] = true;
        correctMatches++;
        score += 10; // Positive points for correct match

        // Update color-specific counters and check for category completion
        boolean categoryJustCompleted = false;
        switch (color) {
            case "yellow":
                yellowMatched++;
                if (yellowMatched == 4 && !yellowCategoryCelebrated) {
                    yellowCategoryCelebrated = true;
                    categoryJustCompleted = true;
                }
                break;
            case "red":
                redMatched++;
                if (redMatched == 4 && !redCategoryCelebrated) {
                    redCategoryCelebrated = true;
                    categoryJustCompleted = true;
                }
                break;
            case "green":
                greenMatched++;
                if (greenMatched == 4 && !greenCategoryCelebrated) {
                    greenCategoryCelebrated = true;
                    categoryJustCompleted = true;
                }
                break;
        }

        // Check for rapid matching and show appropriate feedback
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMatchTime < RAPID_MATCH_THRESHOLD) {
            rapidMatchCount++;
        } else {
            rapidMatchCount = 1;
        }
        lastMatchTime = currentTime;

        // Play success sound
        playSound(correctSound);

        // Show instant feedback based on rapid matching
        if (rapidMatchCount > 1) {
            String[] rapidMessages = {
                    "🔥 On fire! +10 points",
                    "⚡ Lightning fast! +10 points",
                    "🚀 Combo x" + rapidMatchCount + "! +10",
                    "💫 Streak: " + rapidMatchCount + "! +10"
            };
            showInstantFeedback(rapidMessages[(rapidMatchCount - 2) % rapidMessages.length], true);
        } else {
            String[] successMessages = {
                    "🎉 Perfect match! +10",
                    "⭐ Excellent! +10 points",
                    "🌟 Great job! +10",
                    "👏 Amazing! +10 points",
                    "🎯 Bulls-eye! +10"
            };
            showInstantFeedback(successMessages[correctMatches % successMessages.length], true);
        }

        // Animate the successful match
        animateSuccessfulMatch(dragData.cardView, dropZone);

        // Update UI
        updateScoreDisplay();

        // Check if game is complete
        if (correctMatches >= totalItems) {
            animationHandler.postDelayed(this::showGameComplete, 1000);
        } else if (categoryJustCompleted) {
            // Celebrate completing a color category (only once per category)
            showColorCategoryComplete(color);
        }
    }

    private void handleIncorrectMatch(DragData dragData) {
        // Reset rapid match count on wrong answer
        rapidMatchCount = 0;

        // DEDUCT POINTS FOR WRONG ANSWER
        int pointsDeducted = 5; // Deduct 5 points for wrong answer
        score = Math.max(0, score - pointsDeducted); // Ensure score doesn't go below 0

        // Play error sound
        playSound(wrongSound);

        // Show error message with point deduction
        String[] errorMessages = {
                "🤔 Wrong color! -" + pointsDeducted + " points",
                "💭 Think again! -" + pointsDeducted + " points",
                "🔄 Try again! -" + pointsDeducted + " points",
                "🎨 Wrong zone! -" + pointsDeducted + " points"
        };
        showInstantFeedback(errorMessages[(int)(Math.random() * errorMessages.length)], false);

        // Animate the card back to original position with shake effect
        animateIncorrectMatch(dragData.cardView);

        // Update score display immediately to show deduction
        updateScoreDisplay();
    }

    // NEW: Instant feedback method replacing showCustomToast
    private void showInstantFeedback(String message, boolean isSuccess) {
        if (feedbackText == null) return;

        feedbackText.setText(message);
        feedbackText.setTextColor(isSuccess ?
                getResources().getColor(android.R.color.holo_green_dark) :
                getResources().getColor(android.R.color.holo_red_dark));

        // Show with animation
        feedbackText.setVisibility(View.VISIBLE);
        feedbackText.setAlpha(0f);
        feedbackText.animate()
                .alpha(1f)
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(200)
                .start();

        // Hide after delay
        feedbackHandler.removeCallbacksAndMessages(null); // Cancel previous hide
        feedbackHandler.postDelayed(() -> {
            feedbackText.animate()
                    .alpha(0f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(300)
                    .withEndAction(() -> feedbackText.setVisibility(View.INVISIBLE))
                    .start();
        }, 1000);
    }

    private void showColorCategoryComplete(String color) {
        String message = "🎊 All " + color + " items matched! 🎊";
        showInstantFeedback(message, true);
    }

    private void animateSuccessfulMatch(CardView cardView, LinearLayout dropZone) {
        // Scale and fade animation
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(cardView, "scaleX", 1f, 1.2f, 0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(cardView, "scaleY", 1f, 1.2f, 0f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(cardView, "alpha", 1f, 1f, 0f);

        scaleX.setDuration(800);
        scaleY.setDuration(800);
        alpha.setDuration(800);

        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());

        scaleX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                cardView.setVisibility(View.INVISIBLE);
                animateDropZoneSuccess(dropZone);
            }
        });

        scaleX.start();
        scaleY.start();
        alpha.start();
    }

    private void animateIncorrectMatch(CardView cardView) {
        // Shake animation
        ObjectAnimator shake = ObjectAnimator.ofFloat(cardView, "translationX", 0f, -25f, 25f, -25f, 25f, 0f);
        shake.setDuration(500);
        shake.setInterpolator(new BounceInterpolator());

        // Reset scale
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(cardView, "scaleX", 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(cardView, "scaleY", 1f);
        scaleX.setDuration(200);
        scaleY.setDuration(200);

        shake.start();
        scaleX.start();
        scaleY.start();
    }

    private void animateDropZoneHighlight(LinearLayout dropZone, boolean highlight) {
        float targetAlpha = highlight ? 0.8f : 1.0f;
        float targetScale = highlight ? 1.05f : 1.0f;

        ObjectAnimator alpha = ObjectAnimator.ofFloat(dropZone, "alpha", targetAlpha);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(dropZone, "scaleX", targetScale);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(dropZone, "scaleY", targetScale);

        alpha.setDuration(200);
        scaleX.setDuration(200);
        scaleY.setDuration(200);

        alpha.start();
        scaleX.start();
        scaleY.start();
    }

    private void animateDropZoneSuccess(LinearLayout dropZone) {
        // Pulse animation for successful drop zone
        ObjectAnimator pulse = ObjectAnimator.ofFloat(dropZone, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator pulseY = ObjectAnimator.ofFloat(dropZone, "scaleY", 1f, 1.1f, 1f);

        pulse.setDuration(300);
        pulseY.setDuration(300);
        pulse.setInterpolator(new BounceInterpolator());
        pulseY.setInterpolator(new BounceInterpolator());

        pulse.start();
        pulseY.start();
    }

    private void animateCardScale(CardView cardView, float scale) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(cardView, "scaleX", scale);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(cardView, "scaleY", scale);
        scaleX.setDuration(150);
        scaleY.setDuration(150);
        scaleX.start();
        scaleY.start();
    }

    private void startGameIntroAnimation() {
        // Animate cards appearing with stagger - Yellow items first
        animateCardEntry(bananaCard, 0);
        animateCardEntry(sunCard, 100);
        animateCardEntry(lemonCard, 200);
        animateCardEntry(starCard, 300);

        // Red items
        animateCardEntry(appleCard, 400);
        animateCardEntry(heartCard, 500);
        animateCardEntry(strawberryCard, 600);
        animateCardEntry(roseCard, 700);

        // Green items
        animateCardEntry(leafCard, 800);
        animateCardEntry(treeCard, 900);
        animateCardEntry(grassCard, 1000);
        animateCardEntry(cactusCard, 1100);
    }

    private void animateCardEntry(CardView cardView, long delay) {
        cardView.setAlpha(0f);
        cardView.setScaleX(0.5f);
        cardView.setScaleY(0.5f);

        cardView.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setStartDelay(delay)
                .setDuration(400)
                .setInterpolator(new BounceInterpolator())
                .start();
    }

    private void updateScoreDisplay() {
        scoreText.setText("⭐ Score: " + score);
        progressText.setText(correctMatches + "/" + totalItems);

        // Update color counters
        yellowCount.setText(yellowMatched + "/4");
        redCount.setText(redMatched + "/4");
        greenCount.setText(greenMatched + "/4");

        // Animate progress bar
        ValueAnimator progressAnimator = ValueAnimator.ofInt(progressBar.getProgress(), correctMatches);
        progressAnimator.setDuration(300);
        progressAnimator.addUpdateListener(animation -> {
            progressBar.setProgress((Integer) animation.getAnimatedValue());
        });
        progressAnimator.start();
    }


    private void showGameComplete() {
        celebrateGameComplete();

        // Delay before showing quiz page
        animationHandler.postDelayed(() -> {
            Intent intent = new Intent(DragMatchActivity.this, QuizActivity.class);
            intent.putExtra("gameScore", score); // Changed from "score" to "gameScore" to match QuizActivity expectation
            startActivity(intent);
            finish();
        }, 2000); // 2-second delay for animation
    }

    private void celebrateGameComplete() {
        // Show final celebration message
        showInstantFeedback("🏆 GAME COMPLETE! 🏆", true);

        // Pulse the score display
        ObjectAnimator pulse = ObjectAnimator.ofFloat(scoreText, "scaleX", 1f, 1.3f, 1f);
        ObjectAnimator pulseY = ObjectAnimator.ofFloat(scoreText, "scaleY", 1f, 1.3f, 1f);

        pulse.setDuration(600);
        pulseY.setDuration(600);
        pulse.setRepeatCount(3);
        pulseY.setRepeatCount(3);

        pulse.start();
        pulseY.start();

        // Celebrate each drop zone
        animationHandler.postDelayed(() -> animateDropZoneSuccess(yellowBox), 200);
        animationHandler.postDelayed(() -> animateDropZoneSuccess(redBox), 400);
        animationHandler.postDelayed(() -> animateDropZoneSuccess(greenBox), 600);
    }

    // DEPRECATED: Replaced with showInstantFeedback
    private void showCustomToast(String message, boolean isSuccess) {
        showInstantFeedback(message, isSuccess);
    }

    private void playSound(MediaPlayer mediaPlayer) {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(0);
                } else {
                    mediaPlayer.start();
                }
            } catch (Exception e) {
                // Handle sound playback errors gracefully
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // NEW: Clean up feedback handler
        if (feedbackHandler != null) {
            feedbackHandler.removeCallbacksAndMessages(null);
        }

        // Clean up media players
        if (correctSound != null) {
            correctSound.release();
            correctSound = null;
        }
        if (wrongSound != null) {
            wrongSound.release();
            wrongSound = null;
        }

        // Clean up handler callbacks
        if (animationHandler != null) {
            animationHandler.removeCallbacksAndMessages(null);
        }
    }

    // Helper class to pass drag data
    private static class DragData {
        final ImageView imageView;
        final CardView cardView;
        final int itemIndex;

        DragData(ImageView imageView, CardView cardView, int itemIndex) {
            this.imageView = imageView;
            this.cardView = cardView;
            this.itemIndex = itemIndex;
        }
    }

    // Enhanced restart game functionality
    public void restartGame() {
        score = 0;
        correctMatches = 0;
        yellowMatched = 0;
        redMatched = 0;
        greenMatched = 0;
        rapidMatchCount = 0;

        // Reset celebration flags
        yellowCategoryCelebrated = false;
        redCategoryCelebrated = false;
        greenCategoryCelebrated = false;

        itemsMatched = new boolean[12];

        // Update UI
        updateScoreDisplay();

        // Reset all cards visibility and properties
        CardView[] allCards = {
                bananaCard, sunCard, lemonCard, starCard,
                appleCard, heartCard, strawberryCard, roseCard,
                leafCard, treeCard, grassCard, cactusCard
        };

        for (CardView card : allCards) {
            card.setVisibility(View.VISIBLE);
            card.setAlpha(1f);
            card.setScaleX(1f);
            card.setScaleY(1f);
            card.setTranslationX(0f);
            card.setTranslationY(0f);
        }

        // Restart intro animation
        startGameIntroAnimation();
    }
}