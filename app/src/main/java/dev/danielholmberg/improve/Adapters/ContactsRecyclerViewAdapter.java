package dev.danielholmberg.improve.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import dev.danielholmberg.improve.Activities.AddContactActivity;
import dev.danielholmberg.improve.Components.Contact;
import dev.danielholmberg.improve.R;

/**
 * Created by DanielHolmberg on 2018-01-19.
 */

public class ContactsRecyclerViewAdapter extends
        RecyclerView.Adapter<ContactsRecyclerViewAdapter.ViewHolder> {

    private List<Contact> contactsList;
    private Context context;
    private FirebaseFirestore firestoreDB;

    public ContactsRecyclerViewAdapter(List<Contact> list, Context ctx, FirebaseFirestore firestore) {
        contactsList = list;
        context = ctx;
        firestoreDB = firestore;
    }

    public void setAdapterList(List<Contact> list) {
        contactsList = list;
    }

    @Override
    public int getItemCount() {
        return contactsList.size();
    }

    @Override
    public ContactsRecyclerViewAdapter.ViewHolder
    onCreateViewHolder(ViewGroup parent, int viewType) {
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
                editContactActivity(contact);
            }
        });

        AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(context).setTitle("Delete contact")
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

    private void editContactActivity(Contact contact){
        Bundle bundle = new Bundle();
        bundle.putString("cid", contact.getCID());
        bundle.putString("first_name", contact.getFirstName());
        bundle.putString("last_name", contact.getLastName());
        bundle.putString("company", contact.getCompany());
        bundle.putString("email", contact.getEmail());
        bundle.putString("phone", contact.getPhone());

        Intent i = new Intent(context, AddContactActivity.class);
        i.putExtra("contact", bundle);
        context.startActivity(i);

    }

    private void deleteContact(String docId, final int position){
        DocumentReference companyRef = firestoreDB.collection("companies").document(contactsList.get(position).getCompany());

        companyRef.collection("contacts").document(docId).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        contactsList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, contactsList.size());
                        Toast.makeText(context,
                                "Contact document has been deleted",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
