package com.csl.studio.photoshare.app.controller;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.csl.studio.photoshare.app.R;
import com.csl.studio.photoshare.app.model.PostItem;
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

public class PostDetailActivity extends BaseActivity {


    public static final String POST_UID_TAG = "post_uid_tag";

    private String _post_uid = "";
    private DatabaseReference _database_ref;
    private StorageReference _storage_ref;

    private ImageView _post_user_icon;
    private TextView _post_user_name;
    private TextView _post_content;
    private ImageView _post_image;
    private TextView _post_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        _post_uid = getIntent().getStringExtra(POST_UID_TAG);

        if (null == _post_uid || _post_uid.isEmpty()) {
            initialError();
        }

        _post_user_icon = (ImageView) findViewById(R.id.post_user_icon);
        _post_user_name = (TextView) findViewById(R.id.post_user_name);
        _post_content = (TextView) findViewById(R.id.post_content);
        _post_image = (ImageView) findViewById(R.id.post_image);
        _post_time = (TextView) findViewById(R.id.post_upload_time);

        _database_ref = FirebaseDatabase.getInstance().getReference();
        _storage_ref = FirebaseStorage.getInstance().getReference();

        startUpdateDetailPostUI();
    }

    private void startUpdateDetailPostUI() {

        showProgressDialog();

        DatabaseReference post_ref = _database_ref
                .child(DatabaseUtility.PATH.POSTS)
                .child(_post_uid);

        post_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    PostItem item = dataSnapshot.getValue(PostItem.class);
                    _post_user_name.setText(item.auth_uid);
                    _post_content.setText(item.message);
                    _post_time.setText(item.time);


                    StorageReference photo_ref = _storage_ref.child(StorageUtility.PATH.PHOTOS)
                            .child(item.photo);

                    Glide.with(PostDetailActivity.this)
                            .using(new FirebaseImageLoader())
                            .load(photo_ref)
                            .into(_post_image);
                } else {
                    initialError();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                initialError();
            }
        });

    }

    private void initialError() {

        hideProgressDialog();
        Toast.makeText(this, "No Post Data", Toast.LENGTH_SHORT).show();
        finish();
    }


}
