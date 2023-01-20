package dev.danielholmberg.improve.clean.feature_feedback.domain.util

import android.content.Context
import android.view.View
import android.widget.EditText
import dev.danielholmberg.improve.R
import androidx.appcompat.app.AppCompatActivity
import android.view.WindowManager

class FeedbackInputValidator(private val context: Context, inputLayout: View) {
    private val inputSubject: EditText = inputLayout.findViewById<View>(R.id.input_title) as EditText
    private val inputMessage: EditText = inputLayout.findViewById<View>(R.id.input_info) as EditText

    fun formIsValid(): Boolean {
        return validateSubject() && validateMessage()
    }

    private fun validateSubject(): Boolean {
        if (inputSubject.text.toString().isBlank()) {
            inputSubject.error = context.getString(R.string.err_msg_feedback_subject)
            requestFocus(inputSubject)
            return false
        }
        return true
    }

    private fun validateMessage(): Boolean {
        if (inputMessage.text.toString().isBlank()) {
            inputMessage.error = context.getString(R.string.err_msg_feedback_message)
            requestFocus(inputSubject)
            return false
        }
        return true
    }

    private fun requestFocus(view: View) {
        if (view.requestFocus()) {
            (context as AppCompatActivity).window
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }
    }

    companion object {
        private val TAG = FeedbackInputValidator::class.java.simpleName
    }
}