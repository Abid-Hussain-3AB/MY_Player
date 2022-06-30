package com.ab.myplayer;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentQueryMap;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

public class AllVideoFragment extends DialogFragment implements SearchView.OnQueryTextListener{
    public static final String MY_PREF = "my pref";
    RecyclerView recyclerView;
    private ArrayList<MediaFiles> videoFilesArrayList = new ArrayList<>();
    static VideoFilesAdapter videoFilesAdapter;
    SwipeRefreshLayout swipeRefreshLayout;
    String sortOrder;
    View v;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_all_video, container, false);
        recyclerView = v.findViewById(R.id.folder_rv1);
        swipeRefreshLayout = v.findViewById(R.id.swipe_Refresh_Folder);
        SharedPreferences.Editor editor = v.getContext().getSharedPreferences(MY_PREF,MODE_PRIVATE).edit();
        editor.apply();
        showVideoFiles();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                showVideoFiles();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        return v;
    }
    private void showVideoFiles() {
        videoFilesArrayList = fetchMedia();
        videoFilesAdapter = new VideoFilesAdapter(videoFilesArrayList,v.getContext(),0);
        recyclerView.setAdapter(videoFilesAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.VERTICAL,false));
        videoFilesAdapter.notifyDataSetChanged();
    }
    private ArrayList<MediaFiles> fetchMedia() {
        SharedPreferences preferences = v.getContext().getSharedPreferences(MY_PREF,MODE_PRIVATE);
        String sort_Value = preferences.getString("sort","abcd");
        ArrayList<MediaFiles> videoFiles = new ArrayList<>();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        if (sort_Value.equals("sortName"))
        {
            sortOrder = MediaStore.MediaColumns.DISPLAY_NAME+" ASC";
        }else if(sort_Value.equals("sortSize"))
        {
            sortOrder = MediaStore.MediaColumns.SIZE+" DESC";
        } else if (sort_Value.equals("sortDate"))
        {
            sortOrder = MediaStore.MediaColumns.DATE_ADDED+" DESC";
        } else {
            sortOrder = MediaStore.MediaColumns.DURATION+" DESC";
        }
        String selection =  MediaStore.Audio.Media.DATA + " LIKE ? AND " + MediaStore.Audio.Media.DATA + " NOT LIKE ? ";
        Cursor cursor = getContext().getContentResolver().query(uri,null,null,null,sortOrder);
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.video_menu,menu);
        MenuItem menuItem = menu.findItem(R.id.serch_video);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        SharedPreferences preferences = v.getContext().getSharedPreferences(MY_PREF,MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        int id = item.getItemId();
        switch (id)
        {
            case R.id.refresh_files:
                getActivity().finish();
                v.getContext().startActivity(getActivity().getIntent());
                break;
            case R.id.sort_by:
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(v.getContext());
                alertDialog.setTitle("Sort By");
                alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        editor.apply();
                        getActivity().finish();
                        v.getContext().startActivity(getActivity().getIntent());
                        dialogInterface.dismiss();
                    }
                });
                String[] items = {"Name (A to Z)", "Size (Big to Small)","Date (New to Old)","Duration (Long to Short)"};
                alertDialog.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i){
                            case 0:
                                editor.putString("sort","sortName");
                                break;
                            case 1:
                                editor.putString("sort","sortSize");
                                break;
                            case 2:
                                editor.putString("sort","sortDate");
                                break;
                            case 3:
                                editor.putString("sort","sortLength");
                                break;
                        }
                    }
                });
                alertDialog.show();
                break;
        }
        return true;
    }
    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        String inputs = s.toLowerCase();
        ArrayList<MediaFiles> mediaFiles = new ArrayList<>();
        for (MediaFiles media: videoFilesArrayList)
        {
            if (media.getTitle().toLowerCase().contains(inputs))
            {
                mediaFiles.add(media);
            }
        }
        AllVideoFragment.videoFilesAdapter.updateVideoFiles(mediaFiles);
        return true;
    }

}