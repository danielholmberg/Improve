package dev.danielholmberg.improve.Activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;

import dev.danielholmberg.improve.Components.Contact;
import dev.danielholmberg.improve.R;

/**
 * Dialog-window to add a new Contact in MainActivity.
 */

public class AddContactActivity extends AppCompatActivity {
    private static final String TAG = AddContactActivity.class.getSimpleName();

    private EditText inputFirstName, inputLastName, inputCompany, inputEmail, inputPhone;
    private TextInputLayout inputLayoutFirstName, inputLayoutLastName, inputLayoutCompany, inputLayoutEmail, inputLayoutPhone;
    private boolean firstNameIsValid, lastNameIsValid, emailIsValid = false;

    private FirebaseFirestore firestoreDB;
    private boolean isEdit;

    private String oldCID, oldCompany;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_add_contact);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.done);

        inputLayoutFirstName = (TextInputLayout) findViewById(R.id.input_layout_first_name);
        inputLayoutLastName = (TextInputLayout) findViewById(R.id.input_layout_last_name);
        inputLayoutCompany = (TextInputLayout) findViewById(R.id.input_layout_company);
        inputLayoutEmail = (TextInputLayout) findViewById(R.id.input_layout_email);
        inputLayoutPhone = (TextInputLayout) findViewById(R.id.input_layout_phone);
        inputFirstName = (EditText) findViewById(R.id.input_first_name);
        inputLastName = (EditText) findViewById(R.id.input_last_name);
        inputCompany = (EditText) findViewById(R.id.input_company);
        inputEmail = (EditText) findViewById(R.id.input_email);
        inputPhone = (EditText) findViewById(R.id.input_phone);

        Contact contact = null;
        Bundle extras = getIntent().getBundleExtra("contact");
        if(extras != null){
            contact = new Contact();
            contact.setCID(extras.getString("cid"));
            contact.setFirstName(extras.getString("first_name"));
            contact.setLastName(extras.getString("last_name"));
            contact.setCompany(extras.getString("company"));
            contact.setEmail(extras.getString("email"));
            contact.setPhone(extras.getString("phone"));
        }
        if(contact != null){
            Log.d(TAG, "Contact is not null");
            ((TextView) findViewById(R.id.input_first_name)).setText(contact.getFirstName());
            ((TextView) findViewById(R.id.input_last_name)).setText(contact.getLastName());
            ((TextView) findViewById(R.id.input_company)).setText(contact.getCompany());
            ((TextView) findViewById(R.id.input_email)).setText(contact.getEmail());
            ((TextView) findViewById(R.id.input_phone)).setText(contact.getPhone());

            ((TextView) findViewById(R.id.toolbar_add_contact_title)).setText(R.string.title_edit_contact);
            isEdit = true;
            oldCID = contact.getCID();
            oldCompany = contact.getCompany();
        }

        firestoreDB = FirebaseFirestore.getInstance();

        fab.setOnClickListener(new View.OnClickListener()        {
            @Override
            public void onClick(View v)
            {
                if(formIsValid()) {
                    if (!isEdit) {
                        addContact();
                    } else {
                        updateContact();
                    }
                }

            }
        });


    }

    public void addContact(){
        Contact contact = createContactObj();
        addDocumentToCollection(contact);
    }

    public void updateContact(){
        Contact contact = createContactObj();
        updateDocumentToCollection(contact);
    }

    private Contact createContactObj(){
        final Contact contact = new Contact();
        contact.setFirstName(((TextView) findViewById(R.id.input_first_name)).getText().toString());
        contact.setLastName(((TextView) findViewById(R.id.input_last_name)).getText().toString());
        contact.setCompany(((TextView) findViewById(R.id.input_company)).getText().toString());
        contact.setEmail(((TextView) findViewById(R.id.input_email)).getText().toString());
        contact.setPhone(((TextView) findViewById(R.id.input_phone)).getText().toString());

        return contact;
    }

    /**
     * Add a new Contact to the Firestore Database.
     * @param contact
     */
    private void addDocumentToCollection(final Contact contact) {
        firestoreDB.collection("companies").document(contact.getCompany()).set(new HashMap<>(), SetOptions.merge());

        firestoreDB.collection("companies").document(contact.getCompany()).collection("contacts")
                .add(contact)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Contact document added - id: "
                                + documentReference.getId());
                        restUi();
                        showMainActivity();
                        Toast.makeText(getApplicationContext(),
                                "Contact document has been added",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error adding contact document: " + e);
                        Toast.makeText(getApplicationContext(),
                                "Contact document could not be added",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Update a Contact in Firestore Database.
     * @param updatedContact
     */
    private void updateDocumentToCollection(Contact updatedContact){
        firestoreDB.collection("companies")
                .document(oldCompany)
                .collection("contacts")
                .document(oldCID)
                .set(updatedContact, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Contact document updated ");
                        Toast.makeText(getApplicationContext(),
                                "Contact document has been updated",
                                Toast.LENGTH_SHORT).show();
                        showMainActivity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error adding contact document: " + e);
                        Toast.makeText(getApplicationContext(),
                                "Contact document could not be added",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void restUi(){
        ((TextView) findViewById(R.id.input_first_name)).setText("");
        ((TextView) findViewById(R.id.input_last_name)).setText("");
        ((TextView) findViewById(R.id.input_company)).setText("");
        ((TextView) findViewById(R.id.input_email)).setText("");
        ((TextView) findViewById(R.id.input_phone)).setText("");
    }

    private void showMainActivity() {
        NavUtils.navigateUpFromSameTask(this);
    }

    /**
     * Validating new contact form
     */
    private boolean formIsValid() {
        if (!validateFirstName() && !validateLastName() && !validateEmail()) {
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
    private boolean validateFirstName() {
        if (inputFirstName.getText().toString().trim().isEmpty()) {
            inputLayoutFirstName.setError(getString(R.string.err_msg_name));
            requestFocus(inputFirstName);
            return false;
        } else {
            inputLayoutFirstName.setErrorEnabled(false);
        }

        return true;
    }

    /**
     * Validate if the user has entered a last name.
     * @return false if last name is empty.
     */
    private boolean validateLastName() {
        if (inputLastName.getText().toString().trim().isEmpty()) {
            inputLayoutLastName.setError(getString(R.string.err_msg_name));
            requestFocus(inputLastName);
            return false;
        } else {
            inputLayoutLastName.setErrorEnabled(false);
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
            inputLayoutEmail.setError(getString(R.string.err_msg_email));
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
           getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
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
                case R.id.input_first_name:
                    validateFirstName();
                    break;
                case R.id.input_last_name:
                    validateLastName();
                    break;
                case R.id.input_email:
                    validateEmail();
                    break;
            }
        }
    }

}
