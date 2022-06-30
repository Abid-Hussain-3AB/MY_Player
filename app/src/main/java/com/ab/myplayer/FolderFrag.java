package com.ab.myplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.provider.MediaStore;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

public class FolderFrag extends Fragment {
    RecyclerView recyclerView;
    VideoFolderAdapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<MediaFiles> mediaFiles= new ArrayList<>();
    ArrayList<String> allFolderList = new ArrayList<>();
    View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_folder, container, false);
        recyclerView = v.findViewById(R.id.folder_rv);
        swipeRefreshLayout = v.findViewById(R.id.swipe_Refresh_Folder);
        showFolder();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                 showFolder();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        return v;
    }
    @SuppressLint("NotifyDataSetChanged")
    private void showFolder() {
        mediaFiles = fetchMedia();
        adapter = new VideoFolderAdapter(mediaFiles,allFolderList,getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.VERTICAL,false));
        adapter.notifyDataSetChanged();
    }

    private ArrayList<MediaFiles> fetchMedia() {
        ArrayList<MediaFiles> mediaFilesArrayList = new ArrayList<>();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToNext()) {
            do {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
                String displayname = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
                String size = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
                String duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                String path =cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                String dateaded = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED));
                MediaFiles mediaFiles = new MediaFiles(id, title, displayname, size, duration, path, dateaded);
                int index = path.lastIndexOf("/");
                String subString = path.substring(0,index);
                if (!allFolderList.contains(subString)) {
                    allFolderList.add(subString);
                }
                mediaFilesArrayList.add(mediaFiles);

            } while (cursor.moveToNext());

        }
        return mediaFilesArrayList;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.folder_menu,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.rateus:
                Uri uri = Uri.parse("https://play.google.com/store/apps/details?id="+v.getContext().getApplicationContext());
                Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                startActivity(intent);
                break;
            case R.id.resfresh_folder:
                getActivity().finish();
                 v.getContext().startActivity(getActivity().getIntent());
                break;
            case R.id.share_app:
                Intent shreIntent = new Intent();
                shreIntent.setAction(Intent.ACTION_SEND);
                shreIntent.putExtra(Intent.EXTRA_TEXT, "Check this app via\n"+
                        "https://play.google.com/store/apps/details?id="
                        +v.getContext().getApplicationContext().getPackageName());
                shreIntent.setType("text/plain");
                startActivity(Intent.createChooser(shreIntent,"Share App via"));
                break;
        }
        return true;
    }
}