package dev.danielholmberg.improve.ViewHolders

import dev.danielholmberg.improve.Improve.Companion.instance
import androidx.recyclerview.widget.RecyclerView
import dev.danielholmberg.improve.Models.Contact
import dev.danielholmberg.improve.R
import android.widget.TextView
import android.os.Bundle
import dev.danielholmberg.improve.Fragments.ContactDetailsSheetFragment
import android.widget.Toast
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.Button
import androidx.core.content.ContextCompat

class ContactViewHolder(private val mView: View) : RecyclerView.ViewHolder(
    mView
), View.OnClickListener {
    private var contact: Contact? = null

    // OBS! Due to RecyclerView:
    // We need to define all views of each contact!
    // Otherwise each contact view won't be unique.
    fun bindModelToView(contact: Contact) {
        this.contact = contact

        // [START] All views of a contact
        val callBtn = mView.findViewById<Button>(R.id.call_contact_btn)
        val mailBtn = mView.findViewById<Button>(R.id.mail_contact_btn)
        val name = mView.findViewById<TextView>(R.id.name_tv)
        // [END] All views of a contact

        // [START] Define each view
        name.text = contact.name
        if (contact.email == null || contact.email.isEmpty()) {
            mailBtn.background = ContextCompat.getDrawable(
                instance!!,
                R.drawable.ic_contact_email_grey
            )
        } else {
            mailBtn.background =
                ContextCompat.getDrawable(instance!!, R.drawable.ic_contact_email_active)
        }
        mailBtn.setOnClickListener(this)
        if (contact.phone == null || contact.phone.isEmpty()) {
            callBtn.background =
                ContextCompat.getDrawable(instance!!, R.drawable.ic_contact_mobile_grey)
        } else {
            callBtn.background =
                ContextCompat.getDrawable(instance!!, R.drawable.ic_contact_mobile_active)
        }
        callBtn.setOnClickListener(this)
        // [END] Define each view
        mView.setOnClickListener {
            val bundle = createBundle(contact)
            val contactDetailsSheetFragment = ContactDetailsSheetFragment()
            contactDetailsSheetFragment.arguments = bundle
            contactDetailsSheetFragment.show(
                instance!!.mainActivityRef!!.supportFragmentManager,
                contactDetailsSheetFragment.tag
            )
        }
    }

    private fun createBundle(contact: Contact): Bundle {
        val bundle = Bundle()
        bundle.putParcelable(ContactDetailsSheetFragment.CONTACT_KEY, contact)
        bundle.putInt(ContactDetailsSheetFragment.PARENT_FRAGMENT_KEY, R.integer.CONTACT_FRAGMENT)
        return bundle
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.call_contact_btn -> if (contact!!.phone == null || contact!!.phone.isEmpty()) {
                Toast.makeText(
                    instance,
                    instance!!.resources.getString(R.string.contact_no_phone_message),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val callIntent = Intent(Intent.ACTION_DIAL)
                callIntent.data = Uri.parse("tel:" + contact!!.phone)
                callIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                instance!!.startActivity(callIntent)
            }
            R.id.mail_contact_btn -> if (contact!!.email == null || contact!!.email.isEmpty()) {
                Toast.makeText(
                    instance,
                    instance!!.resources.getString(R.string.contact_no_email_message),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val mailIntent = Intent(Intent.ACTION_SENDTO)
                mailIntent.data = Uri.parse("mailto:" + contact!!.email)
                mailIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                instance!!.startActivity(mailIntent)
            }
            else -> {}
        }
    }
}