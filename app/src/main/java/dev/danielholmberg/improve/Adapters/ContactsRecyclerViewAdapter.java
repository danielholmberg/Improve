package dev.danielholmberg.improve.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dev.danielholmberg.improve.Activities.AddContactActivity;
import dev.danielholmberg.improve.Components.Contact;
import dev.danielholmberg.improve.InternalStorage;
import dev.danielholmberg.improve.R;

/**
 * Created by DanielHolmberg on 2018-01-19.
 */

public class ContactsRecyclerViewAdapter extends
        RecyclerView.Adapter<ContactsRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = ContactsRecyclerViewAdapter.class.getSimpleName();

    private List<Contact> contactsList;
    private List<Contact> contactsListCopy;
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
        holder.name.setText(contact.getFullName());
        holder.email.setText(contact.getEmail());

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editContactActivity(contact, itemPos);
            }
        });

        AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(context).setTitle("Delete " + contact.getFullName())
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteContact(contact.getCID(), itemPos);
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        final AlertDialog dialog = alertDialogBuilder.create();

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });
    }

    private void editContactActivity(Contact contact, int itemPos){
        Bundle bundle = new Bundle();
        bundle.putString("cid", contact.getCID());
        bundle.putString("first_name", contact.getFirstName());
        bundle.putString("last_name", contact.getLastName());
        bundle.putString("company", contact.getCompany());
        bundle.putString("email", contact.getEmail());
        bundle.putString("mobile", contact.getMobile());
        bundle.putInt("position", itemPos);

        Intent i = new Intent(context, AddContactActivity.class);
        i.putExtra("contact", bundle);
        context.startActivity(i);
    }

    private void deleteContact(final String docId, final int position){
        DocumentReference companyRef = firestoreDB.collection("companies").document(contactsList.get(position).getCompany());
        final String contactName = contactsList.get(position).getFullName();

        // Delete the OnMyMind from the recycler list.
        contactsList.remove(position);
        contactsListCopy.remove(position);
        if(contactsList.isEmpty()) {
            TextView epmtyListText = (TextView) parentLayout.getRootView().findViewById(R.id.empty_contact_list_tv);
            epmtyListText.setVisibility(View.VISIBLE);
        }
        try {
            // Try to get the stored list of contacts and remove the specified contact.
            List<Contact> storedList = (ArrayList<Contact>) InternalStorage.readObject(context,
                    InternalStorage.CONTACTS_STORAGE_KEY);
            storedList.remove(position);
            InternalStorage.writeObject(context, InternalStorage.CONTACTS_STORAGE_KEY, storedList);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read from Internal Storage: ");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        notifyItemRemoved(position);
        notifyItemRangeChanged(position, contactsList.size());
        // Delete the specified contact from Firestore Database.
        companyRef.collection("contacts").document(docId).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "*** Deleted contact successfully ***");
                        // Deletion was successful.
                        Snackbar.make(parentLayout,
                                contactName + " has been deleted",
                                Snackbar.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to delete contact - id: " + docId);
                        e.printStackTrace();
                    }
                });
    }

    public void filter(String text) {
        if(contactsList != null && contactsListCopy != null) {
            contactsList.clear();
            if (text.isEmpty()) {
                contactsList.addAll(contactsListCopy);
            } else {
                text = text.toLowerCase();
                for (Contact contact : contactsListCopy) {
                    if (contact.getFirstName().toLowerCase().contains(text) || contact.getEmail().toLowerCase().contains(text)) {
                        contactsList.add(contact);
                    }
                }
            }
            notifyDataSetChanged();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView name;
        public TextView email;
        public Button edit;
        public Button delete;

        public ViewHolder(View view) {
            super(view);

            name = (TextView) view.findViewById(R.id.name_tv);
            email = (TextView) view.findViewById(R.id.email_tv);
            edit = (Button) view.findViewById(R.id.edit_contact_b);
            delete = (Button) view.findViewById(R.id.delete_contact_b);
        }
    }
}
