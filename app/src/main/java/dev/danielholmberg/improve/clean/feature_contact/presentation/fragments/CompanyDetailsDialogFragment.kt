package dev.danielholmberg.improve.clean.feature_contact.presentation.fragments

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.os.Parcelable
import android.widget.Toast
import android.view.LayoutInflater
import android.view.ViewGroup
import dev.danielholmberg.improve.R
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.EditText
import android.content.DialogInterface
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import dev.danielholmberg.improve.clean.Improve.Companion.instance
import dev.danielholmberg.improve.clean.feature_contact.domain.model.Company
import dev.danielholmberg.improve.clean.feature_contact.domain.model.Contact
import dev.danielholmberg.improve.clean.feature_contact.domain.repository.CompanyRepository
import java.util.*

class CompanyDetailsDialogFragment : DialogFragment() {

    private lateinit var companyRepository: CompanyRepository

    private var activity: AppCompatActivity? = null
    private var toolbar: Toolbar? = null
    private var companyBundle: Bundle? = null
    private var company: Company? = null
    private var companyId: String? = null
    private var companyNameId: String? = null
    private var companyContacts: HashMap<String?, Contact>? = null
    private var companyName: TextView? = null
    private var companyNrOfContacts: TextView? = null
    private var contactsRecyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        companyRepository = instance!!.companyRepository
        activity = getActivity() as AppCompatActivity?
        companyBundle = arguments
        if (companyBundle != null) {
            company = companyBundle!!.getParcelable<Parcelable>(COMPANY_KEY) as Company?
        } else {
            Toast.makeText(
                activity, "Failed to show Company details, please try again",
                Toast.LENGTH_SHORT
            ).show()
            dismissDialog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_company_details, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar = view.findViewById<View>(R.id.toolbar_company_details_fragment) as Toolbar
        createOptionsMenu()
        toolbar!!.setNavigationIcon(R.drawable.ic_menu_close_primary)
        toolbar!!.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.edit_company -> showEditCompanyDialog()
                R.id.delete_company -> showDeleteCompanyDialog()
            }
            true
        }
        companyName = view.findViewById<View>(R.id.toolbar_company_title_tv) as TextView
        companyNrOfContacts =
            view.findViewById<View>(R.id.company_details_nr_of_contacts) as TextView
        contactsRecyclerView = view.findViewById<View>(R.id.contacts_recyclerview) as RecyclerView
        if (company != null) {
            populateCompanyDetails()
        } else {
            Toast.makeText(activity, "Unable to show Company details", Toast.LENGTH_SHORT).show()
            dismissDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        Objects.requireNonNull(dialog!!.window)
            ?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
    }

    private fun createOptionsMenu() {
        val menu = toolbar!!.menu
        menu.clear()
        toolbar!!.inflateMenu(R.menu.fragment_company_details)
        toolbar!!.setNavigationOnClickListener { dismissDialog() }
    }

    /**
     * Sets all the necessary detailed information about the Note.
     */
    private fun populateCompanyDetails() {
        companyId = company!!.id
        companyNameId = company!!.name
        companyContacts = company!!.contacts
        Log.d(TAG, "Company.contacts: $companyContacts")
        if (companyNameId != null) {
            companyName!!.text = companyNameId
        }
        if (companyContacts != null) {
            val linearLayoutManager = LinearLayoutManager(instance, RecyclerView.VERTICAL, false)
            contactsRecyclerView!!.layoutManager = linearLayoutManager
            contactsRecyclerView!!.adapter = instance!!.getCompanyContactsAdapter(companyId!!)
            companyNrOfContacts!!.text = companyContacts!!.size.toString()
        }
    }

    private fun showEditCompanyDialog() {
        val editCompanyDialogView = layoutInflater.inflate(R.layout.dialog_new_company, null, false)
        val companyNameEditText =
            editCompanyDialogView.findViewById<View>(R.id.new_company_name_et) as EditText
        companyNameEditText.setText(companyNameId)
        val editCompanyDialog = AlertDialog.Builder(
            context!!
        )
            .setTitle("Edit company")
            .setView(editCompanyDialogView)
            .setPositiveButton("Done") { _, _ ->
                // Dummy
            }
            .setNegativeButton("Cancel") { dialogInterface, _ -> dialogInterface.cancel() }
            .create()
        editCompanyDialog.show()
        companyNameEditText.requestFocus()
        editCompanyDialog.getButton(DialogInterface.BUTTON_POSITIVE)
            .setOnClickListener {
                val newCompanyName =
                    companyNameEditText.text.toString().uppercase(Locale.getDefault())
                if (newCompanyName.isNotEmpty()) {
                    if (instance!!.companyRecyclerViewAdapter!!.companiesName.contains(newCompanyName)) {
                        companyNameEditText.error = "Company already exists!"
                        companyNameEditText.requestFocus()
                    } else {
                        company!!.name = newCompanyName
                        companyNameId = newCompanyName
                        companyName!!.text = newCompanyName
                        companyRepository.addCompany(company!!)
                        editCompanyDialog.dismiss()
                    }
                } else {
                    companyNameEditText.error = "Please enter a company name"
                    companyNameEditText.requestFocus()
                }
            }
    }

    private fun showDeleteCompanyDialog() {
        val alertDialogBuilder = AlertDialog.Builder(
            activity!!
        ).setTitle(R.string.dialog_delete_company_title)
            .setMessage(R.string.dialog_delete_company_msg)
            .setPositiveButton("Yes") { _, _ -> deleteCompany(company) }
            .setNegativeButton("No") { dialogInterface, _ -> dialogInterface.dismiss() }
        val dialog = alertDialogBuilder.create()
        dialog.show()
    }

    private fun deleteCompany(company: Company?) {
        companyRepository.deleteCompany(company!!)
        dismissDialog()
    }

    private fun dismissDialog() {
        dismiss()
    }

    companion object {
        val TAG: String = CompanyDetailsDialogFragment::class.java.simpleName
        const val COMPANY_KEY = "company"
    }
}