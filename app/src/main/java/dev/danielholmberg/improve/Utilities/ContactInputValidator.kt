package dev.danielholmberg.improve.Utilities

import android.content.Context
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.Spinner
import dev.danielholmberg.improve.R
import dev.danielholmberg.improve.Models.Company
import androidx.appcompat.app.AppCompatActivity
import android.view.WindowManager

/**
 * Class ${CLASS}
 */
class ContactInputValidator(private val context: Context, inputLayoutContainer: View) {
    private val inputLayoutContainer: View
    private val inputName: EditText
    private val inputEmail: EditText
    private val inputPhone: EditText
    private val spinnerCompany: Spinner

    init {
        this.inputLayoutContainer = inputLayoutContainer
        inputName = inputLayoutContainer.findViewById<View>(R.id.input_name) as EditText
        spinnerCompany = inputLayoutContainer.findViewById<View>(R.id.spinner_company) as Spinner
        inputEmail = inputLayoutContainer.findViewById<View>(R.id.input_email) as EditText
        inputPhone = inputLayoutContainer.findViewById<View>(R.id.input_mobile) as EditText
    }

    /**
     * Validating new contact form.
     * return true if both Name is not empty and each additional input is correct format.
     */
    fun formIsValid(): Boolean {
        return validateName() && validateCompany() && validateEmail() && validatePhone()
    }

    /**
     * REQUIRED FIELD
     * Validate if the user has entered a name.
     * @return false if name field is empty.
     */
    private fun validateName(): Boolean {
        val name = inputName.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(name)) {
            inputName.error = context.getString(R.string.err_msg_name)
            requestFocus(inputName)
            return false
        }
        return true
    }

    private fun validateCompany(): Boolean {
        val selectedCompany = spinnerCompany.selectedItem as Company
        if (selectedCompany == null) {
            requestFocus(spinnerCompany)
            if (spinnerCompany.childCount > 0) {
                spinnerCompany.performClick()
            }
            return false
        }
        return true
    }

    /**
     * Validate if the user has entered a correct email-format.
     * @return true if company is valid.
     */
    private fun validateEmail(): Boolean {
        val email = inputEmail.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(email)) {
            return true
        } else if (!isValidEmail(email)) {
            inputEmail.error = context.getString(R.string.err_msg_email)
            requestFocus(inputEmail)
            return false
        }
        return true
    }

    /**
     * Validate if the user has entered a non-empty phone number.
     * @return true if phone is not empty.
     */
    private fun validatePhone(): Boolean {
        val phone = inputPhone.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(phone)) {
            return true
        } else if (!isValidPhone(phone)) {
            inputPhone.error = context.getString(R.string.err_msg_phone)
            requestFocus(inputPhone)
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
            (context as AppCompatActivity).window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }

    companion object {
        private val TAG = ContactInputValidator::class.java.simpleName

        /**
         * @return true if the email is of correct format.
         */
        private fun isValidEmail(email: String): Boolean {
            return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }

        /**
         * @return true if the phone is of correct format.
         */
        private fun isValidPhone(phone: String): Boolean {
            return !TextUtils.isEmpty(phone) && Patterns.PHONE.matcher(phone).matches()
        }
    }
}