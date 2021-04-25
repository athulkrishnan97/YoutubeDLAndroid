package com.frozendef.youtubedlj;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;

import java.util.Objects;

public class DownloadFragment extends DialogFragment {

    String url;
    Context context;
    DownloadFragment(String url, Context context){
        this.url=url;
        this.context=context;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView tvName= view.findViewById(R.id.tv_name_dialog);
        ImageView imageView= view.findViewById(R.id.imageViewDialog);
        tvName.setText(url);
        getVideoThumbnail(url,imageView);
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