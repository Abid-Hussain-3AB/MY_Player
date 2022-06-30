package com.ab.myplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
//import android.support.annotation.NonNull;
//import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class VideoFolderAdapter extends RecyclerView.Adapter<VideoFolderAdapter.MyHolder> {
    private ArrayList<MediaFiles> mediaFiles;
    private ArrayList<String> folderPath;
    Context context;

    public VideoFolderAdapter(ArrayList<MediaFiles> mediaFiles, ArrayList<String> folderPath, Context context) {
        this.mediaFiles = mediaFiles;
        this.folderPath = folderPath;
        this.context = context;
    }

    @NonNull
    @Override
    public VideoFolderAdapter.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(context).inflate(R.layout.folder_item,parent,false);
        return new MyHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, @SuppressLint("RecyclerView") int position) {
        int indexPath = folderPath.get(position).lastIndexOf("/");
        String nameOfFolder = folderPath.get(position).substring(indexPath+1);
        holder.folderName.setText(nameOfFolder);
        holder.Folder_Path.setText(folderPath.get(position));
        holder.noofFiles.setText(noOfFiles(folderPath.get(position))+" Videos");
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,VideoFilesActivity.class);
                intent.putExtra("folderName",nameOfFolder);
                intent.putExtra("path",folderPath.get(position));
                context.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return folderPath.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        TextView folderName, Folder_Path, noofFiles;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            folderName = itemView.findViewById(R.id.foldername);
            Folder_Path = itemView.findViewById(R.id.folderPath);
            noofFiles =  itemView.findViewById(R.id.noOfFiles);
        }
    }
    int noOfFiles(String folder_name)
    {
        int files_no = 0;
        for (MediaFiles mediaFiles: mediaFiles)
        {
            if (mediaFiles.getPath().substring(0,mediaFiles.getPath().lastIndexOf("/"))
            .endsWith(folder_name))
            {
                files_no++;
            }
        }
        return files_no;
    }
}
