package com.example.textscanner;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private ArrayList<Uri> uriArrayList;
    public ImageAdapter(ArrayList<Uri> uriArrayList) {
        this.uriArrayList = uriArrayList;
    }
    @NonNull
    @Override
    public ImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.select_images,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageAdapter.ViewHolder holder, int position) {
        holder.imageView.setImageURI(uriArrayList.get(position));
        int imageNumber = position+1;
        holder.imageCount.setText(String.valueOf(imageNumber));
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uriArrayList.remove(uriArrayList.get(holder.getAdapterPosition()));
                notifyItemRemoved(holder.getAdapterPosition());
                notifyItemRangeChanged(holder.getAdapterPosition(), getItemCount());
                notifyDataSetChanged();

            }
        });
    }

    @Override
    public int getItemCount() {
        return uriArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView,delete;
        TextView imageCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.singleImage);
            delete = itemView.findViewById(R.id.deleteImage);
            imageCount = itemView.findViewById(R.id.imageCountText);
        }
    }
}