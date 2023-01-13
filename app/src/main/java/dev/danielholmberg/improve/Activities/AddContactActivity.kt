package dev.danielholmberg.improve.Activities

import dev.danielholmberg.improve.Improve.Companion.instance
import androidx.appcompat.app.AppCompatActivity
import dev.danielholmberg.improve.Managers.DatabaseManager
import dev.danielholmberg.improve.Utilities.ContactInputValidator
import dev.danielholmberg.improve.Models.Contact
import dev.danielholmberg.improve.Models.Company
import com.google.android.material.textfield.TextInputEditText
import android.os.Bundle
import dev.danielholmberg.improve.R
import android.os.Parcelable
import android.content.DialogInterface
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import java.util.*

class AddContactActivity : AppCompatActivity() {

    private var databaseManager: DatabaseManager? = null
    private var validator: ContactInputValidator? = null
    private var contact: Contact? = null
    private var preSelectedCompany: Company? = null
    private var companies: List<Company>? = null
    private var contactName: TextInputEditText? = null
    private var contactEmail: TextInputEditText? = null
    private var contactPhone: TextInputEditText? = null
    private var contactComment: TextInputEditText? = null
    private var contactCompany: Spinner? = null
    private var companyAdapter: ArrayAdapter<Company>? = null
    private var addCompanyButton: ImageView? = null
    private var toolbar: Toolbar? = null
    private var inputLayout: View? = null
    private var isEdit = false
    private var oldCID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_contact)
        databaseManager = instance!!.databaseManager
        initActivity()
    }

    private fun initActivity() {
        toolbar = findViewById<View>(R.id.toolbar_add_contact) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        inputLayout = findViewById(R.id.input_contact_layout)
        contactName = findViewById<View>(R.id.input_name) as TextInputEditText
        contactCompany = findViewById<View>(R.id.spinner_company) as Spinner
        contactEmail = findViewById<View>(R.id.input_email) as TextInputEditText
        contactPhone = findViewById<View>(R.id.input_mobile) as TextInputEditText
        contactComment = findViewById<View>(R.id.input_comment) as TextInputEditText
        addCompanyButton = findViewById<View>(R.id.add_company) as ImageView
        addCompanyButton!!.setOnClickListener { addCompany() }
        companies = instance!!.companyRecyclerViewAdapter!!.companiesList
        companyAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item, companies!!
        )
        contactCompany!!.adapter = companyAdapter
        val contactBundle = intent.getBundleExtra(CONTACT_BUNDLE_KEY)
        if (contactBundle != null) {
            contact = contactBundle.getParcelable<Parcelable>(CONTACT_KEY) as Contact?
        }
        if (contact != null) {
            isEdit = true
            (findViewById<View>(R.id.toolbar_add_contact_title_tv) as TextView).setText(R.string.title_edit_contact)
            oldCID = contact!!.id
            if (contact!!.name != null) {
                contactName!!.setText(contact!!.name)
            }
            if (contact!!.email != null) {
                contactEmail!!.setText(contact!!.email)
            }
            if (contact!!.phone != null) {
                contactPhone!!.setText(contact!!.phone)
            }
            if (contact!!.comment != null) {
                contactComment!!.setText(contact!!.comment)
            }

            // If the company already exists, set that company as selected by default.
            if (contact!!.companyId != null) {
                val company = instance!!.companyRecyclerViewAdapter!!.getCompany(contact!!.companyId!!)
                if (company != null) {
                    val adapterPosition = companies!!.indexOf(company)
                    contactCompany!!.setSelection(adapterPosition)
                }
            }
        }
        preSelectedCompany = intent.getParcelableExtra<Parcelable>(PRE_SELECTED_COMPANY) as Company?
        if (preSelectedCompany != null) {
            val adapterPosition = companies!!.indexOf(preSelectedCompany)
            contactCompany!!.setSelection(adapterPosition)
        }
        validator = ContactInputValidator(this, inputLayout!!)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.activity_contact_mode_edit, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            android.R.id.home -> {
                showDiscardChangesDialog()
                true
            }
            R.id.contactDone -> {
                if (validator!!.formIsValid()) {
                    if (!isEdit) {
                        addContact()
                    } else {
                        updateContact()
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun addCompany() {
        val addCompanyDialogView = layoutInflater.inflate(R.layout.dialog_new_company, null, false)
        val companyNameEditText =
            addCompanyDialogView.findViewById<View>(R.id.new_company_name_et) as EditText
        val addNewCompanyDialog = AlertDialog.Builder(this)
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
                    if (instance!!.companyRecyclerViewAdapter!!.companiesName.contains(newCompanyName)) {
                        companyNameEditText.error = "Company already exists!"
                        companyNameEditText.requestFocus()
                    } else {
                        val newCompanyId = databaseManager!!.companiesRef.push().key
                        company = Company(newCompanyId, newCompanyName)
                        databaseManager!!.addCompany(company)

                        // Add and select created Company to Company Spinner.
                        companyAdapter!!.add(company)
                        companyAdapter!!.notifyDataSetChanged()
                        val adapterPosition = companies!!.indexOf(company)
                        contactCompany!!.setSelection(adapterPosition)
                        addNewCompanyDialog.dismiss()
                    }
                } else {
                    companyNameEditText.error = "Please enter a company name"
                    companyNameEditText.requestFocus()
                }
            }
    }

    private fun showDiscardChangesDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
            .setMessage(R.string.dialog_discard_changes_msg)
            .setPositiveButton("Discard") { dialogInterface, _ ->
                showParentActivity()
                dialogInterface.dismiss()
            }
            .setNegativeButton("Keep editing") { dialogInterface, _ -> dialogInterface.dismiss() }
        val dialog = alertDialogBuilder.create()
        dialog.show()
    }

    private fun addContact() {
        val company = contactCompany!!.selectedItem as Company
        val id = databaseManager!!.companiesRef.child(company.id).child("contacts").push().key
        val name = contactName!!.text.toString()
        val email = contactEmail!!.text.toString().trim { it <= ' ' }
        val phone = contactPhone!!.text.toString().trim { it <= ' ' }
        val comment = contactComment!!.text.toString()
        val timestampAdded = System.currentTimeMillis().toString()
        val newContact = Contact(id, name, company.id, email, phone, comment, timestampAdded)
        newContact.timestampUpdated = timestampAdded
        databaseManager!!.addContact(newContact)
        showParentActivity()
    }

    private fun updateContact() {
        val id = oldCID
        val name = contactName!!.text.toString()
        val company = contactCompany!!.selectedItem as Company
        val email = contactEmail!!.text.toString().trim { it <= ' ' }
        val phone = contactPhone!!.text.toString().trim { it <= ' ' }
        val comment = contactComment!!.text.toString()
        val timestampAdded = contact!!.timestampAdded
        val timestampUpdated = System.currentTimeMillis().toString()
        val updatedContact = Contact(id, name, company.id, email, phone, comment, timestampAdded)
        updatedContact.timestampUpdated = timestampUpdated
        databaseManager!!.updateContact(contact!!, updatedContact)
        showParentActivity()
    }

    private fun showParentActivity() {
        restUI()
        startActivity(Intent(this, MainActivity::class.java))
        finishAfterTransition()
    }

    private fun restUI() {
        contactName!!.text!!.clear()
        contactEmail!!.text!!.clear()
        contactPhone!!.text!!.clear()
        contactComment!!.text!!.clear()
    }

    override fun onBackPressed() {
        showDiscardChangesDialog()
    }

    companion object {
        private val TAG = AddContactActivity::class.java.simpleName
        const val CONTACT_BUNDLE_KEY = "contactBundle"
        private const val CONTACT_KEY = "contact"
        const val PRE_SELECTED_COMPANY = "preSelectedCompany"
    }
}