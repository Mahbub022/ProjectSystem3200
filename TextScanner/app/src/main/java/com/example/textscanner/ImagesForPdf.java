package com.example.textscanner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

//import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
//import android.content.Context;
import android.content.Intent;
//import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.AsyncTask;
//import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
//import android.view.inputmethod.InputMethodManager;
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
    String pdfName = "";
    RecyclerView recyclerView;
    Button selectButton,captureImage;
    EditText pdfNameText;
    ArrayList<Uri> uriArrayList = new ArrayList<>();
    ImageAdapter imageAdapter;
    private ProgressDialog progressDialog;
    private static final int REQUEST_CAMERA_IMAGES = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images_for_pdf);

        pdfNameText = findViewById(R.id.pdfNameText);
        selectButton = findViewById(R.id.selectImageButton);
        captureImage = findViewById(R.id.cameraImage);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating pdf...");
        recyclerView = findViewById(R.id.ImageRecycler);

        imageAdapter = new ImageAdapter(uriArrayList);

        recyclerView.setLayoutManager(new GridLayoutManager(ImagesForPdf.this,2));
        recyclerView.setAdapter(imageAdapter);

        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
//                if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.JELLY_BEAN_MR2)
//                {
//                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
//                }
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Pictures"),1);

            }
        });
    }

    public void ImageFromCamera(View view) {
        Intent takePictureIntent = new Intent();
        takePictureIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
        takePictureIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_CAMERA_IMAGES);
        } else {
            Toast.makeText(ImagesForPdf.this, "No camera app available", Toast.LENGTH_SHORT).show();
        }
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
        } else if (requestCode == REQUEST_CAMERA_IMAGES && resultCode == Activity.RESULT_OK && null != data) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                ArrayList<Uri> cameraImageUris = saveCameraImages(extras);
                uriArrayList.addAll(cameraImageUris);
                imageAdapter.notifyDataSetChanged();
            }
        }
    }

    private ArrayList<Uri> saveCameraImages(Bundle extras) {
        ArrayList<Uri> cameraImageUris = new ArrayList<>();
        for (int i = 0; i <1; i++) {
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            File imageFile = createImageFile();
            try {
                FileOutputStream out = new FileOutputStream(imageFile);
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
                cameraImageUris.add(Uri.fromFile(imageFile));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return cameraImageUris;
    }

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(storageDir, imageFileName + ".jpg");
    }

    public void pdfCreate(View view) {
        if(uriArrayList.isEmpty())
        {
            Toast.makeText(ImagesForPdf.this,"No images selected to create pdf",Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.show();

        CreatePdfTask createPdfTask = new CreatePdfTask();
        createPdfTask.execute(uriArrayList);
    }

    @SuppressLint("StaticFieldLeak")
    private class CreatePdfTask extends AsyncTask<ArrayList<Uri>, Void, String> {

        @Override
        protected String doInBackground(ArrayList<Uri>... uriLists) {
            PdfDocument myPdf = new PdfDocument();
            String fileName = ".pdf";
            pdfName = pdfNameText.getText().toString().trim();

            try {
                ArrayList<Uri> uriList = uriLists[0];

                for (int i = 0; i < uriList.size(); i++) {
                    Bitmap bitmap = getBitmapFromUri(uriList.get(i));
                    int originalWidth = bitmap.getWidth();
                    int originalHeight = bitmap.getHeight();
                    int desiredWidth = 400;
                    int desiredHeight = (int) ((float) originalHeight / originalWidth * desiredWidth);
                    RectF destinationRect = calculateDestinationRect(originalWidth, originalHeight, desiredWidth, desiredHeight);

                    PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(desiredWidth, desiredHeight, i + 1).create();
                    PdfDocument.Page page = myPdf.startPage(pageInfo);
                    Canvas canvas = page.getCanvas();
                    canvas.drawBitmap(bitmap, null, destinationRect, new Paint());
                    myPdf.finishPage(page);
                }

                createFolder();

                if (pdfName.isEmpty()) {
                    Date currentDate = new Date();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    String formattedDateTime = dateFormat.format(currentDate);
                    String resultString = "TextScanner " + formattedDateTime;
                    pdfName = resultString + fileName;
                } else {
                    pdfName = pdfName + fileName;
                }

                File pdfFile = new File(Environment.getExternalStorageDirectory(), "/TextScanner/" + pdfName);
                myPdf.writeTo(new FileOutputStream(pdfFile));
                myPdf.close();

                return pdfFile.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            if (result != null) {
                Toast.makeText(ImagesForPdf.this, pdfName + " file created successfully", Toast.LENGTH_SHORT).show();
                Uri pdfUri = Uri.fromFile(new File(result));
                Intent intent = new Intent(ImagesForPdf.this, PdfViewer.class);
                intent.putExtra("pdfUri", pdfUri.toString());
                startActivity(intent);
            } else {
                Toast.makeText(ImagesForPdf.this, "Error creating PDF", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void createFolder() {

        //creating folder
        String pdfFolderPath = "TextScanner";
        File pdfFolder = new File(Environment.getExternalStorageDirectory(), pdfFolderPath);
        if (!pdfFolder.exists()) {
            if(!pdfFolder.mkdirs())
            {
                Log.d("Folder", Environment.getExternalStorageDirectory().getAbsolutePath().toString());
                Toast.makeText(this,Environment.getExternalStorageDirectory().toString(),Toast.LENGTH_LONG).show();
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


//recyclerView custom gridLayout
//      int spanCount = 2; // or your desired span count
//      int spacing = 2; // or your desired spacing in pixels
//      boolean includeEdge = true; // whether to include spacing at the edge of the grid
//
//      GridLayoutManager layoutManager = new GridLayoutManager(ImagesForPdf.this, spanCount);
//      recyclerView.setLayoutManager(layoutManager);
//      recyclerView.addItemDecoration(new ImagesDecoration(spanCount, spacing, includeEdge));
//      recyclerView.setAdapter(imageAdapter);


//    public void showKeyboard(View view) {
//        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        if (imm != null) {
//            pdfNameText.requestFocus();
//            imm.showSoftInput(pdfNameText, InputMethodManager.SHOW_IMPLICIT);
//        }
//    }
