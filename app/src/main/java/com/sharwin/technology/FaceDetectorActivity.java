package com.sharwin.technology;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.technology.R;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FaceDetectorActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 1000;

    private ImageView imageView;
    private Button btnLoad, btnGray, btnFace, btnEyes, btnSave;
    private LinearLayout placeholderLayout;
    Button nextButton;
    private Bitmap originalBitmap, currentBitmap;
    private CascadeClassifier faceCascade, eyeCascade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detector);

        // Initialize views
        imageView = findViewById(R.id.imageView);
        placeholderLayout = findViewById(R.id.placeholderLayout);
        btnLoad = findViewById(R.id.btnLoadImage);
        btnGray = findViewById(R.id.btnGrayscale);
        btnFace = findViewById(R.id.btnDetectFaces);
        btnEyes = findViewById(R.id.btnDetectEyes);
        btnSave = findViewById(R.id.btnSaveImage);
        Button btnReset = findViewById(R.id.btnResetImage);

        btnReset.setOnClickListener(v -> resetImage());

        // Initialize OpenCV
        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(this, "OpenCV not loaded!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Load cascade files
        loadCascadeFiles();
        setButtonState(false);

        // Set click listeners
        btnLoad.setOnClickListener(v -> openGallery());
        btnGray.setOnClickListener(v -> applyGrayscale());
        btnFace.setOnClickListener(v -> detectFeatures("face"));
        btnEyes.setOnClickListener(v -> detectFeatures("eyes"));
        btnSave.setOnClickListener(v -> saveToGallery());

        nextButton = findViewById(R.id.nextButton);

        // next button listener
        nextButton.setOnClickListener(v -> {

            Intent intent = new Intent(FaceDetectorActivity.this, FilterBuilderActivity.class); // Change to your game activity
            startActivity(intent);
            finish();
        });
    }



    private void loadCascadeFiles() {
        faceCascade = loadCascade(R.raw.haarcascade_frontalface_default);
        eyeCascade = loadCascade(R.raw.haarcascade_eye);

        if (faceCascade == null) {
            Toast.makeText(this, "Failed to load face cascade", Toast.LENGTH_SHORT).show();
        }
        if (eyeCascade == null) {
            Toast.makeText(this, "Failed to load eye cascade", Toast.LENGTH_SHORT).show();
        }
    }

    private CascadeClassifier loadCascade(int rawResId) {
        try {
            InputStream is = getResources().openRawResource(rawResId);
            File cascadeDir = getDir("cascade", MODE_PRIVATE);
            File cascadeFile = new File(cascadeDir, rawResId + ".xml");

            FileOutputStream os = new FileOutputStream(cascadeFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            CascadeClassifier classifier = new CascadeClassifier(cascadeFile.getAbsolutePath());
            return classifier.empty() ? null : classifier;

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("FaceDetector", "Error loading cascade: " + e.getMessage());
            return null;
        }
    }

    private void openGallery() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Unable to open gallery", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("FaceDetector", "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            Log.d("FaceDetector", "Image URI: " + uri);

            if (uri != null) {
                loadImageFromUri(uri);
            } else {
                Toast.makeText(this, "Failed to get image URI", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadImageFromUri(Uri uri) {
        try {
            // Method 1: Using BitmapFactory with InputStream (more reliable)
            InputStream inputStream = getContentResolver().openInputStream(uri);
            originalBitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            // Alternative Method 2: Using MediaStore (if Method 1 fails)
            if (originalBitmap == null) {
                originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            }

            if (originalBitmap != null) {
                // Resize if image is too large
                originalBitmap = resizeBitmap(originalBitmap, 1024);

                currentBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
                imageView.setImageBitmap(currentBitmap);

                // Hide placeholder and show image
                placeholderLayout.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);

                setButtonState(true);

                Toast.makeText(this, "Image loaded successfully", Toast.LENGTH_SHORT).show();
                Log.d("FaceDetector", "Image loaded: " + originalBitmap.getWidth() + "x" + originalBitmap.getHeight());
            } else {
                Toast.makeText(this, "Failed to decode image", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("FaceDetector", "Error loading image: " + e.getMessage());
            Toast.makeText(this, "Image load failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= maxSize && height <= maxSize) {
            return bitmap;
        }

        float ratio = Math.min((float) maxSize / width, (float) maxSize / height);
        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    private void setButtonState(boolean enabled) {
        btnGray.setEnabled(enabled);
        btnFace.setEnabled(enabled);
        btnEyes.setEnabled(enabled);
        btnSave.setEnabled(enabled);
    }

    private void resetImage() {
        if (originalBitmap != null) {
            currentBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
            imageView.setImageBitmap(currentBitmap);
            placeholderLayout.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
        } else {
            // Show placeholder if no original image
            placeholderLayout.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            setButtonState(false);
        }
    }

    // Add your other methods (applyGrayscale, detectFeatures, saveToGallery) here




    private void applyGrayscale() {
        if (originalBitmap == null) return;

        Mat mat = new Mat();
        Utils.bitmapToMat(originalBitmap, mat);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_GRAY2RGBA);

        currentBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, currentBitmap);
        imageView.setImageBitmap(currentBitmap);
    }

    private void detectFeatures(String type) {
        if (originalBitmap == null) return;

        // Work on a copy of the original colored image
        currentBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Mat matColor = new Mat();
        Utils.bitmapToMat(currentBitmap, matColor);

        // Convert to grayscale for detection only (without changing image colors)
        Mat matGray = new Mat();
        Imgproc.cvtColor(matColor, matGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(matGray, matGray);

        CascadeClassifier detector;
        Scalar color;

        if (type.equals("face")) {
            detector = faceCascade;
            color = new Scalar(255, 0, 0, 255); // Blue box
        } else if (type.equals("eyes")) {
            detector = eyeCascade;
            color = new Scalar(0, 255, 0, 255); // Green box
        } else {
            return;
        }

        MatOfRect features = new MatOfRect();
        if (detector != null) {
            detector.detectMultiScale(matGray, features, 1.1, 5);
            for (Rect rect : features.toArray()) {
                Imgproc.rectangle(matColor, rect.tl(), rect.br(), color, 3);
            }
        }

        // Convert matColor back to bitmap and display
        Utils.matToBitmap(matColor, currentBitmap);
        imageView.setImageBitmap(currentBitmap);
    }

    private void saveToGallery() {
        if (currentBitmap == null) {
            Toast.makeText(this, "No image to save!", Toast.LENGTH_SHORT).show();
            return;
        }
        MediaStore.Images.Media.insertImage(
                getContentResolver(),
                currentBitmap,
                "Processed_Image",
                "OpenCV Image"
        );
        Toast.makeText(this, "Image saved to gallery!", Toast.LENGTH_SHORT).show();
    }


}



