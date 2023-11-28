package com.example.textscanner;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PdfViewAdapter  extends RecyclerView.Adapter<PdfViewAdapter.PdfViewHolder> {
    private List<Bitmap> pdfPages;

    public PdfViewAdapter(List<Bitmap> pdfPages) {
        this.pdfPages = pdfPages;
    }

    @NonNull
    @Override
    public PdfViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pdf_page_view, parent, false);
        return new PdfViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PdfViewHolder holder, int position) {
        Bitmap page = pdfPages.get(position);
        holder.pdfImage.setImageBitmap(page);
        holder.countImage.setText(String.valueOf(position+1));
    }

    @Override
    public int getItemCount() {
        return pdfPages.size();
    }

    public static class PdfViewHolder extends RecyclerView.ViewHolder {
        ImageView pdfImage;
        TextView countImage;


        public PdfViewHolder(@NonNull View itemView) {
            super(itemView);
            pdfImage = itemView.findViewById(R.id.pdfImageView);
            countImage = itemView.findViewById(R.id.pdfImageCountText);
        }
    }
}
