package com.csl.studio.photoshare.app.controller

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.csl.studio.photoshare.app.R
import com.csl.studio.photoshare.app.model.PostItem
import com.google.firebase.database.*
import java.util.*

/**
 * A fragment representing a list of PhotoItems.
 */

class PhotoListFragment : Fragment() {

    private val _firebase_db_ref = FirebaseDatabase.getInstance().reference

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_photo_item_list, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (view is RecyclerView) {
            val context = view.getContext()
            val recyclerView = view

            recyclerView.layoutManager = LinearLayoutManager(context)

            _firebase_db_ref.child("posts").addValueEventListener(object : ValueEventListener {

                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    val items = ArrayList<PostItem>()

                    for (post_data in dataSnapshot.children) {
                        val key = post_data.key
                        Log.d(TAG, "Snapshot key: " + key)

                        val format = post_data.getValue<PostItem>(PostItem::class.java)

                        format?.let {
                            format.post_uid = key
                            items.add(format)
                        }
                    }
                    recyclerView.adapter = PhotoItemRecyclerViewAdapter(activity, items)
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })

        }

    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

    }

    override fun onDetach() {
        super.onDetach()
    }

    companion object {

        private val TAG = "PhotoListFragment"

        fun newInstance(): PhotoListFragment {
            val fragment = PhotoListFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}
