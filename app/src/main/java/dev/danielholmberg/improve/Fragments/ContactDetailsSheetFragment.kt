package dev.danielholmberg.improve.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.api.services.drive.DriveScopes;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;

import dev.danielholmberg.improve.Activities.AddContactActivity;
import dev.danielholmberg.improve.Models.Contact;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Managers.DatabaseManager;
import dev.danielholmberg.improve.R;
import dev.danielholmberg.improve.Services.DriveServiceHelper;

/**
 * Created by DanielHolmberg on 2018-02-21.
 */

public class ContactDetailsSheetFragment extends BottomSheetDialogFragment implements View.OnClickListener{
    private static final String TAG = ContactDetailsSheetFragment.class.getSimpleName();
    public static final String CONTACT_KEY = "contact";
    public static final String PARENT_FRAGMENT_KEY = "parentFragment";

    private static final int REQUEST_PERMISSION_SUCCESS_CONTINUE_FILE_CREATION = 999;

    private Improve app;
    private DatabaseManager databaseManager;
    private Context context;
    private DriveServiceHelper mDriveServiceHelper;

    private Bundle contactBundle;
    private Contact contact;

    private ContactDetailsSheetFragment detailsDialog;
    private View view;
    private int parentFragment;

    private Toolbar toolbar;
    private TextView title, name, email, mobile, comment;
    private String timestampAdded, timestampUpdated;

    private AppCompatActivity activity;
    private ProgressDialog exportDialog;

    public ContactDetailsSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = Improve.getInstance();
        databaseManager = app.getDatabaseManager();
        detailsDialog = this;
        context = getContext();
        activity = (AppCompatActivity) getActivity();
        mDriveServiceHelper = app.getDriveServiceHelper();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_contact_details, container, false);

        toolbar = view.findViewById(R.id.toolbar_contact_details_fragment);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.contactEdit:
                        detailsDialog.dismiss();
                        Intent updateContact = new Intent(getContext(), AddContactActivity.class);
                        updateContact.putExtra(AddContactActivity.CONTACT_BUNDLE_KEY, contactBundle);
                        startActivity(updateContact);
                        return true;
                    case R.id.contactDelete:
                        showDeleteContactDialog();
                        return true;
                    case R.id.contactInfo:
                        showInfoDialog();
                        return true;
                    case R.id.contactExport:
                        checkDrivePermission();
                        return true;
                    default:
                        return true;
                }
            }
        });
        toolbar.inflateMenu(R.menu.fragment_contact_details_show);

        Button actionCallContact = (Button) view.findViewById(R.id.details_call_contact_btn);
        Button actionSendMailToContact = (Button) view.findViewById(R.id.details_mail_contact_btn);

        contactBundle =  this.getArguments();

        if(contactBundle != null) {
            parentFragment = contactBundle.getInt(PARENT_FRAGMENT_KEY);
            contact = (Contact) contactBundle.getParcelable(CONTACT_KEY);
        } else {
            Toast.makeText(context, "Unable to show contact details", Toast.LENGTH_SHORT).show();
            detailsDialog.dismiss();
        }

        title = (TextView) view.findViewById(R.id.toolbar_contact_details_company_tv);
        name = (TextView) view.findViewById(R.id.contact_details_name_tv);
        email = (TextView) view.findViewById(R.id.contact_details_email_tv);
        mobile = (TextView) view.findViewById(R.id.contact_details_mobile_tv);
        comment = (TextView) view.findViewById(R.id.contact_details_comment_tv);

        if(contact != null){
            name.setText(contact.getName());
            email.setText(contact.getEmail());
            mobile.setText(contact.getPhone());
            comment.setText(contact.getComment());
            comment.setMovementMethod(new ScrollingMovementMethod());

            if(contact.getTimestampAdded() != null) {
                timestampAdded = tranformMillisToDateSring(Long.parseLong(contact.getTimestampAdded()));
            }

            if(contact.getTimestampUpdated() != null) {
                timestampUpdated = tranformMillisToDateSring(Long.parseLong(contact.getTimestampUpdated()));
            }

            if(contact.getCompanyId() != null) {
                if (app.getCompanyRecyclerViewAdapter().getCompany(contact.getCompanyId()) != null) {
                    title.setText(app.getCompanyRecyclerViewAdapter().getCompany(contact.getCompanyId()).getName());
                }
            }

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
            Toast.makeText(context, "Unable to show contact details", Toast.LENGTH_SHORT).show();
        }

        actionCallContact.setOnClickListener(this);
        actionSendMailToContact.setOnClickListener(this);

        // Inflate the layout for this fragment
        return view;
    }

    private void exportContactToDrive(Contact contact) {
        Log.d(TAG, "Exporting contact (" + contact.getId() + " to Google Drive...");

        if (mDriveServiceHelper != null) {
            Log.d(TAG, "Creating a file.");

            exportDialog = ProgressDialog.show(context, "Exporting Contact to Google Drive",
                    "In progress...", true);

            exportDialog.show();

            Log.d(TAG, "Contact exported info: " + contact.toString());

            mDriveServiceHelper.createFile(DriveServiceHelper.TYPE_CONTACT, contact.getName(), contact.toString())
                    .addOnSuccessListener(new OnSuccessListener<String>() {
                        @Override
                        public void onSuccess(String id) {
                            Log.d(TAG, "Created file");
                            exportDialog.cancel();
                            dismiss();
                            Toast.makeText(app, "Contact exported", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Couldn't create file.", e);
                            exportDialog.cancel();
                            Toast.makeText(app, "Failed to export Contact", Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            Log.e(TAG, "DriveServiceHelper wasn't initialized.");
            Toast.makeText(app, "Failed to export Contact", Toast.LENGTH_LONG).show();
        }
    }

    private void checkDrivePermission() {
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(app), new Scope(DriveScopes.DRIVE_FILE))) {

            GoogleSignIn.requestPermissions(
                    app.getContactsFragmentRef(),
                    REQUEST_PERMISSION_SUCCESS_CONTINUE_FILE_CREATION,
                    GoogleSignIn.getLastSignedInAccount(app), new Scope(DriveScopes.DRIVE_FILE));
        } else {
            exportContactToDrive(contact);
        }
    }

    private void deleteContact(final Contact contact) {
        databaseManager.deleteContact(contact);
        detailsDialog.dismiss();
    }

    private String tranformMillisToDateSring(long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);

        return DateFormat.getDateTimeInstance().format(calendar.getTime());
    }

    private void showInfoDialog() {
        RelativeLayout contactInfoLayout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.dialog_contact_info, null);
        TextView contactAddedTimestamp = contactInfoLayout.findViewById(R.id.contact_info_added_timestamp_tv);
        TextView contactUpdatedTimestamp = contactInfoLayout.findViewById(R.id.contact_info_updated_timestamp_tv);

        contactAddedTimestamp.setText(timestampAdded);
        contactUpdatedTimestamp.setText(timestampUpdated);

        AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(context).setTitle(R.string.dialog_info_contact_title)
                        .setIcon(R.drawable.ic_menu_info_primary)
                        .setView(contactInfoLayout)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
        final AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
    }

    private void showDeleteContactDialog() {
        AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(context).setTitle(R.string.dialog_delete_contact_title)
                        .setMessage(R.string.dialog_delete_contact_msg)
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
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
            default:
                break;
        }
    }
}
