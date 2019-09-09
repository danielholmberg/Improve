package dev.danielholmberg.improve.Managers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import dev.danielholmberg.improve.Callbacks.FirebaseDatabaseCallback;
import dev.danielholmberg.improve.Models.Company;
import dev.danielholmberg.improve.Models.Contact;
import dev.danielholmberg.improve.Models.Feedback;
import dev.danielholmberg.improve.Models.Note;
import dev.danielholmberg.improve.Models.Tag;
import dev.danielholmberg.improve.Improve;

/**
 * Created by Daniel Holmberg.
 *
 * Remember:
 * Uploading data is free.
 * Downloading data is costly.
 *
 * Avoid using .keepSynced(boolean) as it downloads the node even if it hasn't changed.
 */

public class FirebaseDatabaseManager {
    private static final String TAG = FirebaseDatabaseManager.class.getSimpleName();

    private static final String USERS_REF = "users";
    private static final String NOTES_REF = "notes";
    private static final String ARCHIVED_NOTES_REF = "archived_notes";
    private static final String TAGS_REF = "tags";
    private static final String CONTACTS_REF = "contacts";
    private static final String COMPANIES_REF = "companies";
    private static final String FEEDBACK_REF = "feedback";

    public FirebaseDatabaseManager() {}

    public FirebaseDatabase getDatabase() {
        return FirebaseDatabase.getInstance();
    }

    public DatabaseReference getUserRef() {
        String userId = Improve.getInstance().getAuthManager().getCurrentUserId();
        return getDatabase().getReference(USERS_REF).child(userId);
    }

    /**
     * Returns the database reference to the Notes-node.
     * @return - Database reference to Notes-node
     */
    public DatabaseReference getNotesRef() {
        return getUserRef().child(NOTES_REF);
    }

    /**
     * Returns the database reference to the Archive-node.
     * @return - Database reference to Archive-node
     */
    public DatabaseReference getArchivedNotesRef() {
        return getUserRef().child(ARCHIVED_NOTES_REF);
    }

    /**
     * Returns the database reference to the Companies-node.
     * @return - Database reference to Companies-node
     */
    public DatabaseReference getCompaniesRef() {
        return getUserRef().child(COMPANIES_REF);
    }

    /**
     * Returns the database reference to the Feedback-node.
     * @return - Database reference to Feedback-node
     */
    public DatabaseReference getFeedbackRef() {
        return getDatabase().getReference().child(FEEDBACK_REF);
    }

    /**
     * Returns the database reference to the Tags-node.
     * @return - Database reference to Tags-node
     */
    public DatabaseReference getTagRef() {
        return getUserRef().child(TAGS_REF);
    }

    // ---- Note specific functions ---- //

    /**
     * Uploads a note to the Archive-node, and removes the note from the Notes-node.
     * @param note - The note to upload
     */
    public void archiveNote(Note note) {
        note.setArchived(true);

        // Add Note to Archived_notes-node.
        addArchivedNote(note);

        // Delete Note from Note-node.
        deleteNote(note);
    }

    /**
     * Uploads a new note to the Notes-node.
     * @param newNote - The note to upload
     */
    public void addNote(final Note newNote) {
        Log.d(TAG, "addNote: " + newNote.getId());

        getNotesRef().child(newNote.getId()).setValue(newNote, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError != null) {
                    Log.e(TAG, "Failed to add Note: " + newNote.getId() + " to Firebase: " + databaseError);
                }
            }
        });
    }

    /**
     * Uploads an updated note to the Notes-node.
     * @param updatedNote - The note to upload
     */
    public void updateNote(final Note updatedNote) {
        getNotesRef().child(updatedNote.getId()).setValue(updatedNote, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError != null) {
                    Log.e(TAG, "Failed to update Note: " + updatedNote.getId() + " to Firebase: " + databaseError);
                }
            }
        });
    }

    /**
     * Removes a note from the Notes-node.
     * @param noteToDelete - The note to remove
     */
    public void deleteNote(final Note noteToDelete) {
        if(noteToDelete.isArchived()) {
            updateNote(noteToDelete);
        }
        getNotesRef().child(noteToDelete.getId()).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError != null) {
                    Log.e(TAG, "Failed to delete Note: " + noteToDelete.getId() + " from Firebase: " + databaseError);
                }
            }
        });
    }
    
    // ---- Archived notes functions ---- //

    /**
     * Uploads a note to the Notes-node, and removes the note from the Archive-node.
     * @param note - The note to upload
     */
    public void unarchiveNote(Note note) {
        note.setArchived(false);

        // Add Note to NoteRef.
        addNote(note);

        // Delete Note from Archived_notes-node.
        deleteNoteFromArchive(note);

    }

    /**
     * Uploads a new note to the Notes-node.
     * @param archivedNote - The note to upload
     */
    public void addArchivedNote(final Note archivedNote) {
        getArchivedNotesRef().child(archivedNote.getId()).setValue(archivedNote, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError != null) {
                    Log.e(TAG, "Failed to add Archived Note: " + archivedNote.getId() + " to Firebase: " + databaseError);
                }
            }
        });
    }

    /**
     * Uploads an updated note to the Archive-node.
     * @param archivedNote - The note to upload
     */
    public void updateArchivedNote(final Note archivedNote) {
        getArchivedNotesRef().child(archivedNote.getId()).setValue(archivedNote, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError != null) {
                    Log.e(TAG, "Failed to update Archived Note: " + archivedNote.getId() + " to Firebase: " + databaseError);
                }
            }
        });
    }

    /**
     * Removes a note from the Archive-node.
     * @param noteToDelete - The note to remove
     */
    public void deleteNoteFromArchive(final Note noteToDelete) {
        if(!noteToDelete.isArchived()) {
            updateArchivedNote(noteToDelete);
        }
        getArchivedNotesRef().child(noteToDelete.getId()).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError != null) {
                    Log.e(TAG, "Failed to delete Archived Note: " + noteToDelete.getId() + "from Firebase: " + databaseError);
                }
            }
        });
    }

    // ---- Tag specific functions ---- //

    /**
     * Uploads a tag to the Tags-node.
     * @param newTag - The tag to upload
     */
    public void addTag(final Tag newTag) {
        Log.d(TAG, "addTag: " + newTag.getId());
        getTagRef().child(newTag.getId()).setValue(newTag, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError != null) {
                    Log.e(TAG, "Failed to add Tag: " + newTag.getId() + " to Firebase: " + databaseError);
                    getTagRef().child(newTag.getId()).removeValue();
                }
            }
        });
    }

    /**
     * Deletes the tag entry, and all its usages.
     * @param tagId
     */
    public void deleteTag(final String tagId) {
        Log.d(TAG, "deleteTag: " + tagId);
        getTagRef().child(tagId).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError != null) {
                    Log.e(TAG, "Failed to delete Tag: " + tagId + "from Firebase: " + databaseError);
                } else {
                    for(Note note: Improve.getInstance().getNotesAdapter().getNotesList()) {
                        note.removeTag(tagId);
                        updateNote(note);
                    }
                    for(Note archivedNote: Improve.getInstance().getArchivedNotesAdapter().getArchivedNotesList()) {
                        archivedNote.removeTag(tagId);
                        updateArchivedNote(archivedNote);
                    }
                }
            }
        });
    }
    
    // ---- Contact specific functions ---- //
    
    /**
     * Uploads a contact to the Contacts-node.
     * @param contact - The contact to upload
     */
    public void addContact(final Contact contact) {
        Log.d(TAG, "addContact: " + contact.getId());
        getCompaniesRef().child(contact.getCompanyId()).child(CONTACTS_REF)
                .child(contact.getId()).setValue(contact, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError != null) {
                    Log.e(TAG, "Failed to add new Contact-id to Company-ref: " + databaseError);
                }
            }
        });
    }

    /**
     * Uploads an updated contact to the Contacts-node.
     * @param updatedContact - The contact to upload
     */
    public void updateContact(final Contact oldContact, final Contact updatedContact) {
        getCompaniesRef().child(updatedContact.getCompanyId()).child(CONTACTS_REF)
                .child(updatedContact.getId()).setValue(updatedContact, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError != null) {
                    Log.e(TAG, "Failed to update Contact: " + updatedContact.getId() + " to Firebase: " + databaseError);
                }
            }
        });

        if(!updatedContact.getCompanyId().equals(oldContact.getCompanyId())) {
            getCompaniesRef().child(oldContact.getCompanyId()).child("contacts").child(oldContact.getId()).removeValue();
        }
    }

    /**
     * Removes a Contact from the related Company.
     * @param contactToDelete - The contact to remove
     */
    public void deleteContact(final Contact contactToDelete) {
        getCompaniesRef().child(contactToDelete.getCompanyId()).child(CONTACTS_REF)
                .child(contactToDelete.getId()).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError != null) {
                    Log.e(TAG, "Failed to delete Contact: " + contactToDelete.getId() + " from Firebase: " + databaseError);
                }
            }
        });
    }

    // ---- Company specific functions ---- //
    
    public void addCompany(final Company newCompany) {
        getCompaniesRef().child(newCompany.getId()).setValue(newCompany, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError != null) {
                    Log.e(TAG, "Failed to add Company: " + newCompany.getId() + " to Firebase: " + databaseError);
                }
            }
        });
    }

    public void deleteCompany(final Company company) {
        getCompaniesRef().child(company.getId()).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError != null) {
                    Log.e(TAG, "Failed to delete Company: " + company.getId() + " from Firebase: " + databaseError);
                }
            }
        });
    }
    
    // ---- Feedback specific functions ---- //

    /**
     * Uploads a feedback to the Feedback-node.
     * @param feedback - The feedback to upload
     * @param firebaseDatabaseCallback
     */
    public void submitFeedback(Feedback feedback, final FirebaseDatabaseCallback firebaseDatabaseCallback) {
        getFeedbackRef().child(feedback.getFeedback_id()).setValue(feedback, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError != null) {
                    Log.e(TAG, "Failed to submit feedback to Firebase: " + databaseError);
                    firebaseDatabaseCallback.onFailure(databaseError.toString());
                } else {
                    firebaseDatabaseCallback.onSuccess();
                }
            }
        });
    }

    // ---- Save Database content functions ---- //

    /**
     * Uploads all notes to the Notes-node.
     */
    public void saveNotes(HashMap<String, Object> notes) {
        Log.d(TAG, "saveNotes: " + notes);

        getNotesRef().updateChildren(notes, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError != null) {
                    Log.e(TAG, "Failed to save Notes: " + databaseError);
                }
            }
        });
    }

    /**
     * Uploads all archived notes to the Archived_notes-node.
     */
    public void saveArchivedNotes(HashMap<String, Object> archivedNotes) {
        getArchivedNotesRef().updateChildren(archivedNotes, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError != null) {
                    Log.e(TAG, "Failed to save Archived notes: " + databaseError);
                }
            }
        });
    }

    /**
     * Uploads all tags to the Tags-node.
     */
    public void saveTags(HashMap<String, Object> tags) {
        getTagRef().updateChildren(tags, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError != null) {
                    Log.e(TAG, "Failed to save Tags: " + databaseError);
                }
            }
        });
    }

    /**
     * Uploads all companies to the Companies-node.
     */
    public void saveCompanies(HashMap<String, Object> companies) {
        getCompaniesRef().updateChildren(companies, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError != null) {
                    Log.e(TAG, "Failed to save Companies: " + databaseError);
                }
            }
        });
    }
}
