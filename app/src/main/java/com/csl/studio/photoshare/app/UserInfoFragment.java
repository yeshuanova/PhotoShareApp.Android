package com.csl.studio.photoshare.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserInfoFragment extends Fragment {

    private static String TAG = "UserInfoFragment";

    public UserInfoFragment() {
        // Required empty public constructor
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
        // Inflate the layout for this fragment

        View root_view = inflater.inflate(R.layout.fragment_user_info, container, false);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        updateUserInfoView(root_view, user);

        return root_view;
    }

    private void updateUserInfoView(View root_view, FirebaseUser user) {

        if (null == root_view) {
            Log.d(TAG, "UIView is null");
            return;
        }

        if (null == user) {
            Log.d(TAG, "FirebaseUser object is null");
            return;
        }

        TextView email_view = (TextView) root_view.findViewById(R.id.user_email_view);
        email_view.setText(user.getEmail());

        TextView user_name_view = (TextView) root_view.findViewById(R.id.user_name_view);
        user_name_view.setText(user.getDisplayName());

        TextView uid_view = (TextView) root_view.findViewById(R.id.user_uid_view);
        uid_view.setText(user.getUid());

        ImageView user_icon_view = (ImageView) root_view.findViewById(R.id.user_icon_view);
        user_icon_view.setImageURI(user.getPhotoUrl());

    }

}
