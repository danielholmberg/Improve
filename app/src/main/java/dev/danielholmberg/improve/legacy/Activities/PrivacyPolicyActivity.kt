package dev.danielholmberg.improve.legacy.Activities

import dev.danielholmberg.improve.Improve.Companion.instance
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import dev.danielholmberg.improve.R
import android.webkit.WebView
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import dev.danielholmberg.improve.legacy.Managers.RemoteConfigManager

class PrivacyPolicyActivity : AppCompatActivity() {

    private var toolbar: Toolbar? = null
    private var remoteConfig: RemoteConfigManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)
        remoteConfig = instance!!.remoteConfigManager
        toolbar = findViewById<View>(R.id.toolbar_privacy_policy) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val privacyPolicyWebView = findViewById<View>(R.id.privacy_policy_webview) as WebView
        privacyPolicyWebView.setBackgroundColor(Color.TRANSPARENT)

        // [START fetch_config_with_callback]
        remoteConfig!!.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val updated = task.result
                    Log.d(TAG, "Config params updated: $updated")
                }
                privacyPolicyWebView.loadData(
                    remoteConfig!!.getPrivatePolicyText(),
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