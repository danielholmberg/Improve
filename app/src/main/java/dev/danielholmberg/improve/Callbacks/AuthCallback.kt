package dev.danielholmberg.improve.Callbacks;

/**
 * Created by Daniel Holmberg.
 */

public interface AuthCallback {

    void onSuccess();
    void onFailure(String errorMessage);

}
