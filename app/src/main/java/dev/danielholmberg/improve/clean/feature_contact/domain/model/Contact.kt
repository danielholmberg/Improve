package dev.danielholmberg.improve.clean.feature_contact.domain.model

import android.os.Parcelable
import android.os.Parcel
import com.google.firebase.database.IgnoreExtraProperties
import dev.danielholmberg.improve.clean.Improve.Companion.instance
import java.util.*

@IgnoreExtraProperties
class Contact(
    var id: String? = null,
    var name: String? = null,
    var companyId: String? = null,
    var email: String? = "",
    var phone: String? = "",
    var comment: String? = "",
    var timestampAdded: String? = null,
    var timestampUpdated: String? = null
) : Parcelable {
    constructor(
        id: String?,
        name: String?,
        companyId: String?,
        email: String?,
        phone: String?,
        comment: String?,
        timestampAdded: String?
    ) : this() {
        this.id = id
        this.name = name
        this.companyId = companyId
        this.email = email
        this.phone = phone
        this.comment = comment
        this.timestampAdded = timestampAdded
    }

    constructor(`in`: Parcel) : this() {
        id = `in`.readString()
        name = `in`.readString()
        companyId = `in`.readString()
        email = `in`.readString()
        phone = `in`.readString()
        comment = `in`.readString()
        timestampAdded = `in`.readString()
        timestampUpdated = `in`.readString()
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(companyId)
        parcel.writeString(email)
        parcel.writeString(phone)
        parcel.writeString(comment)
        parcel.writeString(timestampAdded)
        parcel.writeString(timestampUpdated)
    }

    override fun toString(): String {
        var contactAsString = """
               BEGIN:VCARD
               VERSION:3.0
               FN:${name}
               
               """.trimIndent()
        val company = instance!!.companies[companyId]
        if (company != null) {
            contactAsString += """
                ORG:${company.name}
                
                """.trimIndent()
        }
        if (phone != null) {
            contactAsString += """
                TEL;TYPE=HOME,VOICE:${phone}
                
                """.trimIndent()
        }
        if (email != null) {
            contactAsString += """
                EMAIL;TYPE=PREF,INTERNET:${email}
                
                """.trimIndent()
        }
        if (comment != null) {
            contactAsString += """
                NOTE:${comment}
                
                """.trimIndent()
        }
        contactAsString += "END:VCARD\r\n"
        return contactAsString
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val contact = other as Contact
        return id == contact.id &&
                name == contact.name &&
                companyId == contact.companyId &&
                email == contact.email &&
                phone == contact.phone &&
                comment == contact.comment
    }

    override fun hashCode(): Int {
        return Objects.hash(id, name, companyId, email, phone, comment)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Contact> = object : Parcelable.Creator<Contact> {
            override fun createFromParcel(`in`: Parcel): Contact {
                return Contact(`in`)
            }

            override fun newArray(size: Int): Array<Contact?> {
                return arrayOfNulls(size)
            }
        }
    }
}