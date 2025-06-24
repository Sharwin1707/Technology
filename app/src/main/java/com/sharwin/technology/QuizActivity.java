package com.sharwin.technology;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.media.MediaPlayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.technology.R;

public class QuizActivity extends AppCompatActivity {

    private TextView questionText, questionCounter, timerText, scoreText;
    private Button option1, option2, option3, option4;
    private ImageView questionImage;
    private ProgressBar progressBar;
    private CardView questionCard;
    private MediaPlayer correctSoundPlayer;
    private MediaPlayer wrongSoundPlayer;

    private int currentQuestionIndex = 0;
    private int gameScore = 0; // Score from previous game
    private int quizScore = 0; // Quiz score
    private int totalScore = 0;
    private CountDownTimer questionTimer;
    private boolean answered = false;
    private boolean isActivityPaused = false;
    private long remainingTime = 0; // Track remaining time for pause/resume

    private static final int QUESTION_TIME_LIMIT = 15000; // 15 seconds per question
    private static final int POINTS_PER_CORRECT = 10;
    private static final int POINTS_DEDUCTED_WRONG = 5; // Points deducted for wrong answer
    private static final int POINTS_DEDUCTED_TIMEOUT = 3; // Points deducted for timeout

    // Quiz questions array
    private QuizQuestion[] questions = {
            new QuizQuestion(
                    "What part of a computer is used to see things, like your face or colors?",
                    new String[]{"Keyboard", "Camera", "Speaker", "Mouse"},
                    1, // Correct answer index (Camera)
                    "camera"
            ),
            new QuizQuestion(
                    "What can a computer do when it sees different colors?",
                    new String[]{"Change the weather", "Detect and sort objects", "Sing a song", " Sleep"},
                    1, // Correct answer index (Detect and sort objects)
                    "computer"
            ),
            new QuizQuestion(
                    "Which technology helps computers to understand what they see?",
                    new String[]{"Bluetooth", "WiFi", "Computer Vision", "Antivirus"},
                    2, // Correct answer index (Computer Vision)
                    "visual"
            ),
            new QuizQuestion(
                    "Why do robots use cameras?",
                    new String[]{"To see and learn about the world", "To play music", "To send messages", "To charge faster"},
                    0, // Correct answer index (To see and learn about the world)
                    "robot"
            ),
            new QuizQuestion(
                    "What can a computer recognize using face detection?",
                    new String[]{"Your favorite food", "Your backpack", "Your shoes", "Your face"},
                    3, // Correct answer index (Your face)
                    "face"
            ),
            new QuizQuestion(
                    "Which of the following is an example of how computers help in real life?",
                    new String[]{"Detecting faces to unlock phones", "Baking a cake", "Taking you to school", "Drawing a picture"},
                    0, // Correct answer index (Detecting faces to unlock phones)
                    "unlock"
            )
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // Initialize sound players
        try {
            correctSoundPlayer = MediaPlayer.create(this, R.raw.correct);
            wrongSoundPlayer = MediaPlayer.create(this, R.raw.wrong);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Get game score from previous activity
        gameScore = getIntent().getIntExtra("gameScore", 0);
        totalScore = gameScore;

        initializeViews();
        setupUI();
        displayQuestion();
    }

    private void initializeViews() {
        questionText = findViewById(R.id.questionText);
        questionCounter = findViewById(R.id.questionCounter);
        timerText = findViewById(R.id.timerText);
        scoreText = findViewById(R.id.scoreText);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);
        questionImage = findViewById(R.id.questionImage);
        progressBar = findViewById(R.id.progressBar);
        questionCard = findViewById(R.id.questionCard);
    }

    private void setupUI() {
        scoreText.setText("Total Score: " + totalScore);
        progressBar.setMax(questions.length);

        // Set click listeners for options
        option1.setOnClickListener(v -> selectAnswer(0));
        option2.setOnClickListener(v -> selectAnswer(1));
        option3.setOnClickListener(v -> selectAnswer(2));
        option4.setOnClickListener(v -> selectAnswer(3));
    }

    private void displayQuestion() {
        if (currentQuestionIndex >= questions.length) {
            finishQuiz();
            return;
        }

        answered = false;
        remainingTime = QUESTION_TIME_LIMIT; // Reset remaining time
        QuizQuestion currentQuestion = questions[currentQuestionIndex];

        // Update UI
        questionCounter.setText("Question " + (currentQuestionIndex + 1) + "/" + questions.length);
        questionText.setText(currentQuestion.question);
        option1.setText(currentQuestion.options[0]);
        option2.setText(currentQuestion.options[1]);
        option3.setText(currentQuestion.options[2]);
        option4.setText(currentQuestion.options[3]);

        // Set question image
        try {
            questionImage.setImageResource(getResources().getIdentifier(
                    currentQuestion.imageResource, "drawable", getPackageName()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Reset button states and timer color
        resetButtonStates();
        timerText.setTextColor(Color.BLACK);

        // Update progress
        progressBar.setProgress(currentQuestionIndex);

        // Start timer for this question (only if not paused)
        if (!isActivityPaused) {
            startQuestionTimer();
        }
    }

    private void resetButtonStates() {
        Button[] buttons = {option1, option2, option3, option4};
        for (Button button : buttons) {
            button.setEnabled(true);
            button.setAlpha(1f); // Reset transparency
            button.setBackgroundResource(R.drawable.option_button_bg); // Reset background
            button.setTextColor(getResources().getColor(R.color.option_text_color)); // Reset text color
            button.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0); // Remove any icons
        }
    }

    private void selectAnswer(int selectedIndex) {
        if (answered) return;

        answered = true;
        // Cancel timer properly
        cancelTimer();

        QuizQuestion currentQuestion = questions[currentQuestionIndex];
        Button selectedButton = getButtonByIndex(selectedIndex);
        Button correctButton = getButtonByIndex(currentQuestion.correctAnswerIndex);

        if (selectedIndex == currentQuestion.correctAnswerIndex) {
            // Play correct sound
            playSound(correctSoundPlayer);

            // Correct answer - green background with white text
            selectedButton.setBackgroundResource(R.drawable.correct_answer_bg);
            selectedButton.setTextColor(Color.GREEN);
            selectedButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check, 0);

            // ADD POINTS FOR CORRECT ANSWER
            quizScore += POINTS_PER_CORRECT;
            totalScore += POINTS_PER_CORRECT;
            scoreText.setText("Total Score: " + totalScore);
            Toast.makeText(this, "🎉 Correct! +" + POINTS_PER_CORRECT + " points", Toast.LENGTH_SHORT).show();
        } else {
            // Play wrong sound
            playSound(wrongSoundPlayer);

            // Wrong answer - red background with white text
            selectedButton.setBackgroundResource(R.drawable.wrong_answer_bg);
            selectedButton.setTextColor(Color.RED);
            selectedButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_close, 0);

            // DEDUCT POINTS FOR WRONG ANSWER
            quizScore = Math.max(0, quizScore - POINTS_DEDUCTED_WRONG); // Ensure quiz score doesn't go below 0
            totalScore = Math.max(gameScore, totalScore - POINTS_DEDUCTED_WRONG); // Ensure total doesn't go below game score
            scoreText.setText("Total Score: " + totalScore);

            // Correct answer - green outline with green text
            correctButton.setBackgroundResource(R.drawable.correct_answer_outline);
            correctButton.setTextColor(ContextCompat.getColor(this, R.color.green));
            correctButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check, 0);

            Toast.makeText(this, "❌ Wrong! -" + POINTS_DEDUCTED_WRONG + " points. Correct answer highlighted", Toast.LENGTH_LONG).show();
        }

        // Disable all buttons and make others semi-transparent
        Button[] buttons = {option1, option2, option3, option4};
        for (Button button : buttons) {
            if (button != selectedButton && button != correctButton) {
                button.setAlpha(0.6f);
            }
            button.setEnabled(false);
        }

        // Move to next question after delay
        questionCard.postDelayed(() -> {
            currentQuestionIndex++;
            displayQuestion();
        }, 2000);
    }

    private Button getButtonByIndex(int index) {
        switch (index) {
            case 0: return option1;
            case 1: return option2;
            case 2: return option3;
            case 3: return option4;
            default: return option1;
        }
    }

    private void startQuestionTimer() {
        // Cancel any existing timer first
        cancelTimer();

        // Use remaining time if resuming, otherwise use full time limit
        long timeToUse = remainingTime > 0 ? remainingTime : QUESTION_TIME_LIMIT;

        questionTimer = new CountDownTimer(timeToUse, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingTime = millisUntilFinished; // Update remaining time
                long secondsLeft = millisUntilFinished / 1000;

                // Ensure UI updates are on main thread
                runOnUiThread(() -> {
                    if (timerText != null) {
                        timerText.setText("Time: " + secondsLeft + "s");

                        // Change timer color when time is running out
                        if (secondsLeft <= 5) {
                            timerText.setTextColor(Color.RED);
                        } else if (secondsLeft <= 10) {
                            timerText.setTextColor(Color.parseColor("#FF8C00")); // Orange
                        } else {
                            timerText.setTextColor(Color.BLACK);
                        }
                    }
                });
            }

            @Override
            public void onFinish() {
                remainingTime = 0; // Reset remaining time

                // Ensure this runs on main thread
                runOnUiThread(() -> {
                    if (!answered && !isActivityPaused) {
                        handleTimeOut();
                    }
                });
            }
        };

        questionTimer.start();
    }

    private void handleTimeOut() {
        answered = true;
        QuizQuestion currentQuestion = questions[currentQuestionIndex];
        Button correctButton = getButtonByIndex(currentQuestion.correctAnswerIndex);

        // DEDUCT POINTS FOR TIMEOUT
        quizScore = Math.max(0, quizScore - POINTS_DEDUCTED_TIMEOUT);
        totalScore = Math.max(gameScore, totalScore - POINTS_DEDUCTED_TIMEOUT);
        scoreText.setText("Total Score: " + totalScore);

        // Highlight correct answer with outline
        correctButton.setBackgroundResource(R.drawable.correct_answer_outline);
        correctButton.setTextColor(ContextCompat.getColor(QuizActivity.this, R.color.green));
        correctButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check, 0);

        // Make other options semi-transparent
        Button[] buttons = {option1, option2, option3, option4};
        for (Button button : buttons) {
            if (button != correctButton) {
                button.setAlpha(0.6f);
            }
            button.setEnabled(false);
        }

        Toast.makeText(QuizActivity.this, "⏰ Time's up! -" + POINTS_DEDUCTED_TIMEOUT + " points. Correct answer highlighted", Toast.LENGTH_LONG).show();

        questionCard.postDelayed(() -> {
            currentQuestionIndex++;
            displayQuestion();
        }, 2000);
    }

    private void cancelTimer() {
        if (questionTimer != null) {
            questionTimer.cancel();
            questionTimer = null;
        }
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

    private void finishQuiz() {
        // Cancel timer when finishing quiz
        cancelTimer();

        Intent intent = new Intent(QuizActivity.this, ThankYouActivity.class);
        intent.putExtra("gameScore", gameScore);
        intent.putExtra("quizScore", quizScore);
        intent.putExtra("totalScore", totalScore);
        intent.putExtra("totalQuestions", questions.length);

        // Calculate correct answers based on positive quiz score
        int correctAnswers = Math.max(0, quizScore / POINTS_PER_CORRECT);
        intent.putExtra("correctAnswers", correctAnswers);

        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Cancel timer
        cancelTimer();

        // Release media players
        if (correctSoundPlayer != null) {
            correctSoundPlayer.release();
            correctSoundPlayer = null;
        }
        if (wrongSoundPlayer != null) {
            wrongSoundPlayer.release();
            wrongSoundPlayer = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityPaused = true;

        // Cancel timer when activity is paused (remaining time is already tracked)
        cancelTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActivityPaused = false;

        // Resume timer if activity is resumed and question is still active
        if (!answered && currentQuestionIndex < questions.length) {
            startQuestionTimer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Cancel timer when activity is stopped
        cancelTimer();
    }

    // Inner class for quiz questions
    private static class QuizQuestion {
        String question;
        String[] options;
        int correctAnswerIndex;
        String imageResource;

        QuizQuestion(String question, String[] options, int correctAnswerIndex, String imageResource) {
            this.question = question;
            this.options = options;
            this.correctAnswerIndex = correctAnswerIndex;
            this.imageResource = imageResource;
        }
    }
}