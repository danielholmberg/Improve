package dev.danielholmberg.improve.Managers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import dev.danielholmberg.improve.Callbacks.FirebaseDatabaseCallback;
import dev.danielholmberg.improve.Components.Contact;
import dev.danielholmberg.improve.Components.Feedback;
import dev.danielholmberg.improve.Components.Note;
import dev.danielholmberg.improve.Components.Tag;
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

    public static final String USERS_REF = "users";
    public static final String NOTES_REF = "notes";
    public static final String ARCHIVED_NOTES_REF = "archived_notes";
    public static final String CONTACTS_REF = "contacts";
    public static final String FEEDBACK_REF = "feedback";
    public static final String TAGS_REF = "tags";

    private HashMap<String, Tag> tagHashMap = new HashMap<>();

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
     * Returns the database reference to the Contacts-node.
     * @return - Database reference to Contacts-node
     */
    public DatabaseReference getContactsRef() {
        return getUserRef().child(CONTACTS_REF);
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

    /**
     * Uploads a note to the Archive-node, and removes the note from the Notes-node.
     * @param note - The note to upload
     * @param firebaseDatabaseCallback
     */
    public void archiveNote(Note note, final FirebaseDatabaseCallback firebaseDatabaseCallback) {
        note.setArchived(true);

        // Add Note to Archived_notes-node.
        getArchivedNotesRef().child(note.getId()).setValue(note, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError != null) {
                    Log.e(TAG, "Failed to update Note in Firebase: " + databaseError);
                    firebaseDatabaseCallback.onFailure(databaseError.toString());
                } else {
                    firebaseDatabaseCallback.onSuccess();
                }
            }
        });

        // Delete Note from Note-node.
        deleteNote(note, new FirebaseDatabaseCallback() {
            @Override
            public void onSuccess() { Log.d(TAG, "Successfully deleted note from Notes-node"); }

            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, errorMessage);
            }
        });
    }

    /**
     * Uploads a note to the Notes-node, and removes the note from the Archive-node.
     * @param note - The note to upload
     * @param firebaseDatabaseCallback
     */
    public void unarchiveNote(Note note, final FirebaseDatabaseCallback firebaseDatabaseCallback) {
        note.setArchived(false);

        // Add Note to NoteRef.
        getNotesRef().child(note.getId()).setValue(note, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError != null) {
                    firebaseDatabaseCallback.onFailure(databaseError.toString());
                } else {
                    firebaseDatabaseCallback.onSuccess();
                }
            }
        });

        // Delete Note from Archived_notes-node.
        deleteNoteFromArchive(note, new FirebaseDatabaseCallback() {
            @Override
            public void onSuccess() { Log.d(TAG, "Successfully deleted note from Archived_notes-node"); }

            @Override
            public void onFailure(String errorMessage) { Log.e(TAG, errorMessage); }
        });

    }

    /**
     * Uploads a new note to the Notes-node.
     * @param newNote - The note to upload
     * @param firebaseDatabaseCallback
     */
    public void addNote(Note newNote, final FirebaseDatabaseCallback firebaseDatabaseCallback) {
        getNotesRef().child(newNote.getId()).setValue(newNote, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError != null) {
                    Log.e(TAG, "Failed to add new note to Firebase: " + databaseError);
                    firebaseDatabaseCallback.onFailure(databaseError.toString());
                } else {
                    firebaseDatabaseCallback.onSuccess();
                }
            }
        });
    }

    /**
     * Uploads an updated note to the Notes-node.
     * @param updatedNote - The note to upload
     * @param firebaseDatabaseCallback
     */
    public void updateNote(Note updatedNote, final FirebaseDatabaseCallback firebaseDatabaseCallback) {
        getNotesRef().child(updatedNote.getId()).setValue(updatedNote, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError != null) {
                    Log.e(TAG, "Failed to update Note to Firebase: " + databaseError);
                    firebaseDatabaseCallback.onFailure(databaseError.toString());
                } else {
                    firebaseDatabaseCallback.onSuccess();
                }
            }
        });
    }

    /**
     * Uploads an updated note to the Archive-node.
     * @param updatedNote - The note to upload
     * @param firebaseDatabaseCallback
     */
    public void updateArchivedNote(Note updatedNote, final FirebaseDatabaseCallback firebaseDatabaseCallback) {
        getArchivedNotesRef().child(updatedNote.getId()).setValue(updatedNote, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError != null) {
                    Log.e(TAG, "Failed to update Archived Note to Firebase: " + databaseError);
                    firebaseDatabaseCallback.onFailure(databaseError.toString());
                } else {
                    firebaseDatabaseCallback.onSuccess();
                }
            }
        });
    }

    /**
     * Removes a note from the Notes-node.
     * @param noteToDelete - The note to remove
     * @param firebaseDatabaseCallback
     */
    public void deleteNote(Note noteToDelete, final FirebaseDatabaseCallback firebaseDatabaseCallback) {
        getNotesRef().child(noteToDelete.getId()).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError != null) {
                    Log.e(TAG, "Failed to delete Note from Firebase: " + databaseError);
                    firebaseDatabaseCallback.onFailure(databaseError.toString());
                } else {
                    firebaseDatabaseCallback.onSuccess();
                }
            }
        });
    }

    /**
     * Removes a note from the Archive-node.
     * @param noteToDelete - The note to remove
     * @param firebaseDatabaseCallback
     */
    public void deleteNoteFromArchive(Note noteToDelete, final FirebaseDatabaseCallback firebaseDatabaseCallback) {
        getArchivedNotesRef().child(noteToDelete.getId()).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError != null) {
                    Log.e(TAG, "Failed to delete Archived Note from Firebase: " + databaseError);
                    firebaseDatabaseCallback.onFailure(databaseError.toString());
                } else {
                    firebaseDatabaseCallback.onSuccess();
                }
            }
        });
    }

    /**
     * Uploads a contact to the Contacts-node.
     * @param contact - The contact to upload
     * @param firebaseDatabaseCallback
     */
    public void addContact(Contact contact, final FirebaseDatabaseCallback firebaseDatabaseCallback) {
        getContactsRef().child(contact.getCompany().toUpperCase())
                .child(contact.getId()).setValue(contact, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError != null) {
                    Log.e(TAG, "Failed to add new Contact to Firebase storage: " + databaseError);
                    firebaseDatabaseCallback.onFailure(databaseError.toString());
                } else {
                    firebaseDatabaseCallback.onSuccess();
                }
            }
        });
    }

    /**
     * Uploads an updated contact to the Contacts-node.
     * @param oldContact - The old contact to remove
     * @param updatedContact - The contact to upload
     * @param firebaseDatabaseCallback
     */
    public void updateContact(Contact oldContact, Contact updatedContact, final FirebaseDatabaseCallback firebaseDatabaseCallback) {
        getContactsRef().child(updatedContact.getCompany()).child(updatedContact.getId())
                .setValue(updatedContact, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError != null) {
                    Log.e(TAG, "Failed to update Contact to Firebase: " + databaseError);
                    firebaseDatabaseCallback.onFailure(databaseError.toString());
                } else {
                    firebaseDatabaseCallback.onSuccess();
                }
            }
        });

        deleteContact(oldContact, new FirebaseDatabaseCallback() {
            @Override
            public void onSuccess() {}

            @Override
            public void onFailure(String errorMessage) { Log.e(TAG, errorMessage); }
        });
    }

    /**
     * Removes a contact from the Contacts-node.
     * @param contactToDelete - The contact to remove
     * @param firebaseDatabaseCallback
     */
    public void deleteContact(Contact contactToDelete, final FirebaseDatabaseCallback firebaseDatabaseCallback) {
        getContactsRef().child(contactToDelete.getCompany().toUpperCase())
                .child(contactToDelete.getId()).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError != null) {
                    Log.e(TAG, "Failed to delete Contact from Firebase: " + databaseError);
                    firebaseDatabaseCallback.onFailure(databaseError.toString());
                } else {
                    firebaseDatabaseCallback.onSuccess();
                }
            }
        });
    }

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

    /**
     * Sets the HashMap containing all Tags stored in Firebase.
     * @param tagHashMap - New HashMap to replace the old one
     */
    public void setTagHashMap(HashMap<String,Tag> tagHashMap) {
        this.tagHashMap = tagHashMap;
    }

    /**
     * Returns the HashMap containing all Tags stored in Firebase.
     * @return
     */
    public HashMap<String, Tag> getTagHashMap() {
        return tagHashMap;
    }

    /**
     * Returns the Tag with incoming tag-id.
     * @param tagId - The id of the Tag to return
     * @return
     */
    public Tag getTag(String tagId) {
        return tagHashMap.get(tagId);
    }

    /**
     * Adds a Tag to the HashMap.
     * @param tag - The Tag to add
     */
    public void addTagToList(Tag tag) {
        this.tagHashMap.put(tag.getTagId(), tag);
    }

    /**
     * Uploads a tag to the Tags-node.
     * @param newTag - The tag to upload
     * @param firebaseDatabaseCallback
     */
    public void addTag(Tag newTag, final FirebaseDatabaseCallback firebaseDatabaseCallback) {
        getTagRef().child(newTag.getTagId()).setValue(newTag, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError != null) {
                    Log.e(TAG, "Failed to add new Tag to Firebase: " + databaseError);
                    firebaseDatabaseCallback.onFailure(databaseError.toString());
                } else {
                    firebaseDatabaseCallback.onSuccess();
                }
            }
        });
    }
}
