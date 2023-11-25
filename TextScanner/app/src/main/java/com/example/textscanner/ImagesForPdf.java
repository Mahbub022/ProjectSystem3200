package com.example.textscanner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ImagesForPdf extends AppCompatActivity {
    RecyclerView recyclerView;
    Button selectButton;
    EditText pdfNameText;
    ArrayList<Uri> uriArrayList = new ArrayList<>();
    ImageAdapter imageAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images_for_pdf);

        pdfNameText = findViewById(R.id.pdfNameText);
        selectButton = findViewById(R.id.selectImageButton);
        recyclerView = findViewById(R.id.ImageRecycler);

        imageAdapter = new ImageAdapter(uriArrayList);
        recyclerView.setLayoutManager(new GridLayoutManager(ImagesForPdf.this,2));
        recyclerView.setAdapter(imageAdapter);


        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.JELLY_BEAN_MR2)
                {
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
                }
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Pictures"),1);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == Activity.RESULT_OK && null!=data){
            //Toast.makeText(ImagesForPdf.this, "Pages activity request permitted", Toast.LENGTH_SHORT).show();
            Log.d("TAG", "onActivityResult: Pages activity request permitted");
            if(data.getClipData()!=null){
                Toast.makeText(ImagesForPdf.this, "Images selected", Toast.LENGTH_SHORT).show();
                int x = data.getClipData().getItemCount();

                for(int i=0;i<x;i++)
                {
                    uriArrayList.add(data.getClipData().getItemAt(i).getUri());
                }
                imageAdapter.notifyDataSetChanged();
            }
            else if(data.getData()!=null)
            {
                Toast.makeText(ImagesForPdf.this, "Image selected", Toast.LENGTH_SHORT).show();
                Uri imageUri = data.getData();
                Log.d("Image Path", "Image Path: " + imageUri); // Add this line for debugging

                uriArrayList.add(imageUri);
//                image.setImageURI(imageUri);
                imageAdapter.notifyDataSetChanged();

            }
        }
    }

    public void pdfCreate(View view) {
        PdfDocument myPdf = new PdfDocument();
        String fileName = ".pdf";
        String pdfName = pdfNameText.getText().toString().trim();
        try {
            for (int i = 0; i < uriArrayList.size(); i++) {
                // Convert Uri to Bitmap
                Bitmap bitmap = getBitmapFromUri(uriArrayList.get(i));

                // Calculate desired width and height while maintaining the aspect ratio
                int originalWidth = bitmap.getWidth();
                int originalHeight = bitmap.getHeight();

                int desiredWidth = 400; // Set your desired width for the image in the PDF
                int desiredHeight = (int) ((float) originalHeight / originalWidth * desiredWidth);

                // Calculate the destination rectangle to maintain aspect ratio
                RectF destinationRect = calculateDestinationRect(originalWidth, originalHeight, desiredWidth, desiredHeight);

                // Create a new page
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(desiredWidth, desiredHeight, i + 1).create();
                PdfDocument.Page page = myPdf.startPage(pageInfo);

                // Draw the Bitmap on the page
                Canvas canvas = page.getCanvas();
                canvas.drawBitmap(bitmap, null, destinationRect, new Paint());

                // Finish the page
                myPdf.finishPage(page);
            }

            createFolder();

            // Save the PDF to a file
            if(pdfName.isEmpty())
            {
                Date currentDate = new Date();
                // Define the date and time format
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                // Format the current date and time as a string
                String formattedDateTime = dateFormat.format(currentDate);
                // Combine "TextScanner" with the formatted date and time
                String resultString = "TextScanner " + formattedDateTime;
                pdfName = resultString + fileName;
            }
            else
            {
                pdfName = pdfName + fileName;
            }

            File pdfFile = new File(Environment.getExternalStorageDirectory(),"/TextScanner/"+pdfName);
            myPdf.writeTo(new FileOutputStream(pdfFile));
            myPdf.close();

            Uri pdfUri = Uri.fromFile(pdfFile);
//            Intent intent = new Intent(ImagesForPdf.this, MainActivity2.class);
//            intent.putExtra("pdfUri",pdfUri.toString());
//            startActivity(intent);

            Toast.makeText(ImagesForPdf.this,"Pdf created successfully",Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(ImagesForPdf.this,"Error creating pdf : "+e,Toast.LENGTH_SHORT).show();
        }

    }

    private void createFolder() {

        //creating folder
        String pdfFolderPath = "TextScanner";
        File pdfFolder = new File(Environment.getExternalStorageDirectory(), pdfFolderPath);
        if (!pdfFolder.exists()) {
            if(!pdfFolder.mkdir())
            {
                Toast.makeText(ImagesForPdf.this, "folder not created", Toast.LENGTH_SHORT).show();
            };
        }

    }

    private Bitmap getBitmapFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            return BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            Log.e("CreatePdfActivity", "Error loading bitmap from URI", e);
            return null;
        }
    }

    private RectF calculateDestinationRect(int originalWidth, int originalHeight, int desiredWidth, int desiredHeight) {
        float aspectRatio = (float) originalWidth / originalHeight;
        float destAspectRatio = (float) desiredWidth / desiredHeight;

        float left;
        float top;
        float right;
        float bottom;

        if (aspectRatio > destAspectRatio) {
            // Image is wider than the desired aspect ratio
            left = 0;
            top = 0.5f * (desiredHeight - (desiredWidth / aspectRatio));
            right = desiredWidth;
            bottom = top + (desiredWidth / aspectRatio);
        } else {
            // Image is taller than the desired aspect ratio
            top = 0;
            left = 0.5f * (desiredWidth - (desiredHeight * aspectRatio));
            bottom = desiredHeight;
            right = left + (desiredHeight * aspectRatio);
        }
        return new RectF(left, top, right, bottom);
    }

    public void BackToMainActivity(View view) {
        finish();
    }
}
