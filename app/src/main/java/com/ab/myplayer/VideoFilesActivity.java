package com.ab.myplayer;

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

import java.util.ArrayList;

public class VideoFilesActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    public static final String MY_PREF = "my pref";
    RecyclerView recyclerView;
    private ArrayList<MediaFiles> videoFilesArrayList = new ArrayList<>();
    static VideoFilesAdapter videoFilesAdapter;
    String Folder_Name;
    String path;
    SwipeRefreshLayout swipeRefreshLayout;
    String sortOrder;
    TextView tv1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_files);
        Folder_Name = getIntent().getStringExtra("folderName");
        getSupportActionBar().setTitle(Folder_Name);
        recyclerView = findViewById(R.id.video_rv);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_video);
        path = getIntent().getStringExtra("path");
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREF,MODE_PRIVATE).edit();
        editor.putString("playlistFolderPath",path);
        editor.putString("FolderName",Folder_Name);
        editor.apply();
        showVideoFiles();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                showVideoFiles();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void showVideoFiles() {
        videoFilesArrayList = fetchMedia(path);
        videoFilesAdapter = new VideoFilesAdapter(videoFilesArrayList,this,0);
        recyclerView.setAdapter(videoFilesAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,RecyclerView.VERTICAL,false));
        videoFilesAdapter.notifyDataSetChanged();
    }

    private ArrayList<MediaFiles> fetchMedia(String folderName) {
        SharedPreferences preferences = getSharedPreferences(MY_PREF,MODE_PRIVATE);
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
        String [] selectionArg = new String[]{"%" + folderName + "%",
                "%" + folderName + "/%/%"};
        Cursor cursor = getContentResolver().query(uri,null,selection,selectionArg,sortOrder);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.video_menu,menu);
        MenuItem menuItem = menu.findItem(R.id.serch_video);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        SharedPreferences preferences = getSharedPreferences(MY_PREF,MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        int id = item.getItemId();
        switch (id)
        {
            case R.id.refresh_files:
                finish();
                startActivity(getIntent());
                break;
            case R.id.sort_by:
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("Sort By");
                alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        editor.apply();
                        finish();
                        startActivity(getIntent());
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
                alertDialog.create().show();
                break;
        }
        return super.onOptionsItemSelected(item);
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
        VideoFilesActivity.videoFilesAdapter.updateVideoFiles(mediaFiles);
        return true;
    }
}