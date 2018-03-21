package dev.danielholmberg.improve.Utilities;

import android.content.Context;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
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

    private EditText inputTitle, inputInfo;
    private TextInputLayout inputLayoutTitle, inputLayoutInfo;

    public NoteInputValidator(Context context, View inputLayoutConatiner) {
        this.context = context;

        // Input Components
        inputLayoutTitle = (TextInputLayout) inputLayoutConatiner.findViewById(R.id.input_layout_title);
        inputLayoutInfo = (TextInputLayout) inputLayoutConatiner.findViewById(R.id.input_layout_info);
        inputTitle = (TextInputEditText) inputLayoutConatiner.findViewById(R.id.input_title);
        inputInfo = (TextInputEditText) inputLayoutConatiner.findViewById(R.id.input_info);
    }

    /**
     * Validating Note form (should at least contain a Title)
     * return true if Title-field is not empty.
     */
    public boolean formIsValid() {
        return validateTitle();
    }

    private boolean validateTitle() {
        if(TextUtils.isEmpty(inputTitle.getText())) {
            inputLayoutTitle.setError(context.getString(R.string.err_msg_title));
            requestFocus(inputTitle);
            return false;
        } else {
            return true;
        }
    }

    private boolean valitdateInfo() {
        if(TextUtils.isEmpty(inputInfo.getText())) {
            inputLayoutInfo.setError(context.getString(R.string.err_msg_info));
            requestFocus(inputInfo);
            return false;
        } else {
            return true;
        }
    }

    /**
     * Requests focus of the incoming view.
     * @param view
     */
    private void requestFocus(View view) {
        if (view.requestFocus()) {
            ((AppCompatActivity)context).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

}
