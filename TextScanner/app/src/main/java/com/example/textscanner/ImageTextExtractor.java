package com.example.textscanner;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class ImageTextExtractor {
    private final Context context;
    private final TextRecognizer textRecognizer;

    public ImageTextExtractor(Context context) {
        this.context = context;
        this.textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    }

    public String extractTextFromImage(Uri imageUri) {
        try {
            InputImage inputImage = InputImage.fromFilePath(context, imageUri);
            Task<Text> result = textRecognizer.process(inputImage);

            // Block the current thread until the task completes
            Tasks.await(result);

            if (result.isSuccessful()) {
                return result.getResult().getText();
            } else {
                throw new IOException("Text extraction failed");
            }

        } catch (IOException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
