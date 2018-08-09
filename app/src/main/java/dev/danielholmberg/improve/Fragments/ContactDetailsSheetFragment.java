package dev.danielholmberg.improve.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import dev.danielholmberg.improve.Activities.AddContactActivity;
import dev.danielholmberg.improve.Callbacks.FirebaseStorageCallback;
import dev.danielholmberg.improve.Components.Contact;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Managers.FirebaseStorageManager;
import dev.danielholmberg.improve.R;

/**
 * Created by DanielHolmberg on 2018-02-21.
 */

public class ContactDetailsSheetFragment extends BottomSheetDialogFragment implements View.OnClickListener{
    private static final String TAG = ContactDetailsSheetFragment.class.getSimpleName();

    private Improve app;
    private FirebaseStorageManager storageManager;

    private Bundle contactBundle;
    private Contact contact;

    private ContactDetailsSheetFragment detailsDialog;
    private View view;
    private int parentFragment;

    private RelativeLayout toolbar;
    private LinearLayout marker;
    private String markerColor;
    private TextView title, name, email, mobile, comment;

    private View targetView;

    public ContactDetailsSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = Improve.getInstance();
        storageManager = app.getFirebaseStorageManager();
        detailsDialog = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_contact_details, container, false);

        Button actionCallContact = (Button) view.findViewById(R.id.details_call_contact_btn);
        Button actionSendMailToContact = (Button) view.findViewById(R.id.details_mail_contact_btn);

        toolbar = (RelativeLayout) view.findViewById(R.id.toolbar_contact_details);

        contactBundle =  this.getArguments();

        if(contactBundle != null) {
            parentFragment = contactBundle.getInt("parentFragment");
            contact = (Contact) contactBundle.getSerializable("contact");
        } else {
            Toast.makeText(getContext(), "Unable to show contact details", Toast.LENGTH_SHORT).show();
            detailsDialog.dismiss();
        }

        title = (TextView) view.findViewById(R.id.toolbar_contact_details_company_tv);
        name = (TextView) view.findViewById(R.id.contact_details_name_tv);
        email = (TextView) view.findViewById(R.id.contact_details_email_tv);
        mobile = (TextView) view.findViewById(R.id.contact_details_mobile_tv);
        comment = (TextView) view.findViewById(R.id.contact_details_comment_tv);

        marker = (LinearLayout) view.findViewById(R.id.include_item_marker);

        if(contact != null){
            markerColor = contact.getColor();

            name.setText(contact.getName());
            email.setText(contact.getEmail());
            mobile.setText(contact.getPhone());
            comment.setText(contact.getComment());
            comment.setMovementMethod(new ScrollingMovementMethod());

            if (markerColor != null && !markerColor.isEmpty()) {
                GradientDrawable marker_shape = (GradientDrawable) marker.getBackground();
                marker_shape.setColor(Color.parseColor(markerColor));
            }
            title.setText(contact.getCompany());


            // Handle if the voluntary contact information fields is empty
            // Change e-mail field
            if(contact.getEmail() != null) {
                if (contact.getEmail().isEmpty()) {
                    // Change text and disable mail action
                    email.setText(getString(R.string.contact_details_empty_email_text));
                    email.setTextColor(getResources().getColor(R.color.contact_form_icon));
                    actionSendMailToContact.setEnabled(false);
                    actionSendMailToContact.setTextColor(getResources().getColor(R.color.contact_form_icon));
                    actionSendMailToContact.setCompoundDrawablesWithIntrinsicBounds(
                            getResources().getDrawable(R.drawable.ic_contact_email_grey),
                            null, null, null);
                }
            } else {
                email.setText(getString(R.string.contact_details_empty_email_text));
                email.setTextColor(getResources().getColor(R.color.contact_form_icon));
                actionSendMailToContact.setEnabled(false);
                actionSendMailToContact.setTextColor(getResources().getColor(R.color.contact_form_icon));
                actionSendMailToContact.setCompoundDrawablesWithIntrinsicBounds(
                        getResources().getDrawable(R.drawable.ic_contact_email_grey),
                        null, null, null);
            }
            // Change phone field
            if(contact.getPhone() != null) {
                if (contact.getPhone().isEmpty()) {
                    // Change text and disable call action
                    mobile.setText(getString(R.string.contact_details_empty_mobile_text));
                    mobile.setTextColor(getResources().getColor(R.color.contact_form_icon));
                    actionCallContact.setEnabled(false);
                    actionCallContact.setTextColor(getResources().getColor(R.color.contact_form_icon));
                    actionCallContact.setCompoundDrawablesWithIntrinsicBounds(
                            getResources().getDrawable(R.drawable.ic_contact_mobile_grey),
                            null, null, null);
                }
            } else {
                mobile.setText(getString(R.string.contact_details_empty_mobile_text));
                mobile.setTextColor(getResources().getColor(R.color.contact_form_icon));
                actionCallContact.setEnabled(false);
                actionCallContact.setTextColor(getResources().getColor(R.color.contact_form_icon));
                actionCallContact.setCompoundDrawablesWithIntrinsicBounds(
                        getResources().getDrawable(R.drawable.ic_contact_mobile_grey),
                        null, null, null);
            }
            // Change comment field
            if(contact.getComment() != null) {
                if (contact.getComment().isEmpty()) {
                    // Change text
                    comment.setText(getString(R.string.contact_details_empty_comment_text));
                    comment.setTextColor(getResources().getColor(R.color.contact_form_icon));
                }
            } else {
                comment.setText(getString(R.string.contact_details_empty_comment_text));
                comment.setTextColor(getResources().getColor(R.color.contact_form_icon));
            }
        } else {
            // Dismiss dialog and show Toast.
            this.dismiss();
            Toast.makeText(getContext(), "Unable to show contact details", Toast.LENGTH_SHORT).show();
        }

        view.findViewById(R.id.edit_contact_btn).setOnClickListener(this);
        view.findViewById(R.id.delete_contact_btn).setOnClickListener(this);
        actionCallContact.setOnClickListener(this);
        actionSendMailToContact.setOnClickListener(this);

        // Inflate the layout for this fragment
        return view;
    }

    private void deleteContact(final Contact contact) {
        storageManager.deleteContact(contact, new FirebaseStorageCallback() {
            @Override
            public void onSuccess() {
                boolean error = false;

                if(parentFragment == R.integer.CONTACT_FRAGMENT) {
                    targetView = app.getContactsFragmentRef().getView().findViewById(R.id.contacts_fragment_container);
                } else {
                    error = true;
                }

                if(!error) {
                    Snackbar.make(app.getContactsFragmentRef().getView().findViewById(R.id.contacts_fragment_container), "Deleted contact", Snackbar.LENGTH_LONG)
                            .setAction("UNDO", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    storageManager.writeContactToFirebase(contact, new FirebaseStorageCallback() {
                                        @Override
                                        public void onSuccess() {
                                            Log.d(TAG, "*** Successfully undid 'Delete contact' ***");
                                        }

                                        @Override
                                        public void onFailure(String errorMessage) {
                                            Log.e(TAG, "Failed to undo 'Delete contact': " + errorMessage);
                                        }
                                    });
                                }
                            }).show();
                } else {
                    Toast.makeText(getContext(), "Failed to delete note",
                            Toast.LENGTH_SHORT).show();
                    detailsDialog.dismiss();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                boolean error = false;

                if(parentFragment == R.integer.ARCHIVED_NOTES_FRAGMENT) {
                    targetView = app.getArchivedNotesFragmentRef().getView().findViewById(R.id.archivednote_fragment_container);
                } else if(parentFragment == R.integer.NOTES_FRAGMENT){
                    targetView = app.getNotesFragmentRef().getView().findViewById(R.id.note_fragment_container);
                } else {
                    error = true;
                }

                if(!error) {
                    Snackbar.make(targetView,
                            "Failed to delete contact", Snackbar.LENGTH_LONG)
                            .setAction("RETRY", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    deleteContact(contact);
                                }
                            }).show();
                } else {
                    detailsDialog.dismiss();
                }
            }
        });

        detailsDialog.dismiss();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.edit_contact_btn:
                this.dismiss();
                Intent updateContact = new Intent(getContext(), AddContactActivity.class);
                updateContact.putExtra("contactBundle", contactBundle);
                startActivity(updateContact);
                break;
            case R.id.details_call_contact_btn:
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + contact.getPhone()));
                startActivity(callIntent);
                break;
            case R.id.details_mail_contact_btn:
                Intent mailIntent = new Intent(Intent.ACTION_SENDTO);
                mailIntent.setData(Uri.parse("mailto:" + contact.getEmail()));
                startActivity(mailIntent);
                break;
            case R.id.delete_contact_btn:
                showDeleteContactDialog();
                break;
            default:
                break;
        }
    }

    private void showDeleteContactDialog() {
        AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(getContext()).setTitle("Permanently delete contact")
                        .setMessage("Do you want to delete this contact?")
                        .setIcon(R.drawable.ic_menu_delete_grey)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteContact(contact);
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        final AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
    }
}
