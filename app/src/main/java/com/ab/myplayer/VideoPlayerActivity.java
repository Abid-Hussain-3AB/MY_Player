package com.ab.myplayer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PictureInPictureParams;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.service.controls.Control;
import android.service.controls.ControlsProviderService;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Rational;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.developer.filepicker.controller.DialogSelectionListener;
import com.developer.filepicker.model.DialogConfigs;
import com.developer.filepicker.model.DialogProperties;
import com.developer.filepicker.view.FilePickerDialog;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.mediacodec.MediaCodecAdapter;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class VideoPlayerActivity extends AppCompatActivity implements View.OnClickListener {
    PlayerView playerView;
    SimpleExoPlayer player;
    int position;
    String videoTitle;
    TextView title;
    ArrayList<MediaFiles> mVideoFiles = new ArrayList<>();
    ConcatenatingMediaSource concatenatingMediaSource;

    public enum ControlsMode{
        LOCK,FULLSCREEN;
    }
    public ControlsMode controlsMode;
    private ArrayList<IconModel> iconModelArrayList = new ArrayList<>();
    PlayBackIconAdapter playBackIconAdapter;
    RecyclerView recyclerViewIcons;
    boolean expand = false;
    View nightMode;
    boolean dark = false;
    boolean mute = false;
    PlaybackParameters parameters;
    float speed;
    DialogProperties dialogProperties;
    FilePickerDialog filePickerDialog;
    Uri uriSubtitle;
    PictureInPictureParams.Builder pictureInpicture;
    boolean isCrossChecked;
    //horizantel recyclerview vriables

    VideoFilesAdapter videoFilesAdapter;
    ImageView nextButton, preButton, videoBack, lock, unlock, scalling, videoList;
    RelativeLayout root;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_video_player);
        getSupportActionBar().hide();
        playerView = findViewById(R.id.exoplayer_view1);
        nextButton = findViewById(R.id.exo_nrxt);
        preButton = findViewById(R.id.exo_pre);
        videoBack = findViewById(R.id.video_back);
        lock = findViewById(R.id.lock);
        unlock = findViewById(R.id.unlock);
        root = findViewById(R.id.root_layout);
        scalling = findViewById(R.id.exo_scalling);
        recyclerViewIcons = findViewById(R.id.recyclericon_icon);
        nightMode = findViewById(R.id.night_mode);
        videoList = findViewById(R.id.video_list);
        position = getIntent().getIntExtra("position",1);
        videoTitle = getIntent().getStringExtra("video_title");
        mVideoFiles = getIntent().getExtras().getParcelableArrayList("videoArrayList");
        screenOrientation();
        title = findViewById(R.id.video_title);
        title.setText(videoTitle);
        nextButton.setOnClickListener(this);
        preButton.setOnClickListener(this);
        videoBack.setOnClickListener(this);
        lock.setOnClickListener(this);
        unlock.setOnClickListener(this);
        videoList.setOnClickListener(this);
        scalling.setOnClickListener(firstListener);
        dialogProperties = new DialogProperties();
        filePickerDialog = new FilePickerDialog(VideoPlayerActivity.this);
        filePickerDialog.setTitle("Select a Subtitle File");
        filePickerDialog.setPositiveBtnName("Ok");
        filePickerDialog.setNegativeBtnName("Cancel");
      //  if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            pictureInpicture = new PictureInPictureParams.Builder();
        }
        iconModelArrayList.add(new IconModel(R.drawable.ic_right,""));
        iconModelArrayList.add(new IconModel(R.drawable.ic_night_mode,"Night"));
        iconModelArrayList.add(new IconModel(R.drawable.ic_piv_mode,"Popup"));
        iconModelArrayList.add(new IconModel(R.drawable.ic_equalizer,"Equalizer"));
        iconModelArrayList.add(new IconModel(R.drawable.ic_rotate,"Rotate"));
        playBackIconAdapter = new PlayBackIconAdapter(iconModelArrayList,this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,
                RecyclerView.HORIZONTAL,true);
        recyclerViewIcons.setLayoutManager(linearLayoutManager);
        recyclerViewIcons.setAdapter(playBackIconAdapter);
        playBackIconAdapter.notifyDataSetChanged();



        playBackIconAdapter.setOnItemClickListener(new PlayBackIconAdapter.onItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onItemClick(int position) {
                if (position==0)
                {
                   if (expand)
                   {
                       iconModelArrayList.clear();
                       iconModelArrayList.add(new IconModel(R.drawable.ic_right,""));
                       iconModelArrayList.add(new IconModel(R.drawable.ic_night_mode,"Night"));
                       iconModelArrayList.add(new IconModel(R.drawable.ic_piv_mode,"Popup"));
                       iconModelArrayList.add(new IconModel(R.drawable.ic_equalizer,"Equalizer"));
                       iconModelArrayList.add(new IconModel(R.drawable.ic_rotate,"Rotate"));
                       playBackIconAdapter.notifyDataSetChanged();
                       expand = false;
                   }else {
                       if (iconModelArrayList.size()==5)
                       {
                           iconModelArrayList.add(new IconModel(R.drawable.ic_volume_off,"Mute"));
                           iconModelArrayList.add(new IconModel(R.drawable.ic_volume,"Volume"));
                           iconModelArrayList.add(new IconModel(R.drawable.ic_brightness,"Brightness"));
                           iconModelArrayList.add(new IconModel(R.drawable.ic_speed,"Speed"));
                           iconModelArrayList.add(new IconModel(R.drawable.ic_subtitles,"Subtitle"));
                       }
                       iconModelArrayList.set(position, new IconModel(R.drawable.ic_left,""));
                       playBackIconAdapter.notifyDataSetChanged();
                       expand = true;
                   }
                }
                if (position==1)
                {
                    //night mode
                    if (dark){
                        nightMode.setVisibility(View.GONE);
                        iconModelArrayList.set(position,new IconModel(R.drawable.ic_night_mode,"Night"));
                        playBackIconAdapter.notifyDataSetChanged();
                        dark = false;
                    }
                    else {
                        nightMode.setVisibility(View.VISIBLE);
                        iconModelArrayList.set(position,new IconModel(R.drawable.ic_night_mode,"Day"));
                        playBackIconAdapter.notifyDataSetChanged();
                        dark = true;
                    }
                }
                if (position==2)
                {
                    Display d = getWindowManager()
                            .getDefaultDisplay();
                    Point p = new Point();
                    d.getSize(p);
                    int width = p.x;
                    int height = p.y;
                    //popup
                   //if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
                   {
                       Rational aspectRatio = new Rational(width,height);
                       pictureInpicture.setAspectRatio(aspectRatio).build();
                       enterPictureInPictureMode(pictureInpicture.build());
                   }
                  // else
                   {
                       Log.wtf("not Oreo","yes");
                   }
                }
                if (position==3)
                {
                    //Equalizer
                    Intent intent = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
                    if((intent.resolveActivity(getPackageManager())!=null))
                    {
                        startActivityForResult(intent,123);
                    }
                    else {
                        Toast.makeText(VideoPlayerActivity.this, "No Equalizer Found", Toast.LENGTH_SHORT).show();
                    }
                    playBackIconAdapter.notifyDataSetChanged();

                }
                if(position == 4)
                {
                    //rotate
                    if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                    {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        playBackIconAdapter.notifyDataSetChanged();
                    }
                    else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                    {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        playBackIconAdapter.notifyDataSetChanged();
                    }

                }
                if(position == 5)
                {
                    //mute
                    if (mute)
                    {
                        player.setVolume(100);
                        iconModelArrayList.set(position, new IconModel(R.drawable.ic_volume_off,"Mute"));
                        playBackIconAdapter.notifyDataSetChanged();
                        mute = false;
                    }
                    else {
                        player.setVolume(0);
                        iconModelArrayList.set(position,new IconModel(R.drawable.ic_volume,"unMute"));
                        playBackIconAdapter.notifyDataSetChanged();
                        mute = true;
                    }


                }
                if (position == 6)
                {
                    //volume
                    VolumeDialog volumeDialog = new VolumeDialog();
                    volumeDialog.show(getSupportFragmentManager(),"dialog");
                    playBackIconAdapter.notifyDataSetChanged();
                }
                if(position == 7)
                {
                    //brightness
                    BrightnessDialog brightnessDialog = new BrightnessDialog();
                    brightnessDialog.show(getSupportFragmentManager(),"dialog");
                    playBackIconAdapter.notifyDataSetChanged();

                }
                if (position == 8)
                {
                    //speed
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(VideoPlayerActivity.this);
                    alertDialog.setTitle("Select PlacyBack Speed").setPositiveButton("OK",null);
                    String[] items = {"0.5x","1x Normal Speed","1.25x","1.5x","2x"};
                    int checkedItem = -1;
                    alertDialog.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            switch (i)
                            {
                                case 0:
                                    speed = 0.5f;
                                    parameters = new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;
                                case 1:
                                    speed = 1f;
                                    parameters = new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;
                                case 2:
                                    speed = 1.25f;
                                    parameters = new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;
                                case 3:
                                    speed = 1.5f;
                                    parameters = new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;
                                case 4:
                                    speed = 2f;
                                    parameters = new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
                    AlertDialog alert = alertDialog.create();
                    alert.show();

                }
                if(position == 9)
                {
                    //subtitle
                    dialogProperties.selection_mode = DialogConfigs.SINGLE_MODE;
                    dialogProperties.extensions = new String[]{".str"};
                    dialogProperties.root = new File("/storage/emulated/0");
                    filePickerDialog.setProperties(dialogProperties);
                    filePickerDialog.show();
                    filePickerDialog.setDialogSelectionListener(new DialogSelectionListener() {
                        @Override
                        public void onSelectedFilePaths(String[] files) {
                            for (String path:files)
                            {
                                File file = new File(path);
                                uriSubtitle = Uri.parse(file.getAbsolutePath().toString());
                            }
                            playVideoSubtitle(uriSubtitle);
                        }
                    });
                }
            }
        });
        playVideo();
    }

    private void playVideo() {
        String path = mVideoFiles.get(position).getPath();
        Uri uri1 = Uri.parse(path);
        player = new SimpleExoPlayer.Builder(this).build();
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this,"app"));
        concatenatingMediaSource = new ConcatenatingMediaSource();
        for (int i = 0; i<mVideoFiles.size();i++)
        {
            new File(String.valueOf(mVideoFiles.get(i)));
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(uri1);
            concatenatingMediaSource.addMediaSource(mediaSource);
        }
        playerView.setPlayer(player);
        playerView.setKeepScreenOn(true);
        player.setPlaybackParameters(parameters);
        player.prepare(concatenatingMediaSource);
        player.seekTo(position, C.TIME_UNSET);

        playError();

    }
    private void playVideoSubtitle(Uri subTitle) {
        long oldposition = player.getCurrentPosition();
        player.stop();

        String path = mVideoFiles.get(position).getPath();
        Uri uri = Uri.parse(path);
        player = new SimpleExoPlayer.Builder(this).build();
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this,"app"));
        concatenatingMediaSource = new ConcatenatingMediaSource();
        for (int i = 0; i<mVideoFiles.size();i++)
        {
            new File(String.valueOf(mVideoFiles.get(i)));
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(uri);
            Format textFormat = Format.createTextSampleFormat(null, MimeTypes.APPLICATION_SUBRIP,Format.NO_VALUE,"app");
            MediaSource subtitleSSource = new SingleSampleMediaSource.Factory(dataSourceFactory).setTreatLoadErrorsAsEndOfStream(true)
                    .createMediaSource(Uri.parse(String.valueOf(subTitle)),textFormat,C.TIME_UNSET);
            MergingMediaSource mergingMediaSource = new MergingMediaSource(mediaSource,subtitleSSource);
            concatenatingMediaSource.addMediaSource(mergingMediaSource);
        }
        playerView.setPlayer(player);
        playerView.setKeepScreenOn(true);
        player.setPlaybackParameters(parameters);
        player.prepare(concatenatingMediaSource);
        player.seekTo(position, oldposition);
        playError();

    }
    private void screenOrientation()
    {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            Bitmap bitmap;
            String path = mVideoFiles.get(position).getPath();
            Uri uri = Uri.parse(path);
            retriever.setDataSource(this,uri);
            bitmap = retriever.getFrameAtTime();

            int videoWidth = bitmap.getWidth();
            int videoHeight = bitmap.getHeight();
            if (videoWidth>videoHeight)
            {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            }
            else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        }catch (Exception e)
        {
            Log.e("MediaMetadataRetriever","screenOrientation: ");
        }
    }

    private void playError() {
        player.addListener(new Player.EventListener() {
            @Override
            public void onPlayerError(@NonNull ExoPlaybackException error) {
                Toast.makeText(VideoPlayerActivity.this, "Error Playing", Toast.LENGTH_SHORT).show();
            }
        });
        player.setPlayWhenReady(true);
    }


    private void setFullScreen()
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.video_back:
                if (player!=null)
                {
                    player.release();
                }
                finish();
                break;
            case R.id.video_list:
                PlayListDialog playListDialog = new PlayListDialog(mVideoFiles,videoFilesAdapter);
                playListDialog.show(getSupportFragmentManager(),playListDialog.getTag());
                break;
            case R.id.lock:
                controlsMode = ControlsMode.FULLSCREEN;
                root.setVisibility(View.VISIBLE);
                lock.setVisibility(View.INVISIBLE);
                Toast.makeText(this, "unLocked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.unlock:
                controlsMode = ControlsMode.LOCK;
                root.setVisibility(View.INVISIBLE);
                lock.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Locked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.exo_nrxt:
                try {
                    player.stop();
                    position++;
                    playVideo();
                }catch (Exception e)
                {
                    Toast.makeText(this, "No next Video", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case R.id.exo_pre:
                try {
                    player.stop();
                    position--;
                    playVideo();
                }catch (Exception e)
                {
                    Toast.makeText(this, "No Previous Video", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }
    View.OnClickListener firstListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scalling.setImageResource(R.drawable.fullscreen);
            Toast.makeText(VideoPlayerActivity.this, "Full Screen", Toast.LENGTH_SHORT).show();
            scalling.setOnClickListener(secondListener);
        }
    };
    View.OnClickListener secondListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scalling.setImageResource(R.drawable.zoom);
            Toast.makeText(VideoPlayerActivity.this, "Zoom", Toast.LENGTH_SHORT).show();
            scalling.setOnClickListener(thirdListener);
        }
    };
    View.OnClickListener thirdListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scalling.setImageResource(R.drawable.fit);
            Toast.makeText(VideoPlayerActivity.this, "Fit", Toast.LENGTH_SHORT).show();
            scalling.setOnClickListener(firstListener);
        }
    };

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        isCrossChecked = isInPictureInPictureMode;
        if(isInPictureInPictureMode){
            playerView.hideController();
        }
        else {
            playerView.showController();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (player.isPlaying())
        {
            player.stop();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onPause() {
        super.onPause();
        player.setPlayWhenReady(false);
        player.getPlaybackState();
        if(isInPictureInPictureMode())
        {
            player.setPlayWhenReady(true);
        }else {
            player.setPlayWhenReady(false);
            player.getPlaybackState();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        player.setPlayWhenReady(true);
        player.getPlaybackState();
    }
    @Override
    protected void onStop() {
        super.onStop();
        if(isCrossChecked)
        {
            player.release();
            finish();
        }
    }
}