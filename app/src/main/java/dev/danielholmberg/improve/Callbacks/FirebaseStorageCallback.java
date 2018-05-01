package dev.danielholmberg.improve.Callbacks;

/**
 * Created by Daniel Holmberg.
 */

public interface FirebaseStorageCallback {

    void onSuccess();
    void onFailure(String errorMessage);

}
