package dev.danielholmberg.improve.ViewHolders;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import dev.danielholmberg.improve.Components.Contact;
import dev.danielholmberg.improve.Fragments.ContactDetailsSheetFragment;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.R;

/**
 * Created by Daniel Holmberg.
 */

public class ContactViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    private View mView;
    private Context context;

    private Contact contact;

    public ContactViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        context = itemView.getContext();
    }

    public void bindModelToView(final Contact contact) {
        this.contact = contact;

        Button callBtn = (Button) mView.findViewById(R.id.call_contact_btn);
        Button mailBtn = (Button) mView.findViewById(R.id.mail_contact_btn);

        ((TextView) mView.findViewById(R.id.name_tv)).setText(contact.getName());
        ((TextView) mView.findViewById(R.id.company_tv)).setText(contact.getCompany());

        if (contact.getEmail().isEmpty()) {
            mailBtn.setBackground(context.getResources().getDrawable(R.drawable.ic_contact_email_grey));
            mailBtn.setEnabled(false);
        }

        if (contact.getPhone().isEmpty()) {
            callBtn.setBackground(context.getResources().getDrawable(R.drawable.ic_contact_mobile_grey));
            callBtn.setEnabled(false);
        }

        callBtn.setOnClickListener(this);
        mailBtn.setOnClickListener(this);

        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference ref = Improve.getInstance().getFirebaseStorageManager().getContactsRef()
                        .child(contact.getId());
                ref.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Contact contact = dataSnapshot.getValue(Contact.class);
                        int itemPosition = getAdapterPosition();

                        Bundle bundle = createBundle(contact, itemPosition);
                        ContactDetailsSheetFragment contactDetailsSheetFragment = new ContactDetailsSheetFragment();
                        contactDetailsSheetFragment.setArguments(bundle);
                        contactDetailsSheetFragment.show(((AppCompatActivity)context).getSupportFragmentManager(),
                                contactDetailsSheetFragment.getTag());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    private Bundle createBundle(Contact contact, int itemPos) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("contact", contact);
        bundle.putInt("position", itemPos);
        return bundle;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.call_contact_btn:
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + contact.getPhone()));
                context.startActivity(callIntent);
                break;
            case R.id.mail_contact_btn:
                Intent mailIntent = new Intent(Intent.ACTION_SENDTO);
                mailIntent.setData(Uri.parse("mailto:" + contact.getEmail()));
                context.startActivity(mailIntent);
                break;
            default:
                break;
        }
    }
}
