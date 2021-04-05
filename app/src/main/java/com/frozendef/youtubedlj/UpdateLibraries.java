package com.frozendef.youtubedlj;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.yausername.youtubedl_android.YoutubeDL;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class UpdateLibraries {

    boolean updating=false;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public void update(ProgressBar progressBar, Context context){
        if (updating) {
            Toast.makeText(context, "Update is already in progress", Toast.LENGTH_LONG).show();
            return;
        }

        updating = true;
        progressBar.setVisibility(View.VISIBLE);
        Disposable disposable = Observable.fromCallable(() -> YoutubeDL.getInstance().updateYoutubeDL(context))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(status -> {
                    //progressBar.setProgress(50);
                    switch (status) {
                        case DONE:
                            Toast.makeText(context, "Update successful", Toast.LENGTH_LONG).show();
                            break;
                        case ALREADY_UP_TO_DATE:
                            Toast.makeText(context, "Already up to date", Toast.LENGTH_LONG).show();
                            break;
                        default:
                            Toast.makeText(context, status.toString(), Toast.LENGTH_LONG).show();
                            break;
                    }
                    updating = false;
                }, e -> {
                    if(BuildConfig.DEBUG) Log.w("Update", "failed to update", e);
                    //progressBar.setVisibility(View.GONE);
                    Toast.makeText(context, "Update failed", Toast.LENGTH_LONG).show();
                    updating = false;
                });
        compositeDisposable.add(disposable);


    }




}
