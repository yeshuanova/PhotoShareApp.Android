package com.csl.studio.photoshare.app.controller

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.csl.studio.photoshare.app.R
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.fragment_user_info.*

class UserInfoFragment : Fragment() {

    private inner class DownloadImageTask internal constructor(private val _image: ImageView) : AsyncTask<String, Void, Bitmap>() {

        override fun doInBackground(vararg urls: String): Bitmap? {

            val image_url = urls[0]

            Log.d(TAG, "Load Image from " + image_url)

            var _icon_bmp: Bitmap? = null
            try {
                val in_stream = java.net.URL(image_url).openStream()
                _icon_bmp = BitmapFactory.decodeStream(in_stream)
            } catch (e: Exception) {
                Log.e(TAG, e.message)
                e.printStackTrace()
            }

            return _icon_bmp
        }

        override fun onPostExecute(result: Bitmap) {
            _image.setImageBitmap(result)
        }
    }

    private var _google_api_client: GoogleApiClient? = null
    private var _auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var _auth_listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        updateUserInfoView(user)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_user_info, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sign_out_button.setOnClickListener { signOut() }
    }

    override fun onStart() {

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()

        _google_api_client = GoogleApiClient.Builder(activity)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()

        _google_api_client?.connect()

        super.onStart()

        _auth.addAuthStateListener(_auth_listener)
    }

    override fun onStop() {
        super.onStop()
        _auth.removeAuthStateListener(_auth_listener)
    }

    private fun updateUserInfoView(user: FirebaseUser?) {

        if (null == user) {
            Log.d(TAG, "FirebaseUser object is null")
            return
        }

        user_email_view.text = user.email
        post_user_name.text = user.displayName
        user_uid_view.text = user.uid

        if (null != user.photoUrl) {
            val task = DownloadImageTask(user_icon_view)
            task.execute(user.photoUrl!!.toString())
        } else {
            user_icon_view.setImageResource(R.drawable.anonymous_person)
        }

    }

    private fun signOut() {
        // Firebase sign out
        FirebaseAuth.getInstance().signOut()

        // Google sign out
        Auth.GoogleSignInApi.signOut(_google_api_client).setResultCallback { Log.d(TAG, "Logout!") }
    }

    companion object {

        private val TAG = "UserInfoFragment"

        fun newInstance(): UserInfoFragment {
            return UserInfoFragment()
        }
    }

}
