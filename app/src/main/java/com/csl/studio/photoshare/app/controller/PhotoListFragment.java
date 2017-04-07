package com.csl.studio.photoshare.app.controller;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.csl.studio.photoshare.app.R;
import com.csl.studio.photoshare.app.model.PostItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of PhotoItems.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class PhotoListFragment extends Fragment {

    private static final String TAG = "PhotoListFragment";
    private DatabaseReference _firebase_db_ref;

    public PhotoListFragment() {
    }

    public static PhotoListFragment newInstance() {
        PhotoListFragment fragment = new PhotoListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _firebase_db_ref = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_item_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            final RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));

            _firebase_db_ref.child("posts").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    List<PostItem> items = new ArrayList<>();

                    for (DataSnapshot post_data : dataSnapshot.getChildren()) {
                        String key = post_data.getKey();
                        Log.d(TAG, "Snapshot key: " + key);

                        PostItem format = post_data.getValue(PostItem.class);
                        items.add(format);

                        recyclerView.setAdapter(new PhotoItemRecyclerViewAdapter(
                                getActivity(),
                                items,
                                null
                        ));

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(PostItem item);
    }
}
