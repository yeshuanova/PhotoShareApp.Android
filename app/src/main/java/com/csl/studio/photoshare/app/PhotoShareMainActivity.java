package com.csl.studio.photoshare.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PhotoShareMainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private BottomNavigationView.OnNavigationItemSelectedListener _select_nav_item_listener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:

                    Toast.makeText(PhotoShareMainActivity.this, "Home", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.navigation_user_shared_photo:

                    Toast.makeText(PhotoShareMainActivity.this, "Shared", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.navigation_user_info:

                    FragmentManager mgr = getSupportFragmentManager();
                    FragmentTransaction tr = mgr.beginTransaction();

                    tr.replace(R.id.content, _user_info_frag);
                    tr.commit();

                    return true;
            }
            return false;
        }

    };

    private static final String TAG = "PhotoShareMainActivity";
    private static final int CREATE_MESSAGE_CODE = 1000;

    private UserInfoFragment _user_info_frag;
    private BottomNavigationView _nav_item;
    private FirebaseAuth _auth;
    private FirebaseAuth.AuthStateListener _auth_listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_show);

        _nav_item = (BottomNavigationView) findViewById(R.id.navigation);
        _nav_item.setOnNavigationItemSelectedListener(_select_nav_item_listener);

        // Initial Firebase Authentication
        _auth = FirebaseAuth.getInstance();
        _auth_listener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in");
                    updateUI();
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Intent it = new Intent(PhotoShareMainActivity.this, LoginActivity.class);
                    startActivity(it);
                }
            }
        };

        _user_info_frag = UserInfoFragment.newInstance();

    }

    @Override
    protected void onStart() {
        super.onStart();
        _auth.addAuthStateListener(_auth_listener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != _auth_listener) {
            _auth.removeAuthStateListener(_auth_listener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navigation_main_top, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        final int id = item.getItemId();
        if (R.id.action_open_post_activity == id) {
            openCreateMessageActivity();
        }
        return false;
    }

    private void updateUI() {
        _nav_item.setOnNavigationItemSelectedListener(_select_nav_item_listener);
        _nav_item.setSelectedItemId(R.id.navigation_home);
    }

    private void openCreateMessageActivity() {
        Intent it = new Intent(this, CreateMessageActivity.class);
        startActivityForResult(it, CREATE_MESSAGE_CODE);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
}
