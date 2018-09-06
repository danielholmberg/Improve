package dev.danielholmberg.improve.Activities;

import android.app.Instrumentation;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import dev.danielholmberg.improve.Callbacks.FirebaseAuthCallback;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.R;

/**
 * Google Login Activity
 */

public class SignInActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = SignInActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 9001;

    private Improve app;
    private ProgressBar progressBar;
    private LinearLayout signInButtonsLayout;
    private Button googleSignInBtn;
    private Button anonymousSignInBtn;

    private GoogleSignInClient googleSignInClient;

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

        setContentView(R.layout.activity_sign_in);

        // ProgressBar
        progressBar = (ProgressBar) findViewById(R.id.sign_in_progressBar);

        // Sign in buttons layout
        signInButtonsLayout = (LinearLayout) findViewById(R.id.sign_in_buttons_layout);

        // Anonymous Sign in button
        anonymousSignInBtn = (Button) findViewById(R.id.anonymous_login_btn);
        anonymousSignInBtn.setOnClickListener(this);

        // Google Sign in button
        googleSignInBtn = (Button) findViewById(R.id.google_sign_in_btn);
        googleSignInBtn.setOnClickListener(this);

        // Configure Google Sign In Client
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
        // Add the Google Sign In Client to AuthManager.
        app.getAuthManager().setGoogleSignInClient(googleSignInClient);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if(result.isSuccess()) {
                Log.d(TAG, "Google sign in was successful!");

                // Google Sign in was successful, authenticate with Firebase...
                try {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    // Google Sign In was successful!
                    // authenticating with Firebase
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    app.getAuthManager().authGoogleAccountWithFirebase(account, new FirebaseAuthCallback() {
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
                            progressBar.setVisibility(View.GONE);
                            signInButtonsLayout.setVisibility(View.VISIBLE);
                        }
                    });
                } catch (ApiException e) {
                    // Google Sign In failed, update UI appropriately
                    Log.e(TAG, "!!! Google sign in failed: " + e);
                    Crashlytics.log("Google sign in failed: " + e.toString());

                    Toast.makeText(getApplicationContext(), "Google sign in failed", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    signInButtonsLayout.setVisibility(View.VISIBLE);
                }
            } else if(result.getStatus().getStatusCode() == GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                Log.e(TAG, "Google sign in was cancelled!");
                Toast.makeText(getApplicationContext(), "Google sign in was cancelled", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                signInButtonsLayout.setVisibility(View.VISIBLE);
            } else {
                Log.e(TAG, "Google sign in failed!");
                Toast.makeText(getApplicationContext(), "Google sign in failed", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                signInButtonsLayout.setVisibility(View.VISIBLE);
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
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    /**
     * Starts an Intent to get the users Google-account to be used to sign in.
     */
    private void startGoogleSignIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void startAnonymousSignIn() {
        app.getAuthManager().signInAnonymously(new FirebaseAuthCallback() {
            @Override
            public void onSuccess() {
                startMainActivity();
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, "!!! Failed to Sign in Anonymously: " + errorMessage);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.google_sign_in_btn:
                signInButtonsLayout.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                startGoogleSignIn();
                break;
            case R.id.anonymous_login_btn:
                signInButtonsLayout.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                startAnonymousSignIn();
                break;
            default:
                break;
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
