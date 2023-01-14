package dev.danielholmberg.improve.ViewHolders

import dev.danielholmberg.improve.Improve.Companion.instance
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import android.graphics.drawable.GradientDrawable
import dev.danielholmberg.improve.R
import android.graphics.Color
import android.graphics.drawable.DrawableContainer
import android.graphics.drawable.StateListDrawable
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import dev.danielholmberg.improve.Models.Tag

class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var tag: Tag? = null
    private var tagLabel: TextView? = null
    private var tagEnabledIndicator: ImageView? = null
    private var background: GradientDrawable? = null

    /**
     * Binds data from Tag (Model) object to related View.
     *
     * OBS! Because the RecyclerView reuses old ViewHolders in the list, we therefore
     * need to define ALL Views of each Tag to make them display specific information
     * for targeted Tag!
     *
     * @param tag - Target Tag (Model)
     */
    fun bindModelToView(tag: Tag?) {
        if (tag == null) return
        this.tag = tag
        tagLabel = itemView.findViewById<View>(R.id.tag_label_tv) as TextView
        tagEnabledIndicator = itemView.findViewById<View>(R.id.tag_enabled_indicator) as ImageView
        if (tag.label != null) tagLabel!!.text = tag.label
        if (tag.textColor != null) tagLabel!!.setTextColor(Color.parseColor(tag.textColor))
        val gradientDrawable = itemView.background as StateListDrawable
        val drawableContainerState =
            gradientDrawable.constantState as DrawableContainer.DrawableContainerState?
        if (drawableContainerState != null) {
            val children = drawableContainerState.children
            background = children[0] as GradientDrawable
            background!!.setColor(Color.parseColor(tag.color))
        }
    }

    fun setTagStatusOnNote(isEnabled: Boolean) {
        if (isEnabled) {
            tagEnabledIndicator!!.visibility = View.VISIBLE
        } else {
            tagEnabledIndicator!!.visibility = View.GONE
        }
    }

    fun setEditMode(editMode: Boolean) {
        val deleteButton = itemView.findViewById<ImageView>(R.id.tag_delete_btn)
        if (editMode) {
            deleteButton.visibility = View.VISIBLE
            deleteButton.setOnClickListener {
                val alertDialogBuilder = AlertDialog.Builder(itemView.context)
                    .setTitle(R.string.dialog_delete_tag_title)
                    .setMessage(R.string.dialog_delete_tag_msg)
                    .setPositiveButton("Yes") { _, _ ->
                        instance!!.databaseManager.deleteTag(
                            tag!!.id
                        )
                    }
                    .setNegativeButton("No") { dialogInterface, _ -> dialogInterface.dismiss() }
                val dialog = alertDialogBuilder.create()
                dialog.show()
            }
        } else {
            deleteButton.visibility = View.GONE
        }
    }
}