package dev.danielholmberg.improve.Activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import dev.danielholmberg.improve.Components.Contact;
import dev.danielholmberg.improve.R;

/**
 * Class ${CLASS}
 */

public class ContactDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_contact_details);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView name = (TextView) findViewById(R.id.toolbar_contact_details_name_tv);
        TextView email = (TextView) findViewById(R.id.contact_details_email_tv);

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
            name.setText(contact.getFullName());
            email.setText(contact.getEmail());
        }
    }
}
