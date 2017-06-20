package com.csl.studio.photoshare.app.controller

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.csl.studio.photoshare.app.R
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : BaseActivity(), GoogleApiClient.OnConnectionFailedListener {

    private var _auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var _auth_listener = FirebaseAuth.AuthStateListener { firebaseAuth ->

        val user = firebaseAuth.currentUser

        user?.let {
            Log.d(TAG, "onAuthStateChanged:signed_in:" + user.uid)
            finish()
        } ?: Log.d(TAG, "onAuthStateChanged:signed_out")
    }

    private var _google_api_client: GoogleApiClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        // Set up the login form.

        email.setOnEditorActionListener { _, id, _ ->
            if (id == R.id.login || id == EditorInfo.IME_NULL) {
                checkLoginFormat()
                return@setOnEditorActionListener true
            }
            false
        }

        email_sign_in_button.setOnClickListener { signInUser() }
        email_register_button.setOnClickListener { registerUser() }
        google_sign_in_button.setOnClickListener { googleSignIn() }

        // Initial Google sign in component
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        _google_api_client = GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()

    }

    public override fun onStart() {
        super.onStart()
        _auth.addAuthStateListener(_auth_listener)
    }

    override fun onStop() {
        super.onStop()
        _auth.removeAuthStateListener(_auth_listener)
    }

    override fun onBackPressed() {
        if (_auth.currentUser == null) {
            return
        }
        super.onBackPressed()
    }

    private fun registerUser() {
        Log.d(TAG, "Register Action")

        if (!checkLoginFormat()) {
            return
        }

        val email = email.text.toString()
        val password = password.text.toString()

        showProgressDialog()

        _auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful)

                    hideProgressDialog()

                    if (!task.isSuccessful) {
                        Toast.makeText(this@LoginActivity, "Register Error", Toast.LENGTH_SHORT).show()
                    }
                }

    }

    private fun signInUser() {
        Log.d(TAG, "Sign In Action")

        if (!checkLoginFormat()) {
            Log.d(TAG, "Check Login Error")
            return
        }

        val email = email.text.toString()
        val password = password.text.toString()

        showProgressDialog()

        _auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful)

                    if (!task.isSuccessful) {
                        Log.w(TAG, "Sign In User: failed", task.exception)
                        Toast.makeText(this@LoginActivity, "Sign In Error", Toast.LENGTH_SHORT).show()
                    }

                    hideProgressDialog()
                }

    }

    private fun googleSignIn() {
        Log.d(TAG, "Start Google Sign IN")

        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(_google_api_client)
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                val account = result.signInAccount
                firebaseAuthWithGoogle(account!!)
            } else {
                Log.d(TAG, "Google Sign In Error: " + result.status.statusMessage!!)
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + account.id!!)

        showProgressDialog()

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        _auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful)

                    if (!task.isSuccessful) {
                        Log.w(TAG, "signInWithCredential", task.exception)
                        Toast.makeText(this@LoginActivity, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    }

                    hideProgressDialog()
                }
    }

    private fun checkLoginFormat(): Boolean {

        // Reset errors.
        email.error = null
        password.error = null

        // Store values at the time of the login attempt.
        val email_str = email.text.toString()
        val password_str = password.text.toString()

        var result = true

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password_str) && !isPasswordValid(password_str)) {
            password.error = getString(R.string.error_invalid_password)
            result = false
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email_str)) {

            email.error = getString(R.string.error_field_required)
            result = false
        } else if (!isEmailValid(email_str)) {
            email.error = getString(R.string.error_invalid_email)
            result = false
        }

        return result
    }

    private fun isEmailValid(email: String): Boolean {
        return email.contains("@")
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length > 4
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult)
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show()
    }

    companion object {

        private val GOOGLE_SIGN_IN = 9001
        private val TAG = "LoginActivity"
    }

}

