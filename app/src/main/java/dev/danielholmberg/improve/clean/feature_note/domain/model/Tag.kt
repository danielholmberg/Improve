package dev.danielholmberg.improve.clean.feature_note.domain.model

import android.os.Parcelable
import android.os.Parcel
import androidx.core.content.ContextCompat
import dev.danielholmberg.improve.R
import dev.danielholmberg.improve.clean.Improve

class Tag(
    var id: String? = null,
    var label: String? = null,
    var color: String? = DEFAULT_COLOR.toHex(),
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
        color = `in`.readString() ?: DEFAULT_COLOR.toHex()
    }

    companion object {
        val DEFAULT_COLOR = Color(R.color.tagColorNull)
        val DEFAULT_TEXT_COLOR = Color(R.color.tagTextColorNull)
        const val CLEAR_BACKGROUND = R.drawable.ic_tag_no_color
        val tagTextColor = Color(R.color.tagTextColor)
        val tagColors =
            listOf(
                Color(R.color.tagColor1),
                Color(R.color.tagColor2),
                Color(R.color.tagColor3),
                Color(R.color.tagColor4),
                Color(R.color.tagColor5),
                Color(R.color.tagColor6),
                Color(R.color.tagColor7),
                Color(R.color.tagColor8)
            )
        val tagUncheckedBackgrounds = listOf(
            R.drawable.ic_tag_1,
            R.drawable.ic_tag_2,
            R.drawable.ic_tag_3,
            R.drawable.ic_tag_4,
            R.drawable.ic_tag_5,
            R.drawable.ic_tag_6,
            R.drawable.ic_tag_7,
            R.drawable.ic_tag_8
        )
        val tagCheckedBackgrounds = listOf(
            R.drawable.ic_tag_1_checked,
            R.drawable.ic_tag_2_checked,
            R.drawable.ic_tag_3_checked,
            R.drawable.ic_tag_4_checked,
            R.drawable.ic_tag_5_checked,
            R.drawable.ic_tag_6_checked,
            R.drawable.ic_tag_7_checked,
            R.drawable.ic_tag_8_checked
        )

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

    data class Color(
        val color: Int
    ) {
        fun toHex(): String {
            return "#${Integer.toHexString(ContextCompat.getColor(Improve.instance!!, color))}"
        }
    }
}