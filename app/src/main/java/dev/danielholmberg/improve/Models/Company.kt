package dev.danielholmberg.improve.Models

import android.os.Parcelable
import android.os.Parcel
import java.util.*

class Company : Parcelable {
    var id: String? = null
    var name: String? = null
    private var contactsList: HashMap<String, Any>? = null

    constructor() {}
    constructor(id: String?, name: String?) {
        this.id = id
        this.name = name
    }

    constructor(id: String?, name: String?, contacts: HashMap<String, Any>?) {
        this.id = id
        this.name = name
        contactsList = contacts
    }

    val contacts: HashMap<String, Any>?
        get() = contactsList

    fun setContacts(contactsList: HashMap<String, Any>?) {
        this.contactsList = contactsList
    }

    override fun toString(): String {
        return name!!
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeMap(contactsList)
    }

    protected constructor(`in`: Parcel) {
        id = `in`.readString()
        name = `in`.readString()
        contactsList = `in`.readHashMap(HashMap::class.java.classLoader) as HashMap<String, Any>?
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val company = other as Company
        return id == company.id &&
                name == company.name &&
                contactsList == company.contactsList
    }

    override fun hashCode(): Int {
        return Objects.hash(id, name, contactsList)
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