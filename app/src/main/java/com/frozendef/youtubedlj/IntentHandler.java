package com.frozendef.youtubedlj;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.Objects;

public class IntentHandler {
    Context context;
    Intent intent;
    FragmentManager fragmentManager;

    IntentHandler(Context context,Intent intent,FragmentManager fragmentManager){
        this.context=context;
        this.intent=intent;
        this.fragmentManager = fragmentManager;


    }



    void handleIntentLink(){
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            if(sharedText.toLowerCase().contains("www.youtube.com") || sharedText.toLowerCase().contains("youtu.be")) {
                Toast.makeText(context, "Initialising download", Toast.LENGTH_LONG).show();
                Log.w("TAG","Got the link from intent: "+sharedText);

                //Check if the fragment already exists and if it does, kill it before making a new one
                if(fragmentManager.findFragmentByTag(DownloadFragment.TAG)==null) {

                    DownloadFragment downloadFragment = new DownloadFragment(sharedText, context,fragmentManager);
                    //downloadFragment.show(fragmentManager, DownloadFragment.TAG);
                    fragmentManager.beginTransaction().add(downloadFragment,DownloadFragment.TAG).commitAllowingStateLoss();
                }
                else {
                   // fragmentManager.findFragmentByTag(DownloadFragment.TAG).rem
                    fragmentManager.beginTransaction().remove(Objects.requireNonNull(fragmentManager.findFragmentByTag(DownloadFragment.TAG))).commit();
                    DownloadFragment downloadFragment = new DownloadFragment(sharedText, context,fragmentManager);
                    fragmentManager.beginTransaction().add(downloadFragment,DownloadFragment.TAG).commitAllowingStateLoss();


                    //downloadFragment.show(fragmentManager, DownloadFragment.TAG);
                    //fragmentManager.beginTransaction().show(downloadFragment).commitAllowingStateLoss();
                }



                /*((MainActivity) context).etUrl.setText(sharedText);
                ((MainActivity) context).startDownload();*/
            }
            else{
                Toast.makeText(context,"Unsupported Link",Toast.LENGTH_LONG).show();
            }
        }

    }
}
