package dev.danielholmberg.improve.Utilities;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import dev.danielholmberg.improve.R;

/**
 * Class ${CLASS}
 */

public class ContactInputValidator {
    private static final String TAG = ContactInputValidator.class.getSimpleName();

    private View inputLayoutContainer;
    private Context context;

    private EditText inputName, inputCompany, inputEmail, inputPhone;
    private TextInputLayout inputLayoutName, inputLayoutCompany, inputLayoutEmail, inputLayoutPhone;
    private boolean nameIsValid, emailIsValid = false;


    public ContactInputValidator(Context context, View inputLayoutContainer) {
        this.context = context;
        this.inputLayoutContainer = (View) inputLayoutContainer;

        inputLayoutName = (TextInputLayout) inputLayoutContainer.findViewById(R.id.input_layout_name);
        inputLayoutCompany = (TextInputLayout) inputLayoutContainer.findViewById(R.id.input_layout_company);
        inputLayoutEmail = (TextInputLayout) inputLayoutContainer.findViewById(R.id.input_layout_email);
        inputLayoutPhone = (TextInputLayout) inputLayoutContainer.findViewById(R.id.input_layout_mobile);
        inputName = (EditText) inputLayoutContainer.findViewById(R.id.input_name);
        inputCompany = (EditText) inputLayoutContainer.findViewById(R.id.input_company);
        inputEmail = (EditText) inputLayoutContainer.findViewById(R.id.input_email);
        inputPhone = (EditText) inputLayoutContainer.findViewById(R.id.input_mobile);
    }

    /**
     * Validating new contact form (should at least contain Name and Company)
     * return true if both Name- and Company-field is not empty.
     */
    public boolean formIsValid() {
        return validateName() && validateCompany();
    }

    /**
     * REQUIRED FIELD
     * Validate if the user has entered a name.
     * @return false if first name is empty.
     */
    private boolean validateName() {
        if (inputName.getText().toString().trim().isEmpty()) {
            inputLayoutName.setError(context.getString(R.string.err_msg_name));
            requestFocus(inputName);
            return false;
        } else {
            inputLayoutName.setErrorEnabled(false);
        }

        return true;
    }

    /**
     * REQUIRED FIELD
     * Validate if the user has entered a company.
     * @return false if last name is empty.
     */
    private boolean validateCompany() {
        if (inputCompany.getText().toString().isEmpty()) {
            inputLayoutCompany.setError(context.getString(R.string.err_msg_company));
            requestFocus(inputCompany);
            return false;
        } else {
            inputLayoutCompany.setErrorEnabled(false);
        }

        return true;
    }

    /**
     * Validate if the user has entered a correct email-format.
     * @return true if company is valid.
     */
    private boolean validateEmail() {
        String email = inputEmail.getText().toString().trim();

        if (!isValidEmail(email)) {
            inputLayoutEmail.setError(context.getString(R.string.err_msg_email));
            requestFocus(inputEmail);
            return false;
        } else {
            inputLayoutEmail.setErrorEnabled(false);
        }

        return true;
    }

    /**
     * Check if the entered company is of correct format.
     * @param email
     * @return true if the company is of correct format.
     */
    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Requests focus of the incoming view.
     * @param view
     */
    private void requestFocus(View view) {
        if (view.requestFocus()) {
            ((AppCompatActivity) context).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    /**
     * Class to live-check if the input is valid.
     */
    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.input_name:
                    validateName();
                    break;
                case R.id.input_company:
                    validateCompany();
                    break;
                case R.id.input_email:
                    validateEmail();
                    break;
            }
        }
    }

}
