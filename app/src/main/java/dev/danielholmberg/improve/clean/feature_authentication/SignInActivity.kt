package dev.danielholmberg.improve.clean.feature_authentication

import androidx.appcompat.app.AppCompatActivity
import android.widget.ProgressBar
import android.widget.LinearLayout
import android.os.Bundle
import dev.danielholmberg.improve.R
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Button
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import dev.danielholmberg.improve.clean.Improve.Companion.instance
import dev.danielholmberg.improve.clean.MainActivity
import dev.danielholmberg.improve.clean.feature_authentication.util.AuthCallback

class SignInActivity : AppCompatActivity(), View.OnClickListener {
    private var progressBar: ProgressBar? = null
    private var signInButtonsLayout: LinearLayout? = null
    private var googleSignInBtn: Button? = null
    private var anonymousSignInBtn: Button? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Starting application...")

        // The getFireAuth().getCurrentUser() is not null, then the user has been previously authenticated.
        if (instance!!.authService.currentUser != null) {
            startMainActivity()
        }
        setContentView(R.layout.activity_sign_in)

        // ProgressBar
        progressBar = findViewById<View>(R.id.sign_in_progressBar) as ProgressBar

        // Sign in buttons layout
        signInButtonsLayout = findViewById<View>(R.id.sign_in_buttons_layout) as LinearLayout

        // Anonymous Sign in button
        anonymousSignInBtn = findViewById<View>(R.id.anonymous_login_btn) as Button
        anonymousSignInBtn!!.setOnClickListener(this)

        // Google Sign in button
        googleSignInBtn = findViewById<View>(R.id.google_sign_in_btn) as Button
        googleSignInBtn!!.setOnClickListener(this)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                Log.d(TAG, "Google sign in was successful!")

                // Google Sign In was successful, authenticating with Firebase...
                val account = result.signInAccount

                // TODO: Extract to UseCase and Call from ViewModel instead
                instance!!.authRepository.authGoogleAccountWithFirebase(account!!, object :
                    AuthCallback {
                    override fun onSuccess() {
                        progressBar!!.visibility = View.GONE
                        startMainActivity()
                    }

                    override fun onFailure(errorMessage: String?) {
                        Log.e(TAG, "!!! Failed to authenticate with Firebase: $errorMessage")
                        progressBar!!.visibility = View.GONE
                        signInButtonsLayout!!.visibility = View.VISIBLE
                    }
                })
            } else if (result.status.statusCode == GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                Log.e(TAG, "Google sign in was cancelled!")
                Toast.makeText(
                    applicationContext,
                    "Google sign in was cancelled",
                    Toast.LENGTH_SHORT
                ).show()
                progressBar!!.visibility = View.GONE
                signInButtonsLayout!!.visibility = View.VISIBLE
            } else {
                Log.e(TAG, "Google sign in failed!")
                Toast.makeText(applicationContext, "Google sign in failed", Toast.LENGTH_SHORT)
                    .show()
                progressBar!!.visibility = View.GONE
                signInButtonsLayout!!.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Sends the user to the MainActivity.
     * Called when the user is correctly authenticated with Google and Firebase.
     */
    private fun startMainActivity() {
        val i = Intent(applicationContext, MainActivity::class.java)
        startActivity(i)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    private fun startAnonymousSignIn() {
        // TODO: Extract to UseCase and Call from ViewModel instead
        instance!!.authRepository.signInAnonymously(object : AuthCallback {
            override fun onSuccess() {
                startMainActivity()
            }

            override fun onFailure(errorMessage: String?) {
                Log.e(TAG, "!!! Failed to Sign in Anonymously: $errorMessage")
            }
        })
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.google_sign_in_btn -> {
                signInButtonsLayout!!.visibility = View.GONE
                progressBar!!.visibility = View.VISIBLE
                instance!!.startGoogleSignIn(this, RC_SIGN_IN)
            }
            R.id.anonymous_login_btn -> {
                signInButtonsLayout!!.visibility = View.GONE
                progressBar!!.visibility = View.VISIBLE
                startAnonymousSignIn()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Locking the app to the orientation that was used at startup.
        // So that the UI does not re-render when the user turns the device.
        requestedOrientation = resources.configuration.orientation
    }

    override fun onBackPressed() {
        finish()
    }

    companion object {
        private val TAG = SignInActivity::class.java.simpleName
        private const val RC_SIGN_IN = 9001
    }
}