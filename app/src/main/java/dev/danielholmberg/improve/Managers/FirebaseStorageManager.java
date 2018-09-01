package dev.danielholmberg.improve.Managers;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Deque;

import dev.danielholmberg.improve.Callbacks.FirebaseStorageCallback;
import dev.danielholmberg.improve.Components.Contact;
import dev.danielholmberg.improve.Components.Feedback;
import dev.danielholmberg.improve.Components.Note;
import dev.danielholmberg.improve.Improve;

/**
 * Created by Daniel Holmberg.
 */

public class FirebaseStorageManager {
    private static final String TAG = FirebaseStorageManager.class.getSimpleName();

    public static final String USERS_REF = "users";
    public static final String NOTES_REF = "notes";
    public static final String ARCHIVED_NOTES_REF = "archived_notes";
    public static final String CONTACTS_REF = "contacts";
    public static final String FEEDBACK_REF = "feedback";

    public FirebaseStorageManager() {}

    public DatabaseReference getUserRef() {
        String userId = Improve.getInstance().getAuthManager().getCurrentUserId();
        return FirebaseDatabase.getInstance().getReference(USERS_REF).child(userId);
    }

    public DatabaseReference getNotesRef() {
        DatabaseReference notesRef = getUserRef().child(NOTES_REF);
        notesRef.keepSynced(true);
        return notesRef;
    }

    public DatabaseReference getArchivedNotesRef() {
        DatabaseReference archivedNotesRef = getUserRef().child(ARCHIVED_NOTES_REF);
        archivedNotesRef.keepSynced(true);
        return archivedNotesRef;
    }

    public DatabaseReference getContactsRef() {
        DatabaseReference contactsRef = getUserRef().child(CONTACTS_REF);
        contactsRef.keepSynced(true);
        return contactsRef;
    }

    public DatabaseReference getFeedbackRef() {
        DatabaseReference feedbackRef = FirebaseDatabase.getInstance().getReference().child(FEEDBACK_REF);
        return feedbackRef;
    }

    public void writeNoteToFirebase(Note note, boolean toArchive, final FirebaseStorageCallback callback) {
        if(toArchive) {
            note.setArchived(true);
            getArchivedNotesRef().child(note.getId()).setValue(note)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Successfully updated Note.
                            Log.d(TAG, "*** Successfully updated Note in Firebase storage ***");
                            callback.onSuccess();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "!!! Failed to update Note in Firebase storage: " + e);
                            callback.onFailure(e.toString());
                        }
                    });
            deleteNote(note, false, new FirebaseStorageCallback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "*** Successfully deleted note from Notes ***");
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.e(TAG, errorMessage);
                }
            });
        } else {
            note.setArchived(false);
            getNotesRef().child(note.getId()).setValue(note)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Successfully added new Note.
                            Log.d(TAG, "*** Successfully added new Note to Firebase storage ***");
                            callback.onSuccess();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "!!! Failed to add new Note to Firebase storage: " + e);
                            callback.onFailure(e.toString());
                        }
                    });
            deleteNote(note, true, new FirebaseStorageCallback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "*** Successfully deleted note from Archive ***");
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.e(TAG, errorMessage);
                }
            });
        }
    }

    public void writeContactToFirebase(Contact contact, final FirebaseStorageCallback callback) {
        getContactsRef().child(contact.getCompany().toUpperCase())
                .child(contact.getId()).setValue(contact)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Successfully added new Contact.
                        Log.d(TAG, "*** Successfully added new Contact to Firebase storage ***");
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "!!! Failed to add new Contact to Firebase storage: " + e);
                        callback.onFailure(e.toString());
                    }
                });
    }

    public void deleteNote(Note noteToDelete, boolean fromArchive, final FirebaseStorageCallback callback) {
        if(fromArchive){
            getArchivedNotesRef().child(noteToDelete.getId()).removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Successfully deleted the Note.
                            Log.d(TAG, "*** Successfully deleted the Note in Firebase storage ***");
                            callback.onSuccess();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "!!! Failed to delete the Note in Firebase storage: " + e);
                            callback.onFailure(e.toString());
                        }
                    });
        } else {
            getNotesRef().child(noteToDelete.getId()).removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Successfully deleted the Note.
                            Log.d(TAG, "*** Successfully deleted the Note in Firebase storage ***");
                            callback.onSuccess();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "!!! Failed to delete the Note in Firebase storage: " + e);
                            callback.onFailure(e.toString());
                        }
                    });
        }
    }

    public void deleteContact(Contact contactToDelete, final FirebaseStorageCallback callback) {
        getContactsRef().child(contactToDelete.getCompany().toUpperCase())
                .child(contactToDelete.getId()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Successfully deleted the Contact.
                        Log.d(TAG, "*** Successfully deleted the Contact in Firebase storage ***");
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "!!! Failed to delete the Contact in Firebase storage: " + e);
                        callback.onFailure(e.toString());
                    }
                });
    }

    public void submitFeedback(Feedback feedback, final FirebaseStorageCallback callback) {
        getFeedbackRef().child(feedback.getFeedback_id()).setValue(feedback)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "*** Successfully submitted feedback to Firebase storage ***");
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "!!! Failed to submit feedback to Firebase storage: " + e);
                        callback.onFailure(e.toString());
                    }
                });
    }
}
