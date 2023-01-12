package dev.danielholmberg.improve.Callbacks;

/**
 * Created by Daniel Holmberg.
 */

public interface DatabaseCallback {

    void onSuccess();
    void onFailure(String errorMessage);

}
