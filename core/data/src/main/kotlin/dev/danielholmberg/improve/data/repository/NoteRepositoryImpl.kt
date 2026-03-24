package dev.danielholmberg.improve.data.repository

import dev.danielholmberg.improve.data.firebase.datasource.FirebaseArchivedNoteDataSource
import dev.danielholmberg.improve.data.firebase.datasource.FirebaseNoteDataSource
import dev.danielholmberg.improve.data.mapper.NoteMapper
import dev.danielholmberg.improve.domain.model.Note
import dev.danielholmberg.improve.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val noteDataSource: FirebaseNoteDataSource,
    private val archivedNoteDataSource: FirebaseArchivedNoteDataSource
) : NoteRepository {

    override fun getNotes(): Flow<List<Note>> = flow {
        val notes = mutableMapOf<String, Note>()
        noteDataSource.observeNotes().collect { event ->
            when (event) {
                is FirebaseNoteDataSource.NoteEvent.Added -> {
                    val note = NoteMapper.toDomain(event.dto)
                    notes[note.id] = note
                }
                is FirebaseNoteDataSource.NoteEvent.Changed -> {
                    val note = NoteMapper.toDomain(event.dto)
                    notes[note.id] = note
                }
                is FirebaseNoteDataSource.NoteEvent.Removed -> {
                    notes.remove(event.id)
                }
            }
            emit(notes.values.toList())
        }
    }

    override fun getArchivedNotes(): Flow<List<Note>> = flow {
        val notes = mutableMapOf<String, Note>()
        archivedNoteDataSource.observeArchivedNotes().collect { event ->
            when (event) {
                is FirebaseArchivedNoteDataSource.ArchivedNoteEvent.Added -> {
                    val note = NoteMapper.toDomain(event.dto)
                    notes[note.id] = note
                }
                is FirebaseArchivedNoteDataSource.ArchivedNoteEvent.Changed -> {
                    val note = NoteMapper.toDomain(event.dto)
                    notes[note.id] = note
                }
                is FirebaseArchivedNoteDataSource.ArchivedNoteEvent.Removed -> {
                    notes.remove(event.id)
                }
            }
            emit(notes.values.toList())
        }
    }

    override suspend fun addNote(note: Note): Result<Unit> =
        noteDataSource.addNote(NoteMapper.toDto(note))

    override suspend fun updateNote(note: Note): Result<Unit> =
        noteDataSource.updateNote(NoteMapper.toDto(note))

    override suspend fun deleteNote(noteId: String): Result<Unit> =
        noteDataSource.deleteNote(noteId, emptyList())

    override suspend fun archiveNote(note: Note): Result<Unit> =
        noteDataSource.archiveNote(NoteMapper.toDto(note))

    override suspend fun unarchiveNote(note: Note): Result<Unit> =
        noteDataSource.unarchiveNote(NoteMapper.toDto(note))

    override suspend fun starNote(noteId: String, starred: Boolean): Result<Unit> =
        noteDataSource.starNote(noteId, starred)
}
