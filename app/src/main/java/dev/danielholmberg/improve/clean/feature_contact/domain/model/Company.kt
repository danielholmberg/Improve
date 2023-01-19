package dev.danielholmberg.improve.clean.feature_contact.domain.model

import android.os.Parcelable
import android.os.Parcel
import com.google.firebase.database.IgnoreExtraProperties
import java.util.*
import kotlin.collections.HashMap

@IgnoreExtraProperties
class Company(
    var id: String? = null,
    var name: String? = null,
    var contacts: HashMap<String?, Contact> = HashMap()
) : Parcelable {

    constructor(id: String?, name: String?) : this() {
        this.id = id
        this.name = name
    }

    @JvmName("setContacts1")
    fun setContacts(contacts: HashMap<String?, Contact>) {
        this.contacts = contacts
    }

    constructor(`in`: Parcel) : this() {
        id = `in`.readString()
        name = `in`.readString()
        `in`.readMap(contacts, Contact::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeMap(contacts)
    }

    override fun toString(): String {
        return name!!
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val company = other as Company
        return id == company.id &&
                name == company.name &&
                contacts == company.contacts
    }

    override fun hashCode(): Int {
        return Objects.hash(id, name, contacts)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Company> = object : Parcelable.Creator<Company> {
            override fun createFromParcel(`in`: Parcel): Company {
                return Company(`in`)
            }

            override fun newArray(size: Int): Array<Company?> {
                return arrayOfNulls(size)
            }
        }
    }
}