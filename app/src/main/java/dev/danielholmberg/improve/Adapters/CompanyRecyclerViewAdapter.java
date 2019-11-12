package dev.danielholmberg.improve.Adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.SortedList;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import java.util.Map;

import dev.danielholmberg.improve.Models.Company;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Managers.FirebaseDatabaseManager;
import dev.danielholmberg.improve.Models.Contact;
import dev.danielholmberg.improve.R;
import dev.danielholmberg.improve.ViewHolders.CompanyViewHolder;

public class CompanyRecyclerViewAdapter extends RecyclerView.Adapter<CompanyViewHolder> {
    private static final String TAG = CompanyRecyclerViewAdapter.class.getSimpleName();

    private Improve app;
    private final FirebaseDatabaseManager databaseManager;
    private SortedList<Company> companies;

    public CompanyRecyclerViewAdapter() {
        this.app = Improve.getInstance();
        this.databaseManager = app.getFirebaseDatabaseManager();

        companies = new SortedList<Company>(Company.class, new SortedList.Callback<Company>() {
            @Override
            public int compare(Company o1, Company o2) {
                // Sorts the list depending on the compared attribute
                return o1.getName().compareTo(o2.getName());
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemChanged(position, count);
            }

            @Override
            public boolean areContentsTheSame(Company oldItem, Company newItem) {
                if(oldItem.getName().equals(newItem.getName()))
                    if(oldItem.getContacts() != null)
                        if(newItem.getContacts() != null)
                            return oldItem.getContacts().equals(newItem.getContacts());
                return false;
            }

            @Override
            public boolean areItemsTheSame(Company oldItem, Company newItem) {
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
        Query query = databaseManager.getCompaniesRef().orderByChild("name");

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // This method is triggered when a new child is added
                // to the location to which this listener was added.

                Company addedCompany = dataSnapshot.getValue(Company.class);

                if(addedCompany != null) {
                    Log.d(TAG, "Added Company: " + addedCompany.getName());
                    companies.add(addedCompany);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // This method is triggered when the data at a child location has changed.
                Company updatedCompany = dataSnapshot.getValue(Company.class);

                if(updatedCompany != null) {
                    Company existingCompany = (Company) getCompany(updatedCompany.getId());
                    if(existingCompany == null) {
                        companies.add(updatedCompany);
                    } else {
                        companies.updateItemAt(getCompaniesList().indexOf(existingCompany), updatedCompany);
                    }

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

                final Company removedCompany = dataSnapshot.getValue(Company.class);

                if(removedCompany != null) {
                    Log.d(TAG, "Removed Company: " + removedCompany.getName());
                    companies.remove(removedCompany);
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

                Log.e(TAG, "Companys ChildEventListener cancelled: " + databaseError);
            }
        });
    }

    public HashMap<String, Object> getCompaniesHashMap() {
        HashMap<String, Object> hashMap = new HashMap<>();
        for(int i = 0; i < companies.size(); i++) {
            Company company = companies.get(i);
            hashMap.put(company.getId(), company);
        }
        return hashMap;
    }

    public ArrayList<String> getCompaniesName() {
        ArrayList<String> companiesName = new ArrayList<>();
        for(Map.Entry<String, Object> companyEntry: getCompaniesHashMap().entrySet()) {
            Company company = (Company) companyEntry.getValue();

            if(company != null) {
                companiesName.add(company.getName());
            }
        }
        return companiesName;
    }

    public Company getCompany(String companyId) {
        return (Company) getCompaniesHashMap().get(companyId);
    }

    @NonNull
    @Override
    public CompanyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_company, parent, false);
        return new CompanyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompanyViewHolder holder, int position) {
        final Company company = companies.get(position);
        holder.bindModelToView(company);

        holder.contactRecyclerView.setNestedScrollingEnabled(false);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(app, RecyclerView.VERTICAL, false);
        holder.contactRecyclerView.setLayoutManager(linearLayoutManager);

        ContactRecyclerViewAdapter contactsAdapter = new ContactRecyclerViewAdapter(company);
        holder.contactRecyclerView.setAdapter(contactsAdapter);
        app.addContactsAdapter(company.getId(), contactsAdapter);

    }

    @Override
    public int getItemCount() {
        return companies.size();
    }

    public List<Company> getCompaniesList() {
        ArrayList<Company> companyList = new ArrayList<>();
        for(int i=0; i<companies.size() ; i++) {
            companyList.add(companies.get(i));
        }
        return companyList;
    }
}


