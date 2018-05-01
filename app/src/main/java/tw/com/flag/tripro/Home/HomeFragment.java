package tw.com.flag.tripro.Home;

/**
 * Created by Tony on 2018/2/7.
 */


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import tw.com.flag.tripro.R;
import tw.com.flag.tripro.Utils.MainfeedListAdapter;
import tw.com.flag.tripro.models.Comment;
import tw.com.flag.tripro.models.Photo;


/**
 * Created by User on 5/28/2017.
 */

public class HomeFragment extends Fragment {

    // set "TAG" for using Fragment Name easily
    private static final String TAG = "HomeFragment";

    //vars
    private ArrayList<Photo> mPhotos;
    private ArrayList<Photo> mPaginatedPhotos;
    private ArrayList<String> mFollowing;
    private ListView mListView;
    private MainfeedListAdapter mAdapter;
    private int mResults;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mListView = (ListView) view.findViewById(R.id.listView);
        mFollowing = new ArrayList<>();
        mPhotos = new ArrayList<>();

        getFollowing();

        return view;
    }

    //  add the following username from database into a list
    //  原本是從使用者的追蹤人找
    //  改變後->只要是這個app的使用者的po文都可以看得到

    private void getFollowing(){
        Log.d(TAG, "getFollowing: searching for following");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query tmp= ref.child("users");
        tmp.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found user: " +singleSnapshot.getKey());
                    mFollowing.add(singleSnapshot.getKey().toString());
                }
                getPhotos();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    // get photos and other information, such as, comments, username and...... of the following users
    private void getPhotos(){
        Log.d(TAG, "getPhotos: getting photos");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        for(int i = 0; i < mFollowing.size(); i++){
            final int count = i;
            Query query = reference
                    .child("user_posts")
                    .child(mFollowing.get(i))
                    .orderByChild(getString(R.string.field_user_id));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "datasnapshot: "+ dataSnapshot);
                    if(dataSnapshot.getValue()!=null){
                        for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                            Log.d(TAG, "getchildren: "+ singleSnapshot);

                            Photo photo = new Photo();
                            Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                            Log.d(TAG, " object here: "+ objectMap);
                            photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                            photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                            photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                            photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                            photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                            photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());
                            photo.setTrip_key(objectMap.get("trip_key").toString());

                            ArrayList<Comment> comments = new ArrayList<Comment>();
                            for (DataSnapshot dSnapshot : singleSnapshot
                                    .child(getString(R.string.field_comments)).getChildren()){
                                Comment comment = new Comment();
                                comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                                comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                                comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                                comments.add(comment);
                            }

                            photo.setComments(comments);
                            mPhotos.add(photo);
                        }
                    }
                    displayPhotos();

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    // display photos in the sequential order by time created.
    private void displayPhotos(){
        mPaginatedPhotos = new ArrayList<>();
        if(mPhotos != null){
            try{
                // 使按照創照時間順序排列
                Collections.sort(mPhotos, new Comparator<Photo>() {
                    @Override
                    public int compare(Photo o1, Photo o2) {
                        return o2.getDate_created().compareTo(o1.getDate_created());
                    }
                });
                //
                int iterations = mPhotos.size();

                if(iterations > 10){
                    iterations = 10;
                }

                mResults = 10;
                for(int i = 0; i < iterations; i++){
                    mPaginatedPhotos.add(mPhotos.get(i));
                }

                mAdapter = new MainfeedListAdapter(getActivity(), R.layout.layout_mainfeed_listitem, mPaginatedPhotos);
                mListView.setAdapter(mAdapter);

            }catch (NullPointerException e){
                Log.e(TAG, "displayPhotos: NullPointerException: " + e.getMessage() );
            }catch (IndexOutOfBoundsException e){
                Log.e(TAG, "displayPhotos: IndexOutOfBoundsException: " + e.getMessage() );
            }
        }
    }

    // do not know the function yet
    public void displayMorePhotos(){
        Log.d(TAG, "displayMorePhotos: displaying more photos");

        try{

            if(mPhotos.size() > mResults && mPhotos.size() > 0){

                int iterations;
                if(mPhotos.size() > (mResults + 10)){
                    Log.d(TAG, "displayMorePhotos: there are greater than 10 more photos");
                    iterations = 10;
                }else{
                    Log.d(TAG, "displayMorePhotos: there is less than 10 more photos");
                    iterations = mPhotos.size() - mResults;
                }

                //add the new photos to the paginated results
                for(int i = mResults; i < mResults + iterations; i++){
                    mPaginatedPhotos.add(mPhotos.get(i));
                }
                mResults = mResults + iterations;
                mAdapter.notifyDataSetChanged();
            }
        }catch (NullPointerException e){
            Log.e(TAG, "displayPhotos: NullPointerException: " + e.getMessage() );
        }catch (IndexOutOfBoundsException e){
            Log.e(TAG, "displayPhotos: IndexOutOfBoundsException: " + e.getMessage() );
        }
    }

}












