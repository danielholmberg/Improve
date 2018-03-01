package dev.danielholmberg.improve.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.IOException;
import java.util.HashMap;

import dev.danielholmberg.improve.InternalStorage;
import dev.danielholmberg.improve.R;

/**
 * Google Login Activity
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 9001;

    private ProgressBar progressBar;
    private SignInButton signInButton;

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth fireAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        // ProgressBar
        progressBar = (ProgressBar) findViewById(R.id.sign_in_progressBar);

        // Button listeners
        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(this);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        fireAuth = FirebaseAuth.getInstance();

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Locking the app to the orientation that was used at startup.
        // So that the UI does not re-render when the user turns the device.
        setRequestedOrientation(getResources().getConfiguration().orientation);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        progressBar.setVisibility(View.GONE);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            try {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(getApplicationContext(), "Google sign in failed", Toast.LENGTH_SHORT).show();
                signInButton.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Called when the user has chosen a Google-account for sign in.
     * @param acct - GoogleSignInAccount
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        fireAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in successful
                            // Add user to Database before going to MainActivity.
                            Log.d(TAG, "signInWithCredential:success");
                            createUserStorage();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication Failed.", Toast.LENGTH_SHORT).show();
                            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    /**
     * Initializing the Internal- and Cloud storage setup.
     * Called once the user is successfully authorized.
     */
    private void createUserStorage() {
        createInternalStorageFiles();
        addUserToFirestore();
    }

    /**
     * Creates the Internal Storage for the signed in User.
     */
    private void createInternalStorageFiles() {
        try {
            Log.d(TAG, "Creating storage with UserIdToken: " + fireAuth.getCurrentUser().getUid());
            InternalStorage.createStorage(this, fireAuth.getCurrentUser().getUid());
        } catch (IOException e) {
            Log.e(TAG, "Failed to setup Storage files: ");
            e.printStackTrace();
            Toast.makeText(this, "Failed to setup Storage files", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Creates the Firestores Storage for the signed in User.
     */
    private void addUserToFirestore() {
        FirebaseFirestore.getInstance().collection("users")
                .document(fireAuth.getCurrentUser().getUid())
                .set(new HashMap<>(), SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "*** User Successfully Added To DatabaseStorage ***");
                        goToMainActivity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to create user reference in DatabaseStorage: " + e);
                        Toast.makeText(getApplicationContext(),
                                "Failed to create user reference for Cloud Storage",
                                Toast.LENGTH_LONG)
                                .show();
                        e.printStackTrace();
                    }
                });
    }

    /**
     * Sends the user to the MainActivity.
     * Called when the user is correctly authenticated with Google and Firebase.
     */
    private void goToMainActivity() {
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
        finish();
    }

    /**
     * Starts an Intent to get the users Google-account to be used to sign in.
     */
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.sign_in_button) {
            v.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            signIn();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
