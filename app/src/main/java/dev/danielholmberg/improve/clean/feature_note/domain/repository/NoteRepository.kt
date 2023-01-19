package dev.danielholmberg.improve.clean.feature_note.domain.repository

import com.google.firebase.database.ChildEventListener
import dev.danielholmberg.improve.clean.feature_note.domain.model.Note


interface NoteRepository {
    fun addNote(note: Note)
    fun updateNote(updatedNote: Note)
    fun deleteNote(noteToDelete: Note)
    fun saveNotes(notes: HashMap<String?, Note>)
    fun archiveNote(note: Note)
    fun unarchiveNote(note: Note)
    fun addArchivedNote(archivedNote: Note)
    fun updateArchivedNote(archivedNote: Note)
    fun deleteNoteFromArchive(noteToDelete: Note)
    fun saveArchivedNotes(archivedNotes: HashMap<String?, Note>)
    fun addChildEventListener(childEventListener: ChildEventListener)
    fun addChildEventListenerForArchive(childEventListener: ChildEventListener)
    fun generateNewNoteId(): String?
}
