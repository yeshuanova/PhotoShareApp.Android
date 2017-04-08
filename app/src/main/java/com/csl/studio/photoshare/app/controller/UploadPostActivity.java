package com.csl.studio.photoshare.app.controller;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.csl.studio.photoshare.app.BuildConfig;
import com.csl.studio.photoshare.app.R;
import com.csl.studio.photoshare.app.model.PhotoAttribute;
import com.csl.studio.photoshare.app.model.PostItem;
import com.csl.studio.photoshare.app.utility.FileUtility;
import com.csl.studio.photoshare.app.utility.ImageUtility;
import com.csl.studio.photoshare.app.utility.firebase.database.DatabaseUtility;
import com.csl.studio.photoshare.app.utility.firebase.storage.StorageUtility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

public class UploadPostActivity extends BaseActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 299;
    private static final int MY_PERMISSIONS_REQUEST_READ_CAMERA = 299;

    private class TakePhotoListener implements View.OnClickListener {

        private int request_code;
        private String img_name = "";

        TakePhotoListener(int request_code, String img_name) {
            this.request_code = request_code;
            this.img_name = img_name;
        }

        @Override
        public void onClick(View view) {

            if (Build.VERSION.SDK_INT >= 23) {
                if (ContextCompat.checkSelfPermission(UploadPostActivity.this,
                        Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(UploadPostActivity.this,
                            Manifest.permission.CAMERA)) {

                    } else {
                        // No explanation needed, we can request the permission.
                        ActivityCompat.requestPermissions(UploadPostActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                MY_PERMISSIONS_REQUEST_READ_CAMERA);
                    }
                } else {
                    ActivityCompat.requestPermissions(UploadPostActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST_READ_CAMERA);

                    takePhotoAction();
                }

            } else {
                takePhotoAction();
            }
        }

        void takePhotoAction() {
            Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (it.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(it, TAKE_PHOTO_CODE);
            }

            Intent photo_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (photo_intent.resolveActivity(getPackageManager()) != null) {

                try {
                    String dir_name = BuildConfig.APPLICATION_ID;
                    File img_file = new File(FileUtility.getPublicPictureDir(dir_name), img_name);

                    _photo_path = img_file.getAbsolutePath();

                    Log.d(getClass().getName(), "Image Path: " + _photo_path);

                    photo_intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(img_file));
                    startActivityForResult(photo_intent, this.request_code);
                } catch (Exception e) {
                    Log.d(getClass().getName(), "Take Photo Error:\n" + e.toString());
                }
            }
        }
    }

    private class ChooseGalleryPhotoListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {

            if (Build.VERSION.SDK_INT >= 23) {
                if (ContextCompat.checkSelfPermission(UploadPostActivity.this,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(UploadPostActivity.this,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    } else {
                        ActivityCompat.requestPermissions(UploadPostActivity.this,
                                new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                    }
                } else {
                    ActivityCompat.requestPermissions(UploadPostActivity.this,
                            new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                    startChooseGallery();
                }

            } else {
                startChooseGallery();
            }

        }

        void startChooseGallery() {
            Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, CHOOSE_GALLERY_CODE);
        }

    }

    TextView _user_view;
    EditText _message_edit;
    ImageView _photo_view;
    ImageButton _gallery_btn;
    ImageButton _take_photo_btn;
    String _photo_path = "";
    String _photo_thumbnail_path = "";

    private StorageReference _storage_ref;
    private FirebaseDatabase _database_ref;

    private static final String TAG = "UploadPostActivity";
    private static final String PHOTO_EXT = "jpg";
    private static final String PHOTO_NAME = "photo_name" + "." + PHOTO_EXT;
    private static final String PHOTO_RESIZE_NAME = "photo_resize_name" + "." + PHOTO_EXT;
    private static final int TAKE_PHOTO_CODE = 1;
    private static final int CHOOSE_GALLERY_CODE = 2;

    private int _upload_count = 0;
    private int _upload_success_count = 0;
    private static final int UPLOAD_MAX_TASK = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);

        _user_view = (TextView) findViewById(R.id.post_user_view);

        _message_edit = (EditText) findViewById(R.id.post_content_view);
        _photo_view = (ImageView) findViewById(R.id.photo_view);

        _gallery_btn = (ImageButton) findViewById(R.id.choose_gallery_photo_btn);
        _gallery_btn.setOnClickListener(new ChooseGalleryPhotoListener());

        _take_photo_btn = (ImageButton) findViewById(R.id.take_photo_camera_btn);
        _take_photo_btn.setOnClickListener(new TakePhotoListener(TAKE_PHOTO_CODE, PHOTO_NAME));

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (null != user) {
            _user_view.setText(user.getEmail());
        }

        _storage_ref = FirebaseStorage.getInstance().getReference();
        _database_ref = FirebaseDatabase.getInstance();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navigation_take_photo_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        final int id = item.getItemId();
        if (R.id.nav_action_post == id) {
            submitPost();
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (TAKE_PHOTO_CODE == requestCode && Activity.RESULT_OK == resultCode) {
            // Show Progress View
            File img_file = new File(_photo_path);
            if (img_file.exists()) {
                Bitmap origin_bmp = ImageUtility.decodeBitmapFromFile(_photo_path);
                _photo_view.setImageBitmap(origin_bmp);

                convertToThumbnail(origin_bmp, 200);
            }
        } else if (CHOOSE_GALLERY_CODE == requestCode && Activity.RESULT_OK == resultCode) {
            Uri image_uri = data.getData();

            Log.d(TAG, "Image URI: " + image_uri);

            String[] file_path_column = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(image_uri, file_path_column, null, null, null);

            if (null != cursor) {
                cursor.moveToFirst();
                int column_index = cursor.getColumnIndex(file_path_column[0]);
                _photo_path = cursor.getString(column_index);
                Log.d(TAG, "Image Path" + _photo_path);

                Bitmap bitmap = BitmapFactory.decodeFile(_photo_path);
                _photo_view.setImageBitmap(bitmap);
                convertToThumbnail(bitmap, 200);
                cursor.close();
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSIONS_REQUEST_READ_CAMERA) {
            Log.d(TAG, "Receive MY_PERMISSIONS_REQUEST_READ_CAMERA");
        } else if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            Log.d(TAG, "Receive MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE");
        }
    }

    private void convertToThumbnail(Bitmap origin_bmp, int max_size) {

        final float ratio = origin_bmp.getWidth() / (float) origin_bmp.getHeight();

        int new_width;
        int new_height;

        if (origin_bmp.getWidth() >= origin_bmp.getHeight()) {
            new_width = max_size;
            new_height = Math.round(new_width / ratio);
        } else if (origin_bmp.getHeight() > origin_bmp.getWidth()) {
            new_height = max_size;
            new_width = Math.round(new_height * ratio);
        } else {
            new_width = origin_bmp.getWidth();
            new_height = origin_bmp.getHeight();
        }

        Bitmap resize_bmp = Bitmap.createScaledBitmap(origin_bmp, new_width, new_height, true);
        Log.d(getClass().getName(), "Resize Width: " + resize_bmp.getWidth() + ", Height: " + resize_bmp.getHeight());

        FileOutputStream out = null;
        try {
            String dir_name = BuildConfig.APPLICATION_ID;
            _photo_thumbnail_path = FileUtility.getPublicPictureDir(dir_name).getAbsolutePath() + "/" + PHOTO_RESIZE_NAME;
            out = new FileOutputStream(_photo_thumbnail_path);
            resize_bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d(getClass().getName(), "Resize Photo Path: " + _photo_thumbnail_path);
    }

    private void submitPost() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (null == user) {
            Toast.makeText(this, "Please Sign", Toast.LENGTH_SHORT).show();
            return;
        }

        if (null == _photo_path || _photo_path.isEmpty()) {
            Toast.makeText(this, "No shared photo", Toast.LENGTH_SHORT).show();
            return;
        }

        if (null == _photo_thumbnail_path || _photo_thumbnail_path.isEmpty()) {
            Toast.makeText(this, "No thumbnail photo", Toast.LENGTH_SHORT).show();
            return;
        }

        try {

            initUploadCount();
            showProgressDialog();

            DatabaseReference post_ref = _database_ref.getReference();
            final String key = post_ref.child("posts").push().getKey();
            final String photo_name = sha1_file(new File(_photo_path));
            final String thumbnail_name = sha1_file(new File(_photo_thumbnail_path));
            final String auth_uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            Map<String, Object> post_map = createPostMap(photo_name, thumbnail_name);

            final String post_path = "/" + DatabaseUtility.PATH.POSTS + "/" + key;
            final String user_posts_path = "/" + DatabaseUtility.PATH.USER_POSTS
                    + "/" + auth_uid + "/" + key;
            final String image_info_path = "/" + DatabaseUtility.PATH.IMAGE_INFO + "/";

            final Map<String, Object> update_list = new HashMap<>();
            update_list.put(post_path, post_map);
            update_list.put(user_posts_path, post_map);
            update_list.put(image_info_path + photo_name, createImageInfo(_photo_path));
            update_list.put(image_info_path + thumbnail_name, createImageInfo(_photo_thumbnail_path));

            post_ref.updateChildren(update_list, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    Log.d(TAG, "Update Post complete");
                    uploadComplete(null == databaseError);
                }
            });

            // Upload original image to Firebase Storage
            StorageReference image_ref = _storage_ref
                    .child(StorageUtility.PATH.PHOTOS).child(photo_name);

            image_ref.putFile(Uri.fromFile(new File(_photo_path)))
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            Log.d(TAG, "Update Image Complete");
                            uploadComplete(task.isSuccessful());
                        }
                    });

            // Upload thumbnail of image to Firebase Storage
            StorageReference thumbnail_ref = _storage_ref
                    .child(StorageUtility.PATH.THUMBNAILS).child(thumbnail_name);

            thumbnail_ref.putFile(Uri.fromFile(new File(_photo_thumbnail_path)))
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            Log.d(TAG, "Update Thumbnail Complete");
                            uploadComplete(task.isSuccessful());
                        }
                    });

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();

            Toast.makeText(this, "Upload Post Error!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Exception Error!", Toast.LENGTH_SHORT).show();
        }

    }

    private Map<String, Object> createPostMap(String photo_name, String thumbnail_name) {
        PostItem post = new PostItem();
        post.auth_uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        post.message = _message_edit.getText().toString();
        post.photo = photo_name;
        post.thumbnail = thumbnail_name;
        post.time = getCurrentTimeString();

        return post.toMap();
    }

    private Map<String, Object> createImageInfo(String file_path) {
        PhotoAttribute attr = new PhotoAttribute();
        attr.ext = file_path.substring(file_path.lastIndexOf(".") + 1);
        attr.upload_time = getCurrentTimeString();

        return attr.toMap();
    }

    private void initUploadCount() {
        _upload_count = 0;
        _upload_success_count = 0;
    }

    private void checkUploadCount() {

        if (_upload_count >= UPLOAD_MAX_TASK) {
            if (_upload_count > _upload_success_count) {
                Toast.makeText(this, "Upload Error!", Toast.LENGTH_SHORT).show();
                initUploadCount();
            } else {
                hideProgressDialog();
                finish();
            }
        }
    }

    private void uploadComplete(Boolean is_success) {
        _upload_count++;
        if (is_success) {
            _upload_success_count++;
        }
        checkUploadCount();
    }

    private String getCurrentTimeString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        return sdf.format(new Date());
    }

    private String sha1_file(final File file) throws NoSuchAlgorithmException, IOException {
        final MessageDigest messageDigest = MessageDigest.getInstance("SHA1");

        try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
            final byte[] buffer = new byte[1024];
            for (int read; (read = is.read(buffer)) != -1; ) {
                messageDigest.update(buffer, 0, read);
            }
        }

        // Convert the byte to hex format
        try (Formatter formatter = new Formatter()) {
            for (final byte b : messageDigest.digest()) {
                formatter.format("%02x", b);
            }
            return formatter.toString();
        }
    }

}
