package dev.danielholmberg.improve.Callbacks;

public interface StorageCallback {

    void onSuccess(Object object);
    void onFailure(String errorMessage);
    void onProgress(int progress);

}
