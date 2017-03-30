package com.csl.studio.photoshare.app;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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

import com.csl.studio.photoshare.app.utility.FileUtility;
import com.csl.studio.photoshare.app.utility.ImageUtility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class CreateMessageActivity extends BaseActivity {

    private class TakePhotoListener implements View.OnClickListener {

        private int request_code;
        private String img_name = "";

        TakePhotoListener(int request_code, String img_name) {
            this.request_code = request_code;
            this.img_name = img_name;
        }

        @Override
        public void onClick(View view) {
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

    private static final String TAG = "CreateMessageActivity";
    private static final String PHOTO_EXT = "jpg";
    private static final String PHOTO_NAME = "photo_name" + "." + PHOTO_EXT;
    private static final String PHOTO_RESIZE_NAME = "photo_resize_name" + "." + PHOTO_EXT;
    private final int TAKE_PHOTO_CODE = 1;
    private final int CHOOSE_GALLERY_CODE = 2;

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
            postMessage();
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
                _photo_view.setImageBitmap(BitmapFactory.decodeFile(_photo_path));
            }
            cursor.close();
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

    void postMessage() {

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

        final String message_uuid = UUID.randomUUID().toString();
        final String photo_uuid = UUID.randomUUID().toString();
        final String user_uid = user.getUid();

        Log.d(TAG, "message uuid:" + message_uuid);
        Log.d(TAG, "photo uuid: " + photo_uuid);
        Log.d(TAG, "user uid: " + user_uid);

        uploadPhoto(_photo_path, photo_uuid + ".jpg");
        uploadThumbnail(_photo_thumbnail_path, photo_uuid + ".jpg");

    }

    private void uploadPhoto(String file_path, String upload_name) {

        StorageReference photos_ref = _storage_ref.child("Photos/" + upload_name);

        Uri file_photo = Uri.fromFile(new File(file_path));
        photos_ref.putFile(file_photo)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        Log.d(TAG, "Upload Photo Successful");
                        Log.d(TAG, "URL: " + taskSnapshot.getDownloadUrl());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.d(TAG, "Upload Photo Error: " + exception.toString());
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        Log.d(TAG, "Upload photo complete");
                    }
                });

    }

    private void uploadThumbnail(String file_path, String upload_name) {

        Uri file_thumbnail = Uri.fromFile(new File(file_path));

        StorageReference thumbnails_ref = _storage_ref.child("Thumbnails/" + upload_name + ".jpg");

        thumbnails_ref.putFile(file_thumbnail)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "Upload Thumbnail success");
                        Log.d(TAG, "URL: " + taskSnapshot.getDownloadUrl());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Upload Thumbnail failure");
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        Log.d(TAG, "Upload Thumbnail complete");
                    }
                });

    }



}
