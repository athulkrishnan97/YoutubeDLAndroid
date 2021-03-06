package com.frozendef.youtubedlj;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.yausername.youtubedl_android.DownloadProgressCallback;
import com.yausername.youtubedl_android.ResponseCallback;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLRequest;
import com.yausername.youtubedl_android.YoutubeDLResponse;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
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
import java.util.Objects;

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
    FragmentManager fragmentManager;
    NotificationModel notificationModel;
    TextView tvName;
    String pathToDownloadedMP3 ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isStoragePermissionGranted();
        notificationModel = new NotificationModel(getApplicationContext());
        notificationModel.createDownloadNotificationChannel();
        initViews();
        fragmentManager=getSupportFragmentManager();
        initListeners();
        etUrl.setText("https://www.youtube.com/watch?v=5LgiiYaa96Q");
        Intent intent = getIntent();
        getIntentAndPassToHandler(intent);

       /* new DownloadFragment().show(
                getSupportFragmentManager(), DownloadFragment.TAG);*/

        //updateYoutubeDL();
        //startDownload();

    }

    public void removeFragment(String TAG){
        fragmentManager.beginTransaction().remove(Objects.requireNonNull(fragmentManager.findFragmentByTag(TAG))).commit();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        getIntentAndPassToHandler(intent);
        super.onNewIntent(intent);
    }

    public void getIntentAndPassToHandler(Intent intent){

        //DownloadFragment downloadFragment= new DownloadFragment();






        String action = intent.getAction();
        String type = intent.getType();
        Log.w("Intent","Got the link: "+intent.getStringExtra(Intent.EXTRA_TEXT));
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {

                IntentHandler intentHandler = new IntentHandler(MainActivity.this,intent,fragmentManager);
                intentHandler.handleIntentLink();


            }
            else {
                Toast.makeText(getApplicationContext(),"Unsupported Link",Toast.LENGTH_LONG).show();
            }

        }
    }

    private void getVideoThumbnail(Editable s, ImageView imgView){

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
                        getWindow().setStatusBarColor(p.getDarkVibrantColor(getResources().getColor(R.color.purple_200)));

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

                                             getVideoThumbnail(s,imgView);



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
        int itemId = item.getItemId();
        if (itemId == R.id.updateLibrary) {
            updateYoutubeDL();
            return true;
        } else if (itemId == R.id.exit) {
            exitApp();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void exitApp(){
        notificationModel.cancelAllNotification();
        compositeDisposable.dispose();
        finishAndRemoveTask();
        System.exit(0);
    }

    public void initViews(){
        tvName = findViewById(R.id.tvName);
        progressBar=findViewById(R.id.progressBar);
        tvDownloadStatus = findViewById(R.id.tv_status);
        etUrl=findViewById(R.id.etUrl);
        btnStartDownload= findViewById(R.id.btnDownload);
        imgView = findViewById(R.id.imageView2);
        actionBar=getSupportActionBar();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }





    @NonNull
    private File getDownloadLocation() {
        File musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        File youtubeDLDir = new File(musicDir, "Youtube MP3s");
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
        btnStartDownload.setText("Downloading...");
        btnStartDownload.setTextColor(getResources().getColor(R.color.white));

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
            btnStartDownload.setText("Download");
            return;
        }

        YoutubeDLRequest request = new YoutubeDLRequest(url);
        File youtubeDLDir = getDownloadLocation();
        request.addOption("-o", youtubeDLDir.getAbsolutePath() + "/%(title)s.%(ext)s");
        request.addOption("-x");
        request.addOption("--audio-format", "mp3");
        request.addOption("--embed-thumbnail");



        showStart();
        downloading = true;
        notificationModel.showInitialNotification();
        Disposable disposable = Observable.fromCallable(() -> YoutubeDL.getInstance().execute(request, callback,responseCallback))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(youtubeDLResponse -> {
                    youtubeDLResponseCopy=youtubeDLResponse;
//                    pbLoading.setVisibility(View.GONE);
                    progressBar.setProgress(100);
                    tvDownloadStatus.setText("Download Completed");
                    Log.w("Command Output:",youtubeDLResponse.getOut());
                    Toast.makeText(getApplicationContext(), "Download completed", Toast.LENGTH_LONG).show();
                    notificationModel.completeNotification(1,true,tvName.getText().toString());
                    downloading = false;
                    btnStartDownload.setEnabled(true);
                    btnStartDownload.setText("Download");
                    progressBar.setProgress(0);


                    MediaScannerConnection.scanFile(this,
                            new String[] {pathToDownloadedMP3}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.i("Scanning file for mediaplayers", "Scanned " + path + ":");
                                    Log.i("Scanning file for mediaplayers", "-> uri=" + uri);
                                }
                            });





                }, e -> {
                    if(BuildConfig.DEBUG) Log.e("TAG",  "Failed to download", e);
                    //pbLoading.setVisibility(View.GONE);
                    tvDownloadStatus.setText("Download Failed");
                    Log.e("Download error",e.getMessage());
                    Log.w("Command Output:",youtubeDLResponseCopy.getOut());

                    Toast.makeText(getApplicationContext(), "Download failed", Toast.LENGTH_LONG).show();
                    notificationModel.completeNotification(1,false,"Video Download Failed");
                    downloading = false;
                    btnStartDownload.setEnabled(true);
                    btnStartDownload.setText("Download");
                    progressBar.setProgress(0);


                });
        compositeDisposable.add(disposable);

    }


    private ResponseCallback responseCallback= new ResponseCallback() {
        @Override
        public void onResponseReceived(String out) {

            //Toast.makeText(getApplicationContext(),"Got response"+out,Toast.LENGTH_LONG).show();

        }

        @Override
        public void onErrorReceived(String out) {
            notificationModel.completeNotification(1,false,"Video Download Failed");
            //Toast.makeText(getApplicationContext(),"Got Error"+out,Toast.LENGTH_LONG).show();
        }
    };


    private DownloadProgressCallback callback = new DownloadProgressCallback() {

        @Override
        public void onFileNameReceived(String receivedFileName) {
            //Changes the file name to end in .mp3 as that's how the final file will be saved
            StringBuilder str= new StringBuilder(receivedFileName);
            str.replace(str.length()-4,str.length(),"mp3");
            pathToDownloadedMP3 = str.toString();

        }

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