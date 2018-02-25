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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import dev.danielholmberg.improve.Components.Contact;
import dev.danielholmberg.improve.InternalStorage;
import dev.danielholmberg.improve.R;

/**
 * Created by DanielHolmberg on 2018-01-27.
 */

public class AddContactActivity extends AppCompatActivity {
    private static final String TAG = AddContactActivity.class.getSimpleName();
    public static final int CONTACT_ADDED = 9998;
    public static final int CONTACT_UPDATED = 9999;

    private List<Contact> storedContacts;

    private EditText inputFirstName, inputLastName, inputCompany, inputEmail, inputPhone;
    private TextInputLayout inputLayoutFirstName, inputLayoutLastName, inputLayoutCompany, inputLayoutEmail, inputLayoutPhone;
    private boolean firstNameIsValid, lastNameIsValid, emailIsValid = false;

    private FirebaseFirestore firestoreDB;

    private boolean isEdit;
    private String oldCID, oldCompany;
    private int contactPosition;
    private boolean contactAdded = false;
    private boolean contactUpdated = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_add_contact);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.contact_done);

        inputLayoutFirstName = (TextInputLayout) findViewById(R.id.input_layout_first_name);
        inputLayoutLastName = (TextInputLayout) findViewById(R.id.input_layout_last_name);
        inputLayoutCompany = (TextInputLayout) findViewById(R.id.input_layout_company);
        inputLayoutEmail = (TextInputLayout) findViewById(R.id.input_layout_email);
        inputLayoutPhone = (TextInputLayout) findViewById(R.id.input_layout_mobile);
        inputFirstName = (EditText) findViewById(R.id.input_first_name);
        inputLastName = (EditText) findViewById(R.id.input_last_name);
        inputCompany = (EditText) findViewById(R.id.input_company);
        inputEmail = (EditText) findViewById(R.id.input_email);
        inputPhone = (EditText) findViewById(R.id.input_mobile);

        Contact contact = null;
        Bundle extras = getIntent().getBundleExtra("contact");
        if(extras != null){
            contact = new Contact();
            contact.setCID(extras.getString("cid"));
            contact.setFirstName(extras.getString("first_name"));
            contact.setLastName(extras.getString("last_name"));
            contact.setCompany(extras.getString("company"));
            contact.setEmail(extras.getString("email"));
            contact.setMobile(extras.getString("mobile"));
        }
        if(contact != null){
            Log.d(TAG, "Contact is not null");
            ((TextView) findViewById(R.id.input_first_name)).setText(contact.getFirstName());
            ((TextView) findViewById(R.id.input_last_name)).setText(contact.getLastName());
            ((TextView) findViewById(R.id.input_company)).setText(contact.getCompany());
            ((TextView) findViewById(R.id.input_email)).setText(contact.getEmail());
            ((TextView) findViewById(R.id.input_mobile)).setText(contact.getMobile());

            ((TextView) findViewById(R.id.toolbar_add_contact_title)).setText(R.string.title_edit_contact);
            isEdit = true;
            oldCID = contact.getCID();
            oldCompany = contact.getCompany();
            contactPosition = extras.getInt("position");
        }

        firestoreDB = FirebaseFirestore.getInstance();

        try {
            storedContacts = (List<Contact>) InternalStorage.readObject(getApplicationContext(),
                    InternalStorage.CONTACTS_STORAGE_KEY);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read from Internal Storage: ");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if(storedContacts.isEmpty()) {
            storedContacts = new ArrayList<>();
        }

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
        addContact(contact);
    }

    public void updateContact(){
        Contact contact = createContactObj();
        updateContact(contact);
    }

    private Contact createContactObj(){
        final Contact contact = new Contact();
        contact.setFirstName(((TextView) findViewById(R.id.input_first_name)).getText().toString());
        contact.setLastName(((TextView) findViewById(R.id.input_last_name)).getText().toString());
        contact.setCompany(((TextView) findViewById(R.id.input_company)).getText().toString());
        contact.setEmail(((TextView) findViewById(R.id.input_email)).getText().toString());
        contact.setMobile(((TextView) findViewById(R.id.input_mobile)).getText().toString());

        return contact;
    }

    /**
     * Add a new Contact to the Firestore Database.
     * @param contact
     */
    private void addContact(final Contact contact) {
        contact.setCID(UUID.randomUUID().toString());
        Log.d(TAG, "Id: " + contact.getCID());
        storedContacts.add(contact);
        try {
            InternalStorage.writeObject(getApplicationContext(), InternalStorage.CONTACTS_STORAGE_KEY,
                    storedContacts);
        } catch (IOException e) {
            Log.e(TAG, "Failed to write to Internal Storage: ");
            e.printStackTrace();
        }

        firestoreDB.collection("companies").document(contact.getCompany()).set(new HashMap<>(), SetOptions.merge());
        firestoreDB.collection("companies").document(contact.getCompany()).collection("contacts")
                .add(contact)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Contact document added - id: "
                                + documentReference.getId());
                        contactAdded = true;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error adding contact document: " + e);
                        Toast.makeText(getApplicationContext(),
                                "Adding new contact failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
        showMainActivity();
    }

    /**
     * Update a Contact in Firestore Database.
     * @param updatedContact
     */
    private void updateContact(final Contact updatedContact){
        updatedContact.setCID(oldCID);
        storedContacts.set(contactPosition, updatedContact);
        try {
            InternalStorage.writeObject(getApplicationContext(), InternalStorage.CONTACTS_STORAGE_KEY,
                    storedContacts);
        } catch (IOException e) {
            e.printStackTrace();
        }
        firestoreDB.collection("companies")
                .document(oldCompany)
                .collection("contacts")
                .document(oldCID)
                .set(updatedContact, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, updatedContact.getFullName() + " updated successfully");
                        contactUpdated = true;
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
        showMainActivity();
    }

    private void showMainActivity() {
        restUi();
        if(contactAdded) {
            setResult(CONTACT_ADDED);
        } else if(contactUpdated){
            setResult(CONTACT_UPDATED);
        }
        NavUtils.navigateUpFromSameTask(this);
    }

    private void restUi(){
        ((TextView) findViewById(R.id.input_first_name)).setText("");
        ((TextView) findViewById(R.id.input_last_name)).setText("");
        ((TextView) findViewById(R.id.input_company)).setText("");
        ((TextView) findViewById(R.id.input_email)).setText("");
        ((TextView) findViewById(R.id.input_mobile)).setText("");
    }

    /**
     * Validating new contact form
     */
    private boolean formIsValid() {
        if (validateFirstName() && validateLastName() && validateCompany() && validateEmail()) {
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
     * Validate if the user has entered a company.
     * @return false if last name is empty.
     */
    private boolean validateCompany() {
        if (inputCompany.getText().toString().isEmpty()) {
            inputLayoutCompany.setError(getString(R.string.err_msg_company));
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
