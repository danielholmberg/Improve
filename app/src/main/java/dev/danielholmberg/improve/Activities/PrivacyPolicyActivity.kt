package dev.danielholmberg.improve.Activities

import dev.danielholmberg.improve.Improve.Companion.instance
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import android.os.Bundle
import dev.danielholmberg.improve.R
import android.webkit.WebView
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar

class PrivacyPolicyActivity : AppCompatActivity() {

    private var toolbar: Toolbar? = null
    private var firebaseRemoteConfig: FirebaseRemoteConfig? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)
        firebaseRemoteConfig = instance!!.remoteConfig
        toolbar = findViewById<View>(R.id.toolbar_privacy_policy) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val privacyPolicyWebView = findViewById<View>(R.id.privacy_policy_webview) as WebView
        privacyPolicyWebView.setBackgroundColor(Color.TRANSPARENT)

        // [START fetch_config_with_callback]
        firebaseRemoteConfig!!.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val updated = task.result
                    Log.d(TAG, "Config params updated: $updated")
                }
                privacyPolicyWebView.loadData(
                    firebaseRemoteConfig!!.getString("privacy_policy_text"),
                    "text/html",
                    "utf-8"
                )
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))
        finishAfterTransition()
    }

    companion object {
        private val TAG = PrivacyPolicyActivity::class.java.simpleName
    }
}