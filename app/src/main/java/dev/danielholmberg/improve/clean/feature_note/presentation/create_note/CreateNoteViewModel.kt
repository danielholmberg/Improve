package dev.danielholmberg.improve.clean.feature_note.presentation.create_note

import androidx.lifecycle.ViewModel
import dev.danielholmberg.improve.clean.feature_note.domain.model.Image
import dev.danielholmberg.improve.clean.feature_note.domain.model.Note
import dev.danielholmberg.improve.clean.feature_note.domain.model.Tag
import dev.danielholmberg.improve.clean.feature_note.domain.use_case.ImageUseCases
import dev.danielholmberg.improve.clean.feature_note.domain.use_case.NoteUseCases
import dev.danielholmberg.improve.clean.feature_note.domain.use_case.TagUseCases
import dev.danielholmberg.improve.clean.feature_note.presentation.util.ImageCallback

class CreateNoteViewModel(
    private val noteUseCases: NoteUseCases,
    private val tagUseCases: TagUseCases,
    private val imageUseCases: ImageUseCases
) : ViewModel() {

    var note: Note = Note()
    var tagIdList: ArrayList<String?> = ArrayList()

    fun generateNewNote() {
        note = noteUseCases.generateNewNoteUseCase()
    }

    fun generateNewTag(label: String, color: String): Tag {
        return tagUseCases.generateNewTagUseCase(label, color)
    }

    fun addTag(tag: Tag) {
        tagUseCases.addTagUseCase(tag)
        note.addTag(tag.id!!)
        tagIdList.add(tag.id)
    }

    fun addNote(title: String, info: String) {
        val timestampAdded = System.currentTimeMillis().toString()
        note.title = title
        note.info = info
        note.addedAt = timestampAdded
        note.updatedAt = timestampAdded
        note.isArchived = false
        noteUseCases.addNoteUseCase(note)
    }

    fun uploadMultipleImages(imageList: ArrayList<Image>, callback: ImageCallback) {
        imageUseCases.uploadImagesUseCase(imageList, callback)
    }

}
