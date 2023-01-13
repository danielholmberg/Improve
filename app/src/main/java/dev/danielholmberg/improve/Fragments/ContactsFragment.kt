package dev.danielholmberg.improve.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import dev.danielholmberg.improve.Activities.AddContactActivity;
import dev.danielholmberg.improve.Models.Company;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Managers.DatabaseManager;
import dev.danielholmberg.improve.R;

/**
 * Created by DanielHolmberg on 2018-01-18.
 */

public class ContactsFragment extends Fragment{
    private static final String TAG = ContactsFragment.class.getSimpleName();

    private Improve app;
    private View view;
    private RecyclerView companyRecyclerView;
    private FloatingActionButton fab, addContactFAB, addCompanyFAB;
    private TextView addCompanyFABTextView, addContactFABTextView;
    private View snackbarView;
    private DatabaseManager databaseManager;
    private View emptyListView;
    private boolean isFABOpen = false;

    public ContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = Improve.getInstance();
        databaseManager = app.getDatabaseManager();
        app.setContactFragmentRef(this);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_contacts,
                container, false);

        app.getMainActivityRef().findViewById(R.id.toolbar_dropshadow).setVisibility(View.GONE);

        snackbarView = view.findViewById(R.id.contacts_fragment_container);

        companyRecyclerView = view.findViewById(R.id.company_recycler_view);
        emptyListView = view.findViewById(R.id.empty_contact_list_tv);

        fab = view.findViewById(R.id.fab_menu);
        addCompanyFABTextView = view.findViewById(R.id.add_company_fab_text);
        addContactFABTextView = view.findViewById(R.id.add_contact_fab_text);
        addContactFAB = view.findViewById(R.id.add_contact);
        addCompanyFAB = view.findViewById(R.id.add_company);

        LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(getActivity());
        companyRecyclerView.setLayoutManager(recyclerLayoutManager);

        initListScrollListener();
        initAdapter();

        initListDataChangeListener();

        return view;
    }

    private void initListDataChangeListener() {
        // Company listener
        databaseManager.getCompaniesRef().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(app.getCompanyRecyclerViewAdapter().getItemCount() > 0) {
                    companyRecyclerView.setVisibility(View.VISIBLE);
                    emptyListView.setVisibility(View.GONE);
                } else {
                    companyRecyclerView.setVisibility(View.GONE);
                    emptyListView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                final Company removedCompany = dataSnapshot.getValue(Company.class);

                if(removedCompany != null) {
                    Snackbar.make(snackbarView,
                            "Company deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    databaseManager.addCompany(removedCompany);
                                }
                            }).show();
                }

                if(app.getCompanyRecyclerViewAdapter().getItemCount() > 0) {
                    companyRecyclerView.setVisibility(View.VISIBLE);
                    emptyListView.setVisibility(View.GONE);
                } else {
                    companyRecyclerView.setVisibility(View.GONE);
                    emptyListView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void initListScrollListener() {
        // Add a OnScrollListener to change when to show the Floating Action Button for adding a new Note.
        companyRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && fab.isShown()) {
                    // Hide FABs when the user scrolls down.
                    closeFABMenu();
                    fab.hide();
                }

                if(!recyclerView.canScrollVertically(-1)) {
                    // we have reached the top of the list
                    app.getMainActivityRef().findViewById(R.id.toolbar_dropshadow).setVisibility(View.GONE);
                } else {
                    // we are not at the top yet
                    app.getMainActivityRef().findViewById(R.id.toolbar_dropshadow).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // Show FABs when the user has stopped scrolling.
                    fab.show();
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isFABOpen) {
                    closeFABMenu();
                } else {
                    showFABMenu();
                }
            }
        });
        addContactFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addContact();
                closeFABMenu();
            }
        });
        addCompanyFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addCompany();
                closeFABMenu();
            }
        });
    }

    private void addCompany() {
        View addCompanyDialogView = getLayoutInflater().inflate(R.layout.dialog_new_company, null, false);

        final EditText companyNameEditText = (EditText) addCompanyDialogView.findViewById(R.id.new_company_name_et);

        final AlertDialog addNewCompanyDialog = new AlertDialog.Builder(getContext())
                .setTitle("Add new company")
                .setView(addCompanyDialogView)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Dummy
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .create();
        addNewCompanyDialog.show();

        companyNameEditText.requestFocus();

        addNewCompanyDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String newCompanyName = companyNameEditText.getText().toString().toUpperCase();

                        if(!newCompanyName.isEmpty()) {
                            Company company;

                            if(app.getCompanyRecyclerViewAdapter().getCompaniesName().contains(newCompanyName)) {
                                companyNameEditText.setError("Company already exists!");
                                companyNameEditText.requestFocus();
                            } else {
                                String newCompanyId = databaseManager.getCompaniesRef().push().getKey();
                                company = new Company(newCompanyId, newCompanyName);
                                databaseManager.addCompany(company);
                                addNewCompanyDialog.dismiss();
                            }

                        } else {
                            companyNameEditText.setError("Please enter a company name");
                            companyNameEditText.requestFocus();
                        }
                    }
                });
    }

    private void showFABMenu(){
        isFABOpen = true;
        fab.animate().rotation(45).setDuration(300).start();

        if (app.getCompanyRecyclerViewAdapter().getItemCount() > 0) {
            // If a Company has already been added, make the FAB for adding a Contact visible.

            addContactFAB.show();
            addContactFAB.animate().translationY(-getResources().getDimension(R.dimen.fab_menu_item_position_1)).setDuration(300).withEndAction(new Runnable() {
                @Override
                public void run() {
                    addContactFABTextView.setVisibility(View.VISIBLE);
                    addContactFABTextView.animate().alpha(1f).setDuration(300);
                }
            });

            addCompanyFAB.show();
            addCompanyFAB.animate().translationY(-getResources().getDimension(R.dimen.fab_menu_item_position_2)).setDuration(300).withEndAction(new Runnable() {
                @Override
                public void run() {
                    addCompanyFABTextView.setVisibility(View.VISIBLE);
                    addCompanyFABTextView.animate().alpha(1f).setDuration(300);
                }
            });
        } else {
            // No Company has been added, hide the FAB for adding a Contact and switch position of Company FAB.
            addContactFAB.hide();
            addContactFABTextView.setVisibility(View.GONE);
            addContactFABTextView.setAlpha(0f);

            addCompanyFAB.show();
            addCompanyFAB.animate().translationY(-getResources().getDimension(R.dimen.fab_menu_item_position_1)).setDuration(300).withEndAction(new Runnable() {
                @Override
                public void run() {
                    addCompanyFABTextView.setVisibility(View.VISIBLE);
                    addCompanyFABTextView.animate().alpha(1f).setDuration(300);
                }
            });
        }

    }

    private void closeFABMenu(){
        isFABOpen = false;
        fab.animate().rotation(0).setDuration(300).start();

        addContactFAB.hide();
        addContactFABTextView.animate().alpha(0f).setDuration(300).withEndAction(new Runnable() {
            @Override
            public void run() {
                addContactFAB.animate().translationY(0).setDuration(300).withStartAction(new Runnable() {
                    @Override
                    public void run() {
                        addContactFABTextView.setVisibility(View.GONE);
                    }
                });
            }
        });

        addCompanyFAB.hide();
        addCompanyFABTextView.animate().alpha(0f).setDuration(300).withEndAction(new Runnable() {
            @Override
            public void run() {
                addCompanyFAB.animate().translationY(0).setDuration(300).withStartAction(new Runnable() {
                    @Override
                    public void run() {
                        addCompanyFABTextView.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    public void initAdapter() {
        companyRecyclerView.setAdapter(app.getCompanyRecyclerViewAdapter());
    }

    public void addContact() {
        Intent addContactIntent = new Intent(getContext(), AddContactActivity.class);
        startActivity(addContactIntent);
    }
}
