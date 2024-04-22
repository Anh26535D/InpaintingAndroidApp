package com.dvabk.inpaintingapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Button;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private Button uploadImageBtn;
    private Button nextBtn;
    private FingerPaintImageView uploadImgView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uploadImageBtn = findViewById(R.id.upload_img_btn);
        uploadImgView = findViewById(R.id.upload_img_view);
        nextBtn = findViewById(R.id.next_btn);

        ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    Log.d("PhotoPicker", "Selected URI: " + uri);
                    try {
                        DisplayMetrics displayMetrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                        int screenWidth = displayMetrics.widthPixels;
                        int screenHeight = displayMetrics.heightPixels;
                        uploadImgView.updateImageBitmap(this.getContentResolver(), uri, screenWidth, screenHeight);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Log.d("PhotoPicker", "No media selected");
                }
            });

        uploadImageBtn.setOnClickListener(v -> {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        nextBtn.setOnClickListener(v -> {
            Bitmap maskBitmap = uploadImgView.getMaskBitmap();
            Bitmap imgBitmap = uploadImgView.getImgBitmap();
            if (imgBitmap != null && maskBitmap != null) {
                int originalW = imgBitmap.getWidth();
                int originalH = imgBitmap.getHeight();
                int[] imgArr = BitmapUtility.bitmapToArray(imgBitmap);
                int[] maskArr = BitmapUtility.bitmapToArray(maskBitmap);
                Engine.inverseColor(imgArr, maskArr);
                imgBitmap = BitmapUtility.arrayToBitmap(imgArr, originalW, originalH);
                uploadImgView.updateImageBitmap(imgBitmap);
            }
        });
    }

}