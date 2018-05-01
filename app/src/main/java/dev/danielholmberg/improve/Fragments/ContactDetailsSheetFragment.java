package dev.danielholmberg.improve.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
 * Class ${CLASS}
 */

public class ContactDetailsSheetFragment extends BottomSheetDialogFragment implements View.OnClickListener{
    private static final String TAG = ContactDetailsSheetFragment.class.getSimpleName();

    private Improve app;
    private FirebaseStorageManager storageManager;

    private Contact contact;
    private int contactPos;
    private Bundle contactBundle;

    private ContactDetailsSheetFragment detailsDialog;
    private View view;
    private ViewGroup parentLayout;

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
        parentLayout = getActivity().findViewById(R.id.main_fragment_container);

        Button actionCallContact = (Button) view.findViewById(R.id.details_call_contact_btn);
        Button actionSendMailToContact = (Button) view.findViewById(R.id.details_mail_contact_btn);

        RelativeLayout toolbar = (RelativeLayout) view.findViewById(R.id.toolbar_contact_details);
        TextView title = (TextView) view.findViewById(R.id.toolbar_contact_details_company_tv);
        TextView name = (TextView) view.findViewById(R.id.contact_details_name_tv);
        TextView email = (TextView) view.findViewById(R.id.contact_details_email_tv);
        TextView mobile = (TextView) view.findViewById(R.id.contact_details_mobile_tv);
        TextView comment = (TextView) view.findViewById(R.id.contact_details_comment_tv);

        contactBundle =  this.getArguments();
        contact = (Contact) contactBundle.getSerializable("contact");

        if(contactBundle != null){
            contactPos = contactBundle.getInt("position");
        }
        if(contact != null){
            int contactColor = getResources().getColor(R.color.colorPickerDeepOrange);
            int titleColor = getResources().getColor(R.color.titleColorDeepOrange);

            if(contact.getColor() != null) {
                contactColor = Color.parseColor(contact.getColor());
                if(contactColor == getResources().getColor(R.color.colorPickerGreen))
                    titleColor = getResources().getColor(R.color.titleColorGreen);
                else if(contactColor == getResources().getColor(R.color.colorPickerLightGreen))
                    titleColor = getResources().getColor(R.color.titleColorLightGreen);
                else if(contactColor == getResources().getColor(R.color.colorPickerAmber))
                    titleColor = getResources().getColor(R.color.titleColorAmber);
                else if(contactColor == getResources().getColor(R.color.colorPickerDeepOrange))
                    titleColor = getResources().getColor(R.color.titleColorDeepOrange);
                else if(contactColor == getResources().getColor(R.color.colorPickerBrown))
                    titleColor = getResources().getColor(R.color.titleColorBrown);
                else if(contactColor == getResources().getColor(R.color.colorPickerBlueGrey))
                    titleColor = getResources().getColor(R.color.titleColorBlueGrey);
                else if(contactColor == getResources().getColor(R.color.colorPickerTurquoise))
                    titleColor = getResources().getColor(R.color.titleColorTurquoise);
                else if(contactColor == getResources().getColor(R.color.colorPickerPink))
                    titleColor = getResources().getColor(R.color.titleColorPink);
                else if(contactColor == getResources().getColor(R.color.colorPickerDeepPurple))
                    titleColor = getResources().getColor(R.color.titleColorDeepPurple);
                else if(contactColor == getResources().getColor(R.color.colorPickerIndigo))
                    titleColor = getResources().getColor(R.color.titleColorIndigo);
            }

            toolbar.setBackgroundColor(contactColor);
            title.setText(contact.getCompany());
            title.setTextColor(titleColor);
            name.setText(contact.getName());
            email.setText(contact.getEmail());
            mobile.setText(contact.getPhone());
            comment.setText(contact.getComment());

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
                Snackbar.make(parentLayout, "Deleted contact", Snackbar.LENGTH_LONG)
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
                                        Log.e(TAG, "Failed to undo 'Delete contact': "+ errorMessage);
                                    }
                                });
                            }
                        }).show();
            }

            @Override
            public void onFailure(String errorMessage) {
                Snackbar.make(parentLayout, "Failed to delete contact", Snackbar.LENGTH_LONG)
                        .setAction("RETRY", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                deleteContact(contact);
                            }
                        }).show();
            }
        });
        detailsDialog.dismiss();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.edit_contact_btn:
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
                AlertDialog.Builder alertDialogBuilder =
                        new AlertDialog.Builder(getContext()).setTitle("Delete contact")
                                .setMessage("Do you really want to delete: " + contact.getName())
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
                break;
            default:
                break;
        }
    }
}
