package dev.danielholmberg.improve.Adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.SortedList;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dev.danielholmberg.improve.Models.Company;
import dev.danielholmberg.improve.Models.Contact;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Managers.FirebaseDatabaseManager;
import dev.danielholmberg.improve.Models.Note;
import dev.danielholmberg.improve.R;
import dev.danielholmberg.improve.ViewHolders.ContactViewHolder;

public class ContactRecyclerViewAdapter extends RecyclerView.Adapter<ContactViewHolder> {
    private static final String TAG = ContactRecyclerViewAdapter.class.getSimpleName();

    private Improve app;
    private FirebaseDatabaseManager databaseManager;

    private final Company company;
    private SortedList<Contact> contacts;

    public ContactRecyclerViewAdapter(Company company) {
        this.app = Improve.getInstance();
        this.databaseManager = app.getFirebaseDatabaseManager();
        this.company = company;

        contacts = new SortedList<>(Contact.class, new SortedList.Callback<Contact>() {
            @Override
            public int compare(Contact o1, Contact o2) {
                // Sorts the list depending on the compared attribute
                // Uses .toUpperCase() due to UTF-8 value difference between Uppercase and Lowercase letters.
                return o1.getName().toUpperCase().compareTo(o2.getName().toUpperCase());
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemChanged(position, count);
            }

            @Override
            public boolean areContentsTheSame(Contact oldItem, Contact newItem) {
                return oldItem.getName().equals(newItem.getName())
                        && oldItem.getCompanyId().equals(newItem.getCompanyId())
                        && oldItem.getEmail().equals(newItem.getEmail())
                        && oldItem.getPhone().equals(newItem.getPhone())
                        && oldItem.getComment().equals(newItem.getComment());
            }

            @Override
            public boolean areItemsTheSame(Contact oldItem, Contact newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public void onInserted(int position, int count) {
                notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                notifyItemMoved(fromPosition, toPosition);
            }
        });

        initDatabaseListener();
    }

    private void initDatabaseListener() {
        Query query = databaseManager.getCompaniesRef().child(company.getId()).child("contacts").orderByChild("name");

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // This method is triggered when a new child is added
                // to the location to which this listener was added.
                Contact addedContact = dataSnapshot.getValue(Contact.class);

                if(addedContact != null) {
                    add(addedContact);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // This method is triggered when the data at a child location has changed.
                Contact updatedContact = dataSnapshot.getValue(Contact.class);

                if(updatedContact != null) {
                    Contact existingContact = (Contact) getHashMap().get(updatedContact.getId());
                    if(existingContact == null) {
                        contacts.add(updatedContact);
                    } else {
                        contacts.updateItemAt(getContactsList().indexOf(existingContact), updatedContact);
                    }

                    Toast.makeText(app, "Contact updated", Toast.LENGTH_SHORT).show();

                    app.getMainActivityRef().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            notifyDataSetChanged();
                        }
                    });
                } else {
                    Toast.makeText(app, "Failed to update contact, please try again later",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                // This method is triggered when a child is removed from the location
                // to which this listener was added.
                Contact removedContact = dataSnapshot.getValue(Contact.class);

                if(removedContact != null) {
                    remove(removedContact);
                } else {
                    Toast.makeText(app, "Failed to delete contact, please try again later",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // This method is triggered when a child location's priority changes.
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // This method will be triggered in the event that this listener either failed
                // at the server, or is removed as a result of the security and Firebase rules.

                Log.e(TAG, "Contacts ChildEventListener cancelled: " + databaseError);
            }
        });
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(app).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        holder.bindModelToView(contacts.get(position));
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    private void add(Contact contact) {
        contacts.add(contact);
    }

    private void remove(Contact contact) {
        contacts.remove(contact);
    }

    public List<Contact> getContactsList() {
        List<Contact> contactsCopy = new ArrayList<>();
        for(int i = 0; i < contacts.size(); i++) {
            contactsCopy.add(contacts.get(i));
        }
        return contactsCopy;
    }

    public HashMap<String, Object> getHashMap() {
        HashMap<String, Object> hashMap = new HashMap<>();
        for(int i = 0; i < contacts.size(); i++) {
            Contact contact = contacts.get(i);
            hashMap.put(contact.getId(), contact);
        }
        return hashMap;
    }
}
