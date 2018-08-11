package dev.danielholmberg.improve.Utilities;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
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

    public ContactInputValidator(Context context, View inputLayoutContainer) {
        this.context = context;
        this.inputLayoutContainer = (View) inputLayoutContainer;

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
        return validateName() && validateCompany() && validateEmail() && validatePhone();
    }

    /**
     * REQUIRED FIELD
     * Validate if the user has entered a name.
     * @return false if name field is empty.
     */
    private boolean validateName() {
        String name = inputName.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            inputName.setError(context.getString(R.string.err_msg_name));
            requestFocus(inputName);
            return false;
        }

        return true;
    }

    /**
     * REQUIRED FIELD
     * Validate if the user has entered a company.
     * @return false company field is empty.
     */
    private boolean validateCompany() {
        String company = inputCompany.getText().toString().trim();

        if (TextUtils.isEmpty(company)) {
            inputCompany.setError(context.getString(R.string.err_msg_company));
            requestFocus(inputCompany);
            return false;
        }

        return true;
    }

    /**
     * Validate if the user has entered a correct email-format.
     * @return true if company is valid.
     */
    private boolean validateEmail() {
        String email = inputEmail.getText().toString().trim();

        if(TextUtils.isEmpty(email)) {
          return true;
        } else if (!isValidEmail(email)) {
            inputEmail.setError(context.getString(R.string.err_msg_email));
            requestFocus(inputEmail);
            return false;
        }

        return true;
    }

    /**
     * @return true if the email is of correct format.
     */
    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Validate if the user has entered a non-empty phone number.
     * @return true if phone is not empty.
     */
    private boolean validatePhone() {
        String phone = inputPhone.getText().toString().trim();

        if(TextUtils.isEmpty(phone)) {
            return true;
        } else if(!isValidPhone(phone)) {
            inputPhone.setError(context.getString(R.string.err_msg_phone));
            requestFocus(inputPhone);
            return false;
        }

        return true;
    }

    /**
     * @return true if the phone is of correct format.
     */
    private static boolean isValidPhone(String phone) {
        return !TextUtils.isEmpty(phone) && Patterns.PHONE.matcher(phone).matches();
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
}
