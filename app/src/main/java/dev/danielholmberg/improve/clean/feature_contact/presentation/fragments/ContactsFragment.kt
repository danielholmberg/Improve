package dev.danielholmberg.improve.clean.feature_contact.presentation.fragments

import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.TextView
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import dev.danielholmberg.improve.R
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseError
import android.widget.EditText
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import dev.danielholmberg.improve.clean.Improve.Companion.instance
import dev.danielholmberg.improve.clean.feature_contact.data.source.entity.CompanyEntity
import dev.danielholmberg.improve.clean.feature_contact.domain.model.Company
import dev.danielholmberg.improve.clean.feature_contact.domain.repository.CompanyRepository
import dev.danielholmberg.improve.clean.feature_contact.domain.repository.ContactRepository
import dev.danielholmberg.improve.clean.feature_contact.presentation.AddContactActivity
import java.util.*

class ContactsFragment : Fragment() {

    private lateinit var contactRepository: ContactRepository
    private lateinit var companyRepository: CompanyRepository

    private lateinit var companyRecyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var addContactFAB: FloatingActionButton
    private lateinit var addCompanyFAB: FloatingActionButton
    private lateinit var addCompanyFABTextView: TextView
    private lateinit var addContactFABTextView: TextView
    private lateinit var snackBarView: View
    private lateinit var emptyListView: View
    private var isFABOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contactRepository = instance!!.contactRepository
        companyRepository = instance!!.companyRepository
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(
            R.layout.fragment_contacts,
            container, false
        )
        instance!!.mainActivityRef.findViewById<View>(R.id.toolbar_dropshadow).visibility =
            View.GONE
        snackBarView = view.findViewById(R.id.contacts_fragment_container)
        companyRecyclerView = view.findViewById(R.id.company_recycler_view)
        emptyListView = view.findViewById(R.id.empty_contact_list_tv)
        fab = view.findViewById(R.id.fab_menu)
        addCompanyFABTextView = view.findViewById(R.id.add_company_fab_text)
        addContactFABTextView = view.findViewById(R.id.add_contact_fab_text)
        addContactFAB = view.findViewById(R.id.add_contact)
        addCompanyFAB = view.findViewById(R.id.add_company)
        val recyclerLayoutManager = LinearLayoutManager(activity)
        companyRecyclerView.layoutManager = recyclerLayoutManager
        initListScrollListener()
        initAdapter()
        initListDataChangeListener()
        return view
    }

    private fun initListDataChangeListener() {

        // TODO: Should be moved to UseCase

        companyRepository.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                if (instance!!.companyRecyclerViewAdapter!!.itemCount > 0) {
                    companyRecyclerView.visibility = View.VISIBLE
                    emptyListView.visibility = View.GONE
                } else {
                    companyRecyclerView.visibility = View.GONE
                    emptyListView.visibility = View.VISIBLE
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                val previouslyRemovedContact = instance!!.companyRecyclerViewAdapter?.previouslyRemovedContact
                if (previouslyRemovedContact != null) {
                    Log.i(TAG, "COMPANY CHANGED")
                    Snackbar.make(
                        (snackBarView),
                        "Contact deleted", Snackbar.LENGTH_LONG
                    )
                        .setAction("UNDO") {

                            // TODO: Should be moved to UseCase

                            instance!!.companyRecyclerViewAdapter?.previouslyRemovedContact = null
                            contactRepository.addContact(previouslyRemovedContact)
                        }.show()
                }
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                val removedCompany = dataSnapshot.getValue(
                    CompanyEntity::class.java
                )?.toCompany()

                if (removedCompany != null) {
                    Snackbar.make(
                        (snackBarView),
                        "Company deleted", Snackbar.LENGTH_LONG
                    )
                        .setAction("UNDO") {

                            // TODO: Should be moved to UseCase

                            companyRepository.addCompany(removedCompany)
                        }.show()
                }
                if (instance!!.companyRecyclerViewAdapter!!.itemCount > 0) {
                    companyRecyclerView.visibility = View.VISIBLE
                    emptyListView.visibility = View.GONE
                } else {
                    companyRecyclerView.visibility = View.GONE
                    emptyListView.visibility = View.VISIBLE
                }
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun initListScrollListener() {
        // Add a OnScrollListener to change when to show the Floating Action Button for adding a new Note.
        companyRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 && fab.isShown) {
                    // Hide FABs when the user scrolls down.
                    closeFABMenu()
                    fab.hide()
                }
                if (!recyclerView.canScrollVertically(-1)) {
                    // we have reached the top of the list
                    instance!!.mainActivityRef.findViewById<View>(R.id.toolbar_dropshadow).visibility =
                        View.GONE
                } else {
                    // we are not at the top yet
                    instance!!.mainActivityRef.findViewById<View>(R.id.toolbar_dropshadow).visibility =
                        View.VISIBLE
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // Show FABs when the user has stopped scrolling.
                    fab.show()
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
        fab.setOnClickListener {
            if (isFABOpen) {
                closeFABMenu()
            } else {
                showFABMenu()
            }
        }
        addContactFAB.setOnClickListener {
            addContact()
            closeFABMenu()
        }
        addCompanyFAB.setOnClickListener {
            addCompany()
            closeFABMenu()
        }
    }

    private fun addCompany() {
        val addCompanyDialogView = layoutInflater.inflate(R.layout.dialog_new_company, null, false)
        val companyNameEditText =
            addCompanyDialogView.findViewById<View>(R.id.new_company_name_et) as EditText
        val addNewCompanyDialog = AlertDialog.Builder(
            (context)!!
        )
            .setTitle("Add new company")
            .setView(addCompanyDialogView)
            .setPositiveButton("Add") { _, _ ->
                // Dummy
            }
            .setNegativeButton("Cancel") { dialogInterface, _ -> dialogInterface.cancel() }
            .create()
        addNewCompanyDialog.show()
        companyNameEditText.requestFocus()
        addNewCompanyDialog.getButton(DialogInterface.BUTTON_POSITIVE)
            .setOnClickListener {
                val newCompanyName =
                    companyNameEditText.text.toString().uppercase(Locale.getDefault())
                if (newCompanyName.isNotEmpty()) {
                    val company: Company
                    if (instance!!.companyRecyclerViewAdapter!!.companiesName.contains(
                            newCompanyName
                        )
                    ) {
                        companyNameEditText.error = "Company already exists!"
                        companyNameEditText.requestFocus()
                    } else {
                        val newCompanyId = companyRepository.generateNewCompanyId()
                        company = Company(newCompanyId, newCompanyName)
                        companyRepository.addCompany(company)
                        addNewCompanyDialog.dismiss()
                    }
                } else {
                    companyNameEditText.error = "Please enter a company name"
                    companyNameEditText.requestFocus()
                }
            }
    }

    private fun showFABMenu() {
        isFABOpen = true
        fab.animate().rotation(45f).setDuration(300).start()
        if (instance!!.companyRecyclerViewAdapter!!.itemCount > 0) {
            // If a Company has already been added, make the FAB for adding a Contact visible.
            addContactFAB.show()
            addContactFAB.animate()
                .translationY(-resources.getDimension(R.dimen.fab_menu_item_position_1))
                .setDuration(300).withEndAction {
                    addContactFABTextView.visibility = View.VISIBLE
                    addContactFABTextView.animate().alpha(1f).duration = 300
                }
            addCompanyFAB.show()
            addCompanyFAB.animate()
                .translationY(-resources.getDimension(R.dimen.fab_menu_item_position_2))
                .setDuration(300).withEndAction {
                    addCompanyFABTextView.visibility = View.VISIBLE
                    addCompanyFABTextView.animate().alpha(1f).duration = 300
                }
        } else {
            // No Company has been added, hide the FAB for adding a Contact and switch position of Company FAB.
            addContactFAB.hide()
            addContactFABTextView.visibility = View.GONE
            addContactFABTextView.alpha = 0f
            addCompanyFAB.show()
            addCompanyFAB.animate()
                .translationY(-resources.getDimension(R.dimen.fab_menu_item_position_1))
                .setDuration(300).withEndAction {
                    addCompanyFABTextView.visibility = View.VISIBLE
                    addCompanyFABTextView.animate().alpha(1f).duration = 300
                }
        }
    }

    private fun closeFABMenu() {
        isFABOpen = false
        fab.animate().rotation(0f).setDuration(300).start()
        addContactFAB.hide()
        addContactFABTextView.animate().alpha(0f).setDuration(300)
            .withEndAction {
                addContactFAB.animate().translationY(0f).setDuration(300)
                    .withStartAction { addContactFABTextView.visibility = View.GONE }
            }
        addCompanyFAB.hide()
        addCompanyFABTextView.animate().alpha(0f).setDuration(300)
            .withEndAction {
                addCompanyFAB.animate().translationY(0f).setDuration(300)
                    .withStartAction { addCompanyFABTextView.visibility = View.GONE }
            }
    }

    private fun initAdapter() {
        companyRecyclerView.adapter = instance!!.companyRecyclerViewAdapter
    }

    private fun addContact() {
        val addContactIntent = Intent(context, AddContactActivity::class.java)
        startActivity(addContactIntent)
    }

    companion object {
        private val TAG = ContactsFragment::class.java.simpleName
    }
}