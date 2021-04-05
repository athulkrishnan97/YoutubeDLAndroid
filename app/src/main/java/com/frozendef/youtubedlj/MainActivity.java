package com.frozendef.youtubedlj;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.yausername.youtubedl_android.DownloadProgressCallback;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLRequest;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isStoragePermissionGranted();




        initViews();
        initListeners();
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



    public void initListeners(){

        btnStartDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDownload();
            }
        });




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
                    downloading = false;
                    btnStartDownload.setEnabled(true);
                    progressBar.setProgress(0);
                }, e -> {
                    if(BuildConfig.DEBUG) Log.e("TAG",  "Failed to download", e);
                    //pbLoading.setVisibility(View.GONE);
                    tvDownloadStatus.setText("Download Failed");
                    Log.e("Download error",e.getMessage());
                    Toast.makeText(getApplicationContext(), "Download failed", Toast.LENGTH_LONG).show();
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

                        if(progress==100){
                            tvDownloadStatus.setText("Finishing Up...");


                        }

                    }
            );
        }
    };


}