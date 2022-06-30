package com.ab.myplayer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.UriPermission;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.common.collect.Collections2;

import org.w3c.dom.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class VideoFilesAdapter extends RecyclerView.Adapter<VideoFilesAdapter.ViewHolder> {
    private ArrayList<MediaFiles> videolist;
    private Context context;
    BottomSheetDialog bottomSheetDialog;
    private int viewType;
    public static final int DELETE_REQUEST_CODE = 13;
    public static final int Rename_REQUEST_CODE = 14;
    public VideoFilesAdapter(ArrayList<MediaFiles> videolist, Context context, int viewType) {
        this.videolist = videolist;
        this.context = context;
        this.viewType = viewType;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.video_item,parent,false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.videoName.setText(videolist.get(position).getDisplayName());
        String size = videolist.get(position).getSize();
        holder.videoSize.setText(android.text.format.Formatter.formatFileSize(context, Long.parseLong(size)));
        double miliseconds = Double.parseDouble(videolist.get(position).getDuration());
        Glide.with(context).load(new File(videolist.get(position).getPath())).into(holder.thumbnail);
        holder.videoDuration.setText(timeConversion((long) miliseconds));


        if(viewType ==0)
        {
            holder.menu_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetDialog = new BottomSheetDialog(context,R.style.BottomSheetTheme);
                    View bsView = LayoutInflater.from(context).inflate(R.layout.video_bs_layout,view.findViewById(R.id.bottom_sheet));
                    bsView.findViewById(R.id.bs_play).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            holder.itemView.performClick();
                            bottomSheetDialog.dismiss();
                        }
                    });
                    bsView.findViewById(R.id.bs_rename).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Uri contentUri = ContentUris
                                    .withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                            Long.parseLong(videolist.get(position).getId()));
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                            alertDialog.setTitle("Rename to");
                            EditText edittext = new EditText(context);
                            String path =videolist.get(position).getPath();
                            final File file = new File(path);
                            String videoName =file.getName();
                            videoName = videoName.substring(0,videoName.lastIndexOf("."));
                            edittext.setText(videoName);
                            alertDialog.setView(edittext);
                            edittext.requestFocus();
                            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @RequiresApi(api = Build.VERSION_CODES.R)
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (TextUtils.isEmpty(edittext.getText().toString()))
                                    {
                                        Toast.makeText(context, "Cant Rename empty File", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    String onlyPath = Objects.requireNonNull(file.getParentFile()).getAbsolutePath();
                                    String ext = file.getAbsolutePath();
                                    ext = ext.substring(ext.lastIndexOf("."));
                                    String newpath = onlyPath+"/"+edittext.getText().toString() + ext;
                                    File newFile = new File(newpath);
                                    boolean rename = file.renameTo(newFile);
                                    if (rename)
                                    {
                                        ContentResolver resolver = context.getApplicationContext().getContentResolver();
                                        resolver.delete(MediaStore.Files.getContentUri("external"),
                                                MediaStore.MediaColumns.DATA+"=?", new String[]
                                                        {file.getAbsolutePath()});
                                        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                        intent.setData(Uri.fromFile(newFile));
                                        context.getApplicationContext().sendBroadcast(intent);
                                        notifyDataSetChanged();
                                        SystemClock.sleep(200);
                                        ((Activity)context).recreate();
                                        Toast.makeText(context, "Video Renamed", Toast.LENGTH_SHORT).show();

                                    }
                                    else {
                                        try {
                                            requestRenameR(contentUri);
                                        } catch (IntentSender.SendIntentException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            alertDialog.create().show();
                            bottomSheetDialog.dismiss();
                        }
                    });
                    bsView.findViewById(R.id.bs_share).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Uri uri = Uri.parse(videolist.get(position).getPath());
                            Intent shreIntent = new Intent(Intent.ACTION_SEND);
                            shreIntent.setType("video/*");
                            shreIntent.putExtra(Intent.EXTRA_STREAM,uri);
                            context.startActivity(Intent.createChooser(shreIntent,"Share video via"));
                            bottomSheetDialog.dismiss();
                        }
                    });
                    bsView.findViewById(R.id.bs_delete).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                            alertDialog.setTitle("Delete");
                            alertDialog.setMessage("Do you want to delete");
                            alertDialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @RequiresApi(api = Build.VERSION_CODES.R)
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Uri contentUri = ContentUris
                                            .withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                                    Long.parseLong(videolist.get(position).getId()));
                                    File file = new File(videolist.get(position).getPath());
                                    boolean delv=file.delete();
                                    if (delv)
                                    {
                                        context.getContentResolver().delete(contentUri,null, null);
                                        videolist.remove(position);
                                        notifyItemRemoved(position);
                                        notifyItemRangeChanged(position,videolist.size());
                                        Toast.makeText(context, "Video Deleted", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                       // Toast.makeText(context, "Not Deleted", Toast.LENGTH_SHORT).show();
                                        try {
                                            requestDeleteR(contentUri);
                                        } catch (IntentSender.SendIntentException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            alertDialog.show();
                            bottomSheetDialog.dismiss();
                        }
                    });
                    bsView.findViewById(R.id.bs_properties).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                            alertDialog.setTitle("Properties");
                            String one = "Name: " + videolist.get(position).getDisplayName();
                            String path = videolist.get(position).getPath();
                            int indexofPath = path.lastIndexOf("/");
                            String two = "Location: " + path.substring(0, indexofPath);
                            String three = "Size: " + android.text.format.Formatter
                                    .formatFileSize(context, Long.parseLong(videolist.get(position).getSize()));
                            String four = "Duration: "+ timeConversion((long) miliseconds);
                            String namewithFormat = videolist.get(position).getDisplayName();
                            int index = namewithFormat.lastIndexOf(".");
                            String format = namewithFormat.substring(index+1);
                            String five = "Format: " + format;
                            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                            mediaMetadataRetriever.setDataSource(videolist.get(position).getPath());
                            String height = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                            String width = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
                            String six = "Resolution: "+ width + "x" + height;

                            alertDialog.setMessage(one + "\n\n" + two + "\n\n" + three + "\n\n" + four + "\n\n" + five + "\n\n" + six + "\n\n");
                            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            alertDialog.show();
                            bottomSheetDialog.dismiss();
                        }
                    });
                    bottomSheetDialog.setContentView(bsView);
                    bottomSheetDialog.show();
                }
            });

        }
        else
        {
            holder.menu_more.setVisibility(View.GONE);
            holder.videoName.setTextColor(Color.WHITE);
            holder.videoSize.setTextColor(Color.WHITE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,VideoPlayerActivity.class);
                intent.putExtra("position",position);
                intent.putExtra("video_title",videolist.get(position).getDisplayName());
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("videoArrayList",videolist);
                intent.putExtras(bundle);
                context.startActivity(intent);
                if (viewType ==1)
                {
                    ((Activity)context).finish();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return videolist.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail, menu_more;
        TextView videoName, videoSize, videoDuration;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.thumnail);
            menu_more = itemView.findViewById(R.id.video_menu_more);
            videoName = itemView.findViewById(R.id.video_name);
            videoSize = itemView.findViewById(R.id.video_size);
            videoDuration = itemView.findViewById(R.id.video_duration);

        }
    }
    @SuppressLint("DefaultLocale")
    public String timeConversion(long value)
    {
        String videoTime;
        int duration = (int) value;
        //int hrs = (duration/3600000);
        //int mns = (duration/60000)%60000;
       // int scs = duration%60000/1000;
        int sec = (duration % 60000 / 1000);
        int min = (duration / 60000) % 60000;
        int hrs = (duration / 3600000);
        if (hrs>0)
        {
            videoTime = String.format("%02d:%02d:%02d", hrs, min, sec);
        }
        else {
            videoTime = String.format("%02d:%02d", min, sec);
        }
        return videoTime;
    }
    @SuppressLint("NotifyDataSetChanged")
    void updateVideoFiles(ArrayList<MediaFiles> files)
    {
        videolist = new ArrayList<>();
        videolist.addAll(files);
        notifyDataSetChanged();
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void requestDeleteR(Uri position) throws IntentSender.SendIntentException {
        List<Uri> uriList = new ArrayList<Uri>();
        Collections.addAll(uriList,position);
        PendingIntent pendingIntent = MediaStore.createDeleteRequest(context.getContentResolver(),uriList);
        ((Activity)context).startIntentSenderForResult(pendingIntent.getIntentSender(),DELETE_REQUEST_CODE,null,0,0,0,null);

    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void requestRenameR(Uri position) throws IntentSender.SendIntentException {
        List<Uri> uriList = new ArrayList<Uri>();
        Collections.addAll(uriList,position);
         PendingIntent pendingIntent = MediaStore.createWriteRequest(context.getContentResolver(),uriList);
        ((Activity)context).startIntentSenderForResult(pendingIntent.getIntentSender(),Rename_REQUEST_CODE,null,0,0,0,null);

    }

}
