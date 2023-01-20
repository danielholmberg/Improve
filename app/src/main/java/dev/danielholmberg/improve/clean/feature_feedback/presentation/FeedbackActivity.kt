package dev.danielholmberg.improve.clean.feature_feedback.presentation

import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.os.Bundle
import dev.danielholmberg.improve.R
import android.view.WindowManager
import android.widget.Toast
import android.content.Intent
import android.os.Build
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import dev.danielholmberg.improve.clean.Improve.Companion.instance
import dev.danielholmberg.improve.clean.MainActivity
import dev.danielholmberg.improve.clean.feature_authentication.domain.repository.AuthRepository
import dev.danielholmberg.improve.clean.feature_feedback.domain.repository.FeedbackRepository
import dev.danielholmberg.improve.clean.feature_feedback.domain.use_case.FeedbackUseCases
import dev.danielholmberg.improve.clean.feature_feedback.domain.use_case.SubmitFeedbackUseCase
import dev.danielholmberg.improve.clean.feature_feedback.domain.util.FeedbackCallback
import dev.danielholmberg.improve.clean.feature_feedback.domain.util.FeedbackInputValidator

class FeedbackActivity : AppCompatActivity() {

    private lateinit var viewModel: FeedbackViewModel

    private lateinit var validator: FeedbackInputValidator
    private lateinit var inputSubject: TextInputEditText
    private lateinit var inputMessage: TextInputEditText
    private lateinit var toolbar: Toolbar
    private lateinit var inputLayout: View
    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        toolbar = findViewById<View>(R.id.toolbar_feedback) as Toolbar
        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // 1. Create ViewModel with UseCases and inject necessary repositories
        val feedbackRepository: FeedbackRepository = instance!!.feedbackRepository
        val authRepository: AuthRepository = instance!!.authRepository
        viewModel = FeedbackViewModel(
            feedbackUseCases = FeedbackUseCases(
                submitFeedbackUseCase = SubmitFeedbackUseCase(
                    feedbackRepository = feedbackRepository,
                    authRepository = authRepository
                )
            )
        )

        inputLayout = findViewById(R.id.input_layout)
        inputSubject = findViewById<View>(R.id.input_title) as TextInputEditText
        inputMessage = findViewById<View>(R.id.input_info) as TextInputEditText
        fab = findViewById<View>(R.id.submit_feedback) as FloatingActionButton
        fab.setOnClickListener {
            if (validator.formIsValid()) {
                submitFeedback()
            }
        }

        inputSubject.requestFocus()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        validator = FeedbackInputValidator(this, inputLayout)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(0) {
                handleBackPressedNavigation()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                handleBackPressedNavigation()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun submitFeedback() {
        val subject = inputSubject.text.toString()
        val message = inputMessage.text.toString()

        viewModel.submit(subject, message, object : FeedbackCallback {
            override fun onSuccess() {
                Toast.makeText(instance, "Feedback submitted, you're awesome!", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onFailure(errorMessage: String?) {
                Toast.makeText(
                    instance,
                    "Failed to submit feedback, please try again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
        showParentActivity()
    }

    private fun showParentActivity() {
        restoreUI()
        startActivity(Intent(this, MainActivity::class.java))
        finishAfterTransition()
    }

    private fun restoreUI() {
        inputSubject.text!!.clear()
        inputMessage.text!!.clear()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
    }

    override fun onBackPressed() {
        handleBackPressedNavigation()
    }

    private fun handleBackPressedNavigation() {
        showParentActivity()
    }

    companion object {
        private val TAG = FeedbackActivity::class.java.simpleName
    }
}