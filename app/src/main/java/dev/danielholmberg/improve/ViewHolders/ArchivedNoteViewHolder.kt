package dev.danielholmberg.improve.ViewHolders

import android.content.Context
import dev.danielholmberg.improve.Improve.Companion.instance
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import dev.danielholmberg.improve.R
import com.google.android.flexbox.FlexboxLayout
import android.view.LayoutInflater
import dev.danielholmberg.improve.Adapters.VipImagesAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import dev.danielholmberg.improve.Models.VipImage
import dev.danielholmberg.improve.Fragments.NoteDetailsDialogFragment
import android.os.Bundle
import android.util.Log
import android.view.View
import dev.danielholmberg.improve.Models.Note

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
        val vipImagesRecyclerView =
            itemView.findViewById<View>(R.id.vip_images_thumbnail_list) as RecyclerView
        val additionalImagesIndicator =
            itemView.findViewById<TextView>(R.id.vip_images_additionals_indicator)
        val tagsList = itemView.findViewById<View>(R.id.footer_note_tags_list) as FlexboxLayout
        if (note.title != null) title!!.text = note.title

        // Reset Tag list view in Footer.
        tagsList.removeAllViews()

        // Populate Tag list view in Footer.
        for (tagId in note.getTags().keys) {
            val tagView = LayoutInflater.from(context).inflate(R.layout.item_tag, parent, false)

            // Create a Tag View and add it to the Tag list view.
            val tagViewHolder = TagViewHolder(tagView)
            val tag = instance!!.tagsAdapter!!.getTag(tagId)
            tagViewHolder.bindModelToView(tag)
            tagsList.addView(tagView)
        }

        // Note has VIP image or not
        if (note.hasImage()) {
            val vipImagesAdapter = VipImagesAdapter(note.id!!, true)
            vipImagesRecyclerView.adapter = vipImagesAdapter
            val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            vipImagesRecyclerView.layoutManager = layoutManager
            vipImagesRecyclerView.visibility = View.VISIBLE
            additionalImagesIndicator.visibility = View.GONE
            var thumbnails = 0
            val maxThumbnails = 2
            Log.d(TAG, "Total number of images attached to Note: " + note.vipImages.size)
            for (vipImageId in note.vipImages) {
                thumbnails++
                Log.d(TAG, "Thumbnail nr $thumbnails with id: $vipImageId")
                if (thumbnails <= maxThumbnails) {
                    vipImagesAdapter.add(VipImage(vipImageId))
                } else {
                    // Show number indicator on total amount of attached images
                    val numberOfAdditionalImages = note.vipImages.size - maxThumbnails
                    additionalImagesIndicator.text = instance!!.resources
                        .getString(
                            R.string.vip_images_additionals_indicator,
                            numberOfAdditionalImages
                        )
                    additionalImagesIndicator.visibility = View.VISIBLE
                    break
                }
            }
        } else {
            // Remove VIP Views from layout
            vipImagesRecyclerView.visibility = View.GONE
            additionalImagesIndicator.visibility = View.GONE
        }

        // Set OnClickListener to display a DialogFragment to show all the details.
        itemView.setOnClickListener { showNoteDetailDialog() }
    }

    /**
     * Triggers a DialogFragment to show detailed content of the target Note.
     */
    private fun showNoteDetailDialog() {
        val fm = instance!!.archivedNotesFragmentRef!!.fragmentManager
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
        bundle.putParcelable(NoteDetailsDialogFragment.NOTE_KEY, note)
        bundle.putInt(
            NoteDetailsDialogFragment.NOTE_PARENT_FRAGMENT_KEY,
            R.integer.ARCHIVED_NOTES_FRAGMENT
        )
        bundle.putInt(NoteDetailsDialogFragment.NOTE_ADAPTER_POS_KEY, itemPos)
        return bundle
    }

    companion object {
        private val TAG = ArchivedNoteViewHolder::class.java.simpleName
    }
}