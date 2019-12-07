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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import dev.danielholmberg.improve.Callbacks.FirebaseStorageCallback;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Models.VipImage;

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

    public void uploadImage(final String noteId, final String imageId, final Uri imageUri, FirebaseStorageCallback callback) {
        getImagesRef().child(noteId).child(imageId).putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                String fileName = taskSnapshot.getMetadata().getName();
                Log.d(TAG, "SUCCESS: Image (" + imageId + ") uploaded to Firebase Cloud Storage for Note (" + noteId + ")");
                callback.onSuccess(null);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "FAILURE: To upload image (" + imageId + ") to Firebase Cloud Storage for Note (" + noteId + "): " + e);
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

    public void deleteImage(String noteId, String imageId) {
        getImagesRef().child(noteId).child(imageId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
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

    public File downloadImageToLocalFile(String noteId, String imageId, FirebaseStorageCallback callback) {
        Log.d(TAG, "Downloading image to Local Filesystem...");

        File noteImagesDir = new File(Improve.getInstance().getImageDir().getPath() +
                File.separator + noteId);
        if(!noteImagesDir.exists()) noteImagesDir.mkdirs();

        File targetFile = new File(noteImagesDir + File.separator + imageId);

        Log.d(TAG, "targetFile path: " + targetFile.getPath());

        getImagesRef().child(noteId).child(imageId).getFile(targetFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
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

    public void uploadMultipleImages(String noteId, List<VipImage> vipImagesList, FirebaseStorageCallback callback) {
        Log.d(TAG, "Uploading multiple ("+ vipImagesList.size() +") images to Firebase");

        ArrayList<VipImage> vipImagesUploaded = new ArrayList<>();

        File noteImagesDir = new File(Improve.getInstance().getImageDir().getPath() + File.separator + noteId);
        if(!noteImagesDir.exists()) noteImagesDir.mkdirs();

        for(int i=0; i<vipImagesList.size(); i++) {
            VipImage vipImage = vipImagesList.get(i);

            String imageId = vipImage.getId();
            Uri originalFilePath = Uri.parse(vipImage.getOriginalFilePath());

            File cachedImage = new File(noteImagesDir + File.separator + imageId);

            if(!cachedImage.exists()) {
                try {
                    Log.d(TAG, "Copying image to Local Filesystem for Note: " + noteId +
                            " with image id: " + imageId);
                    boolean cachedImageCreated = cachedImage.createNewFile();

                    if(cachedImageCreated) {
                        copyFileFromUri(originalFilePath, cachedImage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            uploadImage(noteId, imageId, Uri.fromFile(cachedImage), new FirebaseStorageCallback() {
                @Override
                public void onSuccess(Object object) {
                    vipImagesUploaded.add(vipImage);

                    if(vipImagesList.size() == vipImagesUploaded.size()) {
                        callback.onSuccess(vipImagesUploaded);
                    }
                }

                @Override
                public void onFailure(String errorMessage) {}

                @Override
                public void onProgress(int progress) {}
            });
        }
    }

    private void copyFileFromUri(Uri sourceUri, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        Log.d(TAG, "Copying File from: " + sourceUri + " to File: " + destFile.getPath());

        InputStream in = Improve.getInstance().getContentResolver().openInputStream(sourceUri);
        OutputStream out = new FileOutputStream(destFile);

        // Copy the bits from instream to outstream
        byte[] buf = new byte[1024];
        int len;
        if (in != null) {
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
        }
        out.close();
    }

}
