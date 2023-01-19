package dev.danielholmberg.improve.clean.feature_contact.presentation.fragments

import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.app.ProgressDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import dev.danielholmberg.improve.R
import android.content.Intent
import android.os.Parcelable
import android.widget.Toast
import android.text.method.ScrollingMovementMethod
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.services.drive.DriveScopes
import android.widget.RelativeLayout
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.Scope
import dev.danielholmberg.improve.clean.Improve.Companion.instance
import dev.danielholmberg.improve.clean.core.GoogleDriveService
import dev.danielholmberg.improve.clean.feature_contact.domain.model.Contact
import dev.danielholmberg.improve.clean.feature_contact.domain.repository.ContactRepository
import dev.danielholmberg.improve.clean.feature_contact.presentation.AddContactActivity
import java.text.DateFormat
import java.util.*

class ContactDetailsSheetFragment : BottomSheetDialogFragment(), View.OnClickListener {

    private lateinit var contactRepository: ContactRepository
    private lateinit var googleDriveService: GoogleDriveService

    private var contactBundle: Bundle? = null
    private var contact: Contact? = null
    private var detailsDialog: ContactDetailsSheetFragment? = null
    private var parentFragment = 0
    private var toolbar: Toolbar? = null
    private var title: TextView? = null
    private var name: TextView? = null
    private var email: TextView? = null
    private var mobile: TextView? = null
    private var comment: TextView? = null
    private var timestampAdded: String? = null
    private var timestampUpdated: String? = null
    private var activity: AppCompatActivity? = null
    private var exportDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contactRepository = instance!!.contactRepository
        googleDriveService = instance!!.googleDriveService
        detailsDialog = this
        activity = getActivity() as AppCompatActivity?
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_contact_details, container, false)
        toolbar = view.findViewById(R.id.toolbar_contact_details_fragment)
        toolbar!!.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.contactEdit -> {
                    detailsDialog!!.dismiss()
                    val updateContact = Intent(instance!!, AddContactActivity::class.java)
                    updateContact.putExtra(AddContactActivity.CONTACT_BUNDLE_KEY, contactBundle)
                    startActivity(updateContact)
                    true
                }
                R.id.contactDelete -> {
                    showDeleteContactDialog()
                    true
                }
                R.id.contactInfo -> {
                    showInfoDialog()
                    true
                }
                R.id.contactExport -> {
                    checkDrivePermission()
                    true
                }
                else -> true
            }
        }
        toolbar!!.inflateMenu(R.menu.fragment_contact_details_show)
        val actionCallContact = view.findViewById<View>(R.id.details_call_contact_btn) as Button
        val actionSendMailToContact =
            view.findViewById<View>(R.id.details_mail_contact_btn) as Button
        contactBundle = this.arguments
        if (contactBundle != null) {
            parentFragment = contactBundle!!.getInt(PARENT_FRAGMENT_KEY)
            contact = contactBundle!!.getParcelable<Parcelable>(CONTACT_KEY) as Contact?
        } else {
            Toast.makeText(context, "Unable to show contact details", Toast.LENGTH_SHORT).show()
            detailsDialog!!.dismiss()
        }
        title = view.findViewById<View>(R.id.toolbar_contact_details_company_tv) as TextView
        name = view.findViewById<View>(R.id.contact_details_name_tv) as TextView
        email = view.findViewById<View>(R.id.contact_details_email_tv) as TextView
        mobile = view.findViewById<View>(R.id.contact_details_mobile_tv) as TextView
        comment = view.findViewById<View>(R.id.contact_details_comment_tv) as TextView
        if (contact != null) {
            name!!.text = contact!!.name
            email!!.text = contact!!.email
            mobile!!.text = contact!!.phone
            comment!!.text = contact!!.comment
            comment!!.movementMethod = ScrollingMovementMethod()
            if (contact!!.timestampAdded != null) {
                timestampAdded = transformMillisToDateString(contact!!.timestampAdded!!.toLong())
            }
            if (contact!!.timestampUpdated != null) {
                timestampUpdated = transformMillisToDateString(contact!!.timestampUpdated!!.toLong())
            }
            if (contact!!.companyId != null) {
                if (instance!!.companyRecyclerViewAdapter!!.getCompany(contact!!.companyId!!) != null) {
                    title!!.text =
                        instance!!.companyRecyclerViewAdapter!!.getCompany(contact!!.companyId!!)!!.name
                }
            }

            // Handle if the voluntary contact information fields is empty
            // Change e-mail field
            if (contact!!.email != null) {
                if (contact!!.email!!.isEmpty()) {
                    // Change text and disable mail action
                    email!!.text = getString(R.string.contact_details_empty_email_text)
                    email!!.setTextColor(ContextCompat.getColor(instance!!, R.color.contact_form_icon))
                    actionSendMailToContact.isEnabled = false
                    actionSendMailToContact.setTextColor(ContextCompat.getColor(instance!!, R.color.contact_form_icon))
                    actionSendMailToContact.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(instance!!, R.drawable.ic_contact_email_grey),
                        null, null, null
                    )
                }
            } else {
                email!!.text = getString(R.string.contact_details_empty_email_text)
                email!!.setTextColor(ContextCompat.getColor(instance!!, R.color.contact_form_icon))
                actionSendMailToContact.isEnabled = false
                actionSendMailToContact.setTextColor(ContextCompat.getColor(instance!!, R.color.contact_form_icon))
                actionSendMailToContact.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(instance!!, R.drawable.ic_contact_email_grey),
                    null, null, null
                )
            }
            // Change phone field
            if (contact!!.phone != null) {
                if (contact!!.phone!!.isEmpty()) {
                    // Change text and disable call action
                    mobile!!.text = getString(R.string.contact_details_empty_mobile_text)
                    mobile!!.setTextColor(ContextCompat.getColor(instance!!, R.color.contact_form_icon))
                    actionCallContact.isEnabled = false
                    actionCallContact.setTextColor(ContextCompat.getColor(instance!!, R.color.contact_form_icon))
                    actionCallContact.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(instance!!, R.drawable.ic_contact_mobile_grey),
                        null, null, null
                    )
                }
            } else {
                mobile!!.text = getString(R.string.contact_details_empty_mobile_text)
                mobile!!.setTextColor(ContextCompat.getColor(instance!!, R.color.contact_form_icon))
                actionCallContact.isEnabled = false
                actionCallContact.setTextColor(ContextCompat.getColor(instance!!, R.color.contact_form_icon))
                actionCallContact.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(instance!!, R.drawable.ic_contact_mobile_grey),
                    null, null, null
                )
            }
            // Change comment field
            if (contact!!.comment != null) {
                if (contact!!.comment!!.isEmpty()) {
                    // Change text
                    comment!!.text = getString(R.string.contact_details_empty_comment_text)
                    comment!!.setTextColor(ContextCompat.getColor(instance!!, R.color.contact_form_icon))
                }
            } else {
                comment!!.text = getString(R.string.contact_details_empty_comment_text)
                comment!!.setTextColor(ContextCompat.getColor(instance!!, R.color.contact_form_icon))
            }
        } else {
            // Dismiss dialog and show Toast.
            dismiss()
            Toast.makeText(context, "Unable to show contact details", Toast.LENGTH_SHORT).show()
        }
        actionCallContact.setOnClickListener(this)
        actionSendMailToContact.setOnClickListener(this)

        // Inflate the layout for this fragment
        return view
    }

    private fun exportContactToDrive(contact: Contact?) {
        Log.d(TAG, "Exporting contact (${contact!!.id}) to Google Drive...")
        if (googleDriveService != null) {
            Log.d(TAG, "Creating a file.")
            exportDialog = ProgressDialog.show(
                context, "Exporting Contact to Google Drive",
                "In progress...", true
            )
            exportDialog!!.show()
            Log.d(TAG, "Contact exported info: $contact")
            googleDriveService.createFile(
                GoogleDriveService.TYPE_CONTACT,
                contact.name,
                contact.toString()
            )
                .addOnSuccessListener {
                    Log.d(TAG, "Created file")
                    exportDialog!!.cancel()
                    dismiss()
                    Toast.makeText(instance, "Contact exported", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Couldn't create file.", e)
                    exportDialog!!.cancel()
                    Toast.makeText(instance, "Failed to export Contact", Toast.LENGTH_LONG).show()
                }
        } else {
            Log.e(TAG, "DriveServiceHelper wasn't initialized.")
            Toast.makeText(instance, "Failed to export Contact", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkDrivePermission() {
        if (!GoogleSignIn.hasPermissions(
                GoogleSignIn.getLastSignedInAccount(instance),
                Scope(DriveScopes.DRIVE_FILE)
            )
        ) {
            GoogleSignIn.requestPermissions(
                instance!!.currentFragment!!,
                REQUEST_PERMISSION_SUCCESS_CONTINUE_FILE_CREATION,
                GoogleSignIn.getLastSignedInAccount(instance), Scope(DriveScopes.DRIVE_FILE)
            )
        } else {
            exportContactToDrive(contact)
        }
    }

    private fun deleteContact(contact: Contact?) {
        contactRepository.deleteContact(contact!!)
        detailsDialog!!.dismiss()
    }

    private fun transformMillisToDateString(timeInMillis: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis
        return DateFormat.getDateTimeInstance().format(calendar.time)
    }

    private fun showInfoDialog() {
        val contactInfoLayout = LayoutInflater.from(context)
            .inflate(R.layout.dialog_contact_info, null) as RelativeLayout
        val contactAddedTimestamp =
            contactInfoLayout.findViewById<TextView>(R.id.contact_info_added_timestamp_tv)
        val contactUpdatedTimestamp =
            contactInfoLayout.findViewById<TextView>(R.id.contact_info_updated_timestamp_tv)
        contactAddedTimestamp.text = timestampAdded
        contactUpdatedTimestamp.text = timestampUpdated
        val alertDialogBuilder = AlertDialog.Builder(
            context!!
        ).setTitle(R.string.dialog_info_contact_title)
            .setIcon(R.drawable.ic_menu_info_primary)
            .setView(contactInfoLayout)
            .setPositiveButton("OK") { dialogInterface, _ -> dialogInterface.dismiss() }
        val dialog = alertDialogBuilder.create()
        dialog.show()
    }

    private fun showDeleteContactDialog() {
        val alertDialogBuilder = AlertDialog.Builder(
            context!!
        ).setTitle(R.string.dialog_delete_contact_title)
            .setMessage(R.string.dialog_delete_contact_msg)
            .setIcon(R.drawable.ic_menu_delete_grey)
            .setPositiveButton("Yes") { _, _ -> deleteContact(contact) }
            .setNegativeButton("No") { dialogInterface, _ -> dialogInterface.dismiss() }
        val dialog = alertDialogBuilder.create()
        dialog.show()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.details_call_contact_btn -> {
                val callIntent = Intent(Intent.ACTION_DIAL)
                callIntent.data = Uri.parse("tel:" + contact!!.phone)
                startActivity(callIntent)
            }
            R.id.details_mail_contact_btn -> {
                val mailIntent = Intent(Intent.ACTION_SENDTO)
                mailIntent.data = Uri.parse("mailto:" + contact!!.email)
                startActivity(mailIntent)
            }
        }
    }

    companion object {
        private val TAG = ContactDetailsSheetFragment::class.java.simpleName
        const val CONTACT_KEY = "contact"
        const val PARENT_FRAGMENT_KEY = "parentFragment"
        private const val REQUEST_PERMISSION_SUCCESS_CONTINUE_FILE_CREATION = 999
    }
}