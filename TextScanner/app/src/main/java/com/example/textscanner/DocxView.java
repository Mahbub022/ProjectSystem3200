package com.example.textscanner;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DocxView extends AppCompatActivity {
    String docxName="";
    private List<String> textList = new ArrayList<>();
    EditText text;
    RecyclerView recyclerView;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_docx_view);

        text = findViewById(R.id.editTextTitle);
        recyclerView = findViewById(R.id.recyclerDocxView);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating Docx file...");

        // Retrieve the list of strings from the intent
        textList = getIntent().getStringArrayListExtra("textList");

        // Create and set the adapter
        DocxViewAdapter adapter = new DocxViewAdapter(textList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);

    }

    public void BackToPdfView(View view){
        finish();
    }
    public void saveDocx(View view) {
        progressDialog.show();
    //Create a file with a unique name (you can use a timestamp or any other unique identifier)
    String fileName = "Docx" + System.currentTimeMillis() + ".docx";
    docxName = text.getText().toString().trim();
    if (docxName.isEmpty()) {
        docxName = fileName;
    } else {
        docxName = docxName + ".docx";
    }

    DocxCreator docxCreator = new DocxCreator();
        docxCreator.createDocxFileAsync(docxName, textList, new DocxCreator.DocxCreationListener() {
            @Override
            public void onDocxCreated(boolean success) {
                progressDialog.dismiss();
                if (success) {
                    Toast.makeText(DocxView.this, docxName + " file created successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DocxView.this, "Failed to create " + docxName + " file", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
