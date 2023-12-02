package com.example.textscanner;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final int REQUEST_CODE_PERMISSIONS = 123;
    RecyclerView recycler;
    ArrayList<String> pdfFiles = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recycler = findViewById(R.id.pdfFileRecycler);

        // Check and request permissions on activity creation
        requestPermissions();
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(new pdfFilesAdapter(this,pdfFiles()));
    }

    public ArrayList<String> pdfFiles()
    {
        try{
        ContentResolver contentResolver = getContentResolver();

        String file = MediaStore.Files.FileColumns.MIME_TYPE + "=?";
        String fileType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf");
        String[] args = new String[] {fileType};
        String[] data = {MediaStore.Files.FileColumns.DATA,MediaStore.Files.FileColumns.DISPLAY_NAME};
        String sortingOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC";
        Cursor cursor = contentResolver.query(MediaStore.Files.getContentUri("external"),
                data,file,args,sortingOrder);
        if(cursor != null){
            while(cursor.moveToNext()){
                int index = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
                String path = cursor.getString(index);
                Log.d("FilePath", "Path: " + path);
                pdfFiles.add(path);
                }
            cursor.close();
            }
        } catch (Exception e) {
            Toast.makeText(this,"Error : "+e,Toast.LENGTH_SHORT).show();
        }
        return pdfFiles;
    }

    public void ImageToText(View view) {
        startActivity(new Intent(MainActivity.this,ImageToText.class));
    }

    public void createPdf(View view) {
        startActivity(new Intent(MainActivity.this,ImagesForPdf.class));
    }
    private void requestPermissions() {
        String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
//        String[] permissions = {Manifest.permission.CAMERA,Manifest.permission.MANAGE_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(this, permissions)) {
            // Request permissions
            EasyPermissions.requestPermissions(
                    new PermissionRequest.Builder(this, REQUEST_CODE_PERMISSIONS, permissions)
                            .setRationale("Permissions are required to create pdf and docx file for capturing or selecting images.")
                            .setPositiveButtonText("Grant")
                            .setNegativeButtonText("Deny")
                            .setTheme(R.style.Base_Theme_TextScanner)
                            .build()
            );
        } else {
            // All permissions are already granted
            performTasks();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        // Permissions have been granted
        performTasks();
    }

    private void performOneTasks() {
        Toast.makeText(this, "Not all permissions granted. Some features may not work.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        // Permissions denied
      //  showCustomMessage();
        performOneTasks();
    }

    private void performTasks() {
        // Tasks to perform after getting all permissions
        Toast.makeText(this, "All permissions granted. Performing tasks...", Toast.LENGTH_SHORT).show();
        // Add your tasks here
    }


//    public void pdfFiles(View view) {
//        recycler.setLayoutManager(new LinearLayoutManager(this));
//        recycler.setAdapter(new pdfFilesAdapter(this,pdfFiles()));
//    }
}
