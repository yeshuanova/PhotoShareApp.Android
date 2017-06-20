package com.csl.studio.photoshare.app.controller

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.csl.studio.photoshare.app.R
import com.csl.studio.photoshare.app.model.UserAttribute
import com.csl.studio.photoshare.app.utility.firebase.database.DatabaseUtility
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class PhotoShareMainActivity : BaseActivity(), GoogleApiClient.OnConnectionFailedListener {

    private val _select_nav_item_listener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {

                Toast.makeText(this@PhotoShareMainActivity, "Home", Toast.LENGTH_SHORT).show()
                run {
                    val mgr = supportFragmentManager
                    val tr = mgr.beginTransaction()

                    tr.replace(R.id.content, _photo_list_frag)
                    tr.commit()
                }

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_user_info -> {

                val mgr = supportFragmentManager
                val tr = mgr.beginTransaction()

                tr.replace(R.id.content, _user_info_frag)
                tr.commit()

                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private var _user_info_frag: UserInfoFragment? = null
    private var _photo_list_frag: PhotoListFragment? = null
    private var _nav_item: BottomNavigationView? = null
    private var _auth: FirebaseAuth? = null
    private var _auth_listener: FirebaseAuth.AuthStateListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_show)

        _nav_item = findViewById(R.id.navigation) as BottomNavigationView
        _nav_item!!.setOnNavigationItemSelectedListener(_select_nav_item_listener)

        // Initial Firebase Authentication
        _auth = FirebaseAuth.getInstance()
        _auth_listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                Log.d(TAG, "onAuthStateChanged:signed_in")
                writeUserInfo(user)
                updateUI()
            } else {
                Log.d(TAG, "onAuthStateChanged:signed_out")
                val it = Intent(this@PhotoShareMainActivity, LoginActivity::class.java)
                startActivity(it)
            }
        }

        _user_info_frag = UserInfoFragment.newInstance()
        _photo_list_frag = PhotoListFragment.newInstance()

    }

    public override fun onStart() {
        super.onStart()
        _auth!!.addAuthStateListener(_auth_listener!!)
    }

    override fun onStop() {
        super.onStop()
        if (null != _auth_listener) {
            _auth!!.removeAuthStateListener(_auth_listener!!)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.navigation_main_top, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        val id = item.itemId
        if (R.id.action_open_post_activity == id) {
            openCreateMessageActivity()
        }
        return false
    }

    private fun writeUserInfo(user: FirebaseUser) {
        val user_attr = UserAttribute()
        user_attr.name = user.displayName ?: ""
        user_attr.email = user.email ?: ""

        val user_info_ref = FirebaseDatabase.getInstance().reference
        user_info_ref.child(DatabaseUtility.PATH.USER_INFO).child(user.uid).setValue(user_attr)
    }

    private fun updateUI() {
        _nav_item!!.setOnNavigationItemSelectedListener(_select_nav_item_listener)
        _nav_item!!.selectedItemId = R.id.navigation_home
    }

    private fun openCreateMessageActivity() {
        val it = Intent(this, UploadPostActivity::class.java)
        startActivityForResult(it, CREATE_MESSAGE_CODE)
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult)
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show()
    }

    companion object {

        private val TAG = "PhotoShareMainActivity"
        private val CREATE_MESSAGE_CODE = 1000
    }
}
