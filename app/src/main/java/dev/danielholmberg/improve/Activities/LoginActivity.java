package dev.danielholmberg.improve.Activities;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.android.gms.tasks.Task;

import dev.danielholmberg.improve.Callbacks.FirebaseAuthCallback;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.R;

/**
 * Google Login Activity
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 9001;

    private Improve app;
    private ProgressBar progressBar;
    private SignInButton signInButton;

    private GoogleSignInClient mGoogleSignInClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Starting application...");

        // Initialize Singleton.
        app = Improve.getInstance();

        // The getFireAuth().getCurrentUser() is not null, then the user has been previously authenticated.
        if(app.getAuthManager().getFireAuth().getCurrentUser() != null) {
            startMainActivity();
        }

        setContentView(R.layout.activity_signin);

        // ProgressBar
        progressBar = (ProgressBar) findViewById(R.id.sign_in_progressBar);

        // Sign in Button
        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(this);

        // Configure Google Sign In Client
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        // Add the Google Sign In Client to AuthManager.
        app.getAuthManager().setGoogleSignInClient(mGoogleSignInClient);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            try {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                // Google Sign In was successful!
                // authenticating with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                app.getAuthManager().authWithFirebase(account, new FirebaseAuthCallback() {
                    @Override
                    public void onSuccess() {
                        // Authentication with Firebase was successful.
                        progressBar.setVisibility(View.GONE);
                        startMainActivity();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        // Authenication with Firebase was unsuccessful.
                        Log.e(TAG, "!!! Failed to authenticate with Firebase: " + errorMessage);
                    }
                });
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.e(TAG, "!!! Google sign in failed: " + e);
                Toast.makeText(getApplicationContext(), "Google sign in failed", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                signInButton.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Sends the user to the MainActivity.
     * Called when the user is correctly authenticated with Google and Firebase.
     */
    private void startMainActivity() {
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
    protected void onStart() {
        super.onStart();
        // Locking the app to the orientation that was used at startup.
        // So that the UI does not re-render when the user turns the device.
        setRequestedOrientation(getResources().getConfiguration().orientation);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
