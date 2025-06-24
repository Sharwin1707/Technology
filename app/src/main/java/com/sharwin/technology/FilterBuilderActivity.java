package com.sharwin.technology;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.technology.R;

public class FilterBuilderActivity extends AppCompatActivity {

    // UI Components
    View shapeView;
    SeekBar redSeek, greenSeek, blueSeek, sizeSeek;
    RadioGroup shapeSelector;
    TextView aiPreview, colorHex, sizeValue;
    TextView redValue, greenValue, blueValue;
    Button resetButton, applyButton, nextButton;

    // State variables
    int red = 255, green = 87, blue = 51; // Default to a nice orange
    String shape = "Circle";
    int currentSize = 150;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_builder);

        initializeViews();
        setupListeners();
        setInitialValues();
        updateShape();
    }

    private void initializeViews() {
        shapeView = findViewById(R.id.shapeView);
        redSeek = findViewById(R.id.redSeek);
        greenSeek = findViewById(R.id.greenSeek);
        blueSeek = findViewById(R.id.blueSeek);
        sizeSeek = findViewById(R.id.sizeSeek);
        shapeSelector = findViewById(R.id.shapeSelector);
        aiPreview = findViewById(R.id.aiPreview);

        // New UI elements
        colorHex = findViewById(R.id.colorHex);
        sizeValue = findViewById(R.id.sizeValue);
        redValue = findViewById(R.id.redValue);
        greenValue = findViewById(R.id.greenValue);
        blueValue = findViewById(R.id.blueValue);
        resetButton = findViewById(R.id.resetButton);
        applyButton = findViewById(R.id.applyButton);
        nextButton = findViewById(R.id.nextButton);
    }

    private void setupListeners() {
        redSeek.setOnSeekBarChangeListener(createColorChangeListener());
        greenSeek.setOnSeekBarChangeListener(createColorChangeListener());
        blueSeek.setOnSeekBarChangeListener(createColorChangeListener());
        sizeSeek.setOnSeekBarChangeListener(sizeChangeListener);

        shapeSelector.setOnCheckedChangeListener((group, checkedId) -> {
            String oldShape = shape;
            if (checkedId == R.id.shapeCircle) shape = "Circle";
            else if (checkedId == R.id.shapeSquare) shape = "Square";

            if (!oldShape.equals(shape)) {
                animateShapeChange();
            }
        });

        resetButton.setOnClickListener(v -> resetToDefaults());
        applyButton.setOnClickListener(v -> applyFilter());
        nextButton.setOnClickListener(v -> goToNextPage());
    }

    private void setInitialValues() {
        redSeek.setProgress(red);
        greenSeek.setProgress(green);
        blueSeek.setProgress(blue);
        sizeSeek.setProgress(currentSize);

        updateValueDisplays();
    }

    private SeekBar.OnSeekBarChangeListener createColorChangeListener() {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;

                // Update color values
                red = redSeek.getProgress();
                green = greenSeek.getProgress();
                blue = blueSeek.getProgress();

                updateValueDisplays();
                updateShape();
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        };
    }

    SeekBar.OnSeekBarChangeListener sizeChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!fromUser) return;

            currentSize = Math.max(50, progress); // Minimum size of 50dp
            animateSize(currentSize);
            sizeValue.setText(currentSize + "dp");
        }

        @Override public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override public void onStopTrackingTouch(SeekBar seekBar) {}
    };

    private void updateValueDisplays() {
        // Update individual color values
        redValue.setText(String.valueOf(red));
        greenValue.setText(String.valueOf(green));
        blueValue.setText(String.valueOf(blue));

        // Update hex color
        String hexColor = String.format("#%02X%02X%02X", red, green, blue);
        colorHex.setText(hexColor);
    }

    private void animateSize(int targetSize) {
        int currentWidth = shapeView.getLayoutParams().width;

        ValueAnimator animator = ValueAnimator.ofInt(currentWidth, targetSize);
        animator.setDuration(200);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            shapeView.getLayoutParams().width = animatedValue;
            shapeView.getLayoutParams().height = animatedValue;
            shapeView.requestLayout();
        });
        animator                            .start();
    }

    private void goToNextPage() {
        // Add haptic feedback
        nextButton.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);

        // Create intent to next activity
        Intent intent = new Intent(this, DragMatchActivity.class);

        // Pass current filter data to next activity
        Bundle filterData = new Bundle();
        filterData.putInt("red", red);
        filterData.putInt("green", green);
        filterData.putInt("blue", blue);
        filterData.putInt("size", currentSize);
        filterData.putString("shape", shape);
        filterData.putString("colorName", getEnhancedColorName(red, green, blue));
        filterData.putString("hexColor", String.format("#%02X%02X%02X", red, green, blue));

        intent.putExtras(filterData);

        // Add slide transition animation
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        Toast.makeText(this, "Moving to next step...", Toast.LENGTH_SHORT).show();
    }

    private void animateShapeChange() {
        // Scale down animation
        shapeView.animate()
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(150)
                .withEndAction(() -> {
                    updateShape();
                    // Scale back up
                    shapeView.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(150)
                            .start();
                })
                .start();
    }

    private void updateShape() {
        int color = Color.rgb(red, green, blue);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);

        // Add subtle stroke for better definition
        drawable.setStroke(2, Color.argb(50, 0, 0, 0));

        if (shape.equals("Circle")) {
            drawable.setShape(GradientDrawable.OVAL);
        } else {
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setCornerRadius(16); // Rounded corners for square
        }

        shapeView.setBackground(drawable);

        // Update AI preview with enhanced detection
        String colorName = getEnhancedColorName(red, green, blue);
        String confidence = getColorConfidence(red, green, blue);
        aiPreview.setText("AI Detection: " + colorName + " " + shape + " (" + confidence + "%)");
    }

    private String getEnhancedColorName(int r, int g, int b) {
        float[] hsv = new float[3];
        Color.RGBToHSV(r, g, b, hsv);

        float hue = hsv[0];
        float sat = hsv[1];
        float val = hsv[2];

        // Enhanced color detection with more accurate ranges
        if (val < 0.15) return "Black";
        if (val > 0.85 && sat < 0.15) return "White";
        if (sat < 0.25) {
            if (val < 0.3) return "Dark Gray";
            if (val > 0.7) return "Light Gray";
            return "Gray";
        }

        // More precise hue ranges
        if (hue >= 345 || hue <= 15) return getIntensity(sat, val) + "Red";
        if (hue > 15 && hue <= 45) return getIntensity(sat, val) + "Orange";
        if (hue > 45 && hue <= 75) return getIntensity(sat, val) + "Yellow";
        if (hue > 75 && hue <= 155) return getIntensity(sat, val) + "Green";
        if (hue > 155 && hue <= 185) return getIntensity(sat, val) + "Cyan";
        if (hue > 185 && hue <= 255) return getIntensity(sat, val) + "Blue";
        if (hue > 255 && hue <= 285) return getIntensity(sat, val) + "Purple";
        if (hue > 285 && hue < 345) return getIntensity(sat, val) + "Magenta";

        return "Unknown Color";
    }

    private String getIntensity(float saturation, float value) {
        if (value < 0.4) return "Dark ";
        if (value > 0.8 && saturation > 0.6) return "Bright ";
        if (saturation < 0.5) return "Pale ";
        return "";
    }

    private String getColorConfidence(int r, int g, int b) {
        // Calculate confidence based on color distinctiveness
        int max = Math.max(Math.max(r, g), b);
        int min = Math.min(Math.min(r, g), b);
        int difference = max - min;

        // Higher difference means more confident detection
        int confidence = Math.min(95, 60 + (difference / 5));
        return String.valueOf(confidence);
    }

    private void resetToDefaults() {
        // Animate reset
        ValueAnimator colorAnimator = ValueAnimator.ofFloat(0f, 1f);
        colorAnimator.setDuration(500);
        colorAnimator.addUpdateListener(animation -> {
            float progress = animation.getAnimatedFraction();

            // Animate back to default values
            red = (int) (255 * progress + red * (1 - progress));
            green = (int) (87 * progress + green * (1 - progress));
            blue = (int) (51 * progress + blue * (1 - progress));

            redSeek.setProgress(red);
            greenSeek.setProgress(green);
            blueSeek.setProgress(blue);

            updateValueDisplays();
            updateShape();
        });

        colorAnimator.start();

        // Reset size
        sizeSeek.setProgress(150);
        currentSize = 150;
        animateSize(150);
        sizeValue.setText("150dp");

        // Reset shape
        ((RadioButton) findViewById(R.id.shapeCircle)).setChecked(true);
        shape = "Circle";

        Toast.makeText(this, "Reset to defaults", Toast.LENGTH_SHORT).show();
    }

    private void applyFilter() {
        // Add haptic feedback
        shapeView.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);

        // Show confirmation with current settings
        String settings = String.format("Applied: %s %s\nColor: #%02X%02X%02X\nSize: %ddp",
                getEnhancedColorName(red, green, blue), shape, red, green, blue, currentSize);

        Toast.makeText(this, settings, Toast.LENGTH_LONG).show();

        // Add a subtle pulse animation to show it's applied
        shapeView.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(100)
                .withEndAction(() ->
                        shapeView.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(100)
                                .start())
                .start();
    }
}