package dev.danielholmberg.improve.clean.feature_privacy_policy.presentation

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
import dev.danielholmberg.improve.clean.Improve.Companion.instance
import dev.danielholmberg.improve.clean.MainActivity
import dev.danielholmberg.improve.clean.core.RemoteConfigService

class PrivacyPolicyActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var remoteConfigService: RemoteConfigService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)
        remoteConfigService = instance!!.remoteConfigService
        toolbar = findViewById<View>(R.id.toolbar_privacy_policy) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val privacyPolicyWebView = findViewById<View>(R.id.privacy_policy_webview) as WebView
        privacyPolicyWebView.setBackgroundColor(Color.TRANSPARENT)

        // TODO: Should be moved to UseCase

        remoteConfigService.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val updated = task.result
                    Log.d(TAG, "Config params updated: $updated")
                }
                privacyPolicyWebView.loadData(
                    remoteConfigService.getPrivatePolicyText(),
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