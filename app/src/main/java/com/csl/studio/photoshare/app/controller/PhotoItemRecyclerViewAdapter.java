package com.csl.studio.photoshare.app.controller;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.csl.studio.photoshare.app.R;
import com.csl.studio.photoshare.app.model.PostItem;
import com.csl.studio.photoshare.app.controller.viewholder.PostViewHolder;
import com.csl.studio.photoshare.app.model.UserAttribute;
import com.csl.studio.photoshare.app.utility.firebase.database.DatabaseUtility;
import com.csl.studio.photoshare.app.utility.firebase.storage.StorageUtility;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PostItem} and makes a call to the
 * specified {@link PhotoListFragment.OnListFragmentInteractionListener}.
 */
public class PhotoItemRecyclerViewAdapter extends RecyclerView.Adapter<PostViewHolder> {

    private Activity _activity;
    private List<PostItem> _post_data = new ArrayList<>();
    private PhotoListFragment.OnListFragmentInteractionListener _listener;
    private StorageReference _storage_ref;
    private DatabaseReference _database_ref;

    public PhotoItemRecyclerViewAdapter(Activity activity, List<PostItem> items, PhotoListFragment.OnListFragmentInteractionListener listener) {
        _activity = activity;
        _post_data = items;
        _listener = listener;
        _storage_ref = FirebaseStorage.getInstance().getReference();
        _database_ref = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_photo_item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PostViewHolder holder, int position) {

        final PostItem item = _post_data.get(position);
        holder._post_message.setText(item.message);
        holder._view_body.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(_activity, PostDetailActivity.class);
                it.putExtra(PostDetailActivity.POST_UID_TAG, item.post_uid);
                _activity.startActivity(it);
            }
        });

        _database_ref.child(DatabaseUtility.PATH.USER_INFO).child(item.auth_uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            UserAttribute user_attr = dataSnapshot.getValue(UserAttribute.class);
                            if (user_attr.name.isEmpty()) {
                                holder._user_name.setText(user_attr.email);
                            } else {
                                holder._user_name.setText(user_attr.name);
                            }
                        } else {
                            holder._user_name.setText(item.auth_uid);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        holder._user_name.setText(item.auth_uid);
                    }
                });

        Log.d(getClass().getName(), "Thumbnail ID: " + item.thumbnail);

        Glide.with(_activity)
                .using(new FirebaseImageLoader())
                .load(_storage_ref.child(StorageUtility.PATH.THUMBNAILS).child(item.thumbnail))
                .into(holder._post_image);

    }

    @Override
    public int getItemCount() {
        return _post_data.size();
    }

}
