package com.csl.studio.photoshare.app.controller.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import com.csl.studio.photoshare.app.R
import kotlinx.android.synthetic.main.fragment_photo_item.view.*

class PostViewHolder(var _view_body: View) : RecyclerView.ViewHolder(_view_body) {
    var _author_icon: ImageView = _view_body.author_icon
    var _user_name: TextView = _view_body.post_user_name
    var _post_message: TextView = _view_body.post_content
    var _post_image: ImageView = _view_body.post_thumbnail

}
