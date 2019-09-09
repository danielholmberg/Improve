package dev.danielholmberg.improve.ViewHolders;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import dev.danielholmberg.improve.Models.Contact;
import dev.danielholmberg.improve.Fragments.ContactDetailsSheetFragment;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.R;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class ContactViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    private View mView;

    private Contact contact;

    public ContactViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
    }

    // OBS! Due to RecyclerView:
    // We need to define all views of each contact!
    // Otherwise each contact view won't be unique.
    public void bindModelToView(final Contact contact) {
        this.contact = contact;

        // [START] All views of a contact
        Button callBtn = mView.findViewById(R.id.call_contact_btn);
        Button mailBtn = mView.findViewById(R.id.mail_contact_btn);

        TextView name = mView.findViewById(R.id.name_tv);
        // [END] All views of a contact

        // [START] Define each view
        name.setText(contact.getName());

        if (contact.getEmail() == null || contact.getEmail().isEmpty()) {
            mailBtn.setBackground(Improve.getInstance().getResources().getDrawable(R.drawable.ic_contact_email_grey));
        } else {
            mailBtn.setBackground(Improve.getInstance().getResources().getDrawable(R.drawable.ic_contact_email_active));
        }
        mailBtn.setOnClickListener(this);

        if (contact.getPhone() == null || contact.getPhone().isEmpty()) {
            callBtn.setBackground(Improve.getInstance().getResources().getDrawable(R.drawable.ic_contact_mobile_grey));
        } else {
            callBtn.setBackground(Improve.getInstance().getResources().getDrawable(R.drawable.ic_contact_mobile_active));
        }
        callBtn.setOnClickListener(this);
        // [END] Define each view

        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = createBundle(contact);
                ContactDetailsSheetFragment contactDetailsSheetFragment = new ContactDetailsSheetFragment();
                contactDetailsSheetFragment.setArguments(bundle);
                contactDetailsSheetFragment.show(Improve.getInstance().getMainActivityRef().getSupportFragmentManager(),
                        contactDetailsSheetFragment.getTag());
            }
        });
    }

    private Bundle createBundle(Contact contact) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ContactDetailsSheetFragment.CONTACT_KEY, contact);
        bundle.putInt(ContactDetailsSheetFragment.PARENT_FRAGMENT_KEY, R.integer.CONTACT_FRAGMENT);
        return bundle;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.call_contact_btn:
                if(contact.getPhone() == null || contact.getPhone().isEmpty()) {
                    Toast.makeText(Improve.getInstance(),
                            Improve.getInstance().getResources().getString(R.string.contact_no_phone_message),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" + contact.getPhone()));
                    callIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    Improve.getInstance().startActivity(callIntent);
                }
                break;
            case R.id.mail_contact_btn:
                if(contact.getEmail() == null || contact.getEmail().isEmpty()) {
                    Toast.makeText(Improve.getInstance(),
                            Improve.getInstance().getResources().getString(R.string.contact_no_email_message),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Intent mailIntent = new Intent(Intent.ACTION_SENDTO);
                    mailIntent.setData(Uri.parse("mailto:" + contact.getEmail()));
                    mailIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    Improve.getInstance().startActivity(mailIntent);
                }
                break;
            default:
                break;
        }
    }
}