package dev.danielholmberg.improve.domain.repository

import dev.danielholmberg.improve.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getNotes(): Flow<List<Note>>
    fun getArchivedNotes(): Flow<List<Note>>
    suspend fun addNote(note: Note): Result<Unit>
    suspend fun updateNote(note: Note): Result<Unit>
    suspend fun deleteNote(noteId: String): Result<Unit>
    suspend fun archiveNote(note: Note): Result<Unit>
    suspend fun unarchiveNote(note: Note): Result<Unit>
    suspend fun starNote(noteId: String, starred: Boolean): Result<Unit>
}
