package dev.danielholmberg.improve.Utilities;

import android.content.Context;
import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import dev.danielholmberg.improve.R;

/**
 * Created by Daniel Holmberg.
 */

public class NoteInputValidator {
    private static final String TAG = NoteInputValidator.class.getSimpleName();

    private Context context;
    private EditText inputTitle;

    public NoteInputValidator(Context context, View inputTitleLayout) {
        this.context = context;

        // Input Components
        inputTitle = (EditText) inputTitleLayout.findViewById(R.id.input_title);
    }

    /**
     * Validating Note form (should at least contain a Title)
     * return true if Title-field is not empty.
     */
    public boolean formIsValid() {
        return validateTitle();
    }

    private boolean validateTitle() {
        String title = inputTitle.getText().toString().trim();

        if(TextUtils.isEmpty(title)) {
            inputTitle.setError(context.getString(R.string.err_msg_title));
            requestFocus(inputTitle);
            return false;
        }

        return true;
    }

    /**
     * Requests focus of the incoming view.
     * @param view
     */
    private void requestFocus(View view) {
        if (view.requestFocus()) {
            ((AppCompatActivity) context).getWindow()
                    .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
    }

}
