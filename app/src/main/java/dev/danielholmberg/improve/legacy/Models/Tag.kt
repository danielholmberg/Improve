package dev.danielholmberg.improve.legacy.Models

import android.os.Parcelable
import android.os.Parcel
import java.util.*

class Tag : Parcelable {
    var id: String? = null
    var label: String? = null
        private set
    var color: String? = null
    var textColor: String? = null

    constructor()
    constructor(id: String?) {
        this.id = id
    }

    fun setLabel(label: String) {
        this.label = label.uppercase(Locale.getDefault())
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeString(id)
        parcel.writeString(label)
        parcel.writeString(color)
    }

    constructor(`in`: Parcel) {
        id = `in`.readString()
        label = `in`.readString()
        color = `in`.readString()
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Tag> = object : Parcelable.Creator<Tag> {
            override fun createFromParcel(`in`: Parcel): Tag {
                return Tag(`in`)
            }

            override fun newArray(size: Int): Array<Tag?> {
                return arrayOfNulls(size)
            }
        }
    }
}