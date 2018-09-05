package dev.danielholmberg.improve.Callbacks;

/**
 * Created by Daniel Holmberg.
 */

public interface FirebaseDatabaseCallback {

    void onSuccess();
    void onFailure(String errorMessage);

}
