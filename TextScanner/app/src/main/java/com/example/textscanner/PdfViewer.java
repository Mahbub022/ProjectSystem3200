package com.example.textscanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PdfViewer extends AppCompatActivity {

    private static final String TAG = "PdfView";
    private Button docButton,bn,en;
    Uri imageUri;
    ArrayList<Uri> imageUris = new ArrayList<>();
    TextRecognizer textRecognizer;
    private List<String> textList = new ArrayList<>();
    private RecyclerView recyclerView;
    private PdfViewAdapter pdfViewAdapter;
    private PdfRenderer pdfRenderer;
    private ProgressDialog progressDialog;
    BanglaTextExtractor banglaTextExtractor = new BanglaTextExtractor();
    int flag = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        docButton = findViewById(R.id.docCreateButton);
        bn = findViewById(R.id.buttonBn2);
        en = findViewById(R.id.buttonEn2);
        en.setBackgroundColor(Color.rgb(0,150,136));



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

        new PdfProcessingTask().execute(pdfUri);
    }

    private class PdfProcessingTask extends AsyncTask<Uri, Void, List<Bitmap>>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }
        @Override
        protected List<Bitmap> doInBackground(Uri... uris) {
            Uri pdfUri = uris[0];
            List<Bitmap> pdfPages = new ArrayList<>();

            try {
                openPdfRenderer(pdfUri);
                loadPdfPages(pdfUri, pdfPages);
            } catch (Exception e) {
                Log.e(TAG, "Error in background task: " + e.getMessage());
            }
            return pdfPages;
        }
        @Override
        protected void onPostExecute(List<Bitmap> pdfPages) {
            super.onPostExecute(pdfPages);

            // Create and set the adapter after PDF processing is complete
            pdfViewAdapter = new PdfViewAdapter(pdfPages);
            recyclerView.setAdapter(pdfViewAdapter);
        }
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

            int pageCount = renderer.getPageCount();
            for (int i = 0; i < pageCount; i++) {
                PdfRenderer.Page page = renderer.openPage(i);
                Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                pdfPages.add(bitmap);
                page.close();

                // Save the Bitmap to a file
                File file = saveBitmapToFile(bitmap);

                // Create a Uri from the file
                imageUri = Uri.fromFile(file);
                imageUris.add(imageUri);
            }
            renderer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error loading PDF pages: " + e.getMessage());
            Toast.makeText(this, "Error extracting text", Toast.LENGTH_SHORT).show();
        }
        progressDialog.dismiss();
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

    private void recognizeText() {
        progressDialog.show();

        int totalImages = imageUris.size();
        AtomicInteger imagesProcessed = new AtomicInteger(0);

        for (int i = 0; i < totalImages; i++) {
            if (imageUris.get(i) != null) {
                try {
                    InputImage inputImage = InputImage.fromFilePath(PdfViewer.this, imageUris.get(i));
                    Task<Text> result = textRecognizer.process(inputImage)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Text text = task.getResult();
                                    if (text != null) {
                                        String recognizeText = text.getText();
                                        textList.add(recognizeText);
                                        Toast.makeText(PdfViewer.this, "Text selected: " + textList.size(), Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Exception e = task.getException();
                                    Toast.makeText(PdfViewer.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }

                                // Check if all images are processed
                                if (imagesProcessed.incrementAndGet() == totalImages) {
                                    progressDialog.dismiss();
                                    launchDocxViewActivity();
                                }
                            });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void launchDocxViewActivity() {
        Intent intent = new Intent(PdfViewer.this, DocxView.class);
        intent.putStringArrayListExtra("textList", (ArrayList<String>) textList);
        startActivity(intent);
    }

    public void createDocFile(View view) {
        if(flag == 0)
        {
            recognizeText();
        }
        else {
            copyTessDataToSDCard(this);
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    progressDialog.show();
                }
                @Override
                protected Void doInBackground(Void... voids) {
                    for (int i = 0; i < imageUris.size(); i++) {
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUris.get(i));
                            Bitmap photo = BitmapFactory.decodeStream(inputStream);

                            if (photo != null) {
                                String text = banglaTextExtractor.extractText(PdfViewer.this, photo);
                                if (text != null && !text.isEmpty()) {
                                    textList.add(text);
                                } else {
                                    Toast.makeText(PdfViewer.this, "Text not found", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(PdfViewer.this, "Data not found", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(PdfViewer.this, "Error : " + e, Toast.LENGTH_SHORT).show();
                        }
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    progressDialog.dismiss();
                     launchDocxViewActivity();
                }
            }.execute();
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
    public void EnglishText2(View view) {
        flag = 0;
        en.setBackgroundColor(Color.rgb(0,150,136));
        bn.setBackgroundColor(Color.rgb(255,255,255));
    }

    public void BanglaText2(View view) {
        flag = 1;
        bn.setBackgroundColor(Color.rgb(0,150,136));
        en.setBackgroundColor(Color.rgb(255,255,255));
    }

}
//    private void recognizeText() {
//
//        int i = 0;
//        for( ;i<imageUris.size();i++){
//        if (imageUris.get(i) != null) {
//            try {
//                InputImage inputImage = InputImage.fromFilePath(PdfViewer.this, imageUris.get(i));
//                Task<Text> result = textRecognizer.process(inputImage)
//                        .addOnSuccessListener(new OnSuccessListener<Text>() {
//                            @Override
//                            public void onSuccess(Text text) {
//                                String recognizeText = text.getText();
//                                textList.add(recognizeText);
//                                Toast.makeText(PdfViewer.this, "Text selected : "+textList.size(), Toast.LENGTH_SHORT).show();
//                            }
//                        }).addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                Toast.makeText(PdfViewer.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//                            }
//                        });
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//                }
//            }
//        }
//        progressDialog.dismiss();
//        Intent intent = new Intent(PdfViewer.this, DocxView.class);
//        intent.putStringArrayListExtra("textList", (ArrayList<String>) textList);
//        startActivity(intent);
//
//    }

//extras


// Parse the URI and open the PdfRenderer
//        openPdfRenderer(pdfUri);
//
//        // Load all PDF pages
//        loadPdfPages(pdfUri, pdfPages);

// Create and set the adapter
//        pdfViewAdapter = new PdfViewAdapter(pdfPages);
//        recyclerView.setAdapter(pdfViewAdapter);

//            String data = "";
//            for(int i=0 ; i<textList.size() ; i++)
//            {
//                data= data + textList.get(i);
//            }
//            text.setText(String.valueOf(textList.size()));
//            text.setText(data);
