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
import com.csl.studio.photoshare.app.model.PostViewHolder;
import com.firebase.ui.storage.images.FirebaseImageLoader;
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

    public PhotoItemRecyclerViewAdapter(Activity activity, List<PostItem> items, PhotoListFragment.OnListFragmentInteractionListener listener) {
        _activity = activity;
        _post_data = items;
        _listener = listener;
        _storage_ref = FirebaseStorage.getInstance().getReference();
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
        holder._user_name.setText(item.auth_uid);
        holder._post_message.setText(item.message);
        holder._view_body.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(_activity, PostDetailActivity.class);
                it.putExtra(PostDetailActivity.POST_UID_TAG, item.post_uid);
                _activity.startActivity(it);
            }
        });

        Log.d(getClass().getName(), "Thumbnail ID: " + item.thumbnail);

        Glide.with(_activity)
                .using(new FirebaseImageLoader())
                .load(_storage_ref.child("Thumbnails").child(item.thumbnail))
                .into(holder._post_image);
    }

    @Override
    public int getItemCount() {
        return _post_data.size();
    }

}
