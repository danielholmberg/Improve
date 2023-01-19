package dev.danielholmberg.improve.clean.feature_note.data.repository

import com.google.firebase.database.ChildEventListener
import dev.danielholmberg.improve.clean.feature_note.data.source.note.NoteDataSource
import dev.danielholmberg.improve.clean.feature_note.data.source.entity.NoteEntity
import dev.danielholmberg.improve.clean.feature_note.domain.model.Note
import dev.danielholmberg.improve.clean.feature_note.domain.repository.ImageRepository
import dev.danielholmberg.improve.clean.feature_note.domain.repository.NoteRepository

class NoteRepositoryImpl(
    private val noteDataSource: NoteDataSource,
    private val imageRepository: ImageRepository
) : NoteRepository {
    override fun addNote(note: Note) {
        // Transform into Data Source model
        val noteEntity: NoteEntity = NoteEntity().fromNote(note)
        noteDataSource.addNote(noteEntity)
    }

    override fun updateNote(updatedNote: Note) {
        // Transform into Data Source model
        val noteEntity: NoteEntity = NoteEntity().fromNote(updatedNote)
        noteDataSource.updateNote(noteEntity)
    }

    override fun deleteNote(noteToDelete: Note) {
        if (noteToDelete.isArchived) updateNote(noteToDelete)

        // Transform into Data Source model
        val noteEntity: NoteEntity = NoteEntity().fromNote(noteToDelete)
        noteDataSource.deleteNote(noteEntity) {
            // Delete all related images from Firebase Storage
            if (noteToDelete.hasImage()) {
                for (imageId in noteToDelete.images) {
                    imageRepository.deleteImage(noteToDelete.id!!, imageId!!)
                }
            }
        }
    }

    override fun saveNotes(notes: HashMap<String?, Note>) {
        // Transform into Data Source model
        val noteEntitiesMap = notes.entries.associate {
                (id, note) -> Pair(id, NoteEntity().fromNote(note))
        }
        noteDataSource.saveNotes(noteEntitiesMap)
    }

    override fun archiveNote(note: Note) {
        note.isArchived = true

        // Add Note to Archived_notes-node.
        addArchivedNote(note)

        // Delete Note from Note-node.
        deleteNote(note)
    }

    override fun unarchiveNote(note: Note) {
        note.isArchived = false

        // Add Note to NoteRef.
        addNote(note)

        // Delete Note from Archived_notes-node.
        deleteNoteFromArchive(note)
    }

    override fun addArchivedNote(archivedNote: Note) {
        // Transform into Data Source model
        val noteEntity: NoteEntity = NoteEntity().fromNote(archivedNote)
        noteDataSource.addArchivedNote(noteEntity)
    }

    override fun updateArchivedNote(archivedNote: Note) {
        // Transform into Data Source model
        val noteEntity: NoteEntity = NoteEntity().fromNote(archivedNote)
        noteDataSource.updateArchivedNote(noteEntity)
    }

    override fun deleteNoteFromArchive(noteToDelete: Note) {
        if (!noteToDelete.isArchived) {
            updateArchivedNote(noteToDelete)
        }

        // Transform into Data Source model
        val noteEntity: NoteEntity = NoteEntity().fromNote(noteToDelete)
        noteDataSource.deleteNoteFromArchive(noteEntity) {
            // Delete all related images from Firebase Storage
            if (noteToDelete.hasImage()) {
                for (imageId in noteToDelete.images) {
                    imageRepository.deleteImage(noteToDelete.id!!, imageId!!)
                }
            }

        }
    }

    override fun saveArchivedNotes(archivedNotes: HashMap<String?, Note>) {
        // Transform into Data Source model
        val noteEntitiesMap = archivedNotes.entries.associate {
                (id, note) -> Pair(id, NoteEntity().fromNote(note))
        }
        noteDataSource.saveArchivedNotes(noteEntitiesMap)
    }

    override fun addChildEventListener(childEventListener: ChildEventListener) {
        noteDataSource.addChildEventListener(childEventListener)
    }

    override fun addChildEventListenerForArchive(childEventListener: ChildEventListener) {
        noteDataSource.addChildEventListenerForArchive(childEventListener)
    }

    override fun generateNewNoteId(): String? {
        return noteDataSource.generateNewNoteId()
    }

}