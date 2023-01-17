package dev.danielholmberg.improve.legacy.Activities

import dev.danielholmberg.improve.Improve.Companion.instance
import androidx.appcompat.app.AppCompatActivity
import dev.danielholmberg.improve.legacy.Managers.DatabaseManager
import dev.danielholmberg.improve.legacy.Utilities.NoteInputValidator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.os.Bundle
import dev.danielholmberg.improve.R
import android.view.WindowManager
import dev.danielholmberg.improve.legacy.Models.Feedback
import dev.danielholmberg.improve.legacy.Callbacks.DatabaseCallback
import android.widget.Toast
import android.content.Intent
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import java.text.DateFormat
import java.util.*

class FeedbackActivity : AppCompatActivity() {

    private var storageManager: DatabaseManager? = null
    private var validator: NoteInputValidator? = null
    private var inputTitle: TextInputEditText? = null
    private var inputInfo: TextInputEditText? = null
    private var toolbar: Toolbar? = null
    private var inputLayout: View? = null
    private var fab: FloatingActionButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)
        storageManager = instance!!.databaseManager
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
        val userId = instance!!.authManager!!.currentUserId
        val feedbackId = storageManager!!.feedbackRef.push().key
        val title = inputTitle!!.text.toString()
        val info = inputInfo!!.text.toString()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        val timestamp = DateFormat.getDateTimeInstance().format(calendar.time)
        val feedback = Feedback(userId, feedbackId, title, info, timestamp)
        storageManager!!.submitFeedback(feedback, object : DatabaseCallback {
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