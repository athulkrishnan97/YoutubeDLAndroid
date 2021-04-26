package com.frozendef.youtubedlj;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.yausername.youtubedl_android.ResponseCallback;

import java.util.Objects;

public class DownloadFragment extends DialogFragment {

    String url;
    Context context;
    DownloadFragment(String url, Context context){
        this.url=url;
        this.context=context;
    }

    Button downloadButtonInDialog;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView tvName= view.findViewById(R.id.tv_name_dialog);
        ImageView imageView= view.findViewById(R.id.imageViewDialog);
        tvName.setText("Getting Video Details...");
        getVideoThumbnail(url,imageView);
        putVideoNameToTextView(url,tvName);
        //ProgressBar progressBarName = view.findViewById(R.id.progressCircleName);
        //ProgressBar progressBarImage = view.findViewById(R.id.progressCircleImage);
        //downloadButtonInDialog = view.findViewById(R.id.downloadButtonInDialog);
        //downloadButtonInDialog.setEnabled(false);
        tvName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //progressBarName.setVisibility(View.INVISIBLE);
                //progressBarImage.setVisibility(View.INVISIBLE);

            }
        });

    }



    private void putVideoNameToTextView(String url, TextView tv){

        VideoNameResolver videoNameResolver =new VideoNameResolver();

        ResponseCallback responseCallback= new ResponseCallback() {
            @Override
            public void onResponseReceived(String out) {
                Log.w("DownloadFragment","got OUT--------------:> "+out);
                Handler mainHandler = new Handler(context.getMainLooper());
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        tv.setText(out);
                        //downloadButtonInDialog.setEnabled(true);
                    }
                };
                mainHandler.post(myRunnable);
            }

            @Override
            public void onErrorReceived(String out) {
                Log.w("DownloadFragment","got Error "+out);
            }
        };
        videoNameResolver.getNameFromUrl(url,responseCallback);


    }


    private void getVideoThumbnail(String  s, ImageView imgView){

        if(s.toLowerCase().contains("youtube.com")) {
            String[] arr = s.split("=");
            String id = arr[arr.length - 1];
            Glide.with(context)
                    .load("https://img.youtube.com/vi/" + id + "/0.jpg")
                    .into(imgView);



        }

        else if(s.toLowerCase().contains("youtu.be")) {
            String[] arr = s.split("/");
            String id = arr[arr.length - 1];
            Glide.with(context)
                    .load("https://img.youtube.com/vi/" + id + "/0.jpg").into(imgView);

        }

    }







    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //setStyle(DialogFragment.STYLE_NORMAL, R.style.dialog_style);
       // getDialog().getWindow().requestFeature(Window.FEATURE_CUSTOM_TITLE);
        Objects.requireNonNull(getDialog()).setTitle("Download Options");

        //getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        return inflater.inflate(R.layout.dialog_layout,container);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setStyle(DialogFragment.STYLE_NORMAL,R.style.dialog_style);

        super.onCreate(savedInstanceState);
    }





    public static String TAG = "Dialog";
}