package dev.danielholmberg.improve.Activities;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import dev.danielholmberg.improve.Callbacks.FirebaseStorageCallback;
import dev.danielholmberg.improve.Components.Contact;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Managers.FirebaseStorageManager;
import dev.danielholmberg.improve.R;
import dev.danielholmberg.improve.Utilities.ContactInputValidator;

/**
 * Created by DanielHolmberg on 2018-01-27.
 */

public class AddContactActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = AddContactActivity.class.getSimpleName();

    private Improve app;
    private FirebaseStorageManager storageManager;

    private List<Contact> storedContacts;

    private String userId;
    private boolean isEdit;
    private String oldCID, oldColor;
    private int contactPosition;

    private Toolbar toolbar;
    private TextInputEditText inputName, inputEmail, inputCompany, inputPhone, inputComment;
    private ContactInputValidator validator;
    private AlertDialog colorPickerDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        app = Improve.getInstance();
        storageManager = app.getFirebaseStorageManager();

        // Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar_add_contact);
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
            oldColor = contact.getColor();
            contactPosition = intentBundle.getInt("position");

            toolbar.setBackgroundColor(oldColor != null ? Color.parseColor(oldColor) :
                    getResources().getColor(R.color.colorPickerDeepOrange));
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
        inflater.inflate(R.menu.activity_contact_mode_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.chooseBackgroundColor:
                chooseBackgroundColor();
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

    private void chooseBackgroundColor() {
        LinearLayout colorPickerLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.color_picker, null, false);

        // First row
        colorPickerLayout.findViewById(R.id.buttonColorGreen).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorLightGreen).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorAmber).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorDeepOrange).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorBrown).setOnClickListener(this);

        // Second row
        colorPickerLayout.findViewById(R.id.buttonColorBlueGrey).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorRed).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorPink).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorDeepPurple).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorIndigo).setOnClickListener(this);

        AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(this).setTitle("Choose a color")
                        .setMessage("Assign a specific color to your Contact")
                        .setCancelable(true)
                        .setView(colorPickerLayout);
        colorPickerDialog = alertDialogBuilder.create();
        colorPickerDialog.show();
    }

    public void addContact(){
        String id = storageManager.getContactsRef().push().getKey();
        final String name = inputName.getText().toString();
        String company = inputCompany.getText().toString();
        String email = inputEmail.getText().toString();
        String phone = inputPhone.getText().toString();
        String comment = inputComment.getText().toString();
        String color = "#" + Integer.toHexString(((ColorDrawable) toolbar.getBackground()).getColor());

        Contact newContact = new Contact(id, name, company, email, phone, comment, color);
        storageManager.writeContactToFirebase(newContact, new FirebaseStorageCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(app, "Added new contact" , Toast.LENGTH_SHORT).show();
            }

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
        String company = inputCompany.getText().toString();
        String email = inputEmail.getText().toString();
        String phone = inputPhone.getText().toString();
        String comment = inputComment.getText().toString();
        String color = "#" + Integer.toHexString(((ColorDrawable) toolbar.getBackground()).getColor());

        Contact updatedContact = new Contact(id, name, company, email, phone, comment, color);
        storageManager.writeContactToFirebase(updatedContact, new FirebaseStorageCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(app,"Updated contact", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonColorGreen:
                toolbar.setBackgroundColor(getResources().getColor(R.color.colorPickerGreen));
                break;
            case R.id.buttonColorLightGreen:
                toolbar.setBackgroundColor(getResources().getColor(R.color.colorPickerLightGreen));
                break;
            case R.id.buttonColorAmber:
                toolbar.setBackgroundColor(getResources().getColor(R.color.colorPickerAmber));
                break;
            case R.id.buttonColorDeepOrange:
                toolbar.setBackgroundColor(getResources().getColor(R.color.colorPickerDeepOrange));
                break;
            case R.id.buttonColorBrown:
                toolbar.setBackgroundColor(getResources().getColor(R.color.colorPickerBrown));
                break;
            case R.id.buttonColorBlueGrey:
                toolbar.setBackgroundColor(getResources().getColor(R.color.colorPickerBlueGrey));
                break;
            case R.id.buttonColorRed:
                toolbar.setBackgroundColor(getResources().getColor(R.color.colorPickerTurquoise));
                break;
            case R.id.buttonColorPink:
                toolbar.setBackgroundColor(getResources().getColor(R.color.colorPickerPink));
                break;
            case R.id.buttonColorDeepPurple:
                toolbar.setBackgroundColor(getResources().getColor(R.color.colorPickerDeepPurple));
                break;
            case R.id.buttonColorIndigo:
                toolbar.setBackgroundColor(getResources().getColor(R.color.colorPickerIndigo));
                break;
            default:
                toolbar.setBackgroundColor(getResources().getColor(R.color.colorPickerDeepOrange));
                break;
        }
        colorPickerDialog.dismiss();
    }
}
