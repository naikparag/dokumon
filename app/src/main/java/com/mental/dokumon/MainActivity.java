package com.mental.dokumon;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    private static final int INTENT_REQUEST_IMAGE_CAPTURE = 1;

    ImageView originalImageView ;
    ImageView processedImageView;
    Button  cameraButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        originalImageView = findViewById(R.id.original_imageview);
        processedImageView = findViewById(R.id.processed_imageview);
        cameraButton = findViewById(R.id.capture_button);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        validateAndRequestPermissions();
    }

    private void grayScaleTransformation() {
        Bitmap graybitmap = ImageTransformer.applyColorFilter(originalImageView.getDrawable(), ImageTransformer.COLOR_FILTER_BINARY);
        Bitmap processedBitmap = ImageTransformer.convolve3x3(graybitmap, ImageTransformer.COEFFICIENT_BLUR, MainActivity.this);
//        Bitmap processedBitmap = ImageTransformer.blur(graybitmap, 1, MainActivity.this);
//        processedBitmap = ImageTransformer.convolve3x3(processedBitmap, ImageTransformer.COEFFICIENT_EDGE, MainActivity.this);
        processedImageView.setImageBitmap(graybitmap);
    }


    // PRIVATE METHODS

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, INTENT_REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INTENT_REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            originalImageView.setImageBitmap(imageBitmap);
            grayScaleTransformation();
        }
    }

    private void validateAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        }
    }
}
