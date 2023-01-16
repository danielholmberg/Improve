package dev.danielholmberg.improve.Managers

import android.util.Log
import dev.danielholmberg.improve.Improve.Companion.instance
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import dev.danielholmberg.improve.Callbacks.DatabaseCallback
import dev.danielholmberg.improve.Models.*

/**
 * Created by Daniel Holmberg.
 *
 * Remember:
 * Uploading data is free.
 * Downloading data is costly.
 *
 * Avoid using .keepSynced(boolean) as it downloads the node even if it hasn't changed.
 */
class DatabaseManager {
    val database: FirebaseDatabase
        get() = FirebaseDatabase.getInstance()
    val userRef: DatabaseReference
        get() {
            val userId = instance!!.authManager.currentUserId
            return database.getReference(USERS_REF).child(userId!!)
        }

    /**
     * Returns the database reference to the Notes-node.
     * @return - Database reference to Notes-node
     */
    val notesRef: DatabaseReference
        get() = userRef.child(NOTES_REF)

    /**
     * Returns the database reference to the Archive-node.
     * @return - Database reference to Archive-node
     */
    val archivedNotesRef: DatabaseReference
        get() = userRef.child(ARCHIVED_NOTES_REF)

    /**
     * Returns the database reference to the Companies-node.
     * @return - Database reference to Companies-node
     */
    val companiesRef: DatabaseReference
        get() = userRef.child(COMPANIES_REF)

    /**
     * Returns the database reference to the Feedback-node.
     * @return - Database reference to Feedback-node
     */
    val feedbackRef: DatabaseReference
        get() = database.reference.child(FEEDBACK_REF)

    /**
     * Returns the database reference to the Tags-node.
     * @return - Database reference to Tags-node
     */
    val tagRef: DatabaseReference
        get() = userRef.child(TAGS_REF)

    // ---- Note specific functions ---- //
    /**
     * Uploads a note to the Archive-node, and removes the note from the Notes-node.
     * @param note - The note to upload
     */
    fun archiveNote(note: Note) {
        note.archived = true

        // Add Note to Archived_notes-node.
        addArchivedNote(note)

        // Delete Note from Note-node.
        deleteNote(note)
    }

    /**
     * Uploads a new note to the Notes-node.
     * @param newNote - The note to upload
     */
    fun addNote(newNote: Note) {
        Log.d(TAG, "addNote: " + newNote.id)
        notesRef.child(newNote.id!!).setValue(newNote) { databaseError, _ ->
            if (databaseError != null) {
                Log.e(TAG, "Failed to add Note: " + newNote.id + " to Firebase: " + databaseError)
            }
        }
    }

    /**
     * Uploads an updated note to the Notes-node.
     * @param updatedNote - The note to upload
     */
    fun updateNote(updatedNote: Note) {
        notesRef.child(updatedNote.id!!).setValue(updatedNote) { databaseError, _ ->
            if (databaseError != null) {
                Log.e(
                    TAG,
                    "Failed to update Note: " + updatedNote.id + " to Firebase: " + databaseError
                )
            }
        }
    }

    /**
     * Removes a note from the Notes-node.
     * @param noteToDelete - The note to remove
     */
    fun deleteNote(noteToDelete: Note) {
        if (noteToDelete.isArchived()) {
            updateNote(noteToDelete)
        }
        notesRef.child(noteToDelete.id!!).removeValue { databaseError, _ ->
            if (databaseError != null) {
                Log.e(
                    TAG,
                    "Failed to delete Note: " + noteToDelete.id + " from Firebase: " + databaseError
                )
            }

            // Delete all related images from Firebase Storage
            if (noteToDelete.hasImage()) {
                for (imageId in noteToDelete.vipImages) {
                    instance!!.storageManager.deleteImage(noteToDelete.id!!, imageId!!)
                }
            }
        }
    }

    // ---- Archived notes functions ---- //
    /**
     * Uploads a note to the Notes-node, and removes the note from the Archive-node.
     * @param note - The note to upload
     */
    fun unarchiveNote(note: Note) {
        note.archived = false

        // Add Note to NoteRef.
        addNote(note)

        // Delete Note from Archived_notes-node.
        deleteNoteFromArchive(note)
    }

    /**
     * Uploads a new note to the Notes-node.
     * @param archivedNote - The note to upload
     */
    fun addArchivedNote(archivedNote: Note) {
        archivedNotesRef.child(archivedNote.id!!)
            .setValue(archivedNote) { databaseError, _ ->
                if (databaseError != null) {
                    Log.e(
                        TAG,
                        "Failed to add Archived Note: " + archivedNote.id + " to Firebase: " + databaseError
                    )
                }
            }
    }

    /**
     * Uploads an updated note to the Archive-node.
     * @param archivedNote - The note to upload
     */
    fun updateArchivedNote(archivedNote: Note) {
        archivedNotesRef.child(archivedNote.id!!)
            .setValue(archivedNote) { databaseError, _ ->
                if (databaseError != null) {
                    Log.e(
                        TAG,
                        "Failed to update Archived Note: " + archivedNote.id + " to Firebase: " + databaseError
                    )
                }
            }
    }

    /**
     * Removes a note from the Archive-node.
     * @param noteToDelete - The note to remove
     */
    fun deleteNoteFromArchive(noteToDelete: Note) {
        if (!noteToDelete.isArchived()) {
            updateArchivedNote(noteToDelete)
        }
        archivedNotesRef.child(noteToDelete.id!!).removeValue { databaseError, _ ->
            if (databaseError != null) {
                Log.e(
                    TAG,
                    "Failed to delete Archived Note: " + noteToDelete.id + "from Firebase: " + databaseError
                )
            }

            // Delete all related images from Firebase Storage
            if (noteToDelete.hasImage()) {
                for (imageId in noteToDelete.vipImages) {
                    instance!!.storageManager.deleteImage(noteToDelete.id!!, imageId!!)
                }
            }
        }
    }

    // ---- Tag specific functions ---- //
    /**
     * Uploads a tag to the Tags-node.
     * @param newTag - The tag to upload
     */
    fun addTag(newTag: Tag) {
        Log.d(TAG, "addTag: " + newTag.id)
        tagRef.child(newTag.id!!).setValue(newTag) { databaseError, _ ->
            if (databaseError != null) {
                Log.e(TAG, "Failed to add Tag: " + newTag.id + " to Firebase: " + databaseError)
                tagRef.child(newTag.id!!).removeValue()
            }
        }
    }

    /**
     * Deletes the tag entry, and all its usages.
     * @param tagId
     */
    fun deleteTag(tagId: String?) {
        Log.d(TAG, "deleteTag: $tagId")
        tagRef.child(tagId!!).removeValue { databaseError, _ ->
            if (databaseError != null) {
                Log.e(TAG, "Failed to delete Tag: " + tagId + "from Firebase: " + databaseError)
            } else {
                for (note in instance!!.notesAdapter!!.notesList) {
                    note.removeTag(tagId)
                    updateNote(note)
                }
                for (archivedNote in instance!!.archivedNotesAdapter!!.archivedNotesList) {
                    archivedNote.removeTag(tagId)
                    updateArchivedNote(archivedNote)
                }
            }
        }
    }

    // ---- Contact specific functions ---- //
    /**
     * Uploads a contact to the Contacts-node.
     * @param contact - The contact to upload
     */
    fun addContact(contact: Contact) {
        Log.d(TAG, "addContact: " + contact.id)
        companiesRef.child(contact.companyId!!).child(CONTACTS_REF)
            .child(contact.id!!).setValue(contact) { databaseError, _ ->
                if (databaseError != null) {
                    Log.e(TAG, "Failed to add new Contact-id to Company-ref: $databaseError")
                }
            }
    }

    /**
     * Uploads an updated contact to the Contacts-node.
     * @param updatedContact - The contact to upload
     */
    fun updateContact(oldContact: Contact, updatedContact: Contact) {
        companiesRef.child(updatedContact.companyId!!).child(CONTACTS_REF)
            .child(updatedContact.id!!)
            .setValue(updatedContact) { databaseError, _ ->
                if (databaseError != null) {
                    Log.e(
                        TAG,
                        "Failed to update Contact: " + updatedContact.id + " to Firebase: " + databaseError
                    )
                }
            }
        if (updatedContact.companyId != oldContact.companyId) {
            companiesRef.child(oldContact.companyId!!).child("contacts").child(oldContact.id!!)
                .removeValue()
        }
    }

    /**
     * Removes a Contact from the related Company.
     * @param contactToDelete - The contact to remove
     */
    fun deleteContact(contactToDelete: Contact) {
        companiesRef.child(contactToDelete.companyId!!).child(CONTACTS_REF)
            .child(contactToDelete.id!!).removeValue { databaseError, _ ->
                if (databaseError != null) {
                    Log.e(
                        TAG,
                        "Failed to delete Contact: " + contactToDelete.id + " from Firebase: " + databaseError
                    )
                }
            }
    }

    // ---- Company specific functions ---- //
    fun addCompany(newCompany: Company) {
        companiesRef.child(newCompany.id!!).setValue(newCompany) { databaseError, _ ->
            if (databaseError != null) {
                Log.e(
                    TAG,
                    "Failed to add Company: " + newCompany.id + " to Firebase: " + databaseError
                )
            }
        }
    }

    fun deleteCompany(company: Company) {
        companiesRef.child(company.id!!).removeValue { databaseError, _ ->
            if (databaseError != null) {
                Log.e(
                    TAG,
                    "Failed to delete Company: " + company.id + " from Firebase: " + databaseError
                )
            }
        }
    }

    // ---- Feedback specific functions ---- //
    /**
     * Uploads a feedback to the Feedback-node.
     * @param feedback - The feedback to upload
     * @param databaseCallback
     */
    fun submitFeedback(feedback: Feedback, databaseCallback: DatabaseCallback) {
        feedbackRef.child(feedback.feedback_id!!)
            .setValue(feedback) { databaseError, _ ->
                if (databaseError != null) {
                    Log.e(TAG, "Failed to submit feedback to Firebase: $databaseError")
                    databaseCallback.onFailure(databaseError.toString())
                } else {
                    databaseCallback.onSuccess()
                }
            }
    }

    // ---- Save Database content functions ---- //
    /**
     * Uploads all notes to the Notes-node.
     */
    fun saveNotes(notes: HashMap<String?, Any>) {
        notesRef.updateChildren(notes) { databaseError, _ ->
            if (databaseError != null) {
                Log.e(TAG, "Failed to save Notes: $databaseError")
            }
        }
    }

    /**
     * Uploads all archived notes to the Archived_notes-node.
     */
    fun saveArchivedNotes(archivedNotes: HashMap<String?, Any>) {
        archivedNotesRef.updateChildren(archivedNotes) { databaseError, _ ->
            if (databaseError != null) {
                Log.e(TAG, "Failed to save Archived notes: $databaseError")
            }
        }
    }

    /**
     * Uploads all tags to the Tags-node.
     */
    fun saveTags(tags: HashMap<String?, Any>) {
        tagRef.updateChildren(tags) { databaseError, _ ->
            if (databaseError != null) {
                Log.e(TAG, "Failed to save Tags: $databaseError")
            }
        }
    }

    /**
     * Uploads all companies to the Companies-node.
     */
    fun saveCompanies(companies: HashMap<String, Any>) {
        companiesRef.updateChildren(companies) { databaseError, _ ->
            if (databaseError != null) {
                Log.e(TAG, "Failed to save Companies: $databaseError")
            }
        }
    }

    /**
     * Stores the Notification token for the targeted device
     */
    fun updateNotificationToken(token: String) {
        val notificationData: Map<String, String> = object : java.util.HashMap<String, String>() {
            init {
                put("updated", System.currentTimeMillis().toString())
                put("token", token)
            }
        }
        database.reference.child("notification")
            .child(instance!!.deviceId).updateChildren(notificationData) { databaseError, _ ->
                if (databaseError != null) {
                    Log.e(TAG, "Failed to store notification token: $databaseError")
                }
                Log.i(TAG, "Successfully stored notification token: $token")
            }
    }

    companion object {
        private val TAG = DatabaseManager::class.java.simpleName
        private const val USERS_REF = "users"
        private const val NOTES_REF = "notes"
        private const val ARCHIVED_NOTES_REF = "archived_notes"
        private const val TAGS_REF = "tags"
        private const val CONTACTS_REF = "contacts"
        private const val COMPANIES_REF = "companies"
        private const val FEEDBACK_REF = "feedback"
    }
}