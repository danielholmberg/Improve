package dev.danielholmberg.improve.Managers;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import dev.danielholmberg.improve.Improve;

public class FirebaseStorageManager {
    private static final String TAG = FirebaseStorageManager.class.getSimpleName();

    private static final String USERS_REF = "users";
    private static final String IMAGES_REF = "images";

    public FirebaseStorageManager() {}

    public FirebaseStorage getStorage() {
        return FirebaseStorage.getInstance();
    }

    public StorageReference getUserRef() {
        String userId = Improve.getInstance().getAuthManager().getCurrentUserId();
        return getStorage().getReference(USERS_REF).child(userId);
    }

    public StorageReference getImagesRef() {
        return getUserRef().child(IMAGES_REF);
    }

}
