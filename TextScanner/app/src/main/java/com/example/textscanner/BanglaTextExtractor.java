package com.example.textscanner;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BanglaTextExtractor {

    private static final String TAG = "OCRUtils";

    public static String extractText(Context context, Bitmap bitmap) {
        TessBaseAPI tessBaseAPI = new TessBaseAPI();

        // Initialize Tesseract with the trained data
        String dataPath = Environment.getExternalStorageDirectory() + "/tesseract/";
        tessBaseAPI.init(dataPath, "ben"); // Use "ben" for Bengali

        // Set the image to be processed
        tessBaseAPI.setImage(bitmap);

        // Get the extracted text
        String extractedText = tessBaseAPI.getUTF8Text();
        tessBaseAPI.recycle();

        // Return a default text if the extracted text is null or empty
        return extractedText != null && !extractedText.isEmpty() ? extractedText : "Text not detected.";
    }

    public static void copyTessDataToSDCard(Context context) {
        try {
            String dataPath = Environment.getExternalStorageDirectory() + "/tesseract/";
            File dir = new File(dataPath);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.e(TAG, "Error creating directory " + dataPath);
                    Toast.makeText(context,"Error creating directory",Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            String trainedDataPath = dataPath + "tessdata/";
            File trainedDataDir = new File(trainedDataPath);
            if (!trainedDataDir.exists()) {
                if (!trainedDataDir.mkdirs()) {
                    Log.e(TAG, "Error creating directory " + trainedDataPath);
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
}

