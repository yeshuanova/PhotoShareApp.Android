package com.csl.studio.photoshare.app.controller

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.csl.studio.photoshare.app.R
import com.csl.studio.photoshare.app.controller.viewholder.PostViewHolder
import com.csl.studio.photoshare.app.model.PostItem
import com.csl.studio.photoshare.app.model.UserAttribute
import com.csl.studio.photoshare.app.utility.firebase.database.DatabaseUtility
import com.csl.studio.photoshare.app.utility.firebase.storage.StorageUtility
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

/**
 * [RecyclerView.Adapter] that can display a [PostItem] and makes a call to the
 */
class PhotoItemRecyclerViewAdapter(private val _activity: Activity, items: List<PostItem>) : RecyclerView.Adapter<PostViewHolder>() {

    private var _post_data = items
    private val _storage_ref: StorageReference?
    private val _database_ref: DatabaseReference?

    init {
        _storage_ref = FirebaseStorage.getInstance().reference
        _database_ref = FirebaseDatabase.getInstance().reference
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_photo_item, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {

        val item = _post_data[position]
        holder._post_message.text = item.message
        holder._view_body.setOnClickListener {
            val open_detail_intent = Intent(_activity, PostDetailActivity::class.java)
            open_detail_intent.putExtra(PostDetailActivity.POST_UID_TAG, item.post_uid)
            _activity.startActivity(open_detail_intent)
        }

        _database_ref?.child(DatabaseUtility.PATH.USER_INFO)?.child(item.auth_uid)
                ?.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            val user_attr = dataSnapshot.getValue<UserAttribute>(UserAttribute::class.java)
                            if (user_attr!!.name.isEmpty()) {
                                holder._user_name.text = user_attr.email
                            } else {
                                holder._user_name.text = user_attr.name
                            }
                        } else {
                            holder._user_name.text = item.auth_uid
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        holder._user_name.text = item.auth_uid
                    }
                })

        Log.d(javaClass.name, "Thumbnail ID: " + item.thumbnail)

        Glide.with(_activity)
                .using(FirebaseImageLoader())
                .load(_storage_ref?.child(StorageUtility.PATH.THUMBNAILS)?.child(item.thumbnail))
                .into(holder._post_image)

    }

    override fun getItemCount(): Int {
        return _post_data.size
    }

}
