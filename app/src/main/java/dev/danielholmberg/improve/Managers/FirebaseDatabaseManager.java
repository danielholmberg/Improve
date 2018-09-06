package dev.danielholmberg.improve.Managers;

import android.support.annotation.NonNull;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import dev.danielholmberg.improve.Callbacks.FirebaseDatabaseCallback;
import dev.danielholmberg.improve.Components.Contact;
import dev.danielholmberg.improve.Components.Feedback;
import dev.danielholmberg.improve.Components.Note;
import dev.danielholmberg.improve.Improve;

/**
 * Created by Daniel Holmberg.
 */

public class FirebaseDatabaseManager {
    private static final String TAG = FirebaseDatabaseManager.class.getSimpleName();

    public static final String USERS_REF = "users";
    public static final String NOTES_REF = "notes";
    public static final String ARCHIVED_NOTES_REF = "archived_notes";
    public static final String CONTACTS_REF = "contacts";
    public static final String FEEDBACK_REF = "feedback";

    public FirebaseDatabaseManager() {}

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

    public void writeNoteToFirebase(Note note, boolean toArchive, final FirebaseDatabaseCallback callback) {
        if(toArchive) {
            note.setArchived(true);
            getArchivedNotesRef().child(note.getId()).setValue(note)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Successfully updated Note.
                            callback.onSuccess();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            callback.onFailure(e.toString());
                        }
                    });
            deleteNote(note, false, new FirebaseDatabaseCallback() {
                @Override
                public void onSuccess() {}

                @Override
                public void onFailure(String errorMessage) {
                    Crashlytics.log(errorMessage);
                }
            });
        } else {
            note.setArchived(false);
            getNotesRef().child(note.getId()).setValue(note)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Successfully added new Note.
                            callback.onSuccess();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Crashlytics.log(e.toString());
                            callback.onFailure(e.toString());
                        }
                    });
            deleteNote(note, true, new FirebaseDatabaseCallback() {
                @Override
                public void onSuccess() {}

                @Override
                public void onFailure(String errorMessage) {
                    Crashlytics.log(errorMessage);
                }
            });
        }
    }

    public void writeContactToFirebase(Contact contact, final FirebaseDatabaseCallback callback) {
        getContactsRef().child(contact.getCompany().toUpperCase())
                .child(contact.getId()).setValue(contact)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Successfully added new Contact.
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Crashlytics.log(e.toString());
                        callback.onFailure(e.toString());
                    }
                });
    }

    public void deleteNote(Note noteToDelete, boolean fromArchive, final FirebaseDatabaseCallback callback) {
        if(fromArchive){
            getArchivedNotesRef().child(noteToDelete.getId()).removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Successfully deleted the Note.
                            callback.onSuccess();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Crashlytics.log(e.toString());
                            callback.onFailure(e.toString());
                        }
                    });
        } else {
            getNotesRef().child(noteToDelete.getId()).removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Successfully deleted the Note.
                            callback.onSuccess();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Crashlytics.log(e.toString());
                            callback.onFailure(e.toString());
                        }
                    });
        }
    }

    public void deleteContact(Contact contactToDelete, final FirebaseDatabaseCallback callback) {
        getContactsRef().child(contactToDelete.getCompany().toUpperCase())
                .child(contactToDelete.getId()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Successfully deleted the Contact.
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Crashlytics.log(e.toString());
                        callback.onFailure(e.toString());
                    }
                });
    }

    public void submitFeedback(Feedback feedback, final FirebaseDatabaseCallback callback) {
        getFeedbackRef().child(feedback.getFeedback_id()).setValue(feedback)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Crashlytics.log(e.toString());
                        callback.onFailure(e.toString());
                    }
                });
    }
}
