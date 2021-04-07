package com.frozendef.youtubedlj;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.yausername.youtubedl_android.DownloadProgressCallback;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLRequest;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private Button btnStartDownload;
    private EditText etUrl;
    private ProgressBar progressBar;
    private TextView tvDownloadStatus;
    private boolean updating;
    //private TextView tvCommandOutput;
    //private ProgressBar pbLoading;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    boolean downloading=false;
    ImageView imgView;
    Palette p;
    ActionBar actionBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isStoragePermissionGranted();
        createDownloadNotificationChannel();
        actionBar=getSupportActionBar();




        initViews();
        initListeners();
        etUrl.setText("https://www.youtube.com/watch?v=5LgiiYaa96Q");
        handleIntents();

        //updateYoutubeDL();
        //startDownload();





    }

    public void handleIntents(){
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleIntentLink(intent); // Handle text being sent
            }
            else {
                Toast.makeText(getApplicationContext(),"Unsupported Link",Toast.LENGTH_LONG).show();
            }

        }
    }

    void handleIntentLink(Intent intent){
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            if(sharedText.toLowerCase().contains("www.youtube.com") || sharedText.toLowerCase().contains("youtu.be")) {
                Toast.makeText(getApplicationContext(), "Initialising download", Toast.LENGTH_LONG).show();
                Log.w("TAG","Got the link from intent: "+sharedText);
                etUrl.setText(sharedText);
                startDownload();
            }
            else{
                Toast.makeText(getApplicationContext(),"Unsupported Link",Toast.LENGTH_LONG).show();
            }
        }

    }
    NotificationCompat.Builder builder;

    NotificationManagerCompat notificationManager;

    private void showInitialNotification(){


        int notificationId =1;
        notificationManager = NotificationManagerCompat.from(this);
        builder = new NotificationCompat.Builder(this, "downloadNotificationChannel");
        builder.setContentTitle("YoutubeDLJ")
                .setContentText("Download in progress")
                .setSmallIcon(R.drawable.download)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true);

        int PROGRESS_MAX = 100;
        int PROGRESS_CURRENT = 0;
        builder.setNotificationSilent();
        builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
        notificationManager.notify(notificationId, builder.build());

       /* builder.setContentText("Download complete")
                .setProgress(0,0,false);
        notificationManager.notify(notificationId, builder.build());*/


    }

    private void updateNotification(int max, int current,int notificationId){
        if(current==100)builder.setContentText("Finishing Up...");
        builder.setProgress(max,current,false);
        notificationManager.notify(notificationId,builder.build());

    }

    private void completeNotification(int notificationId,boolean successful){

        NotificationCompat.Builder completedBuilder = new NotificationCompat.Builder(this, "downloadNotificationChannel");
        completedBuilder.setContentTitle("")
                .setSmallIcon(R.drawable.download)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(false);

        if(successful) {
            completedBuilder.setContentText("Download Completed");

        }
        else {
           completedBuilder.setContentText("Download Failed");
        }
        notificationManager.notify(notificationId, completedBuilder.build());

    }





    private void createDownloadNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Download Notification";
            String description = "Notification for download progress";
            String channelId="downloadNotificationChannel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    private void getVideoThumbnail(Editable s){




        if(s.toString().toLowerCase().contains("youtube.com")) {
            String[] arr = s.toString().split("=");
            String id = arr[arr.length - 1];
            Glide.with(getApplication())
                    .load("https://img.youtube.com/vi/" + id + "/0.jpg")
                    .into(imgView);

            setColorAccent("https://img.youtube.com/vi/" + id + "/0.jpg");

        }

        else if(s.toString().toLowerCase().contains("youtu.be")) {
            String[] arr = s.toString().split("/");
            String id = arr[arr.length - 1];
            Glide.with(getApplication())
                    .load("https://img.youtube.com/vi/" + id + "/0.jpg").into(imgView);
            setColorAccent("https://img.youtube.com/vi/" + id + "/0.jpg");
        }




    }

    private void setColorAccent(String id){
        Glide.with(this).asBitmap()
                .load(id)
                .into(new SimpleTarget<Bitmap>(){
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        // Extract color
                        p=Palette.from(resource).generate();
                        actionBar.setBackgroundDrawable(new ColorDrawable (p.getDarkMutedColor(getResources().getColor(R.color.purple_200))));
                        btnStartDownload.setBackgroundColor(p.getDarkMutedColor(getResources().getColor(R.color.purple_200)));
                        //progressBar.setProgressTintList(p.getDarkMutedColor(getResources().getColor(R.color.purple_200)));
                    }
                });

    }



    public void initListeners(){

        btnStartDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDownload();
            }
        });

        etUrl.addTextChangedListener(new TextWatcher() {
                                         @Override
                                         public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                         }

                                         @Override
                                         public void onTextChanged(CharSequence s, int start, int before, int count) {

                                         }

                                         @Override
                                         public void afterTextChanged(Editable s) {

                                             getVideoThumbnail(s);



                                         }
                                     }
        );



    }


    private void updateYoutubeDL() {
       UpdateLibraries updateInstance = new UpdateLibraries();
       updateInstance.update(progressBar,getApplicationContext());
    }

    public boolean isStoragePermissionGranted() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            return false;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.updateLibrary:
                updateYoutubeDL();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    public void initViews(){

        progressBar=findViewById(R.id.progressBar);
        tvDownloadStatus = findViewById(R.id.tv_status);
        etUrl=findViewById(R.id.etUrl);
        btnStartDownload= findViewById(R.id.btnDownload);
        imgView = findViewById(R.id.imageView2);



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }





    @NonNull
    private File getDownloadLocation() {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File youtubeDLDir = new File(downloadsDir, "youtubedl-android");
        if (!youtubeDLDir.exists()) youtubeDLDir.mkdir();
        return youtubeDLDir;
    }

    private void showStart() {
        tvDownloadStatus.setText("Download Starting");
        progressBar.setProgress(0);
        //pbLoading.setVisibility(View.VISIBLE);
    }

    private void startDownload() {
        btnStartDownload.setEnabled(false);
        if (downloading) {
            Toast.makeText(getApplicationContext(), "Cannot start download. a download is already in progress", Toast.LENGTH_LONG).show();
            btnStartDownload.setEnabled(true);
            return;
        }
        showInitialNotification();

        String url = etUrl.getText().toString().trim();
        //String url="https://www.youtube.com/watch?v=5LgiiYaa96Q";

        if (TextUtils.isEmpty(url)) {
            etUrl.setError("Enter a proper URL");
            Toast.makeText(getApplicationContext(),"Enter a proper URL",Toast.LENGTH_LONG).show();
            btnStartDownload.setEnabled(true);
            return;
        }

        YoutubeDLRequest request = new YoutubeDLRequest(url);
        File youtubeDLDir = getDownloadLocation();
        request.addOption("-o", youtubeDLDir.getAbsolutePath() + "/%(title)s.%(ext)s");
        request.addOption("-x");
        request.addOption("--audio-format", "mp3");



        showStart();

        downloading = true;

        Disposable disposable = Observable.fromCallable(() -> YoutubeDL.getInstance().execute(request, callback))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(youtubeDLResponse -> {
//                    pbLoading.setVisibility(View.GONE);
                    progressBar.setProgress(100);
                    tvDownloadStatus.setText("Download Completed");
                    Log.w("Command Output:",youtubeDLResponse.getOut());
                    Toast.makeText(getApplicationContext(), "Download completed", Toast.LENGTH_LONG).show();
                    completeNotification(1,true);
                    downloading = false;
                    btnStartDownload.setEnabled(true);
                    progressBar.setProgress(0);
                }, e -> {
                    if(BuildConfig.DEBUG) Log.e("TAG",  "Failed to download", e);
                    //pbLoading.setVisibility(View.GONE);
                    tvDownloadStatus.setText("Download Failed");
                    Log.e("Download error",e.getMessage());
                    Toast.makeText(getApplicationContext(), "Download failed", Toast.LENGTH_LONG).show();
                    completeNotification(1,false);
                    downloading = false;
                    btnStartDownload.setEnabled(true);
                    progressBar.setProgress(0);
                });
        compositeDisposable.add(disposable);

    }





    private DownloadProgressCallback callback = new DownloadProgressCallback() {
        @Override
        public void onProgressUpdate(float progress, long etaInSeconds) {
            runOnUiThread(() -> {
                        progressBar.setProgress((int) progress);
                        tvDownloadStatus.setText(progress + "% (ETA " + etaInSeconds + " seconds)");

                        updateNotification(100,(int)progress,1);


                        if(progress==100){
                            tvDownloadStatus.setText("Finishing Up...");


                        }

                    }
            );
        }
    };


}