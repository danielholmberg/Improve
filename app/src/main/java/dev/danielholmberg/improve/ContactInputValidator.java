package dev.danielholmberg.improve;

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

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

    public void setInputLayoutContainer(View inputLayoutContainer) {
        this.inputLayoutContainer = (View) inputLayoutContainer;
    }

    /**
     * Validating new contact form
     */
    public boolean formIsValid() {
        if (validateName() && validateCompany() && validateEmail()) {
            Log.d(TAG, "New contact form is valid");
            return true;
        } else {
            return false;
        }
    }

    /**
     * Validate if the user has entered a first name.
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
     * Validate if the user has entered a non-empty field or a correct email-format.
     * @return true if email is valid.
     */
    private boolean validateEmail() {
        String email = inputEmail.getText().toString().trim();

        if (email.isEmpty() || !isValidEmail(email)) {
            inputLayoutEmail.setError(context.getString(R.string.err_msg_email));
            requestFocus(inputEmail);
            return false;
        } else {
            inputLayoutEmail.setErrorEnabled(false);
        }

        return true;
    }

    /**
     * Check if the entered email is of correct format.
     * @param email
     * @return true if the email is of correct format.
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
            ((Activity) context).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
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
