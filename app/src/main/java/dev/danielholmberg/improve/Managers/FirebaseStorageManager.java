package dev.danielholmberg.improve.Managers;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import dev.danielholmberg.improve.Components.Contact;
import dev.danielholmberg.improve.Components.OnMyMind;
import dev.danielholmberg.improve.Improve;

/**
 * Created by Daniel Holmberg.
 */

public class FirebaseStorageManager {
    private static final String TAG = FirebaseStorageManager.class.getSimpleName();

    private static final String USERS_REF = "users";
    private static final String ONMYMINDS_REF = "onmyminds";
    private static final String CONTACTS_REF = "contacts";

    public FirebaseStorageManager() {}

    public DatabaseReference getOnMyMindsRef() {
        String userId = Improve.getInstance().getAuthManager().getCurrentUserId();
        DatabaseReference onMyMindsRef = FirebaseDatabase.getInstance()
                .getReference(USERS_REF).child(userId).child(ONMYMINDS_REF);
        onMyMindsRef.keepSynced(true);
        return onMyMindsRef;
    }

    public DatabaseReference getContactsRef() {
        String userId = Improve.getInstance().getAuthManager().getCurrentUserId();
        DatabaseReference contactsRef = FirebaseDatabase.getInstance()
                .getReference(USERS_REF).child(userId).child(CONTACTS_REF);
        contactsRef.keepSynced(true);
        return contactsRef;
    }

    public void writeOnMyMindToFirebase(OnMyMind onMyMind) {
        getOnMyMindsRef().child(onMyMind.getId()).setValue(onMyMind)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Successfully added new OnMyMind.
                        Log.d(TAG, "*** Successfully added new OnMyMind to Firebase storage ***");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "!!! Failed to add new OnMyMind to Firebase storage: " + e);
                    }
                });
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

    public void deleteOnMymind(OnMyMind onMyMindToDelete) {
        getOnMyMindsRef().child(onMyMindToDelete.getId()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Successfully deleted the OnMyMind.
                        Log.d(TAG, "*** Successfully deleted the OnMyMind in Firebase storage ***");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "!!! Failed to delete the OnMyMind in Firebase storage: " + e);
                    }
                });
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
