package com.csl.studio.photoshare.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.InputStream;

public class UserInfoFragment extends Fragment {

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView _image;

        DownloadImageTask(ImageView _image) {
            this._image = _image;
        }

        protected Bitmap doInBackground(String... urls) {

            String image_url = urls[0];

            Log.d(TAG, "Load Image from " + image_url);

            Bitmap _icon_bmp = null;
            try {
                InputStream in = new java.net.URL(image_url).openStream();
                _icon_bmp = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }
            return _icon_bmp;
        }

        protected void onPostExecute(Bitmap result) {
            _image.setImageBitmap(result);
        }
    }

    private static String TAG = "UserInfoFragment";

    private DownloadImageTask _load_image_task;
    private TextView _user_email_view;
    private TextView _user_name_view;
    private TextView _user_uid_view;
    private ImageView _user_photo_view;

    public UserInfoFragment() {

    }

    public static UserInfoFragment newInstance() {
        return new UserInfoFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root_view = inflater.inflate(R.layout.fragment_user_info, container, false);

        _user_photo_view = (ImageView) root_view.findViewById(R.id.user_icon_view);
        _load_image_task = new DownloadImageTask(_user_photo_view);

        _user_email_view = (TextView) root_view.findViewById(R.id.user_email_view);
        _user_name_view = (TextView) root_view.findViewById(R.id.user_name_view);
        _user_uid_view = (TextView) root_view.findViewById(R.id.user_uid_view);

        Button sign_out = (Button) root_view.findViewById(R.id.sign_out_button);
        sign_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
            }
        });

        updateUserInfoView(FirebaseAuth.getInstance().getCurrentUser());

        return root_view;
    }

    private void updateUserInfoView(FirebaseUser user) {

        if (null == user) {
            Log.d(TAG, "FirebaseUser object is null");
            return;
        }

        _user_email_view.setText(user.getEmail());
        _user_name_view.setText(user.getDisplayName());
        _user_uid_view.setText(user.getUid());

        if (null != user.getPhotoUrl()) {
            _load_image_task.execute(user.getPhotoUrl().toString());
        } else {
            _user_photo_view.setImageResource(R.drawable.anonymous_person);
        }

    }

}
