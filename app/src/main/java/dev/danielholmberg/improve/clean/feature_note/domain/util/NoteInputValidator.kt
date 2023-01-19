package dev.danielholmberg.improve.clean.feature_note.domain.util

import android.content.Context
import android.view.View
import android.widget.EditText
import dev.danielholmberg.improve.R
import androidx.appcompat.app.AppCompatActivity
import android.view.WindowManager

class NoteInputValidator(private val context: Context, inputTitleLayout: View) {
    private val inputTitle: EditText

    init {
        inputTitle = inputTitleLayout.findViewById<View>(R.id.input_title) as EditText
    }

    /**
     * Validating Note form (should at least contain a Title)
     * return true if Title-field is not empty.
     */
    fun formIsValid(): Boolean {
        return validateTitle()
    }

    private fun validateTitle(): Boolean {
        if (inputTitle.text.toString().isBlank()) {
            inputTitle.error = context.getString(R.string.err_msg_title)
            requestFocus(inputTitle)
            return false
        }
        return true
    }

    /**
     * Requests focus of the incoming view.
     * @param view
     */
    private fun requestFocus(view: View) {
        if (view.requestFocus()) {
            (context as AppCompatActivity).window
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }
    }

    companion object {
        private val TAG = NoteInputValidator::class.java.simpleName
    }
}