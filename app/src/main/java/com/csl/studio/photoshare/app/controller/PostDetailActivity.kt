package com.csl.studio.photoshare.app.controller

import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.csl.studio.photoshare.app.R
import com.csl.studio.photoshare.app.model.PostItem
import com.csl.studio.photoshare.app.model.UserAttribute
import com.csl.studio.photoshare.app.utility.firebase.database.DatabaseUtility
import com.csl.studio.photoshare.app.utility.firebase.storage.StorageUtility
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_post_detail.*

class PostDetailActivity : BaseActivity() {

    private var _post_uid: String = ""
    private var _database_ref: DatabaseReference? = null
    private var _post_ref: DatabaseReference? = null
    private var _storage_ref: StorageReference? = null
    private var _fetch_post_listener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        _post_uid = intent.getStringExtra(POST_UID_TAG)

        if (_post_uid.isEmpty()) {
            initialError()
        }

        _database_ref = FirebaseDatabase.getInstance().reference
        _storage_ref = FirebaseStorage.getInstance().reference

        _fetch_post_listener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.exists()) {

                    val item = dataSnapshot.getValue<PostItem>(PostItem::class.java)

                    post_user_name.text = item?.auth_uid
                    post_content.text = item?.message
                    post_upload_time.text = item?.time

                    val photo_ref = _storage_ref!!.child(StorageUtility.PATH.PHOTOS)
                            .child(item!!.photo)

                    Glide.with(this@PostDetailActivity)
                            .using(FirebaseImageLoader())
                            .load(photo_ref)
                            .into(post_image!!)

                    val user_ref = _database_ref!!.child(DatabaseUtility.PATH.USER_INFO)
                            .child(item.auth_uid)

                    user_ref.addListenerForSingleValueEvent(object : ValueEventListener {

                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                val user_attr = dataSnapshot.getValue<UserAttribute>(UserAttribute::class.java)
                                if (user_attr!!.name.isEmpty()) {
                                    post_user_name.text = user_attr.email
                                } else {
                                    post_user_name.text = user_attr.email
                                }
                            } else {
                                post_user_name!!.text = item.auth_uid
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            post_user_name.text = item.auth_uid
                        }
                    })
                } else {
                    initialError()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }

    }

    override fun onStart() {
        super.onStart()
        _post_ref = _database_ref?.child(DatabaseUtility.PATH.POSTS)?.child(_post_uid)
        _post_ref?.addListenerForSingleValueEvent(_fetch_post_listener)
    }

    override fun onStop() {
        super.onStop()
        _fetch_post_listener?.let {
            _post_ref!!.removeEventListener(_fetch_post_listener!!)
        }
    }

    private fun initialError() {
        hideProgressDialog()
        Toast.makeText(this, "No Post Data", Toast.LENGTH_SHORT).show()
        finish()
    }

    companion object {
        val POST_UID_TAG = "post_uid_tag"
    }

}
