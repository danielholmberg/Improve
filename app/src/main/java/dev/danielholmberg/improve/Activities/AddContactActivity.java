package dev.danielholmberg.improve.Activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import dev.danielholmberg.improve.Components.Contact;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Managers.FirebaseStorageManager;
import dev.danielholmberg.improve.R;
import dev.danielholmberg.improve.Utilities.ContactInputValidator;

/**
 * Created by DanielHolmberg on 2018-01-27.
 */

public class AddContactActivity extends AppCompatActivity {
    private static final String TAG = AddContactActivity.class.getSimpleName();
    private static final int CONTACT_UPDATED = 9997;

    private Improve app;
    private FirebaseStorageManager storageManager;

    private List<Contact> storedContacts;

    private String userId;
    private boolean isEdit;
    private String oldCID;
    private int contactPosition;

    private TextInputEditText inputName, inputEmail, inputCompany, inputPhone, inputComment;
    private ContactInputValidator validator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        app = Improve.getInstance();
        storageManager = app.getFirebaseStorageManager();

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_add_contact);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Main Layout
        View layout = (View) findViewById(R.id.add_contact_layout);

        // Input Components
        inputName = (TextInputEditText) findViewById(R.id.input_name);
        inputCompany = (TextInputEditText) findViewById(R.id.input_company);
        inputEmail = (TextInputEditText) findViewById(R.id.input_email);
        inputPhone = (TextInputEditText) findViewById(R.id.input_mobile);
        inputComment = (TextInputEditText) findViewById(R.id.input_comment);

        Bundle intentBundle = getIntent().getBundleExtra("contactBundle");
        Contact contact =  intentBundle != null ? (Contact) intentBundle.getSerializable("contact") : null;

        if(contact != null){
            isEdit = true;
            ((TextView) findViewById(R.id.toolbar_add_contact_title_tv)).setText(R.string.title_edit_contact);

            oldCID = contact.getId();
            contactPosition = intentBundle.getInt("position");

            inputName.setText(contact.getName());
            inputCompany.setText(contact.getCompany());
            inputEmail.setText(contact.getEmail());
            inputPhone.setText(contact.getPhone());
            inputComment.setText(contact.getComment());
        }

        // Initialize input validator
        validator = new ContactInputValidator(this, layout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_add_edit_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.contactDone:
                if(validator.formIsValid()) {
                    if (!isEdit) {
                        addContact();
                    } else {
                        updateContact();
                    }
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void addContact(){
        String id = storageManager.getContactsRef().push().getKey();
        String name = inputName.getText().toString();
        String company = inputCompany.getText().toString();
        String email = inputEmail.getText().toString();
        String phone = inputPhone.getText().toString();
        String comment = inputComment.getText().toString();

        Contact newContact = new Contact(id, name, company, email, phone, comment);
        storageManager.writeContactToFirebase(newContact);
        showParentActivity();
    }

    public void updateContact(){
        String id = oldCID;
        String name = inputName.getText().toString();
        String company = inputCompany.getText().toString();
        String email = inputEmail.getText().toString();
        String phone = inputPhone.getText().toString();
        String comment = inputComment.getText().toString();

        Contact updatedContact = new Contact(id, name, company, email, phone, comment);
        storageManager.writeContactToFirebase(updatedContact);
        showParentActivity();
    }

    private void showParentActivity() {
        restUI();
        NavUtils.navigateUpFromSameTask(this);
        finish();
    }

    private void restUI(){
        inputName.getText().clear();
        inputCompany.getText().clear();
        inputEmail.getText().clear();
        inputPhone.getText().clear();
        inputComment.getText().clear();
    }
}
