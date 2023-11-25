package com.example.textscanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PdfViewer extends AppCompatActivity {
    private static final String TAG = "PdfView";
    private Button back;
    private RecyclerView recyclerView;
    private PdfViewAdapter pdfViewAdapter;
    private PdfRenderer pdfRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        back = findViewById(R.id.backButton);
        recyclerView = findViewById(R.id.pdfRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        List<Bitmap> pdfPages = new ArrayList<>();

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
        try {
            ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(pdfUri, "r");
            PdfRenderer renderer = new PdfRenderer(parcelFileDescriptor);

            for (int i = 0; i < renderer.getPageCount(); i++) {
                PdfRenderer.Page page = renderer.openPage(i);
                Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                pdfPages.add(bitmap);
                page.close();
            }
            renderer.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error loading PDF pages: " + e.getMessage());
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
}
