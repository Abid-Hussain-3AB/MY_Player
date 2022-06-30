package com.ab.myplayer;

import static com.ab.myplayer.VideoFilesActivity.MY_PREF;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class PlayListDialog extends BottomSheetDialogFragment {
    ArrayList<MediaFiles> arrayList = new ArrayList<>();
    VideoFilesAdapter videoFilesAdapter;
    BottomSheetDialog bottomSheetDialog;
    RecyclerView recyclerView;
    TextView folderName;

    public PlayListDialog(ArrayList<MediaFiles> arrayList, VideoFilesAdapter videoFilesAdapter) {
        this.arrayList = arrayList;
        this.videoFilesAdapter = videoFilesAdapter;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

      bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.playlist_bs_layout,null);
        bottomSheetDialog.setContentView(view);
        recyclerView = view.findViewById(R.id.playlist_rv);
        folderName = view.findViewById(R.id.playlist_name);
        SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences(MY_PREF, Context.MODE_PRIVATE);
        String folder_path = sharedPreferences.getString("playlistFolderPath","abc");
        String folder_name = sharedPreferences.getString("FolderName","abcd");
        folderName.setText("Play List");
        arrayList =fetchMedia(folder_path);
        videoFilesAdapter = new VideoFilesAdapter(arrayList,getContext(),1);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),RecyclerView.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(videoFilesAdapter);
        videoFilesAdapter.notifyDataSetChanged();
        return bottomSheetDialog;
    }


    private ArrayList<MediaFiles> fetchMedia(String folderName) {
        ArrayList<MediaFiles> videoFiles = new ArrayList<>();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        String selection =  MediaStore.Audio.Media.DATA + " LIKE ? AND " + MediaStore.Audio.Media.DATA + " NOT LIKE ? ";
        Cursor cursor = getContext().getContentResolver().query(uri,null,null,null,null);
        if (cursor!=null && cursor.moveToNext())
        {
            do {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
                String displayname = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
                String size = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
                String duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                String path =cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                String dateaded = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED));
                MediaFiles mediaFiles = new MediaFiles(id, title, displayname, size, duration, path, dateaded);
                videoFiles.add(mediaFiles);

            }while (cursor.moveToNext());
        }
        return videoFiles;
    }
}
