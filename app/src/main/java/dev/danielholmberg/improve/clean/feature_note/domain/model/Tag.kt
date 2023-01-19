package dev.danielholmberg.improve.clean.feature_note.domain.model

import android.os.Parcelable
import android.os.Parcel

class Tag(
    var id: String? = null,
    var label: String? = null,
    var color: String? = DEFAULT_COLOR,
    var textColor: String? = null
) : Parcelable {

    constructor(id: String?) : this() {
        this.id = id
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeString(id)
        parcel.writeString(label)
        parcel.writeString(color)
    }

    constructor(`in`: Parcel) : this() {
        id = `in`.readString()
        label = `in`.readString()
        color = `in`.readString() ?: DEFAULT_COLOR
    }

    companion object {
        private const val DEFAULT_COLOR = "#FFFFFF"

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