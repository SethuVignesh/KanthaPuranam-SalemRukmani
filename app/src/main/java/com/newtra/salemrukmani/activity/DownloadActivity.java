package com.newtra.salemrukmani.activity;

import android.app.Activity;
import android.os.Bundle;

import yt.sdk.access.InitializationException;
import yt.sdk.access.YTSDK;

public class DownloadActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_download);
        YTSDK sdk = null;
        try {
                sdk = YTSDK.getInstance(DownloadActivity.this);
        } catch (InitializationException e) {
            e.printStackTrace();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        sdk.download(DownloadActivity.this, "KkHMX3b0feE");
//        YTSDK sdk = null;
//        try {
//            sdk = YTSDK.getInstance(DownloadActivity.this);
//        } catch (InitializationException e) {
//            e.printStackTrace();
//        }
//        sdk.download(DownloadActivity.this ,"NPCkwiNr6Q0");

    }
}
