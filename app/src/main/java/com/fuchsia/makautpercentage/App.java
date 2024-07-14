package com.fuchsia.makautpercentage;

import android.app.Application;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.HashMap;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        final FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();

        HashMap<String, Object> defaultValue = new HashMap<>();
        defaultValue.put(com.fuchsia.makautpercentage.UpdateHelper.KEY_UPDATE_ENABLE, false);
        defaultValue.put(com.fuchsia.makautpercentage.UpdateHelper.KEY_UPDATE_VERSION, 1.0);
        defaultValue.put(com.fuchsia.makautpercentage.UpdateHelper.KEY_UPDATE_URL, "https://play.google.com/store/apps/details?id=com.fuchsia.makautpercentage");


        remoteConfig.setDefaultsAsync(defaultValue);
        remoteConfig.fetch(5)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            remoteConfig.fetchAndActivate();
                        }
                    }
                });
    }

}