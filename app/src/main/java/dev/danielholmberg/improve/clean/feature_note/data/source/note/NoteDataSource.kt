package dev.danielholmberg.improve.clean.feature_note.data.source.note

import com.google.firebase.database.ChildEventListener
import dev.danielholmberg.improve.clean.feature_note.data.source.entity.NoteEntity


interface NoteDataSource {
    fun addNote(noteEntity: NoteEntity)
    fun updateNote(updatedNoteEntity: NoteEntity)
    fun deleteNote(noteEntityToDelete: NoteEntity, onSuccess: () -> Unit)
    fun saveNotes(noteEntities: Map<String?, NoteEntity>)
    fun addArchivedNote(archivedNoteEntity: NoteEntity)
    fun updateArchivedNote(archivedNoteEntity: NoteEntity)
    fun deleteNoteFromArchive(noteEntityToDelete: NoteEntity, onSuccess: () -> Unit)
    fun saveArchivedNotes(archivedNoteEntities: Map<String?, NoteEntity>)
    fun addChildEventListener(childEventListener: ChildEventListener)
    fun addChildEventListenerForArchive(childEventListener: ChildEventListener)
    fun generateNewNoteId(): String?
}