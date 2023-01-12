package dev.danielholmberg.improve.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import dev.danielholmberg.improve.Models.Company;
import dev.danielholmberg.improve.Models.Contact;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Managers.DatabaseManager;
import dev.danielholmberg.improve.R;
import dev.danielholmberg.improve.Utilities.ContactInputValidator;

/**
 * Created by DanielHolmberg on 2018-01-27.
 */

public class AddContactActivity extends AppCompatActivity {
    private static final String TAG = AddContactActivity.class.getSimpleName();
    public static final String CONTACT_BUNDLE_KEY = "contactBundle";
    private static final String CONTACT_KEY = "contact";
    public static final String PRE_SELECTED_COMPANY = "preSelectedCompany";

    private Improve app;
    private DatabaseManager databaseManager;
    private ContactInputValidator validator;

    private Contact contact;
    private Company preSelectedCompany;
    private List<Company> companies;

    private TextInputEditText contactName, contactEmail, contactPhone, contactComment;
    private Spinner contactCompany;
    private ArrayAdapter<Company> companyAdapter;
    private ImageView addCompanyButton;

    private Toolbar toolbar;
    private View inputLayout;

    private boolean isEdit;
    private String oldCID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        app = Improve.getInstance();
        databaseManager = app.getDatabaseManager();

        initActivity();

    }

    private void initActivity() {
        toolbar = (Toolbar) findViewById(R.id.toolbar_add_contact);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        inputLayout = (View) findViewById(R.id.input_contact_layout);
        contactName = (TextInputEditText) findViewById(R.id.input_name);
        contactCompany = (Spinner) findViewById(R.id.spinner_company);
        contactEmail = (TextInputEditText) findViewById(R.id.input_email);
        contactPhone = (TextInputEditText) findViewById(R.id.input_mobile);
        contactComment = (TextInputEditText) findViewById(R.id.input_comment);

        addCompanyButton = (ImageView) findViewById(R.id.add_company);
        addCompanyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addCompany();
            }
        });

        companies = app.getCompanyRecyclerViewAdapter().getCompaniesList();

        companyAdapter = new ArrayAdapter<Company>(this,
                android.R.layout.simple_spinner_dropdown_item, companies);
        contactCompany.setAdapter(companyAdapter);

        Bundle contactBundle = getIntent().getBundleExtra(CONTACT_BUNDLE_KEY);

        if(contactBundle != null) {
            contact = (Contact) contactBundle.getParcelable(CONTACT_KEY);
        }

        if(contact != null){
            isEdit = true;
            ((TextView) findViewById(R.id.toolbar_add_contact_title_tv)).setText(R.string.title_edit_contact);

            oldCID = contact.getId();

            if(contact.getName() != null) {
                contactName.setText(contact.getName());
            }
            if(contact.getEmail() != null) {
                contactEmail.setText(contact.getEmail());
            }
            if(contact.getPhone() != null) {
                contactPhone.setText(contact.getPhone());
            }
            if(contact.getComment() != null) {
                contactComment.setText(contact.getComment());
            }

            // If the company already exists, set that company as selected by default.
            if(contact.getCompanyId() != null) {
                Company company = app.getCompanyRecyclerViewAdapter().getCompany(contact.getCompanyId());
                if (company != null) {
                    int adapterPosition = companies.indexOf(company);
                    contactCompany.setSelection(adapterPosition);
                }
            }
        }

        preSelectedCompany = (Company) getIntent().getParcelableExtra(PRE_SELECTED_COMPANY);

        if(preSelectedCompany != null) {
            int adapterPosition = companies.indexOf(preSelectedCompany);
            contactCompany.setSelection(adapterPosition);
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
                showDiscardChangesDialog();
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

    private void addCompany() {
        View addCompanyDialogView = getLayoutInflater().inflate(R.layout.dialog_new_company, null, false);

        final EditText companyNameEditText = (EditText) addCompanyDialogView.findViewById(R.id.new_company_name_et);

        final AlertDialog addNewCompanyDialog = new AlertDialog.Builder(this)
                .setTitle("Add new company")
                .setView(addCompanyDialogView)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Dummy
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .create();
        addNewCompanyDialog.show();

        companyNameEditText.requestFocus();

        addNewCompanyDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String newCompanyName = companyNameEditText.getText().toString().toUpperCase();

                        if(!newCompanyName.isEmpty()) {
                            Company company;

                            if(app.getCompanyRecyclerViewAdapter().getCompaniesName().contains(newCompanyName)) {
                                companyNameEditText.setError("Company already exists!");
                                companyNameEditText.requestFocus();
                            } else {
                                String newCompanyId = databaseManager.getCompaniesRef().push().getKey();
                                company = new Company(newCompanyId, newCompanyName);
                                databaseManager.addCompany(company);

                                // Add and select created Company to Company Spinner.
                                companyAdapter.add(company);
                                companyAdapter.notifyDataSetChanged();
                                int adapterPosition = companies.indexOf(company);
                                contactCompany.setSelection(adapterPosition);

                                addNewCompanyDialog.dismiss();
                            }

                        } else {
                            companyNameEditText.setError("Please enter a company name");
                            companyNameEditText.requestFocus();
                        }
                    }
                });
    }

    private void showDiscardChangesDialog() {
        AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(this)
                        .setMessage(R.string.dialog_discard_changes_msg)
                        .setPositiveButton("Discard", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                showParentActivity();
                                dialogInterface.dismiss();
                            }
                        }).setNegativeButton("Keep editing", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        final AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
    }

    public void addContact(){
        final Company company = (Company) contactCompany.getSelectedItem();

        final String id = databaseManager.getCompaniesRef().child(company.getId()).child("contacts").push().getKey();
        final String name = contactName.getText().toString();
        final String email = contactEmail.getText().toString().trim();
        final String phone = contactPhone.getText().toString().trim();
        final String comment = contactComment.getText().toString();
        final String timestampAdded = Long.toString(System.currentTimeMillis());

        final Contact newContact = new Contact(id, name, company.getId(), email, phone, comment, timestampAdded);
        newContact.setTimestampUpdated(timestampAdded);
        databaseManager.addContact(newContact);

        showParentActivity();

    }

    public void updateContact(){
        final String id = oldCID;
        final String name = contactName.getText().toString();
        final Company company = (Company) contactCompany.getSelectedItem();
        final String email = contactEmail.getText().toString().trim();
        final String phone = contactPhone.getText().toString().trim();
        final String comment = contactComment.getText().toString();
        final String timestampAdded = contact.getTimestampAdded();
        final String timestampUpdated = Long.toString(System.currentTimeMillis());

        final Contact updatedContact = new Contact(id, name, company.getId(), email, phone, comment, timestampAdded);
        updatedContact.setTimestampUpdated(timestampUpdated);
        databaseManager.updateContact(contact, updatedContact);

        showParentActivity();

    }

    private void showParentActivity() {
        restUI();
        startActivity(new Intent(this, MainActivity.class));
        finishAfterTransition();
    }

    private void restUI(){
        contactName.getText().clear();
        contactEmail.getText().clear();
        contactPhone.getText().clear();
        contactComment.getText().clear();
    }

    @Override
    public void onBackPressed() {
        showDiscardChangesDialog();
    }
}
