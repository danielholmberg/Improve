package dev.danielholmberg.improve.clean.feature_privacy_policy.presentation

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import dev.danielholmberg.improve.BuildConfig
import dev.danielholmberg.improve.R
import dev.danielholmberg.improve.clean.Improve.Companion.instance
import dev.danielholmberg.improve.clean.MainActivity
import dev.danielholmberg.improve.clean.feature_privacy_policy.domain.repository.PrivacyPolicyRepository
import dev.danielholmberg.improve.clean.feature_privacy_policy.domain.use_case.GetPrivacyPolicyUseCase
import dev.danielholmberg.improve.clean.feature_privacy_policy.domain.use_case.PrivacyPolicyUseCases

class PrivacyPolicyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)

        val toolbar = findViewById<View>(R.id.toolbar_privacy_policy) as Toolbar
        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // 1. Create ViewModel with UseCases and inject necessary repositories
        val privacyPolicyRepository: PrivacyPolicyRepository = instance!!.privacyPolicyRepository
        val viewModel = PrivacyPolicyViewModel(
            privacyPolicyUseCases = PrivacyPolicyUseCases(
                getPrivacyPolicyUseCase = GetPrivacyPolicyUseCase(
                    privacyPolicyRepository = privacyPolicyRepository
                )
            )
        )

        val privacyPolicyWebView = findViewById<View>(R.id.privacy_policy_webview) as WebView
        privacyPolicyWebView.setBackgroundColor(Color.TRANSPARENT)
        privacyPolicyWebView.loadData(
            viewModel.getPrivacyPolicyHtml(),
            "text/html",
            "utf-8"
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(0) {
                handleBackPressedNavigation()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            android.R.id.home -> {
                handleBackPressedNavigation()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        handleBackPressedNavigation()
    }

    private fun handleBackPressedNavigation() {
        startActivity(Intent(this, MainActivity::class.java))
        finishAfterTransition()
    }

    companion object {
        private val TAG = BuildConfig.TAG + PrivacyPolicyActivity::class.java.simpleName
    }
}