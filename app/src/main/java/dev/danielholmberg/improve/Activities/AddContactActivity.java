package dev.danielholmberg.improve.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import dev.danielholmberg.improve.Callbacks.FirebaseDatabaseCallback;
import dev.danielholmberg.improve.Components.Contact;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Managers.FirebaseDatabaseManager;
import dev.danielholmberg.improve.R;
import dev.danielholmberg.improve.Utilities.ContactInputValidator;

/**
 * Created by DanielHolmberg on 2018-01-27.
 */

public class AddContactActivity extends AppCompatActivity {
    private static final String TAG = AddContactActivity.class.getSimpleName();
    public static final String COMPANIES_KEY = "companies";
    public static final String CONTACT_BUNDLE_KEY = "contactBundle";
    private static final String CONTACT_KEY = "contact";

    private Improve app;
    private FirebaseDatabaseManager databaseManager;
    private ContactInputValidator validator;

    private Contact contact;
    private TextInputEditText inputName, inputEmail, inputPhone, inputComment;
    private AutoCompleteTextView inputCompany;

    private String[] COMPANIES;

    private Toolbar toolbar;
    private View inputLayout;

    private boolean isEdit;
    private String oldCID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        app = Improve.getInstance();
        databaseManager = app.getFirebaseDatabaseManager();

        initActivity();

    }

    private void initActivity() {
        toolbar = (Toolbar) findViewById(R.id.toolbar_add_contact);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        inputLayout = (View) findViewById(R.id.input_contact_layout);
        inputName = (TextInputEditText) findViewById(R.id.input_name);
        inputCompany = (AutoCompleteTextView) findViewById(R.id.input_company);
        inputEmail = (TextInputEditText) findViewById(R.id.input_email);
        inputPhone = (TextInputEditText) findViewById(R.id.input_mobile);
        inputComment = (TextInputEditText) findViewById(R.id.input_comment);

        Bundle intentBundle = getIntent().getBundleExtra(CONTACT_BUNDLE_KEY);
        contact =  intentBundle != null ? (Contact) intentBundle.getParcelable(CONTACT_KEY) : null;

        if(contact != null){
            isEdit = true;
            ((TextView) findViewById(R.id.toolbar_add_contact_title_tv)).setText(R.string.title_edit_contact);

            oldCID = contact.getId();

            inputName.setText(contact.getName());
            inputCompany.setText(contact.getCompany());
            inputEmail.setText(contact.getEmail());
            inputPhone.setText(contact.getPhone());
            inputComment.setText(contact.getComment());
        }

        ArrayList<String> companiesList = intentBundle != null ? intentBundle.getStringArrayList(COMPANIES_KEY) : null;
        if(companiesList != null) {
            COMPANIES = companiesList.toArray(new String[0]);
            inputCompany.setAdapter(new ArrayAdapter<String>(this,
                    android.R.layout.simple_dropdown_item_1line, COMPANIES));
        }

        validator = new ContactInputValidator(this, inputLayout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_contact_mode_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
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
        String id = databaseManager.getContactsRef().push().getKey();
        final String name = inputName.getText().toString();
        String company = inputCompany.getText().toString().toUpperCase();
        String email = inputEmail.getText().toString().trim();
        String phone = inputPhone.getText().toString().trim();
        String comment = inputComment.getText().toString();
        String timestampAdded = Long.toString(System.currentTimeMillis());

        if(TextUtils.isEmpty(comment)) {
            comment = "";
        }

        Contact newContact = new Contact(id, name, company, email, phone, comment, timestampAdded);
        newContact.setTimestampUpdated(timestampAdded);

        databaseManager.addContact(newContact, new FirebaseDatabaseCallback() {
            @Override
            public void onSuccess() {}

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(app, "Failed to add new contact", Toast.LENGTH_SHORT).show();
            }
        });

        showParentActivity();

    }

    public void updateContact(){
        String id = oldCID;
        String name = inputName.getText().toString();
        String company = inputCompany.getText().toString().toUpperCase();
        String email = inputEmail.getText().toString().trim();
        String phone = inputPhone.getText().toString().trim();
        String comment = inputComment.getText().toString();
        String timestampAdded = contact.getTimestampAdded();
        String timestampUpdated = Long.toString(System.currentTimeMillis());

        final Contact updatedContact = new Contact(id, name, company, email, phone, comment, timestampAdded);
        updatedContact.setTimestampUpdated(timestampUpdated);

        databaseManager.updateContact(contact, updatedContact, new FirebaseDatabaseCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(app,"Contact updated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(app,"Failed to update contact", Toast.LENGTH_SHORT).show();
            }
        });

        showParentActivity();

    }

    private void showParentActivity() {
        restUI();
        startActivity(new Intent(this, MainActivity.class));
        finishAfterTransition();
    }

    private void restUI(){
        inputName.getText().clear();
        inputCompany.getText().clear();
        inputEmail.getText().clear();
        inputPhone.getText().clear();
        inputComment.getText().clear();
    }

    @Override
    public void onBackPressed() {
        showParentActivity();
    }
}
