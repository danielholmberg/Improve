package dev.danielholmberg.improve.clean.feature_contact.presentation.contacts.adapter

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import android.widget.Toast
import com.google.firebase.database.DatabaseError
import android.view.ViewGroup
import android.view.LayoutInflater
import dev.danielholmberg.improve.BuildConfig
import dev.danielholmberg.improve.R
import dev.danielholmberg.improve.clean.Improve.Companion.instance
import dev.danielholmberg.improve.clean.feature_contact.data.repository.CompanyRepositoryImpl.Companion.ORDER_BY_NAME
import dev.danielholmberg.improve.clean.feature_contact.data.source.entity.ContactEntity
import dev.danielholmberg.improve.clean.feature_contact.domain.model.Company
import dev.danielholmberg.improve.clean.feature_contact.domain.model.Contact
import dev.danielholmberg.improve.clean.feature_contact.domain.repository.ContactRepository
import dev.danielholmberg.improve.clean.feature_contact.presentation.contacts.ContactViewHolder
import java.util.*

class ContactRecyclerViewAdapter(private val company: Company) :
    RecyclerView.Adapter<ContactViewHolder>() {

    private var contactRepository: ContactRepository = instance!!.contactRepository
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

        // TODO: Should be moved to UseCase

        contactRepository.addChildEventListenerForCompany(
            company.id!!,
            ORDER_BY_NAME,
            object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {

                    // TODO: Should be moved to UseCase

                    // This method is triggered when a new child is added
                    // to the location to which this listener was added.
                    val addedContact = dataSnapshot.getValue(
                        ContactEntity::class.java
                    )?.toContact()

                    addedContact?.let { add(it) }
                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

                    // TODO: Should be moved to UseCase

                    // This method is triggered when the data at a child location has changed.
                    val updatedContact = dataSnapshot.getValue(
                        ContactEntity::class.java
                    )?.toContact()

                    if (updatedContact != null) {
                        val existingContact = hashMap[updatedContact.id]
                        var index = contacts.size()
                        if (existingContact == null) {
                            contacts.add(updatedContact)
                        } else {
                            index = contactsList.indexOf(existingContact)
                            contacts.updateItemAt(index, updatedContact)
                        }
                        Toast.makeText(instance, "Contact updated", Toast.LENGTH_SHORT).show()
                        instance!!.mainActivityRef.runOnUiThread { notifyItemChanged(index) }
                    } else {
                        Toast.makeText(
                            instance, "Failed to update contact, please try again later",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onChildRemoved(dataSnapshot: DataSnapshot) {

                    // TODO: Should be moved to UseCase

                    // This method is triggered when a child is removed from the location
                    // to which this listener was added.
                    val removedContact = dataSnapshot.getValue(
                        ContactEntity::class.java
                    )?.toContact()

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
    val hashMap: HashMap<String?, Contact>
        get() {
            val hashMap = HashMap<String?, Contact>()
            for (i in 0 until contacts.size()) {
                val contact = contacts[i]
                hashMap[contact.id] = contact
            }
            return hashMap
        }

    companion object {
        private val TAG = BuildConfig.TAG + ContactRecyclerViewAdapter::class.java.simpleName
    }
}