package com.yourname.notificationcapture;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_NOTIFICATION_ACCESS = 1001;
    private TextView statusText;
    private Button enableButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.status_text);
        enableButton = findViewById(R.id.enable_button);

        checkNotificationAccess();

        enableButton.setOnClickListener(v -> {
            if (!isNotificationServiceEnabled()) {
                requestNotificationAccess();
            } else {
                Toast.makeText(this, "Notification access already enabled!", Toast.LENGTH_SHORT).show();
            }
        });

        startNotificationService();
    }

    private void checkNotificationAccess() {
        if (isNotificationServiceEnabled()) {
            statusText.setText("✅ Status: Notification access ENABLED\nAll notifications will be uploaded automatically");
            enableButton.setText("Re-enable Access");
        } else {
            statusText.setText("❌ Status: Notification access REQUIRED\nPlease enable notification access to start capturing");
            enableButton.setText("Enable Notification Access");
        }
    }

    private boolean isNotificationServiceEnabled() {
        String packageName = getPackageName();
        String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        return flat != null && flat.contains(packageName);
    }

    private void requestNotificationAccess() {
        try {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivityForResult(intent, REQUEST_CODE_NOTIFICATION_ACCESS);
            Toast.makeText(this, "Please enable notification access for this app", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE_NOTIFICATION_ACCESS);
        }
    }

    private void startNotificationService() {
        try {
            Intent serviceIntent = new Intent(this, NotificationService.class);
            startService(serviceIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_NOTIFICATION_ACCESS) {
            checkNotificationAccess();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkNotificationAccess();
    }
}
