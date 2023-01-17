package dev.danielholmberg.improve.legacy.Adapters

import android.util.Log
import dev.danielholmberg.improve.Improve.Companion.instance
import dev.danielholmberg.improve.legacy.Models.Company
import androidx.recyclerview.widget.RecyclerView
import dev.danielholmberg.improve.legacy.ViewHolders.ContactViewHolder
import dev.danielholmberg.improve.legacy.Managers.DatabaseManager
import androidx.recyclerview.widget.SortedList
import dev.danielholmberg.improve.legacy.Models.Contact
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import android.widget.Toast
import com.google.firebase.database.DatabaseError
import android.view.ViewGroup
import android.view.LayoutInflater
import dev.danielholmberg.improve.R
import java.util.*

class ContactRecyclerViewAdapter(private val company: Company) :
    RecyclerView.Adapter<ContactViewHolder>() {

    private val databaseManager: DatabaseManager = instance!!.databaseManager
    private val contacts: SortedList<Contact> =
        SortedList(Contact::class.java, object : SortedList.Callback<Contact>() {
            override fun compare(o1: Contact, o2: Contact): Int {
                // Sorts the list depending on the compared attribute
                // Uses .toUpperCase() due to UTF-8 value difference between Uppercase and Lowercase letters.
                return o1.name!!.uppercase(Locale.getDefault())
                    .compareTo(o2.name!!.uppercase(Locale.getDefault()))
            }

            override fun onChanged(position: Int, count: Int) {
                notifyItemChanged(position, count)
            }

            override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
                return oldItem.name == newItem.name
                        && oldItem.companyId == newItem.companyId
                        && oldItem.email == newItem.email
                        && oldItem.phone == newItem.phone
                        && oldItem.comment == newItem.comment
            }

            override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
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
        val query =
            databaseManager.companiesRef.child(company.id!!).child("contacts").orderByChild("name")
        query.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // This method is triggered when a new child is added
                // to the location to which this listener was added.
                val addedContact = dataSnapshot.getValue(Contact::class.java)
                addedContact?.let { add(it) }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // This method is triggered when the data at a child location has changed.
                val updatedContact = dataSnapshot.getValue(Contact::class.java)
                if (updatedContact != null) {
                    val existingContact = hashMap[updatedContact.id] as Contact?
                    var index = contacts.size()
                    if (existingContact == null) {
                        contacts.add(updatedContact)
                    } else {
                        index = contactsList.indexOf(existingContact)
                        contacts.updateItemAt(index, updatedContact)
                    }
                    Toast.makeText(instance, "Contact updated", Toast.LENGTH_SHORT).show()
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
                val removedContact = dataSnapshot.getValue(Contact::class.java)
                if (removedContact != null) {
                    remove(removedContact)
                } else {
                    Toast.makeText(
                        instance, "Failed to delete contact, please try again later",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {
                // This method is triggered when a child location's priority changes.
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // This method will be triggered in the event that this listener either failed
                // at the server, or is removed as a result of the security and Firebase rules.
                Log.e(TAG, "Contacts ChildEventListener cancelled: $databaseError")
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(instance).inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bindModelToView(contacts[position])
    }

    override fun getItemCount(): Int {
        return contacts.size()
    }

    private fun add(contact: Contact) {
        contacts.add(contact)
    }

    private fun remove(contact: Contact) {
        contacts.remove(contact)
    }

    val contactsList: List<Contact>
        get() {
            val contactsCopy: MutableList<Contact> = ArrayList()
            for (i in 0 until contacts.size()) {
                contactsCopy.add(contacts[i])
            }
            return contactsCopy
        }
    val hashMap: HashMap<String?, Any>
        get() {
            val hashMap = HashMap<String?, Any>()
            for (i in 0 until contacts.size()) {
                val contact = contacts[i]
                hashMap[contact.id] = contact
            }
            return hashMap
        }

    companion object {
        private val TAG = ContactRecyclerViewAdapter::class.java.simpleName
    }
}