package dev.danielholmberg.improve.Callbacks;

import com.google.firebase.database.DataSnapshot;

/**
 * Created by Daniel Holmberg.
 */

public interface FirebaseRequestCallback {

    void onComplete(DataSnapshot dataSnapshot);
    void onFailure(Exception e);

}
