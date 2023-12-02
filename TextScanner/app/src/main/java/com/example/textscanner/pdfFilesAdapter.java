package com.example.textscanner;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class pdfFilesAdapter extends RecyclerView.Adapter<pdfFilesAdapter.AdapterViewHolder> {

    Context context;
    List<String> pdfFiles;

    public pdfFilesAdapter(Context context, List<String> pdfFiles) {
        this.context = context;
        this.pdfFiles = pdfFiles;
    }

    @NonNull
    @Override
    public AdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.pdf_files_item,parent,false);
        return new AdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterViewHolder holder, int position) {
        String path = pdfFiles.get(position);
        File pdfFile = new File(path);
        String fileName = pdfFile.getName();

        holder.pdfFileName.setText(fileName);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Uri pdfUri = Uri.fromFile(new File(path));
                Intent intent = new Intent(context, PdfViewer.class);
                intent.putExtra("pdfUri", pdfUri.toString());
                context.startActivity(intent);
                }
        });
    }

    @Override
    public int getItemCount() {
        return pdfFiles.size();
    }

    static class AdapterViewHolder extends RecyclerView.ViewHolder{
        TextView pdfFileName;
        public AdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            pdfFileName = itemView.findViewById(R.id.pdfFileName);
        }
    }
}
