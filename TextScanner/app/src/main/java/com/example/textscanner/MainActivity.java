package com.example.textscanner;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

import java.util.List;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final int REQUEST_CODE_PERMISSIONS = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check and request permissions on activity creation
        requestPermissions();
    }

    public void ImageToText(View view) {
        startActivity(new Intent(MainActivity.this,ImageToText.class));
    }

    private void requestPermissions() {
        String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};

        if (EasyPermissions.hasPermissions(this, permissions)) {
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

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        // Permissions denied
        showCustomMessage();
    }

    private void performTasks() {
        // Tasks to perform after getting all permissions
        Toast.makeText(this, "All permissions granted. Performing tasks...", Toast.LENGTH_SHORT).show();
        // Add your tasks here
    }

    private void showCustomMessage() {
        // Custom message for permission denial
        Toast.makeText(this, "Permission denied. Some features may not work.", Toast.LENGTH_SHORT).show();
    }
}
