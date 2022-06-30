package com.ab.myplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.Settings;
//import android.support.annotation.RequiresApi;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager viewPager;
    FragmentAdapter fragmentAdapter;
    private static final int REQUEST_PERMISSION_SETTING = 0 ;
    private ArrayList<MediaFiles> mediaFiles= new ArrayList<>();
    ArrayList<String> allFolderList = new ArrayList<>();
    RecyclerView recyclerView;
    VideoFolderAdapter adapter;
    VideoFilesAdapter adapter1;
    SwipeRefreshLayout swipeRefreshLayout;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolBar);
        tabLayout = findViewById(R.id.tablayout);
        viewPager = findViewById(R.id.viewpager);
        setSupportActionBar(toolbar);
        tabLayout.addTab(tabLayout.newTab().setText("Folders"));
        tabLayout.addTab(tabLayout.newTab().setText("All Videos"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        fragmentAdapter = new FragmentAdapter(getSupportFragmentManager(),tabLayout.getTabCount());
        viewPager.setAdapter(fragmentAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED)
        {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package",getPackageName(),null);
            intent.setData(uri);
            startActivityForResult(intent,REQUEST_PERMISSION_SETTING);
        }
      //  recyclerView = findViewById(R.id.folder_rv);
        swipeRefreshLayout = findViewById(R.id.swipe_Refresh_Folder);
       //showFolder();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
               // showFolder();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void showFolder() {
        mediaFiles = fetchMedia();
        adapter = new VideoFolderAdapter(mediaFiles,allFolderList,this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,RecyclerView.VERTICAL,false));
        adapter.notifyDataSetChanged();
    }

    private ArrayList<MediaFiles> fetchMedia() {
        ArrayList<MediaFiles> mediaFilesArrayList = new ArrayList<>();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
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
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.folder_menu,menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == adapter1.DELETE_REQUEST_CODE)
        {
            if (resultCode!=0)
            {
                adapter1.notifyItemRemoved(adapter1.getItemCount());
                adapter1.notifyDataSetChanged();
                SystemClock.sleep(200);

            }
        }
        else if (requestCode == adapter1.Rename_REQUEST_CODE)
        {
            if (resultCode!=0)
            {
                adapter1.notifyItemChanged(adapter1.getItemCount());
                adapter1.notifyDataSetChanged();
                SystemClock.sleep(200);

            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.rateus:
               Uri uri = Uri.parse("https://play.google.com/store/apps/details?id="+getApplicationContext());
            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                startActivity(intent);
                break;
            case R.id.resfresh_folder:
                finish();
                startActivity(getIntent());
                break;
            case R.id.sort_by:
                Toast.makeText(this, "working", Toast.LENGTH_SHORT).show();
                break;
            case R.id.share_app:
                Intent shreIntent = new Intent();
                shreIntent.setAction(Intent.ACTION_SEND);
                shreIntent.putExtra(Intent.EXTRA_TEXT, "Check this app via\n"+
                        "https://play.google.com/store/apps/details?id="
                +getApplicationContext().getPackageName());
                shreIntent.setType("text/plain");
                startActivity(Intent.createChooser(shreIntent,"Share App via"));
                break;
        }
        return true;
    }

 */
}