package dev.danielholmberg.improve.clean.feature_note.presentation.notes.view_holder

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
import dev.danielholmberg.improve.clean.Improve.Companion.instance
import dev.danielholmberg.improve.clean.feature_note.domain.model.Tag

class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private lateinit var tag: Tag
    private lateinit var tagLabel: TextView
    private lateinit var tagEnabledIndicator: ImageView
    private lateinit var tagDeleteButton: ImageView
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
        tagDeleteButton = itemView.findViewById(R.id.tag_delete_btn)

        if (tag.label != null) tagLabel.text = tag.label
        if (tag.textColor != null) tagLabel.setTextColor(Color.parseColor(tag.textColor))
        tagDeleteButton.setOnClickListener {
            val alertDialogBuilder = AlertDialog.Builder(itemView.context)
                .setTitle(R.string.dialog_delete_tag_title)
                .setMessage(R.string.dialog_delete_tag_msg)
                .setPositiveButton("Yes") { _, _ ->

                    // TODO: Should be move to UseCase

                    instance!!.tagRepository.deleteTag(
                        tag.id
                    )
                }
                .setNegativeButton("No") { dialogInterface, _ -> dialogInterface.dismiss() }
            val dialog = alertDialogBuilder.create()
            dialog.show()
        }

        val gradientDrawable = itemView.background as StateListDrawable
        val drawableContainerState: DrawableContainer.DrawableContainerState? =
            gradientDrawable.constantState as DrawableContainer.DrawableContainerState?
        if (drawableContainerState != null) {
            val children = drawableContainerState.children
            background = children[0] as GradientDrawable
            background!!.setColor(Color.parseColor(tag.color))
        }
    }

    fun setTagStatusOnNote(isEnabled: Boolean) {
        if (isEnabled) {
            tagEnabledIndicator.visibility = View.VISIBLE
        } else {
            tagEnabledIndicator.visibility = View.GONE
        }
    }

    fun setEditMode(editMode: Boolean) {
        tagDeleteButton.visibility = if (editMode) View.VISIBLE else View.GONE
    }
}