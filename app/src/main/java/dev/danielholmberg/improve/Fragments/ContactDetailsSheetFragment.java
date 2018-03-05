package dev.danielholmberg.improve.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dev.danielholmberg.improve.Activities.AddContactActivity;
import dev.danielholmberg.improve.Adapters.ContactsRecyclerViewAdapter;
import dev.danielholmberg.improve.Components.Contact;
import dev.danielholmberg.improve.InternalStorage;
import dev.danielholmberg.improve.R;

/**
 * Class ${CLASS}
 */

public class ContactDetailsSheetFragment extends BottomSheetDialogFragment {
    private static final String TAG = ContactDetailsSheetFragment.class.getSimpleName();

    private Contact contact;
    private int contactPos;
    private Bundle contactInfo;

    private FirebaseFirestore firestoreDB;
    private ContactsRecyclerViewAdapter adapter;

    private ContactDetailsSheetFragment context;
    private View view;

    public ContactDetailsSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        firestoreDB = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.contact_details_sheet, container, false);

        Button updateContactBtn = (Button) view.findViewById(R.id.update_contact_btn);
        Button deleteContactBtn = (Button) view.findViewById(R.id.delete_contact_btn);

        Button actionCallContact = (Button) view.findViewById(R.id.call_contact_btn);
        Button actionSendMailToContact = (Button) view.findViewById(R.id.mail_contact_btn);

        TextView name = (TextView) view.findViewById(R.id.contact_details_name_tv);
        TextView email = (TextView) view.findViewById(R.id.contact_details_email_tv);
        TextView company = (TextView) view.findViewById(R.id.contact_details_company_tv);
        TextView mobile = (TextView) view.findViewById(R.id.contact_details_mobile_tv);

        contact = null;
        contactInfo =  this.getArguments();

        if(contactInfo != null){
            contact = new Contact();
            contact.setCID(contactInfo.getString("cid"));
            contact.setName(contactInfo.getString("name"));
            contact.setCompany(contactInfo.getString("company"));
            contact.setEmail(contactInfo.getString("email"));
            contact.setMobile(contactInfo.getString("mobile"));

            contactPos = contactInfo.getInt("position");
        }
        if(contact != null){
            name.setText(contact.getName());
            email.setText(contact.getEmail());
            company.setText(contact.getCompany());
            mobile.setText(contact.getMobile());
        } else {
            // Dismiss dialog and show Toast.
            this.dismiss();
            Toast.makeText(getContext(), "Unable to show contact details", Toast.LENGTH_SHORT).show();
        }

        AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(getContext()).setTitle("Delete contact")
                        .setMessage("Do you really want to delete: " + contact.getName())
                        .setIcon(R.drawable.ic_menu_delete_black)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteContact(contact, contactPos);
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        final AlertDialog dialog = alertDialogBuilder.create();

        updateContactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateContactMode();
            }
        });
        deleteContactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });

        if(contact.getMobile().isEmpty()) {
            actionCallContact.setEnabled(false);
            actionCallContact.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_contact_mobile_grey), null, null, null);
            mobile.setVisibility(View.GONE);
        }

        actionCallContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + contact.getMobile()));
                startActivity(callIntent);
            }
        });
        actionSendMailToContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mailIntent = new Intent(Intent.ACTION_SENDTO);
                mailIntent.setData(Uri.parse("mailto:" + contact.getEmail()));
                startActivity(mailIntent);
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    private void updateContactMode() {
        Intent updateContact = new Intent(getContext(), AddContactActivity.class);
        updateContact.putExtra("contact", contactInfo);
        startActivity(updateContact);
    }

    private void deleteContact(final Contact contact, int contactPos){
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String contactName = contact.getName();

        final View parentLayout = getActivity().getWindow().findViewById(R.id.contacts_fragment_container);

        // Delete the OnMyMind from the recycler list.
        adapter.contactsList.remove(contactPos);
        adapter.contactsListCopy.remove(contactPos);
        if(adapter.contactsList.isEmpty()) {
            Log.d(TAG, "parentLayout: " + parentLayout);
            TextView epmtyListText = (TextView) parentLayout.findViewById(R.id.empty_contact_list_tv);
            epmtyListText.setVisibility(View.VISIBLE);
        }
        try {
            // Try to get the stored list of contacts and remove the specified contact.
            List<Contact> storedList = (ArrayList<Contact>) InternalStorage.readObject(InternalStorage.contacts);
            storedList.remove(contactPos);
            InternalStorage.writeObject(InternalStorage.contacts, storedList);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read from Internal Storage: ");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        adapter.notifyItemRemoved(contactPos);
        adapter.notifyItemRangeChanged(contactPos, adapter.contactsList.size());
        // Delete the specified contact from Firestore Database.
        firestoreDB.collection("users")
                .document(userId)
                .collection("contacts")
                .document(contact.getCID())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Deletion was successful.
                        Log.d(TAG, "*** Deleted contact successfully ***");
                        context.dismiss();
                        Snackbar.make(parentLayout,
                                contactName + " has been deleted",
                                Snackbar.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to delete contact - id: " + contact.getCID());
                        e.printStackTrace();
                    }
                });
    }

    public void setAdapter(ContactsRecyclerViewAdapter adapter) {
        this.adapter = adapter;
    }
}
