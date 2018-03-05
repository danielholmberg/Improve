package dev.danielholmberg.improve.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        holder.email.setText(contact.getEmail());

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
        bundle.putString("cid", contact.getCID());
        bundle.putString("name", contact.getName());
        bundle.putString("company", contact.getCompany());
        bundle.putString("email", contact.getEmail());
        bundle.putString("mobile", contact.getMobile());
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
        public TextView email;

        public ViewHolder(View view) {
            super(view);

            cardBodyView = view;

            name = (TextView) view.findViewById(R.id.name_tv);
            email = (TextView) view.findViewById(R.id.email_tv);
        }
    }
}
