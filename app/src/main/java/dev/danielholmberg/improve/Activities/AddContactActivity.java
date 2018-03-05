package dev.danielholmberg.improve.Activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dev.danielholmberg.improve.Components.Contact;
import dev.danielholmberg.improve.ContactInputValidator;
import dev.danielholmberg.improve.InternalStorage;
import dev.danielholmberg.improve.R;

/**
 * Created by DanielHolmberg on 2018-01-27.
 */

public class AddContactActivity extends AppCompatActivity {
    private static final String TAG = AddContactActivity.class.getSimpleName();
    private static final int CONTACT_UPDATED = 9997;

    private List<Contact> storedContacts;

    private FirebaseFirestore firestoreDB;

    private String userId;
    private boolean isEdit;
    private String oldCID;
    private int contactPosition;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_add_contact);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        View layout = (View) findViewById(R.id.activity_add_contact_layout);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.contact_done);

        Contact contact = null;
        Bundle extras = getIntent().getBundleExtra("contact");

        // Get current userId.
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if(extras != null){
            contact = new Contact();
            contact.setCID(extras.getString("cid"));
            contact.setName(extras.getString("name"));
            contact.setCompany(extras.getString("company"));
            contact.setEmail(extras.getString("email"));
            contact.setMobile(extras.getString("mobile"));
        }
        if(contact != null){
            Log.d(TAG, "Contact is not null");
            isEdit = true;
            ((TextView) findViewById(R.id.toolbar_add_contact_title_tv)).setText(R.string.title_edit_contact);

            oldCID = contact.getCID();
            contactPosition = extras.getInt("position");

            ((TextView) findViewById(R.id.input_name)).setText(contact.getName());
            ((TextView) findViewById(R.id.input_company)).setText(contact.getCompany());
            ((TextView) findViewById(R.id.input_email)).setText(contact.getEmail());
            ((TextView) findViewById(R.id.input_mobile)).setText(contact.getMobile());
        }

        firestoreDB = FirebaseFirestore.getInstance();

        try {
            storedContacts = (List<Contact>) InternalStorage.readObject(InternalStorage.contacts);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read from Internal Storage: ");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if(storedContacts.isEmpty()) {
            storedContacts = new ArrayList<>();
        }

        final ContactInputValidator validator = new ContactInputValidator(this, layout);
        fab.setOnClickListener(new View.OnClickListener()        {
            @Override
            public void onClick(View v)
            {
                if(validator.formIsValid()) {
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
        contact.setName(((TextView) findViewById(R.id.input_name)).getText().toString());
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
            InternalStorage.writeObject(InternalStorage.contacts, storedContacts);
        } catch (IOException e) {
            Log.e(TAG, "Failed to write to Internal Storage: ");
            e.printStackTrace();
        }

        firestoreDB.collection("users")
                .document(userId)
                .collection("contacts")
                .document(contact.getCID())
                .set(contact)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void avoid) {
                        Log.d(TAG, "Contact document added - id: "
                                + contact.getCID());
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
        showParentActivity();
    }

    /**
     * Update a Contact in Firestore Database.
     * @param updatedContact
     */
    private void updateContact(final Contact updatedContact){
        updatedContact.setCID(oldCID);
        storedContacts.set(contactPosition, updatedContact);
        try {
            InternalStorage.writeObject(InternalStorage.contacts, storedContacts);
        } catch (IOException e) {
            e.printStackTrace();
        }
        firestoreDB.collection("users")
                .document(userId)
                .collection("contacts")
                .document(oldCID)
                .set(updatedContact, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, updatedContact.getName() + " updated successfully");
                        setResult(CONTACT_UPDATED);
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
        showParentActivity();
    }

    private void showParentActivity() {
        restUi();
        NavUtils.navigateUpFromSameTask(this);
    }

    private void restUi(){
        ((TextView) findViewById(R.id.input_name)).setText("");
        ((TextView) findViewById(R.id.input_company)).setText("");
        ((TextView) findViewById(R.id.input_email)).setText("");
        ((TextView) findViewById(R.id.input_mobile)).setText("");
    }
}
