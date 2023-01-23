package dev.danielholmberg.improve.clean.feature_note.presentation.note_details

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.lifecycle.ViewModel
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.FlexboxLayoutManager
import dev.danielholmberg.improve.R
import dev.danielholmberg.improve.clean.Improve.Companion.instance
import dev.danielholmberg.improve.clean.core.GoogleDriveService
import dev.danielholmberg.improve.clean.feature_note.domain.model.Image
import dev.danielholmberg.improve.clean.feature_note.domain.model.Note
import dev.danielholmberg.improve.clean.feature_note.domain.model.Tag
import dev.danielholmberg.improve.clean.feature_note.domain.use_case.ImageUseCases
import dev.danielholmberg.improve.clean.feature_note.domain.use_case.NoteDetailsUseCases
import dev.danielholmberg.improve.clean.feature_note.domain.use_case.TagUseCases
import dev.danielholmberg.improve.clean.feature_note.presentation.notes.adapter.ImagesAdapter
import dev.danielholmberg.improve.clean.feature_note.presentation.notes.adapter.TagsAdapter
import dev.danielholmberg.improve.clean.feature_note.presentation.notes.view_holder.TagViewHolder
import dev.danielholmberg.improve.clean.feature_note.presentation.util.ImageCallback
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList

class NoteDetailsViewModel(
    private val noteDetailsUseCases: NoteDetailsUseCases,
    private val tagUseCases: TagUseCases,
    private val imageUseCases: ImageUseCases
) : ViewModel() {

    private var noteBundle: Bundle? = null
    private lateinit var originalNote: Note
    private var isOriginallyStared = false
    private var updatedNote: Note = Note()
    var editMode = false

    private var noteId: String? = null
    private var noteTitle: String? = null
    private var noteInfo: String? = null
    private var noteTimestampAdded: String? = null
    private var noteTimestampUpdated: String? = null

    var imagesAdapter: ImagesAdapter = ImagesAdapter(false)
    private var newImages: ArrayList<Image> = ArrayList()
    private var oldImages: ArrayList<Image> = ArrayList()
    var imagesViewVisibility: Int = View.GONE

    private var tagsAdapter: TagsAdapter? = instance?.tagsAdapter
    private var oldTags: MutableMap<String, Boolean> = mutableMapOf()

    fun parseIncomingBundle(arguments: Bundle?): Boolean {
        noteBundle = arguments

        var parsedNote: Note? = null
        noteBundle?.let { bundle ->
            parsedNote = bundle.getParcelable<Parcelable>(NOTE_KEY) as Note?
            parsedNote?.let { originalNote = it }
        }

        val successfullyParsedNote = parsedNote != null
        return successfullyParsedNote
    }

    fun generateNewTag(label: String, color: String): Tag {
        return tagUseCases.generateNewTagUseCase(label, color)
    }

    fun createTag(tag: Tag) {
        tagUseCases.addTagUseCase(tag)
        tagsAdapter!!.addTag(tag)
    }

    fun updateNote(title: String, info: String) {
        updatedNote.id = originalNote.id
        updatedNote.isArchived = originalNote.isArchived
        updatedNote.isStared = originalNote.isStared
        updatedNote.addedAt = originalNote.addedAt
        updatedNote.updatedAt = System.currentTimeMillis().toString()
        updatedNote.tags.putAll(originalNote.tags)

        updatedNote.title = title
        updatedNote.info = info

        originalNote = updatedNote
        oldTags.clear()
        oldTags.putAll(updatedNote.tags)

        oldImages = imagesAdapter.imageList
        updatedNote.images = oldImages.map { image -> image.id } as ArrayList<String>
        newImages.clear()

        if (originalNote.isArchived) {
            noteDetailsUseCases.updateArchivedNoteUseCase(updatedNote)
        } else {
            noteDetailsUseCases.updateNoteUseCase(updatedNote)
        }
    }

    fun deleteNote() {
        if (originalNote.isArchived) {
            noteDetailsUseCases.deleteNoteFromArchiveUseCase(originalNote)
        } else {
            noteDetailsUseCases.deleteNoteUseCase(originalNote)
        }
    }

    fun archiveNote() {
        noteDetailsUseCases.archiveNoteUseCase(originalNote)
    }

    fun unarchiveNote() {
        noteDetailsUseCases.unarchiveNoteUseCase(originalNote)
    }

    fun uploadMultipleImages(imageList: ArrayList<Image>, callback: ImageCallback) {
        Log.d(TAG, "Uploading ${imagesAdapter.itemCount} image(s)")
        imageUseCases.uploadImagesUseCase(imageList, callback)
    }

    fun discardNoteChanges() {
        if (originalNote.hasImage()) {
            imagesAdapter.clear()
            imagesAdapter.addImages(oldImages)
        }
        originalNote.setTags(oldTags)
        updatedNote = originalNote
    }

    fun initializeNoteDetails(): Boolean {
        if (originalNote.id == null) return false

        if (instance!!.isVipUser && originalNote.hasImage()) {
            oldImages.clear()
            originalNote.images.forEach { imageId -> oldImages.add(Image(imageId)) }
            imagesAdapter.clear()
            imagesAdapter.addImages(oldImages)
            Log.d(TAG, "Initial images count: ${oldImages.size}")
        }

        tagsAdapter = instance!!.tagsAdapter

        oldTags.putAll(originalNote.tags)

        imagesViewVisibility = if (originalNote.hasImage()) View.VISIBLE else View.GONE

        return true
    }

    fun populateNoteDetails(inputTitle: TextView, inputInfo: TextView) {
        noteId = originalNote.id
        noteTitle = originalNote.title?.also { inputTitle.text = it }
        noteInfo = originalNote.info?.also { inputInfo.text = it }

        noteTimestampAdded = originalNote.addedAt?.let { transformMillisToDateString(it.toLong()) }
        noteTimestampUpdated = originalNote.updatedAt?.let { transformMillisToDateString(it.toLong()) }
    }

    private fun transformMillisToDateString(timeInMillis: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis
        return DateFormat.getDateTimeInstance().format(calendar.time)
    }

    fun toggleEditMode() {
        editMode = !editMode
    }

    fun toggleStaredNote() {
        originalNote.isStared = !originalNote.isStared
    }

    fun isNoteStared(): Boolean {
        return originalNote.isStared
    }

    fun isNoteArchived(): Boolean {
        return originalNote.isArchived
    }

    fun setCurrentNoteForTagsAdapter() {
        tagsAdapter!!.setCurrentNote(originalNote.copy())
    }

    fun onEditTagsDone() {
        tagsAdapter!!.removeCurrentNote()
        tagsAdapter!!.setEditMode(false)
    }

    fun onEditTags() {
        tagsAdapter!!.setEditMode(true)
    }

    fun onEditImagesDone() {
        imagesAdapter.setEditMode(false)
    }

    fun onEditImages() {
        isOriginallyStared = isNoteStared()
        imagesAdapter.setEditMode(true)
    }

    fun scrollToPosition(layoutManager: FlexboxLayoutManager) {
        layoutManager.scrollToPosition(tagsAdapter!!.itemCount - 1)
    }


    fun addImageToAdapter(filePath: String) {
        val imageId = UUID.randomUUID().toString()
        val image = Image(imageId)
        image.originalFilePath = filePath
        imagesAdapter.add(image)
    }

    fun onImagesUploaded(uploadedImages: ArrayList<Image>) {
        newImages = uploadedImages
        newImages.forEach { image -> updatedNote.images.add(image.id!!) }
        oldImages.addAll(newImages)
    }

    fun imagesToUpload(): java.util.ArrayList<Image> {
        return imagesAdapter.imageList.filter { image ->
            !oldImages.contains(image)
        } as ArrayList<Image>
    }

    fun exportNoteToGoogleDrive(onSuccess: () -> Unit, onFailure: (e: String?) -> Unit) {
        instance?.googleDriveService?.createFile(
            GoogleDriveService.TYPE_NOTE,
            originalNote.title,
            originalNote.toString()
        )
            ?.addOnSuccessListener { onSuccess() }
            ?.addOnFailureListener { e -> onFailure(e.message) }
    }

    fun populateNoteInfo(noteInfoLayout: RelativeLayout) {
        noteInfoLayout.findViewById<TextView>(R.id.note_info_added_timestamp_tv).text = noteTimestampAdded
        noteInfoLayout.findViewById<TextView>(R.id.note_info_updated_timestamp_tv).text = noteTimestampUpdated
    }

    fun getInputVisibility(): Int {
        return if (editMode) {
            View.VISIBLE
        } else {
            noteInfo?.let { if (it.isBlank()) {
                View.GONE
            } else {
                View.VISIBLE
            } } ?: View.GONE
        }
    }

    fun renderExistingTagList(tagsFooterListLayout: FlexboxLayout) {
        renderTagList(tagsFooterListLayout, originalNote.tags)
    }

    fun renderOldTagList(tagsFooterListLayout: FlexboxLayout) {
        renderTagList(tagsFooterListLayout, oldTags)
    }

    private fun renderTagList(tagsFooterListLayout: FlexboxLayout, tags: MutableMap<String, Boolean>) {
        tagsFooterListLayout.removeAllViews()
        for (tagId: String? in tags.keys) {
            val tagView = LayoutInflater.from(instance).inflate(R.layout.item_tag, tagsFooterListLayout, false)
            val tagViewHolder = TagViewHolder(tagView)
            tagViewHolder.bindModelToView(tagsAdapter!!.getTag(tagId))
            tagsFooterListLayout.addView(tagView)
        }
    }

    companion object {
        private val TAG: String = NoteDetailsViewModel::class.java.simpleName
        const val NOTE_ADAPTER_POS_KEY = "adapterItemPos"
        const val NOTE_KEY = "originalNote"
    }

}
