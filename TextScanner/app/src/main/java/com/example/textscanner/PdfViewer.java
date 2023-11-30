package com.example.textscanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PdfViewer extends AppCompatActivity {
    private static final String TAG = "PdfView";
    private Button back;
    Uri imageUri;
    TextRecognizer textRecognizer;
    private List<String> textList = new ArrayList<>();
    private RecyclerView recyclerView;
    private PdfViewAdapter pdfViewAdapter;
    private PdfRenderer pdfRenderer;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        back = findViewById(R.id.backButton);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading pdf...");

        recyclerView = findViewById(R.id.pdfRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        List<Bitmap> pdfPages = new ArrayList<>();
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        // Retrieve the PDF URI from the intent
        String pdfUriString = getIntent().getStringExtra("pdfUri");
        Uri pdfUri = Uri.parse(pdfUriString);

        // Log the PDF URI for debugging
        Log.d(TAG, "PDF URI: " + pdfUriString);

        // Parse the URI and open the PdfRenderer
        openPdfRenderer(pdfUri);

        // Load all PDF pages
        loadPdfPages(pdfUri, pdfPages);

        // Create and set the adapter
        pdfViewAdapter = new PdfViewAdapter(pdfPages);
        recyclerView.setAdapter(pdfViewAdapter);
    }

    private void openPdfRenderer(Uri pdfUri) {
        try {
            // Open the PdfRenderer
            ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(pdfUri, "r");
            pdfRenderer = new PdfRenderer(parcelFileDescriptor);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error opening PDF renderer: " + e.getMessage());
        }
    }

    private void loadPdfPages(Uri pdfUri, List<Bitmap> pdfPages) {
        progressDialog.show();
        try {
            ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(pdfUri, "r");
            PdfRenderer renderer = new PdfRenderer(parcelFileDescriptor);

            for (int i = 0; i < renderer.getPageCount(); i++) {
                PdfRenderer.Page page = renderer.openPage(i);
                Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                pdfPages.add(bitmap);
                page.close();

                // Save the Bitmap to a file
                File file = saveBitmapToFile(bitmap);

                // Create a Uri from the file
                imageUri = Uri.fromFile(file);
                recognizeText(i);

            }
            renderer.close();
//            String data = "";
//            for(int i=0 ; i<textList.size() ; i++)
//            {
//                data= data + textList.get(i);
//            }
//            text.setText(String.valueOf(textList.size()));
//            text.setText(data);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error loading PDF pages: " + e.getMessage());
            Toast.makeText(this, "Error extracting text", Toast.LENGTH_SHORT).show();
        }
        progressDialog.dismiss();
    }

    private void recognizeText(int num) {
        if (imageUri != null) {
            try {
                InputImage inputImage = InputImage.fromFilePath(PdfViewer.this, imageUri);
                Task<Text> result = textRecognizer.process(inputImage)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text text) {
                                String recognizeText = text.getText();
                                textList.add(recognizeText);
                                Toast.makeText(PdfViewer.this, "Text selected "+num+" : "+textList.size(), Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(PdfViewer.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

        private File saveBitmapToFile(Bitmap bitmap) {
            // Get the directory where you want to save the file
            File directory = new File(Environment.getExternalStorageDirectory(), "TextScanner");
            File newDirectory = new File(directory,"Temporary");
            // Create the directory if it doesn't exist
            if (!newDirectory.exists()) {
                newDirectory.mkdirs();
            }

            try {
                // Create a file with a unique name (you can use a timestamp or any other unique identifier)
                String fileName = "image_" + System.currentTimeMillis() + ".png";
                File file = new File(newDirectory, fileName);

                // Create an output stream and compress the bitmap to the file
                FileOutputStream outputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.flush();
                outputStream.close();

                return file;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

    @Override
    protected void onDestroy() {
        // Close the PdfRenderer and associated resources when the activity is destroyed
        if (pdfRenderer != null) {
            pdfRenderer.close();
        }
        super.onDestroy();
    }

    public void ImagesForPdf(View view) {
        finish();
    }

    public void createDocFile(View view) {
        Intent intent = new Intent(PdfViewer.this, DocxView.class);
        intent.putStringArrayListExtra("textList", (ArrayList<String>) textList);
        startActivity(intent);
    }
}
