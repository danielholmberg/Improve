package dev.danielholmberg.improve.Adapters

import android.util.Log
import dev.danielholmberg.improve.Improve.Companion.instance
import androidx.recyclerview.widget.RecyclerView
import dev.danielholmberg.improve.ViewHolders.CompanyViewHolder
import dev.danielholmberg.improve.Managers.DatabaseManager
import androidx.recyclerview.widget.SortedList
import dev.danielholmberg.improve.Models.Company
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import android.widget.Toast
import com.google.firebase.database.DatabaseError
import android.view.ViewGroup
import android.view.LayoutInflater
import dev.danielholmberg.improve.R
import androidx.recyclerview.widget.LinearLayoutManager
import java.util.ArrayList
import java.util.HashMap

class CompanyRecyclerViewAdapter : RecyclerView.Adapter<CompanyViewHolder>() {

    private val databaseManager: DatabaseManager? = instance!!.databaseManager
    private val companies: SortedList<Company> =
        SortedList(Company::class.java, object : SortedList.Callback<Company>() {
            override fun compare(o1: Company, o2: Company): Int {
                // Sorts the list depending on the compared attribute
                return o1.name.compareTo(o2.name)
            }

            override fun onChanged(position: Int, count: Int) {
                notifyItemChanged(position, count)
            }

            override fun areContentsTheSame(oldItem: Company, newItem: Company): Boolean {
                if (oldItem.name == newItem.name)
                    if (oldItem.contacts != null)
                        if (newItem.contacts != null)
                            return oldItem.contacts == newItem.contacts
                return false
            }

            override fun areItemsTheSame(oldItem: Company, newItem: Company): Boolean {
                return oldItem.id == newItem.id
            }

            override fun onInserted(position: Int, count: Int) {
                notifyItemRangeInserted(position, count)
            }

            override fun onRemoved(position: Int, count: Int) {
                notifyItemRangeRemoved(position, count)
            }

            override fun onMoved(fromPosition: Int, toPosition: Int) {
                notifyItemMoved(fromPosition, toPosition)
            }
        })

    init {
        initDatabaseListener()
    }

    private fun initDatabaseListener() {
        val query = databaseManager!!.companiesRef.orderByChild("name")
        query.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // This method is triggered when a new child is added
                // to the location to which this listener was added.
                val addedCompany = dataSnapshot.getValue(Company::class.java)
                if (addedCompany != null) {
                    Log.d(TAG, "Added Company: " + addedCompany.name)
                    companies.add(addedCompany)
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // This method is triggered when the data at a child location has changed.
                val updatedCompany = dataSnapshot.getValue(Company::class.java)
                if (updatedCompany != null) {
                    val existingCompany = getCompany(updatedCompany.id)
                    var index = companies.size()
                    if (existingCompany == null) {
                        companies.add(updatedCompany)
                    } else {
                        index = companiesList.indexOf(existingCompany)
                        companies.updateItemAt(
                            index,
                            updatedCompany
                        )
                    }
                    instance!!.mainActivityRef!!.runOnUiThread { notifyItemChanged(index) }
                } else {
                    Toast.makeText(
                        instance, "Failed to update contact, please try again later",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                // This method is triggered when a child is removed from the location
                // to which this listener was added.
                val removedCompany = dataSnapshot.getValue(Company::class.java)
                if (removedCompany != null) {
                    Log.d(TAG, "Removed Company: " + removedCompany.name)
                    companies.remove(removedCompany)
                }
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {
                // This method is triggered when a child location's priority changes.
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // This method will be triggered in the event that this listener either failed
                // at the server, or is removed as a result of the security and Firebase rules.
                Log.e(TAG, "Companies ChildEventListener cancelled: $databaseError")
            }
        })
    }

    val companiesHashMap: HashMap<String, Any>
        get() {
            val hashMap = HashMap<String, Any>()
            for (i in 0 until companies.size()) {
                val company = companies[i]
                hashMap[company.id] = company
            }
            return hashMap
        }
    val companiesName: ArrayList<String>
        get() {
            val companiesName = ArrayList<String>()
            for ((_, value) in companiesHashMap) {
                val company = value as Company
                companiesName.add(company.name)
            }
            return companiesName
        }

    fun getCompany(companyId: String): Company? {
        return companiesHashMap[companyId] as Company?
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompanyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_company, parent, false)
        return CompanyViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompanyViewHolder, position: Int) {
        val company = companies[position]
        holder.bindModelToView(company)
        holder.contactRecyclerView!!.isNestedScrollingEnabled = false
        val linearLayoutManager = LinearLayoutManager(instance, RecyclerView.VERTICAL, false)
        holder.contactRecyclerView!!.layoutManager = linearLayoutManager
        val contactsAdapter = ContactRecyclerViewAdapter(company)
        holder.contactRecyclerView!!.adapter = contactsAdapter
        instance!!.addContactsAdapter(company.id, contactsAdapter)
    }

    override fun getItemCount(): Int {
        return companies.size()
    }

    val companiesList: List<Company>
        get() {
            val companyList = ArrayList<Company>()
            for (i in 0 until companies.size()) {
                companyList.add(companies[i])
            }
            return companyList
        }

    companion object {
        private val TAG = CompanyRecyclerViewAdapter::class.java.simpleName
    }
}