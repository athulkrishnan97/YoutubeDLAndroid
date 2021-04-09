package com.frozendef.youtubedlj;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.yausername.youtubedl_android.DownloadProgressCallback;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLRequest;
import com.yausername.youtubedl_android.YoutubeDLResponse;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
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
    protected EditText etUrl;
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
    NotificationModel notificationModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isStoragePermissionGranted();
        notificationModel = new NotificationModel(getApplicationContext());

        notificationModel.createDownloadNotificationChannel();
        actionBar=getSupportActionBar();




        initViews();
        initListeners();
        etUrl.setText("https://www.youtube.com/watch?v=5LgiiYaa96Q");
        getIntentAndPassToHandler();

        //updateYoutubeDL();
        //startDownload();
    }


    public void getIntentAndPassToHandler(){
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {

                IntentHandler intentHandler = new IntentHandler(MainActivity.this,intent);
                intentHandler.handleIntentLink();

                //handleIntentLink(intent); // Handle text being sent
            }
            else {
                Toast.makeText(getApplicationContext(),"Unsupported Link",Toast.LENGTH_LONG).show();
            }

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

    YoutubeDLResponse youtubeDLResponseCopy;

    protected void startDownload() {
        btnStartDownload.setEnabled(false);
        if (p!=null){
            //btnStartDownload.setBackgroundColor(p.getDarkVibrantColor(getResources().getColor(R.color.purple_200)));
            btnStartDownload.setTextColor(getResources().getColor(R.color.grey));
        }
        if (downloading) {
            Toast.makeText(getApplicationContext(), "Cannot start download. a download is already in progress", Toast.LENGTH_LONG).show();
            btnStartDownload.setEnabled(true);
            return;
        }


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
        notificationModel.showInitialNotification();
        Disposable disposable = Observable.fromCallable(() -> YoutubeDL.getInstance().execute(request, callback))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(youtubeDLResponse -> {
                    youtubeDLResponseCopy=youtubeDLResponse;
//                    pbLoading.setVisibility(View.GONE);
                    progressBar.setProgress(100);
                    tvDownloadStatus.setText("Download Completed");
                    Log.w("Command Output:",youtubeDLResponse.getOut());
                    Toast.makeText(getApplicationContext(), "Download completed", Toast.LENGTH_LONG).show();
                    notificationModel.completeNotification(1,true);
                    downloading = false;
                    btnStartDownload.setEnabled(true);
                    progressBar.setProgress(0);
                }, e -> {
                    if(BuildConfig.DEBUG) Log.e("TAG",  "Failed to download", e);
                    //pbLoading.setVisibility(View.GONE);
                    tvDownloadStatus.setText("Download Failed");
                    Log.e("Download error",e.getMessage());
                    Log.w("Command Output:",youtubeDLResponseCopy.getOut());

                    Toast.makeText(getApplicationContext(), "Download failed", Toast.LENGTH_LONG).show();
                    notificationModel.completeNotification(1,false);
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

                notificationModel.updateNotification(100,(int)progress,1);


                        if(progress==100){
                            tvDownloadStatus.setText("Finishing Up...");


                        }

                    }
            );
        }
    };


}