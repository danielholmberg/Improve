package dev.danielholmberg.improve.clean.feature_note.presentation.notes.view_holder

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import dev.danielholmberg.improve.R
import com.google.android.flexbox.FlexboxLayout
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import android.os.Bundle
import android.util.Log
import android.view.View
import dev.danielholmberg.improve.BuildConfig
import dev.danielholmberg.improve.clean.Improve.Companion.instance
import dev.danielholmberg.improve.clean.feature_note.domain.model.Note
import dev.danielholmberg.improve.clean.feature_note.domain.model.Image
import dev.danielholmberg.improve.clean.feature_note.presentation.note_details.NoteDetailsDialogFragment
import dev.danielholmberg.improve.clean.feature_note.presentation.note_details.NoteDetailsViewModel.Companion.NOTE_ADAPTER_POS_KEY
import dev.danielholmberg.improve.clean.feature_note.presentation.note_details.NoteDetailsViewModel.Companion.NOTE_KEY
import dev.danielholmberg.improve.clean.feature_note.presentation.notes.adapter.ImagesAdapter

class ArchivedNoteViewHolder(private val context: Context, itemView: View, parent: ViewGroup) :
    RecyclerView.ViewHolder(
        itemView
    ) {
    private val parent: ViewGroup
    private var note: Note? = null
    private var title: TextView? = null

    init {
        this.parent = parent
    }

    /**
     * Binds data from Note (Model) object to related View.
     *
     * OBS! Because the RecyclerView reuses old ViewHolders in the list, we therefore
     * need to define ALL Views of each Note to make them display specific information
     * for targeted Note!
     *
     * @param note - Target Note (Model)
     */
    fun bindModelToView(note: Note?) {
        if (note == null) return
        this.note = note
        title = itemView.findViewById<View>(R.id.item_archived_note_title_tv) as TextView
        val imagesRecyclerView =
            itemView.findViewById<View>(R.id.images_thumbnail_list) as RecyclerView
        val additionalImagesIndicator =
            itemView.findViewById<TextView>(R.id.images_extras_indicator)
        val tagsList = itemView.findViewById<View>(R.id.footer_note_tags_list) as FlexboxLayout
        if (note.title != null) title!!.text = note.title

        // Reset Tag list view in Footer.
        tagsList.removeAllViews()

        // Populate Tag list view in Footer.
        for (tagId in note.tags.keys) {
            val tagView = LayoutInflater.from(context).inflate(R.layout.item_tag, parent, false)

            // Create a Tag View and add it to the Tag list view.
            val tagViewHolder = TagViewHolder(tagView)
            val tag = instance!!.tagsAdapter!!.getTag(tagId)
            tagViewHolder.bindModelToView(tag)
            tagsList.addView(tagView)
        }

        if (note.hasImage()) {
            val imagesAdapter = ImagesAdapter(true)
            imagesRecyclerView.adapter = imagesAdapter
            val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            imagesRecyclerView.layoutManager = layoutManager

            imagesRecyclerView.visibility = View.VISIBLE
            additionalImagesIndicator.visibility = View.GONE

            var thumbnails = 0
            val maxThumbnails = 2
            Log.d(TAG, "Total number of images attached to Note: " + note.images.size)
            for (imageId in note.images) {
                Log.d(TAG, "Thumbnail nr $thumbnails with id: $imageId")
                if (thumbnails <= maxThumbnails) {
                    thumbnails++
                    imagesAdapter.add(Image(imageId))
                } else {
                    // Show number indicator on total amount of attached images
                    additionalImagesIndicator.text = (note.images.size - thumbnails).let {
                        if (it >= 100) "${99}+" else it.toString()
                    }
                    additionalImagesIndicator.visibility = View.VISIBLE
                    break
                }
            }
        } else {
            // Remove VIP Views from layout
            imagesRecyclerView.visibility = View.GONE
            additionalImagesIndicator.visibility = View.GONE
        }

        // Set OnClickListener to display a DialogFragment to show all the details.
        itemView.setOnClickListener { showNoteDetailDialog() }
    }

    /**
     * Triggers a DialogFragment to show detailed content of the target Note.
     */
    private fun showNoteDetailDialog() {
        val fm = instance!!.currentFragment!!.fragmentManager
        val noteDetailsDialogFragment = NoteDetailsDialogFragment.newInstance()
        noteDetailsDialogFragment.arguments = createBundle(note, adapterPosition)
        noteDetailsDialogFragment.show(fm!!, NoteDetailsDialogFragment.TAG)
    }

    /**
     * Creates a Bundle object to be passed to the Note details DialogFragment.
     * @param note - Target Note
     * @param itemPos - Position of targeted Note in the SortedList
     * @return Bundle with all important data to display details of targeted Note.
     */
    private fun createBundle(note: Note?, itemPos: Int): Bundle {
        val bundle = Bundle()
        bundle.putParcelable(NOTE_KEY, note)
        bundle.putInt(NOTE_ADAPTER_POS_KEY, itemPos)
        return bundle
    }

    companion object {
        private val TAG = BuildConfig.TAG + ArchivedNoteViewHolder::class.java.simpleName
    }
}