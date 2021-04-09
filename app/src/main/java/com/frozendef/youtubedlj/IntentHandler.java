package com.frozendef.youtubedlj;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class IntentHandler {
    Context context;
    Intent intent;

    IntentHandler(Context context,Intent intent){
        this.context=context;
        this.intent=intent;


    }



    void handleIntentLink(){
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            if(sharedText.toLowerCase().contains("www.youtube.com") || sharedText.toLowerCase().contains("youtu.be")) {
                Toast.makeText(context, "Initialising download", Toast.LENGTH_LONG).show();
                Log.w("TAG","Got the link from intent: "+sharedText);
                ((MainActivity) context).etUrl.setText(sharedText);
                ((MainActivity) context).startDownload();
            }
            else{
                Toast.makeText(context,"Unsupported Link",Toast.LENGTH_LONG).show();
            }
        }

    }
}
