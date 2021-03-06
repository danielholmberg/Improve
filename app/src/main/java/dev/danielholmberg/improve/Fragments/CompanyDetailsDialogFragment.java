package dev.danielholmberg.improve.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Objects;

import dev.danielholmberg.improve.Activities.AddContactActivity;
import dev.danielholmberg.improve.Models.Company;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Managers.FirebaseDatabaseManager;
import dev.danielholmberg.improve.R;

public class CompanyDetailsDialogFragment extends DialogFragment {
    public static final String TAG = CompanyDetailsDialogFragment.class.getSimpleName();

    public static final String COMPANY_KEY = "company";

    private Improve app;
    private FirebaseDatabaseManager databaseManager;

    private AppCompatActivity activity;

    private Toolbar toolbar;

    private Bundle companyBundle;
    private Company company;

    private String companyId;
    private String companyNameId;
    private HashMap<String, Object> companyContacts;
    private TextView dialogTitle;
    private RecyclerView contactsRecyclerView;

    public static CompanyDetailsDialogFragment newInstance() {
        return new CompanyDetailsDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = Improve.getInstance();
        databaseManager = app.getFirebaseDatabaseManager();
        activity = (AppCompatActivity) getActivity();

        companyBundle = getArguments();

        if(companyBundle != null) {
            company = (Company) companyBundle.getParcelable(COMPANY_KEY);
        } else {
            Toast.makeText(activity, "Failed to show Company details, please try again",
                    Toast.LENGTH_SHORT).show();
            dismissDialog();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_company_details, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar = (Toolbar) view.findViewById(R.id.toolbar_company_details_fragment);
        createOptionsMenu();
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete_company:
                        showDeleteCompanyDialog();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        dialogTitle = (TextView) view.findViewById(R.id.toolbar_company_title_tv);
        contactsRecyclerView = (RecyclerView) view.findViewById(R.id.contacts_recyclerview);

        if(company != null) {
            populateCompanyDetails();
        } else {
            Toast.makeText(activity, "Unable to show Note details", Toast.LENGTH_SHORT).show();
            dismissDialog();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Objects.requireNonNull(getDialog().getWindow())
                .setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    private void createOptionsMenu() {
        Menu menu = toolbar.getMenu();
        menu.clear();

        toolbar.inflateMenu(R.menu.fragment_company_details);
        toolbar.findViewById(R.id.close_dialog_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissDialog();
            }
        });
    }

    /**
     * Sets all the necessary detailed information about the Note.
     */
    private void populateCompanyDetails() {
        companyId = company.getId();
        companyNameId = company.getName();
        companyContacts = company.getContacts();

        Log.d(TAG, "Company.contacts: "+companyContacts);

        if(companyNameId != null) {
            dialogTitle.setText(companyNameId);
        }

        if(companyContacts != null) {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(app, RecyclerView.VERTICAL, false);
            contactsRecyclerView.setLayoutManager(linearLayoutManager);
            contactsRecyclerView.setAdapter(app.getCompanyContactsAdapter(companyId));
        }
    }

    private void showDeleteCompanyDialog() {
        AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(activity).setTitle(R.string.dialog_delete_company_title)
                        .setMessage(R.string.dialog_delete_company_msg)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteCompany(company);
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

    private void deleteCompany(final Company company) {
        if(getActivity() != null) {
            databaseManager.deleteCompany(company);
            Snackbar.make(getActivity().findViewById(R.id.contacts_fragment_container), "Deleted company: " + company.getName(), Snackbar.LENGTH_LONG)
                    .setAction("UNDO", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            databaseManager.addCompany(company);
                        }
                    });
        }
        dismissDialog();
    }

    private void dismissDialog() {
        this.dismiss();
    }
}
