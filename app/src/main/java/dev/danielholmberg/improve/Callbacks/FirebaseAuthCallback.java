package dev.danielholmberg.improve.Callbacks;

/**
 * Created by Daniel Holmberg.
 */

public interface FirebaseAuthCallback {

    void onSuccess();
    void onFailure(String errorMessage);

}
