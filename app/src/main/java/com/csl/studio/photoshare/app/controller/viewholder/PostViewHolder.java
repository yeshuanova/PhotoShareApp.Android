package com.csl.studio.photoshare.app.model;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.csl.studio.photoshare.app.R;

public class PostViewHolder extends RecyclerView.ViewHolder {
    public ImageView _author_icon;
    public TextView _user_name;
    public TextView _post_message;
    public ImageView _post_image;
    public View _view_body;

    public PostViewHolder(View view) {
        super(view);
        _author_icon = (ImageView) view.findViewById(R.id.author_icon);
        _user_name = (TextView) view.findViewById(R.id.post_user_name);
        _post_message = (TextView) view.findViewById(R.id.post_content);
        _post_image = (ImageView) view.findViewById(R.id.post_thumbnail);
        _view_body = view;
    }

}
