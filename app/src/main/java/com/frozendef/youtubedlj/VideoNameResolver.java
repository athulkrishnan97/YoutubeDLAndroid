package com.frozendef.youtubedlj;

import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.yausername.youtubedl_android.ResponseCallback;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLRequest;
import com.yausername.youtubedl_android.YoutubeDLResponse;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class VideoNameResolver {
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    YoutubeDLResponse youtubeDLResponseCopy;



    public void getNameFromUrl(String url,ResponseCallback responseCallback)
    {
        YoutubeDLRequest request = new YoutubeDLRequest(url);
        request.addOption("--get-title");


        Disposable disposable = Observable.fromCallable(() -> YoutubeDL.getInstance().execute(request,null,responseCallback))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(youtubeDLResponse -> {
                    youtubeDLResponseCopy=youtubeDLResponse;
                    Log.w("Command Output:",youtubeDLResponse.getOut());

                }, e -> {
                    Log.e("TAG",  "Failed to download", e);
                    //pbLoading.setVisibility(View.GONE);
                    Log.e("Download error",e.getMessage());

                });
        compositeDisposable.add(disposable);

    }



}
