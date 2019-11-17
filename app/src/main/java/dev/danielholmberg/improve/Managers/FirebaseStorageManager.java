package dev.danielholmberg.improve.Managers;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

import dev.danielholmberg.improve.Callbacks.FirebaseStorageCallback;
import dev.danielholmberg.improve.Improve;

public class FirebaseStorageManager {
    private static final String TAG = FirebaseStorageManager.class.getSimpleName();

    private static final String USERS_REF = "Users";
    public static final String IMAGES_REF = "Images";

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

    public void uploadImage(final String imageId, final Uri imageUri, FirebaseStorageCallback callback) {
        getImagesRef().child(imageId).putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                String fileName = taskSnapshot.getMetadata().getName();
                Log.d(TAG, "SUCCESS: File uploaded to Firebase Cloud Storage: " + fileName);
                callback.onSuccess(null);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "FAILURE: To upload image to Firebase Cloud Storage: " + e);
                callback.onFailure(e.getMessage());
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                Log.d(TAG, "PROGRESS: " + (int) progress + "%");
                callback.onProgress((int) progress);
            }
        });
    }

    public void deleteImage(String imageId) {
        getImagesRef().child(imageId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "SUCCESS: Image with id: " + imageId + " was deleted.");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "FAILURE: To delete image with id: " + imageId + " due to: " + e);
            }
        });
    }

    public File downloadImageToLocalFile(String imageId, FirebaseStorageCallback callback) {
        Log.d(TAG, "Images dir path: " + Improve.getInstance().getImageDir().getPath());
        File targetFile = new File(Improve.getInstance().getImageDir().getPath() + File.separator + imageId);
        Log.d(TAG, "targetFile path: " + targetFile.getPath());

        getImagesRef().child(imageId).getFile(targetFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "SUCCESS: Downloaded image with id: " + imageId + " to filePath: " + targetFile.getPath());
                callback.onSuccess(targetFile);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "FAILURE: To download image with id: " + imageId + " to filePath: " + targetFile.getPath());
            }
        });

        return targetFile;
    }
}
