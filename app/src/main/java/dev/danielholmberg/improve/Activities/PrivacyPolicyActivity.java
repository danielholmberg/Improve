package dev.danielholmberg.improve.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.R;

public class PrivacyPolicyActivity extends AppCompatActivity {
    private static final String TAG = PrivacyPolicyActivity.class.getSimpleName();

    private Toolbar toolbar;
    private FirebaseRemoteConfig firebaseRemoteConfig;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        firebaseRemoteConfig = Improve.getInstance().getFirebaseRemoteConfig();

        toolbar = (Toolbar) findViewById(R.id.toolbar_privacy_policy);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final WebView privacyPolicyWebView = (WebView) findViewById(R.id.privacy_policy_webview);
        privacyPolicyWebView.setBackgroundColor(Color.TRANSPARENT);

        // [START fetch_config_with_callback]
        firebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            boolean updated = task.getResult();
                            Log.d(TAG, "Config params updated: " + updated);
                        }

                        privacyPolicyWebView.loadData(firebaseRemoteConfig.getString("privacy_policy_text"), "text/html", "utf-8");
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        finishAfterTransition();
    }
}