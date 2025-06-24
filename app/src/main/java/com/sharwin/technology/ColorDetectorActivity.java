package com.sharwin.technology;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.technology.R;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

public class ColorDetectorActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;

    private ImageView imageViewInput, imageViewResult;
    private Button selectImageBtn, btnRed, btnGreen, btnBlue, btnYellow, btnAllColors, btnToFace;

    private final Scalar LOWER_RED1 = new Scalar(0, 120, 70);
    private final Scalar UPPER_RED1 = new Scalar(10, 255, 255);
    private final Scalar LOWER_RED2 = new Scalar(170, 120, 70);
    private final Scalar UPPER_RED2 = new Scalar(180, 255, 255);

    private final Scalar LOWER_BLUE = new Scalar(100, 150, 0);
    private final Scalar UPPER_BLUE = new Scalar(140, 255, 255);

    private final Scalar LOWER_GREEN = new Scalar(40, 70, 70);
    private final Scalar UPPER_GREEN = new Scalar(80, 255, 255);

    private final Scalar LOWER_YELLOW = new Scalar(20, 100, 100);
    private final Scalar UPPER_YELLOW = new Scalar(30, 255, 255);

    // Default selected color
    private String selectedColor = "RED";

    private Bitmap currentBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_detector);
        MusicManager.resumeMusic();

        imageViewInput = findViewById(R.id.imageViewInput);
        imageViewResult = findViewById(R.id.imageViewResult);
        selectImageBtn = findViewById(R.id.selectImageBtn);

        btnRed = findViewById(R.id.btnRed);
        btnGreen = findViewById(R.id.btnGreen);
        btnBlue = findViewById(R.id.btnBlue);
        btnYellow = findViewById(R.id.btnYellow);
        btnAllColors = findViewById(R.id.btnAllColors);
        btnToFace = findViewById(R.id.btnToFaceDetector);

        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(this, "OpenCV failed to load", Toast.LENGTH_SHORT).show();
        }

        // Request permission to read storage
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        selectImageBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE);
        });

        btnRed.setOnClickListener(v -> {
            selectedColor = "RED";
            Toast.makeText(this, "Detecting RED color", Toast.LENGTH_SHORT).show();
            detectIfBitmapLoaded();
        });

        btnGreen.setOnClickListener(v -> {
            selectedColor = "GREEN";
            Toast.makeText(this, "Detecting GREEN color", Toast.LENGTH_SHORT).show();
            detectIfBitmapLoaded();
        });

        btnBlue.setOnClickListener(v -> {
            selectedColor = "BLUE";
            Toast.makeText(this, "Detecting BLUE color", Toast.LENGTH_SHORT).show();
            detectIfBitmapLoaded();
        });

        btnYellow.setOnClickListener(v -> {
            selectedColor = "YELLOW";
            Toast.makeText(this, "Detecting YELLOW color", Toast.LENGTH_SHORT).show();
            detectIfBitmapLoaded();
        });

        btnAllColors.setOnClickListener(v -> {
            selectedColor = "ALL";
            Toast.makeText(this, "Detecting ALL colors", Toast.LENGTH_SHORT).show();
            detectIfBitmapLoaded();
        });

        btnToFace.setOnClickListener(v -> {
            Intent intent = new Intent(ColorDetectorActivity.this, FaceDetectorActivity.class);
            startActivity(intent);
        });
    }

    private void detectIfBitmapLoaded() {
        if (currentBitmap != null) {
            detectColors(currentBitmap);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                imageViewInput.setImageBitmap(bitmap);

                currentBitmap = bitmap;

                // Detect colors on newly selected image with current selected color
                detectColors(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void detectColors(Bitmap bitmap) {
        Mat src = new Mat();
        Utils.bitmapToMat(bitmap, src);
        Imgproc.cvtColor(src, src, Imgproc.COLOR_RGBA2RGB);

        Mat hsv = new Mat();
        Imgproc.cvtColor(src, hsv, Imgproc.COLOR_RGB2HSV);

        Mat mask = new Mat();

        switch (selectedColor) {
            case "RED":
                // Red has two ranges in HSV, so combine masks
                Mat mask1 = new Mat();
                Mat mask2 = new Mat();
                Core.inRange(hsv, LOWER_RED1, UPPER_RED1, mask1);
                Core.inRange(hsv, LOWER_RED2, UPPER_RED2, mask2);
                Core.addWeighted(mask1, 1.0, mask2, 1.0, 0.0, mask);
                mask1.release();
                mask2.release();
                break;

            case "GREEN":
                Core.inRange(hsv, LOWER_GREEN, UPPER_GREEN, mask);
                break;

            case "BLUE":
                Core.inRange(hsv, LOWER_BLUE, UPPER_BLUE, mask);
                break;

            case "YELLOW":
                Core.inRange(hsv, LOWER_YELLOW, UPPER_YELLOW, mask);
                break;

            case "ALL":
                // Red can have two ranges in HSV, define these if not already
                Scalar LOWER_RED1 = new Scalar(0, 120, 70);
                Scalar UPPER_RED1 = new Scalar(10, 255, 255);
                Scalar LOWER_RED2 = new Scalar(170, 120, 70);
                Scalar UPPER_RED2 = new Scalar(180, 255, 255);

                // Yellow range
                Scalar LOWER_YELLOW = new Scalar(20, 100, 100);
                Scalar UPPER_YELLOW = new Scalar(30, 255, 255);

                // Masks for red (two parts)
                Mat redMask1 = new Mat();
                Mat redMask2 = new Mat();
                Mat redMask = new Mat();
                Core.inRange(hsv, LOWER_RED1, UPPER_RED1, redMask1);
                Core.inRange(hsv, LOWER_RED2, UPPER_RED2, redMask2);
                Core.addWeighted(redMask1, 1.0, redMask2, 1.0, 0.0, redMask);

                // Mask for green
                Mat greenMask = new Mat();
                Core.inRange(hsv, LOWER_GREEN, UPPER_GREEN, greenMask);

                // Mask for blue
                Mat blueMask = new Mat();
                Core.inRange(hsv, LOWER_BLUE, UPPER_BLUE, blueMask);

                // Mask for yellow
                Mat yellowMask = new Mat();
                Core.inRange(hsv, LOWER_YELLOW, UPPER_YELLOW, yellowMask);

                // Combine all masks
                Mat allColorsMask = new Mat();
                Core.addWeighted(redMask, 1.0, greenMask, 1.0, 0.0, allColorsMask);
                Core.addWeighted(allColorsMask, 1.0, blueMask, 1.0, 0.0, allColorsMask);
                Core.addWeighted(allColorsMask, 1.0, yellowMask, 1.0, 0.0, mask);

                // Release intermediate masks
                redMask1.release();
                redMask2.release();
                redMask.release();
                greenMask.release();
                blueMask.release();
                yellowMask.release();
                allColorsMask.release();
                break;
        }

        Mat result = new Mat();
        Core.bitwise_and(src, src, result, mask);

        Bitmap resultBitmap = Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(result, resultBitmap);

        imageViewResult.setImageBitmap(resultBitmap);

        // Release mats to avoid memory leaks
        src.release();
        hsv.release();
        mask.release();
        result.release();
    }
}





