package com.example.textscanner;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;

public class ImageToText extends AppCompatActivity {
    ImageView clear , scan , copy;
    EditText extractedText;
    Uri imageUri;
    TextRecognizer textRecognizer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_to_text);

        extractedText = findViewById(R.id.extractedTextView);
        clear = findViewById(R.id.eraserImage);
        copy = findViewById(R.id.copyImage);
        scan = findViewById(R.id.cameraImage);
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        extractedText.setEnabled(false);
    }


    public void getImage(View view) {
        ImagePicker.with(ImageToText.this)
                .crop()	    			//Crop image(Optional), Check Customization for more option
                .compress(1024)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.getData();
            recognizeText();
        } else {
            Toast.makeText(ImageToText.this, "Image not selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void recognizeText() {
        if (imageUri != null) {
            try {
                InputImage inputImage = InputImage.fromFilePath(ImageToText.this, imageUri);
                Task<Text> result = textRecognizer.process(inputImage)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text text) {
                                String recognizeText = text.getText();
                                extractedText.setEnabled(true);
                                extractedText.setText(recognizeText);
                                Toast.makeText(ImageToText.this, "Text selected", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ImageToText.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void copyText(View view) {
        String text = extractedText.getText().toString();
        if(text.isEmpty())
        {
            Toast.makeText(ImageToText.this,"No text found",Toast.LENGTH_SHORT).show();
        }
        else
        {
            extractedText.setEnabled(true);
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(ImageToText.this.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("Data",extractedText.getText().toString());
            clipboardManager.setPrimaryClip(clipData);

            Toast.makeText(ImageToText.this,"Text copied to clipboard",Toast.LENGTH_SHORT).show();
        }
    }

    public void clearText(View view) {
        extractedText.setEnabled(false);
        String text = extractedText.getText().toString();
        if(text.isEmpty())
        {
            Toast.makeText(ImageToText.this,"No text found",Toast.LENGTH_SHORT).show();
        }
        else
        {
            extractedText.setText("");
            Toast.makeText(ImageToText.this,"Text cleared",Toast.LENGTH_SHORT).show();
        }
    }
}