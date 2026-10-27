package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class VacciTrackApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeFirebaseSafely()
    }

    private fun initializeFirebaseSafely() {
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                try {
                    FirebaseApp.initializeApp(this)
                    Log.i("VacciTrackApp", "FirebaseApp initialized with default configuration.")
                } catch (e: Exception) {
                    Log.w("VacciTrackApp", "Default FirebaseApp init failed, using configured options", e)
                    val options = FirebaseOptions.Builder()
                        .setApplicationId("1:341408372305:android:vaccitrackmz")
                        .setProjectId("vaccitrack-mz")
                        .setApiKey("AIzaSyDummyKeyForVacciTrackMozambiqueApp")
                        .build()
                    FirebaseApp.initializeApp(this, options)
                    Log.i("VacciTrackApp", "FirebaseApp initialized successfully with fallback options.")
                }
            }
        } catch (e: Exception) {
            Log.e("VacciTrackApp", "Could not initialize FirebaseApp", e)
        }
    }
}
