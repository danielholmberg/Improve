package dev.danielholmberg.improve.Fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import dev.danielholmberg.improve.Activities.AddContactActivity;
import dev.danielholmberg.improve.Components.Contact;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Managers.FirebaseStorageManager;
import dev.danielholmberg.improve.R;

/**
 * Created by DanielHolmberg on 2018-01-18.
 */

public class ContactsFragment extends Fragment{
    private static final String TAG = "ContactsFragment";

    private Improve app;
    private FirebaseStorageManager storageManager;

    private View view;
    private RecyclerView contactsRecyclerView;
    private TextView emptyListText;
    private FloatingActionButton fab;

    private FirebaseRecyclerAdapter recyclerAdapter;
    private Query query;

    public ContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = Improve.getInstance();
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
        contactsRecyclerView = (RecyclerView) view.findViewById(R.id.contacts_list);
        emptyListText = (TextView) view.findViewById(R.id.empty_contact_list_tv);
        fab = (FloatingActionButton) view.findViewById(R.id.add_contact);

        // Initialize the LinearLayoutManager
        LinearLayoutManager recyclerLayoutManager =
                new LinearLayoutManager(getActivity().getApplicationContext());
        contactsRecyclerView.setLayoutManager(recyclerLayoutManager);

        // Setting RecyclerAdapter to RecyclerList.
        initAdapter();
        contactsRecyclerView.setAdapter(recyclerAdapter);

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
                // Handle action add a new Contact.
                addContact();
            }
        });

        return view;
    }

    private void initAdapter() {
        query = storageManager.getContactsRef().orderByChild("company");

        FirebaseRecyclerOptions<Contact> options =
                new FirebaseRecyclerOptions.Builder<Contact>()
                        .setQuery(query, Contact.class)
                        .build();

        recyclerAdapter = new FirebaseRecyclerAdapter<Contact, ContactViewHolder>(options) {
            @Override
            public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_contact, parent, false);

                return new ContactViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ContactViewHolder holder, int position, @NonNull Contact model) {
                holder.bindModelToView(model);
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        recyclerAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        recyclerAdapter.stopListening();
    }

    /**
     * Called when a user clicks on the Floating Action Button to add a new Contact.
     */
    public void addContact() {
        Intent addContactIntent = new Intent(getContext(), AddContactActivity.class);
        startActivity(addContactIntent);
    }

    /**
     * ViewHolder class for each RecyclerList item.
     */
    public class ContactViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private View mView;
        private Context context;

        private Contact contact;

        public ContactViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            context = itemView.getContext();
        }

        public void bindModelToView(final Contact contact) {
            this.contact = contact;

            Button callBtn = (Button) mView.findViewById(R.id.call_contact_btn);
            Button mailBtn = (Button) mView.findViewById(R.id.mail_contact_btn);

            ((TextView) mView.findViewById(R.id.name_tv)).setText(contact.getName());
            ((TextView) mView.findViewById(R.id.company_tv)).setText(contact.getCompany());

            if (contact.getEmail().isEmpty()) {
                mailBtn.setBackground(context.getResources().getDrawable(R.drawable.ic_contact_email_grey));
                mailBtn.setEnabled(false);
            }

            if (contact.getPhone().isEmpty()) {
                callBtn.setBackground(context.getResources().getDrawable(R.drawable.ic_contact_mobile_grey));
                callBtn.setEnabled(false);
            }

            callBtn.setOnClickListener(this);
            mailBtn.setOnClickListener(this);

            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DatabaseReference ref = Improve.getInstance().getFirebaseStorageManager().getContactsRef()
                            .child(contact.getId());
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Contact contact = dataSnapshot.getValue(Contact.class);
                            int itemPosition = getAdapterPosition();

                            Bundle bundle = createBundle(contact, itemPosition);
                            ContactDetailsSheetFragment contactDetailsSheetFragment = new ContactDetailsSheetFragment();
                            contactDetailsSheetFragment.setArguments(bundle);
                            contactDetailsSheetFragment.show(((AppCompatActivity)context).getSupportFragmentManager(),
                                    contactDetailsSheetFragment.getTag());
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            });
        }

        private Bundle createBundle(Contact contact, int itemPos) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("contact", contact);
            bundle.putInt("position", itemPos);
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
