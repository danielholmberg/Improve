package dev.danielholmberg.improve.Managers;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import dev.danielholmberg.improve.Components.Contact;
import dev.danielholmberg.improve.Components.Note;
import dev.danielholmberg.improve.Improve;

/**
 * Created by Daniel Holmberg.
 */

public class FirebaseStorageManager {
    private static final String TAG = FirebaseStorageManager.class.getSimpleName();

    private static final String USERS_REF = "users";
    private static final String NOTES_REF = "notes";
    private static final String ARCHIVED_NOTES_REF = "archived_notes";
    private static final String CONTACTS_REF = "contacts";

    public FirebaseStorageManager() {}

    public DatabaseReference getNotesRef() {
        String userId = Improve.getInstance().getAuthManager().getCurrentUserId();
        DatabaseReference notesRef = FirebaseDatabase.getInstance()
                .getReference(USERS_REF).child(userId).child(NOTES_REF);
        notesRef.keepSynced(true);
        return notesRef;
    }

    public DatabaseReference getArchivedNotesRef() {
        String userId = Improve.getInstance().getAuthManager().getCurrentUserId();
        DatabaseReference archivedNotesRef = FirebaseDatabase.getInstance()
                .getReference(USERS_REF).child(userId).child(ARCHIVED_NOTES_REF);
        archivedNotesRef.keepSynced(true);
        return archivedNotesRef;
    }

    public DatabaseReference getContactsRef() {
        String userId = Improve.getInstance().getAuthManager().getCurrentUserId();
        DatabaseReference contactsRef = FirebaseDatabase.getInstance()
                .getReference(USERS_REF).child(userId).child(CONTACTS_REF);
        contactsRef.keepSynced(true);
        return contactsRef;
    }

    public void writeNoteToFirebase(Note note, boolean archive) {
        if(archive) {
            getArchivedNotesRef().child(note.getId()).setValue(note)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Successfully updated Note.
                            Log.d(TAG, "*** Successfully updated Note in Firebase storage ***");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "!!! Failed to update Note in Firebase storage: " + e);
                        }
                    });
            deleteNote(note, false);
        } else {
            getNotesRef().child(note.getId()).setValue(note)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Successfully added new Note.
                            Log.d(TAG, "*** Successfully added new Note to Firebase storage ***");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "!!! Failed to add new Note to Firebase storage: " + e);
                        }
                    });
            deleteNote(note, true);
        }
    }

    public void writeContactToFirebase(Contact contact) {
        getContactsRef().child(contact.getId()).setValue(contact)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Successfully added new Contact.
                        Log.d(TAG, "*** Successfully added new Contact to Firebase storage ***");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "!!! Failed to add new Contact to Firebase storage: " + e);
                    }
                });
    }

    public void deleteNote(Note noteToDelete, boolean fromArchive) {
        if(fromArchive){
            getArchivedNotesRef().child(noteToDelete.getId()).removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Successfully deleted the Note.
                            Log.d(TAG, "*** Successfully deleted the Note in Firebase storage ***");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "!!! Failed to delete the Note in Firebase storage: " + e);
                        }
                    });
        } else {
            getNotesRef().child(noteToDelete.getId()).removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Successfully deleted the Note.
                            Log.d(TAG, "*** Successfully deleted the Note in Firebase storage ***");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "!!! Failed to delete the Note in Firebase storage: " + e);
                        }
                    });
        }
    }

    public void deleteContact(Contact contactToDelete) {
        getContactsRef().child(contactToDelete.getId()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Successfully deleted the Contact.
                        Log.d(TAG, "*** Successfully deleted the Contact in Firebase storage ***");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "!!! Failed to delete the Contact in Firebase storage: " + e);
                    }
                });
    }
}
