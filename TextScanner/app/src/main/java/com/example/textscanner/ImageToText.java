package com.example.textscanner;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

import org.apache.poi.ss.formula.functions.T;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.ProgressDialog;


public class ImageToText extends AppCompatActivity {
    ImageView clear , scan , copy;
    Button bn,en;
    EditText extractedText;
    Uri imageUri;
    TextRecognizer textRecognizer;
    BanglaTextExtractor banglaTextExtractor = new BanglaTextExtractor();
    private ProgressDialog progressDialog;
    int flag = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_to_text);

        bn = findViewById(R.id.buttonBn);
        en = findViewById(R.id.buttonEn);
        extractedText = findViewById(R.id.extractedTextView);
        clear = findViewById(R.id.eraserImage);
        copy = findViewById(R.id.copyImage);
        scan = findViewById(R.id.cameraImage);
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Extracting Text from Image...");
        extractedText.setEnabled(false);
        en.setBackgroundColor(Color.rgb(0,150,136));
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
            progressDialog.show();

            if(flag == 0)
            {
                recognizeText();
            }
            else
            {
                try {
                    copyTessDataToSDCard(this);// Copy Tesseract data if permission granted
                    // Open an input stream from the selected image URI
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);

                    // Decode the input stream into a Bitmap
                    Bitmap photo = BitmapFactory.decodeStream(inputStream);

                    if (photo != null) {
                        // Use Tesseract OCR to extract text
                        String text = banglaTextExtractor.extractText(this, photo);
                        if (text != null && !text.isEmpty()) {
                            extractedText.setText(text);
                        } else {
                            extractedText.setText("Text not found.");
                        }
                    }
                    else{
                        Toast.makeText(this,"Data not found", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this,"Error : "+e,Toast.LENGTH_SHORT).show();
                }
            }
        }
        else {
            Toast.makeText(ImageToText.this, "Image not selected", Toast.LENGTH_SHORT).show();
        }
    }

    public static void copyTessDataToSDCard(Context context){
        try {
            String dataPath = Environment.getExternalStorageDirectory() + "/tesseract/";
            File dir = new File(dataPath);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.e("TAG", "Error creating directory " + dataPath);
                    Toast.makeText(context,"Error creating directory",Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            String trainedDataPath = dataPath + "tessdata/";
            File trainedDataDir = new File(trainedDataPath);
            if (!trainedDataDir.exists()) {
                if (!trainedDataDir.mkdirs()) {
                    Log.e("TAG", "Error creating directory " + trainedDataPath);
                    return;
                }
            }

            String trainedDataName = "ben.traineddata"; // Use "ben" for Bengali
            File trainedDataFile = new File(trainedDataPath + trainedDataName);
            if (!trainedDataFile.exists()) {
                try {
                    InputStream in = context.getAssets().open("tessdata/" + trainedDataName);
                    FileOutputStream out = new FileOutputStream(trainedDataFile);

                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }

                    in.close();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(context,"Text : "+e,Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context,"Base : "+e,Toast.LENGTH_SHORT).show();
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
                                progressDialog.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ImageToText.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
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

    public void BackToMainActivity(View view) {
        finish();
    }

    public void EnglishText(View view) {
        flag = 0;
        en.setBackgroundColor(Color.rgb(0,150,136));
        bn.setBackgroundColor(Color.rgb(255,255,255));
    }

    public void BanglaText(View view) {
        flag = 1;
        bn.setBackgroundColor(Color.rgb(0,150,136));
        en.setBackgroundColor(Color.rgb(255,255,255));
    }
}