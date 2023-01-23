package dev.danielholmberg.improve.clean.feature_contact.presentation.contacts

import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import dev.danielholmberg.improve.R
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import dev.danielholmberg.improve.BuildConfig
import dev.danielholmberg.improve.clean.Improve.Companion.instance
import dev.danielholmberg.improve.clean.feature_contact.domain.model.Company
import dev.danielholmberg.improve.clean.feature_contact.presentation.AddContactActivity
import dev.danielholmberg.improve.clean.feature_contact.presentation.fragments.CompanyDetailsDialogFragment

class CompanyViewHolder(private val mView: View) : RecyclerView.ViewHolder(
    mView
) {
    private lateinit var companyName: TextView
    private lateinit var quickAddNewContact: Button

    fun bindModelToView(company: Company) {
        companyName = mView.findViewById<View>(R.id.company_list_tv) as TextView
        companyName.text = company.name
        quickAddNewContact = mView.findViewById<View>(R.id.quick_add_contact_btn) as Button
        contactRecyclerView = mView.findViewById<View>(R.id.contact_recycler_view) as RecyclerView
        quickAddNewContact.setOnClickListener {
            val addContactIntent = Intent(instance, AddContactActivity::class.java)
            addContactIntent.putExtra(AddContactActivity.PRE_SELECTED_COMPANY, company)
            addContactIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            instance!!.startActivity(addContactIntent)
        }
        mView.setOnClickListener {
            val bundle = createCompanyBundle(company)
            val companyDetailsDialogFragment = CompanyDetailsDialogFragment()
            companyDetailsDialogFragment.arguments = bundle
            companyDetailsDialogFragment.show(
                instance!!.mainActivityRef.supportFragmentManager,
                companyDetailsDialogFragment.tag
            )
        }
    }

    private fun createCompanyBundle(company: Company): Bundle {
        val bundle = Bundle()
        bundle.putParcelable(CompanyDetailsDialogFragment.COMPANY_KEY, company)
        return bundle
    }

    companion object {
        private val TAG = BuildConfig.TAG + CompanyViewHolder::class.java.simpleName
        lateinit var contactRecyclerView: RecyclerView
    }
}