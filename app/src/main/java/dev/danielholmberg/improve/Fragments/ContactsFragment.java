package dev.danielholmberg.improve.Fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

import java.util.ArrayList;
import java.util.List;

import dev.danielholmberg.improve.Activities.AddContactActivity;
import dev.danielholmberg.improve.Components.CompanyList;
import dev.danielholmberg.improve.Components.Contact;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Managers.FirebaseStorageManager;
import dev.danielholmberg.improve.R;

/**
 * Created by DanielHolmberg on 2018-01-18.
 */

public class ContactsFragment extends Fragment{
    private static final String TAG = ContactsFragment.class.getSimpleName();

    private Improve app;
    private FirebaseStorageManager storageManager;

    private View view;
    private RecyclerView contactsRecyclerView;
    private DocExpandableRecyclerAdapter adapter;
    private TextView emptyListText;
    private FloatingActionButton fab;

    private String contactOrderBy = "color";
    private FirebaseRecyclerAdapter recyclerAdapter;
    private Query query;

    public ContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = Improve.getInstance();
        app.setContactFragmentRef(this);
        storageManager = app.getFirebaseStorageManager();
        // Enable the OptionsMenu to show the SearchView.
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_contacts,
                container, false);

        // Initialize View components to be used.
        contactsRecyclerView = view.findViewById(R.id.contacts_list);
        emptyListText = view.findViewById(R.id.empty_contact_list_tv);
        fab = view.findViewById(R.id.add_contact);

        // Initialize the LinearLayoutManager
        LinearLayoutManager recyclerLayoutManager =
                new LinearLayoutManager(getActivity().getApplicationContext());
        contactsRecyclerView.setLayoutManager(recyclerLayoutManager);

        // Setting RecyclerAdapter to RecyclerList.
        setUpAdapter();

        // Add a OnScrollListener to change when to show the Floating Action Button for adding a new Note.
        contactsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                if (dy>0 && fab.isShown())
                    // Hide the FAB when the user scrolls down.
                    fab.hide();
                if(dy<0 && !fab.isShown())
                    // Show the FAB when the user scrolls up.
                    fab.show();
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addContact();
            }
        });

        return view;
    }

    private void setUpAdapter() {
        storageManager.getContactsRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final List<CompanyList> Companies = new ArrayList<>();

                // For Each Company.
                for(final DataSnapshot Company: dataSnapshot.getChildren()) {
                    final List<Contact> CompanyContacts = new ArrayList<>();

                    // For Each ContactRef
                    for(final DataSnapshot ContactRef: Company.getChildren()) {
                        final String contactKey = ContactRef.getKey();

                        // Retrieve the Contact-values for corresponding ContactKey.
                        storageManager.getContactsRef().child(Company.getKey()).child(contactKey)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        final Contact contact = dataSnapshot.getValue(Contact.class);
                                        CompanyContacts.add(contact);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        System.err.print("Failed to read Contact values. " + databaseError.toString());
                                    }
                                });
                    }

                    // DONE retrieving all Contacts related to current Company.
                    Companies.add(new CompanyList(Company.getKey(), CompanyContacts));
                    adapter = new DocExpandableRecyclerAdapter(Companies);
                    contactsRecyclerView.setAdapter(adapter);
                }

                if(dataSnapshot.hasChildren()) {
                    contactsRecyclerView.setVisibility(View.VISIBLE);
                    emptyListText.setVisibility(View.GONE);
                } else {
                    contactsRecyclerView.setVisibility(View.GONE);
                    emptyListText.setVisibility(View.VISIBLE);
                }

                fab.show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.print("Failed to read Company values. " + databaseError.toString());
            }
        });

        contactsRecyclerView.setAdapter(adapter);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_contacts, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_contacts_by_company_alphabetical:
                sortContactsByCompany();
                return true;
            case R.id.sort_contacts_by_marker:
                sortContactsByMarker();
                return true;
            default:
                break;
        }
        return false;
    }

    private void sortContactsByMarker() {
        Toast.makeText(app, "Sorted by marker color", Toast.LENGTH_SHORT).show();
    }

    private void sortContactsByCompany() {
        Toast.makeText(app, "Sorted by company", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void addContact() {
        Intent addContactIntent = new Intent(getContext(), AddContactActivity.class);
        startActivity(addContactIntent);
    }

    /**
     * ExpandableRecyclerAdapter to show a list of Companies where each Company possesses
     * children in form of contacts.
     */
    public class DocExpandableRecyclerAdapter extends ExpandableRecyclerViewAdapter<CompanyGroupViewHolder, ContactViewHolder> {


        public DocExpandableRecyclerAdapter(List<CompanyList> groups) {
            super(groups);
        }

        @Override
        public CompanyGroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_company, parent, false);
            return new CompanyGroupViewHolder(view);
        }

        @Override
        public ContactViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
            return new ContactViewHolder(view);
        }

        @Override
        public void onBindChildViewHolder(ContactViewHolder holder, int flatPosition, ExpandableGroup group, int childIndex) {
            final Contact contact = ((CompanyList) group).getItems().get(childIndex);
            Log.d(TAG, "contact: " + contact.getName());
            holder.bindModelToView(contact);
        }

        @Override
        public void onBindGroupViewHolder(CompanyGroupViewHolder holder, int flatPosition, final ExpandableGroup group) {
            holder.setParentTitle(group);
        }

    }

    /**
     * ViewHolder class for each CompanyList view.
     */
    public class CompanyGroupViewHolder extends GroupViewHolder {

        public TextView companyName;

        public CompanyGroupViewHolder(View companyView) {
            super(companyView);
            companyName = companyView.findViewById(R.id.company_list_tv);
        }

        public void setParentTitle(ExpandableGroup group) {
            companyName.setText(group.getTitle());
        }

    }

    /**
     * ViewHolder class for each Contact item.
     */
    public class ContactViewHolder extends ChildViewHolder implements View.OnClickListener{

        private View mView;
        private Context context;

        private Contact contact;

        public ContactViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            context = itemView.getContext();
        }

        // OBS! Due to RecyclerView:
        // We need to define all views of each contact!
        // Otherwise each contact view won't be unique.
        public void bindModelToView(final Contact contact) {
            this.contact = contact;

            // [START] All views of a contact
            Button callBtn = mView.findViewById(R.id.call_contact_btn);
            Button mailBtn = mView.findViewById(R.id.mail_contact_btn);

            TextView name = mView.findViewById(R.id.name_tv);

            LinearLayout marker = mView.findViewById(R.id.item_contact_marker);
            // [END] All views of a note

            // [START] Define each view
            name.setText(contact.getName());

            try {
                marker.setBackgroundColor(contact.getColor() != null ? Color.parseColor(contact.getColor()) :
                        getResources().getColor(R.color.noColor));
            } catch (Exception e) {
                marker.setBackgroundColor(getResources().getColor(R.color.noColor));
            }

            if (contact.getEmail() == null || contact.getEmail().isEmpty()) {
                mailBtn.setBackground(context.getResources().getDrawable(R.drawable.ic_contact_email_grey));
                mailBtn.setEnabled(false);
            } else {
                mailBtn.setBackground(context.getResources().getDrawable(R.drawable.ic_contact_email_active));
                mailBtn.setEnabled(true);
            }
            mailBtn.setOnClickListener(this);

            if (contact.getPhone() == null || contact.getPhone().isEmpty()) {
                callBtn.setBackground(context.getResources().getDrawable(R.drawable.ic_contact_mobile_grey));
                callBtn.setEnabled(false);
            } else {
                callBtn.setBackground(context.getResources().getDrawable(R.drawable.ic_contact_mobile_active));
                callBtn.setEnabled(true);
            }
            callBtn.setOnClickListener(this);
            // [END] Define each view

            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int itemPosition = getAdapterPosition();

                    Bundle bundle = createBundle(contact, itemPosition);
                    ContactDetailsSheetFragment contactDetailsSheetFragment = new ContactDetailsSheetFragment();
                    contactDetailsSheetFragment.setArguments(bundle);
                    contactDetailsSheetFragment.show(((AppCompatActivity)context).getSupportFragmentManager(),
                            contactDetailsSheetFragment.getTag());
                }
            });
        }

        private Bundle createBundle(Contact contact, int itemPos) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("contact", contact);
            bundle.putInt("position", itemPos);
            bundle.putInt("parentFragment", R.integer.CONTACT_FRAGMENT);
            return bundle;
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.call_contact_btn:
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" + contact.getPhone()));
                    context.startActivity(callIntent);
                    break;
                case R.id.mail_contact_btn:
                    Intent mailIntent = new Intent(Intent.ACTION_SENDTO);
                    mailIntent.setData(Uri.parse("mailto:" + contact.getEmail()));
                    context.startActivity(mailIntent);
                    break;
                default:
                    break;
            }
        }
    }
}
