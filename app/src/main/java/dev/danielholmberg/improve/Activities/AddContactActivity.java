package dev.danielholmberg.improve.Activities;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
    private ContactInputValidator validator;

    private LinearLayout marker;
    private int markerColor;
    private GradientDrawable markerBackground;
    private TextInputEditText inputName, inputEmail, inputCompany, inputPhone, inputComment;

    private Toolbar toolbar;
    private View inputLayout;

    private AlertDialog colorPickerDialog;

    private boolean isEdit;
    private String oldCID, oldColor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        app = Improve.getInstance();
        storageManager = app.getFirebaseStorageManager();

        toolbar = (Toolbar) findViewById(R.id.toolbar_add_contact);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        marker = (LinearLayout) findViewById(R.id.include_item_marker);
        markerColor = getResources().getColor(R.color.colorPickerDeepOrange);
        markerBackground = (GradientDrawable) marker.getBackground();
        markerBackground.setColor(markerColor);

        inputLayout = (View) findViewById(R.id.input_contact_layout);
        inputName = (TextInputEditText) findViewById(R.id.input_name);
        inputCompany = (TextInputEditText) findViewById(R.id.input_company);
        inputEmail = (TextInputEditText) findViewById(R.id.input_email);
        inputPhone = (TextInputEditText) findViewById(R.id.input_mobile);
        inputComment = (TextInputEditText) findViewById(R.id.input_comment);

        Bundle intentBundle = getIntent().getBundleExtra("contactBundle");
        Contact contact =  intentBundle != null ? (Contact) intentBundle.getParcelable("contact") : null;

        if(contact != null){
            isEdit = true;
            ((TextView) findViewById(R.id.toolbar_add_contact_title_tv)).setText(R.string.title_edit_contact);

            oldCID = contact.getId();
            oldColor = contact.getColor();

            if(oldColor != null && !oldColor.isEmpty()) {
                markerColor = Color.parseColor(oldColor);
                markerBackground.setColor(markerColor);
            }

            inputName.setText(contact.getName());
            inputCompany.setText(contact.getCompany());
            inputEmail.setText(contact.getEmail());
            inputPhone.setText(contact.getPhone());
            inputComment.setText(contact.getComment());
        }

        validator = new ContactInputValidator(this, inputLayout);

        marker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseMarkerColor();
            }
        });
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
            case R.id.chooseMarkerColor:
                chooseMarkerColor();
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

    private void chooseMarkerColor() {
        LinearLayout colorPickerLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.color_picker, null, false);

        // First row
        colorPickerLayout.findViewById(R.id.buttonColorGreen).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorLightGreen).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorAmber).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorDeepOrange).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorBrown).setOnClickListener(this);

        // Second row
        colorPickerLayout.findViewById(R.id.buttonColorBlueGrey).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorTurquoise).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorPink).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorDeepPurple).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorDarkGrey).setOnClickListener(this);

        // Third row
        colorPickerLayout.findViewById(R.id.buttonColorRed).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorPurple).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorBlue).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorDarkOrange).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorBabyBlue).setOnClickListener(this);

        AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(this).setTitle("Marker color")
                        .setMessage("Assign a specific color to your Contact")
                        .setCancelable(true)
                        .setView(colorPickerLayout);
        colorPickerDialog = alertDialogBuilder.create();
        colorPickerDialog.show();
    }

    public void addContact(){
        String id = storageManager.getContactsRef().push().getKey();
        final String name = inputName.getText().toString();
        String company = inputCompany.getText().toString().toUpperCase();
        String email = inputEmail.getText().toString().trim();
        String phone = inputPhone.getText().toString().trim();
        String comment = inputComment.getText().toString();
        String color = "#" + Integer.toHexString(markerColor);

        if(TextUtils.isEmpty(comment)) {
            comment = "";
        }

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
        String company = inputCompany.getText().toString().toUpperCase();
        String email = inputEmail.getText().toString().trim();
        String phone = inputPhone.getText().toString().trim();
        String comment = inputComment.getText().toString();
        String color = "#" + Integer.toHexString(markerColor);

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
    public void onClick(View view) {
        GradientDrawable marker_shape = (GradientDrawable) marker.getBackground();
        switch (view.getId()) {
            case R.id.buttonColorGreen:
                markerColor = getResources().getColor(R.color.colorPickerGreen);
                marker_shape.setColor(markerColor);
                break;
            case R.id.buttonColorLightGreen:
                markerColor = getResources().getColor(R.color.colorPickerLightGreen);
                marker_shape.setColor(markerColor);
                break;
            case R.id.buttonColorAmber:
                markerColor = getResources().getColor(R.color.colorPickerAmber);
                marker_shape.setColor(markerColor);
                break;
            case R.id.buttonColorDeepOrange:
                markerColor = getResources().getColor(R.color.colorPickerDeepOrange);
                marker_shape.setColor(markerColor);
                break;
            case R.id.buttonColorBrown:
                markerColor = getResources().getColor(R.color.colorPickerBrown);
                marker_shape.setColor(markerColor);
                break;
            case R.id.buttonColorBlueGrey:
                markerColor = getResources().getColor(R.color.colorPickerBlueGrey);
                marker_shape.setColor(markerColor);
                break;
            case R.id.buttonColorTurquoise:
                markerColor = getResources().getColor(R.color.colorPickerTurquoise);
                marker_shape.setColor(markerColor);
                break;
            case R.id.buttonColorPink:
                markerColor = getResources().getColor(R.color.colorPickerPink);
                marker_shape.setColor(markerColor);
                break;
            case R.id.buttonColorDeepPurple:
                markerColor = getResources().getColor(R.color.colorPickerDeepPurple);
                marker_shape.setColor(markerColor);
                break;
            case R.id.buttonColorDarkGrey:
                markerColor = getResources().getColor(R.color.colorPickerDarkGrey);
                marker_shape.setColor(markerColor);
                break;
            case R.id.buttonColorRed:
                markerColor = getResources().getColor(R.color.colorPickerRed);
                marker_shape.setColor(markerColor);
                break;
            case R.id.buttonColorPurple:
                markerColor = getResources().getColor(R.color.colorPickerPurple);
                marker_shape.setColor(markerColor);
                break;
            case R.id.buttonColorBlue:
                markerColor = getResources().getColor(R.color.colorPickerBlue);
                marker_shape.setColor(markerColor);
                break;
            case R.id.buttonColorDarkOrange:
                markerColor = getResources().getColor(R.color.colorPickerDarkOrange);
                marker_shape.setColor(markerColor);
                break;
            case R.id.buttonColorBabyBlue:
                markerColor = getResources().getColor(R.color.colorPickerBabyBlue);
                marker_shape.setColor(markerColor);
                break;
            default:
                markerColor = getResources().getColor(R.color.colorPickerDeepOrange);
                marker_shape.setColor(markerColor);
                break;
        }
        colorPickerDialog.dismiss();
    }
}
