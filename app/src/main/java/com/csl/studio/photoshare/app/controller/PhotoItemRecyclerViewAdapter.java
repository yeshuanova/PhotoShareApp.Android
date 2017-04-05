package com.csl.studio.photoshare.app.controller;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.csl.studio.photoshare.app.R;
import com.csl.studio.photoshare.app.model.PostItem;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PostItem} and makes a call to the
 * specified {@link PhotoListFragment.OnListFragmentInteractionListener}.
 */
public class PhotoItemRecyclerViewAdapter extends RecyclerView.Adapter<PhotoItemRecyclerViewAdapter.ViewHolder> {

    private Activity _activity;
    private List<PostItem> _post_data = new ArrayList<>();
    private PhotoListFragment.OnListFragmentInteractionListener _Listener;
    private StorageReference _storage_ref;
    private FirebaseAuth _auth;

    public PhotoItemRecyclerViewAdapter(Activity activity, List<PostItem> items, PhotoListFragment.OnListFragmentInteractionListener listener) {
        _activity = activity;
        _post_data = items;
        _Listener = listener;
        _storage_ref = FirebaseStorage.getInstance().getReference();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_photo_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        PostItem item = _post_data.get(position);
        holder._user_name.setText(item.user_uid);
        holder._post_message.setText(item.post_content);
        holder._user_icon.setImageResource(R.drawable.anonymous_person);

        StorageReference ref = _storage_ref.child("thumbnail").child(item.thumbnail_name);
        Glide.with(_activity)
                .using(new FirebaseImageLoader())
                .load(ref)
                .into(holder._post_image);

    }
j
    @Override
    public int getItemCount() {
        return _post_data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView _user_icon;
        public TextView _user_name;
        public TextView _post_message;
        public ImageView _post_image;

        public ViewHolder(View view) {
            super(view);
            _user_icon = (ImageView) view.findViewById(R.id.user_icon_view);
            _user_name = (TextView) view.findViewById(R.id.author_name);
            _post_message = (TextView) view.findViewById(R.id.post_content);
            _post_image = (ImageView) view.findViewById(R.id.post_thumbnail);

        }

    }
}
