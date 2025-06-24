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

    private static final int QUESTION_TIME_LIMIT = 15000; // 15 seconds per question
    private static final int POINTS_PER_CORRECT = 10;

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
        correctSoundPlayer = MediaPlayer.create(this, R.raw.correct);
        wrongSoundPlayer = MediaPlayer.create(this, R.raw.wrong);


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
        QuizQuestion currentQuestion = questions[currentQuestionIndex];

        // Update UI
        questionCounter.setText("Question " + (currentQuestionIndex + 1) + "/" + questions.length);
        questionText.setText(currentQuestion.question);
        option1.setText(currentQuestion.options[0]);
        option2.setText(currentQuestion.options[1]);
        option3.setText(currentQuestion.options[2]);
        option4.setText(currentQuestion.options[3]);

        // Set question image
        questionImage.setImageResource(getResources().getIdentifier(
                currentQuestion.imageResource, "drawable", getPackageName()));

        // Reset button states - including removing any icons
        resetButtonStates();

        // Update progress
        progressBar.setProgress(currentQuestionIndex);

        // Start timer for this question
        startQuestionTimer();
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
        questionTimer.cancel();

        QuizQuestion currentQuestion = questions[currentQuestionIndex];
        Button selectedButton = getButtonByIndex(selectedIndex);
        Button correctButton = getButtonByIndex(currentQuestion.correctAnswerIndex);

        if (selectedIndex == currentQuestion.correctAnswerIndex) {
            correctSoundPlayer.start(); // Play correct sound
            // Correct answer - green background with white text
            selectedButton.setBackgroundResource(R.drawable.correct_answer_bg);
            selectedButton.setTextColor(Color.GREEN);
            selectedButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check, 0);

            quizScore += POINTS_PER_CORRECT;
            totalScore += POINTS_PER_CORRECT;
            scoreText.setText("Total Score: " + totalScore);
            Toast.makeText(this, "Correct! +" + POINTS_PER_CORRECT + " points", Toast.LENGTH_SHORT).show();
        } else {
            wrongSoundPlayer.start(); // Play wrong sound
            // Wrong answer - red background with white text
            selectedButton.setBackgroundResource(R.drawable.wrong_answer_bg);
            selectedButton.setTextColor(Color.RED);
            selectedButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_close, 0);

            // Correct answer - green outline with green text
            correctButton.setBackgroundResource(R.drawable.correct_answer_outline);
            correctButton.setTextColor(ContextCompat.getColor(this, R.color.green));
            correctButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check, 0);

            Toast.makeText(this, "Wrong! The correct answer is highlighted", Toast.LENGTH_SHORT).show();
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
        questionTimer = new CountDownTimer(QUESTION_TIME_LIMIT, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerText.setText("Time: " + millisUntilFinished / 1000 + "s");
            }

            @Override
            public void onFinish() {
                if (!answered) {
                    answered = true;
                    QuizQuestion currentQuestion = questions[currentQuestionIndex];
                    Button correctButton = getButtonByIndex(currentQuestion.correctAnswerIndex);

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

                    Toast.makeText(QuizActivity.this, "Time's up! Correct answer highlighted", Toast.LENGTH_SHORT).show();

                    questionCard.postDelayed(() -> {
                        currentQuestionIndex++;
                        displayQuestion();
                    }, 2000);
                }
            }
        }.start();
    }

    private void finishQuiz() {
        Intent intent = new Intent(QuizActivity.this, ThankYouActivity.class);
        intent.putExtra("gameScore", gameScore);
        intent.putExtra("quizScore", quizScore);
        intent.putExtra("totalScore", totalScore);
        intent.putExtra("totalQuestions", questions.length);
        intent.putExtra("correctAnswers", quizScore / POINTS_PER_CORRECT);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (questionTimer != null) {
            questionTimer.cancel();
        }
        if (correctSoundPlayer != null) {
            correctSoundPlayer.release();
            correctSoundPlayer = null;
        }
        if (wrongSoundPlayer != null) {
            wrongSoundPlayer.release();
            wrongSoundPlayer = null;
        }

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



