package com.csl.studio.photoshare.app.controller;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.csl.studio.photoshare.app.R;
import com.csl.studio.photoshare.app.model.PostItem;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PostItem} and makes a call to the
 * specified {@link PhotoListFragment.OnListFragmentInteractionListener}.
 */
public class PhotoItemRecyclerViewAdapter extends RecyclerView.Adapter<PhotoItemRecyclerViewAdapter.ViewHolder> {

    private List<PostItem> _post_data = new ArrayList<>();
    private PhotoListFragment.OnListFragmentInteractionListener _Listener;

    public PhotoItemRecyclerViewAdapter(List<PostItem> items, PhotoListFragment.OnListFragmentInteractionListener listener) {
        _post_data = items;
        _Listener = listener;
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

    }

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
            _user_name = (TextView) view.findViewById(R.id.user_name_view);
            _post_message = (TextView) view.findViewById(R.id.post_message_view);
            _post_image = (ImageView) view.findViewById(R.id.image_thumbnail);

        }

    }
}
