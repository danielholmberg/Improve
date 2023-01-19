package dev.danielholmberg.improve.clean.feature_feedback.presentation

import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.os.Bundle
import dev.danielholmberg.improve.R
import android.view.WindowManager
import android.widget.Toast
import android.content.Intent
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import dev.danielholmberg.improve.clean.Improve.Companion.instance
import dev.danielholmberg.improve.clean.MainActivity
import dev.danielholmberg.improve.clean.feature_feedback.domain.model.Feedback
import dev.danielholmberg.improve.clean.feature_feedback.domain.repository.FeedbackRepository
import dev.danielholmberg.improve.clean.feature_feedback.domain.util.FeedbackCallback
import dev.danielholmberg.improve.clean.feature_note.domain.util.NoteInputValidator
import java.text.DateFormat
import java.util.*

class FeedbackActivity : AppCompatActivity() {

    private lateinit var feedbackRepository: FeedbackRepository

    private var validator: NoteInputValidator? = null
    private var inputTitle: TextInputEditText? = null
    private var inputInfo: TextInputEditText? = null
    private var toolbar: Toolbar? = null
    private var inputLayout: View? = null
    private var fab: FloatingActionButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)
        feedbackRepository = instance!!.feedbackRepository
        toolbar = findViewById<View>(R.id.toolbar_feedback) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        inputLayout = findViewById(R.id.input_layout)
        inputTitle = findViewById<View>(R.id.input_title) as TextInputEditText
        inputInfo = findViewById<View>(R.id.input_info) as TextInputEditText
        fab = findViewById<View>(R.id.submit_feedback) as FloatingActionButton
        inputTitle!!.requestFocus()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        validator = NoteInputValidator(this, inputLayout!!)
        fab!!.setOnClickListener {
            if (validator!!.formIsValid()) {
                submitFeedback()
            }
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

    private fun submitFeedback() {
        val userId = instance!!.authRepository.getCurrentUserId()
        val feedbackId = feedbackRepository.generateNewFeedbackId()
        val title = inputTitle!!.text.toString()
        val info = inputInfo!!.text.toString()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        val timestamp = DateFormat.getDateTimeInstance().format(calendar.time)
        val feedback = Feedback(userId, feedbackId, title, info, timestamp)

        // TODO: Should be moved to UseCase

        feedbackRepository.submit(feedback, object : FeedbackCallback {
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
        restUI()
        startActivity(Intent(this, MainActivity::class.java))
        finishAfterTransition()
    }

    private fun restUI() {
        inputTitle!!.text!!.clear()
        inputInfo!!.text!!.clear()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
    }

    override fun onBackPressed() {
        showParentActivity()
    }

    companion object {
        private val TAG = FeedbackActivity::class.java.simpleName
    }
}