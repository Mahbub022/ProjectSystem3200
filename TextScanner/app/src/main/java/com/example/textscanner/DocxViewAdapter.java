package com.example.textscanner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DocxViewAdapter extends RecyclerView.Adapter<DocxViewAdapter.ViewHolder> {

    private final List<String> textlist;

    public DocxViewAdapter(List<String> textlist) {
        this.textlist = textlist;
    }

    @NonNull
    @Override
    public DocxViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.docx_view_page,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocxViewAdapter.ViewHolder holder, int position) {
        String item = textlist.get(position);
        holder.textView.setText(item);
    }

    @Override
    public int getItemCount() {
        return textlist.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public EditText textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.docxTextView);
        }
    }
}
