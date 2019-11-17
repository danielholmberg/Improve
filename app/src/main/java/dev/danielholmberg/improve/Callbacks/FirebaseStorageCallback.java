package dev.danielholmberg.improve.Callbacks;

import java.io.File;

public interface FirebaseStorageCallback {

    void onSuccess(File file);
    void onFailure(String errorMessage);
    void onProgress(int progress);

}
