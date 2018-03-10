package dev.danielholmberg.improve.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import dev.danielholmberg.improve.Components.Contact;
import dev.danielholmberg.improve.Fragments.ContactDetailsSheetFragment;
import dev.danielholmberg.improve.R;

/**
 * Created by DanielHolmberg on 2018-01-19.
 */

public class ContactsRecyclerViewAdapter extends
        RecyclerView.Adapter<ContactsRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = ContactsRecyclerViewAdapter.class.getSimpleName();

    public List<Contact> contactsList;
    public List<Contact> contactsListCopy;
    private Context context;
    private FirebaseFirestore firestoreDB;
    private View parentLayout;

    public ContactsRecyclerViewAdapter(List<Contact> list, Context ctx, FirebaseFirestore firestore) {
        this.contactsList = list;
        this.context = ctx;
        this.firestoreDB = firestore;
    }

    public void setAdapterList(List<Contact> list) {
        contactsList = list;
        contactsListCopy = new ArrayList<Contact>();
        contactsListCopy.addAll(contactsList);
    }

    @Override
    public int getItemCount() {
        return contactsList.size();
    }

    @Override
    public ContactsRecyclerViewAdapter.ViewHolder
    onCreateViewHolder(ViewGroup parent, int viewType) {
        parentLayout = parent;
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_contact_item, parent, false);

        ContactsRecyclerViewAdapter.ViewHolder viewHolder =
                new ContactsRecyclerViewAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ContactsRecyclerViewAdapter.ViewHolder holder, int position) {
        final int itemPos = position;
        final Contact contact = contactsList.get(position);

        // Fill the list-item with all the necessary content.
        holder.name.setText(contact.getName());
        holder.company.setText(contact.getCompany());

        if(contact.getEmail() != null) {
            if (contact.getEmail().isEmpty()) {
                holder.mailBtn.setBackground(context.getResources().getDrawable(R.drawable.ic_contact_email_grey));
                holder.mailBtn.setEnabled(false);
            }
        } else {
            holder.mailBtn.setBackground(context.getResources().getDrawable(R.drawable.ic_contact_email_grey));
            holder.mailBtn.setEnabled(false);
        }
        if(contact.getMobile() != null) {
            if (contact.getMobile().isEmpty()) {
                holder.callBtn.setBackground(context.getResources().getDrawable(R.drawable.ic_contact_mobile_grey));
                holder.callBtn.setEnabled(false);
            }
        } else {
            holder.callBtn.setBackground(context.getResources().getDrawable(R.drawable.ic_contact_mobile_grey));
            holder.callBtn.setEnabled(false);
        }

        holder.callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + contact.getMobile()));
                context.startActivity(callIntent);
            }
        });
        holder.mailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mailIntent = new Intent(Intent.ACTION_SENDTO);
                mailIntent.setData(Uri.parse("mailto:" + contact.getEmail()));
                context.startActivity(mailIntent);
            }
        });

        // Handle what happens when the user clicks on the contact card.
        setUpOnClickListener(holder.cardBodyView, contact, itemPos);
    }

    private void setUpOnClickListener(View view, final Contact contact, final int itemPos) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showContactDetailsDialogFragment(contact, itemPos);
            }
        });
    }

    private Bundle createBundle(Contact contact, int itemPos) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("contact", contact);
        bundle.putInt("position", itemPos);
        return bundle;
    }

    private void showContactDetailsDialogFragment(Contact contact, int itemPos) {
        Bundle bundle = createBundle(contact, itemPos);
        ContactDetailsSheetFragment contactDetailsSheetFragment = new ContactDetailsSheetFragment();
        contactDetailsSheetFragment.setArguments(bundle);
        contactDetailsSheetFragment.setAdapter(this);
        contactDetailsSheetFragment.show(((AppCompatActivity)context).getSupportFragmentManager(), contactDetailsSheetFragment.getTag());
    }

    public void filter(String text) {
        if(contactsList != null && contactsListCopy != null) {
            contactsList.clear();
            if (text.isEmpty()) {
                contactsList.addAll(contactsListCopy);
            } else {
                text = text.toLowerCase();
                for (Contact contact : contactsListCopy) {
                    if (contact.getName().toLowerCase().contains(text) || contact.getEmail().toLowerCase().contains(text)) {
                        contactsList.add(contact);
                    }
                }
            }
            notifyDataSetChanged();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public View cardBodyView;
        public TextView name;
        public TextView company;
        public Button callBtn;
        public Button mailBtn;

        public ViewHolder(View view) {
            super(view);

            cardBodyView = view;

            name = (TextView) view.findViewById(R.id.name_tv);
            company = (TextView) view.findViewById(R.id.company_tv);
            callBtn = (Button) view.findViewById(R.id.call_contact_btn);
            mailBtn = (Button) view.findViewById(R.id.mail_contact_btn);
        }
    }
}
