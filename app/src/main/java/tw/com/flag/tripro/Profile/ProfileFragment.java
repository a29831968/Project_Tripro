package tw.com.flag.tripro.Profile;

/**
 * Created by Tony on 2018/2/11.
 */
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import tw.com.flag.tripro.Home.ChatActivity;
import tw.com.flag.tripro.Home.ChatDetails;
import tw.com.flag.tripro.R;
import tw.com.flag.tripro.Utils.BottomNavigationViewHelper;
import tw.com.flag.tripro.Utils.FirebaseMethods;
import tw.com.flag.tripro.Utils.GridImageAdapter;
import tw.com.flag.tripro.Utils.UniversalImageLoader;
import tw.com.flag.tripro.models.Comment;
import tw.com.flag.tripro.models.Like;
import tw.com.flag.tripro.models.Photo;
import tw.com.flag.tripro.models.UserAccountSettings;
import tw.com.flag.tripro.models.UserSettings;






public class ProfileFragment extends Fragment {

    // set "TAG" for using Fragment Name easily
    private static final String TAG = "ProfileFragment";


    // Do not know how to use interface
    public interface OnGridImageSelectedListener{
        void onGridImageSelected(Photo photo, int activityNumber);
    }
    OnGridImageSelectedListener mOnGridImageSelectedListener;

    // set "ACTIVITY_NUM" to know which activity does this belongs to
    private static final int ACTIVITY_NUM = 4;

    // set Column in the profile share repository
    private static final int NUM_GRID_COLUMNS = 3;

    //firebase related declare
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;


    //widgets declare
    private TextView mShare, mItinerary, mDisplayName, mUsername, mWebsite, mDescription;
    private ProgressBar mProgressBar;
    private CircleImageView mProfilePhoto;
    private GridView gridView;
    private Toolbar toolbar;
    private ImageView profileMenu;
    private BottomNavigationViewEx bottomNavigationView;
    private Context mContext;
    private TextView textSend;
    private TextView txtshare, txtitineray;


    //vars
    private int mShareCount = 0;
    private int mItineraryCount = 0;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // get all the widget from layout
        mDisplayName = (TextView) view.findViewById(R.id.display_name);
        mUsername = (TextView) view.findViewById(R.id.username);
        mWebsite = (TextView) view.findViewById(R.id.website);
        mDescription = (TextView) view.findViewById(R.id.description);
        mProfilePhoto = (CircleImageView) view.findViewById(R.id.profile_photo);
        mShare = (TextView) view.findViewById(R.id.tvShare);
        mItinerary = (TextView) view.findViewById(R.id.tvItinerary);
        mProgressBar = (ProgressBar) view.findViewById(R.id.profileProgressBar);
        gridView = (GridView) view.findViewById(R.id.gridView);
        toolbar = (Toolbar) view.findViewById(R.id.profileToolBar);
        profileMenu = (ImageView) view.findViewById(R.id.profileMenu);
        bottomNavigationView = (BottomNavigationViewEx) view.findViewById(R.id.bottomNavViewBar);
        mContext = getActivity();
        txtitineray=(TextView)view.findViewById(R.id.textItinerary);
        txtshare=(TextView)view.findViewById(R.id.textShare);
        mFirebaseMethods = new FirebaseMethods(getActivity());
        Log.d(TAG, "onCreateView: stared.");
        mItinerary.setVisibility(View.INVISIBLE);
        mShare.setVisibility(View.INVISIBLE);
        txtitineray.setVisibility(View.INVISIBLE);
        txtshare.setVisibility(View.INVISIBLE);
        textSend=(TextView)view.findViewById(R.id.textSend);

        // set up bottom navigation
        setupBottomNavigationView();

        //set up top tool bar
        setupToolbar();


        // set up "Firebase object"  to check if the user login or not
        // after set up "Firebase object", it will check if the user is log in or not every time back to this fragment
        setupFirebaseAuth();

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("user_account_settings");
        Query query=mDatabase.orderByChild("user_id").equalTo(mAuth.getCurrentUser().getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    Log.d(TAG, "addValueEvent"+singleSnapshot);
                    ChatDetails.display_name =singleSnapshot.getValue(UserAccountSettings.class).getDisplay_name();

                    Log.d(TAG, "displayname:"+ChatDetails.display_name);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        textSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(), ChatActivity.class);
                intent.putExtra(getActivity().getString(R.string.field_display_name),ChatDetails.display_name);
                startActivity(intent);
            }
        });

        // set up the data of GridView
        setupGridView();


        return view;
    }

    // have not known the purpose of this function yet
    @Override
    public void onAttach(Context context) {
        try{
            mOnGridImageSelectedListener = (OnGridImageSelectedListener) getActivity();
        }catch (ClassCastException e){
            Log.e(TAG, "onAttach: ClassCastException: " + e.getMessage() );
        }
        super.onAttach(context);
    }

    // have not known the purpose of this function yet
    private void setupGridView(){
        Log.d(TAG, "setupGridView: Setting up image grid.");

        final ArrayList<Photo> photos = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for ( DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){

                    Photo photo = new Photo();
                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                    try {
                        photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                        photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                        photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                        photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                        photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                        photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());
                        photo.setTrip_key(objectMap.get("trip_key").toString());

                        ArrayList<Comment> comments = new ArrayList<Comment>();
                        for (DataSnapshot dSnapshot : singleSnapshot
                                .child(getString(R.string.field_comments)).getChildren()) {
                            Comment comment = new Comment();
                            comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                            comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                            comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                            comments.add(comment);
                        }

                        photo.setComments(comments);

                        List<Like> likesList = new ArrayList<Like>();
                        for (DataSnapshot dSnapshot : singleSnapshot
                                .child(getString(R.string.field_likes)).getChildren()) {
                            Like like = new Like();
                            like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
                            likesList.add(like);
                        }
                        photo.setLikes(likesList);
                        photos.add(photo);
                    }catch(NullPointerException e){
                        Log.e(TAG, "onDataChange: NullPointerException: " + e.getMessage() );
                    }
                }

                //setup our image grid
                int gridWidth = getResources().getDisplayMetrics().widthPixels;
                int imageWidth = gridWidth/NUM_GRID_COLUMNS;
                gridView.setColumnWidth(imageWidth);

                ArrayList<String> imgUrls = new ArrayList<String>();
                for(int i = 0; i < photos.size(); i++){
                    imgUrls.add(photos.get(i).getImage_path());
                }
                GridImageAdapter adapter = new GridImageAdapter(getActivity(),R.layout.layout_grid_imageview,
                        "", imgUrls);
                gridView.setAdapter(adapter);

                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mOnGridImageSelectedListener.onGridImageSelected(photos.get(position), ACTIVITY_NUM);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled.");
            }
        });
    }




    // after assure the user is login, it will retrieve data from Firebase and set up the information of the user.
    private void setProfileWidgets(UserSettings userSettings) {

        UserAccountSettings settings = userSettings.getSettings();

        UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null, "");

        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
        mProgressBar.setVisibility(View.GONE);
    }


    /**
     * Responsible for setting up the profile toolbar
     */
    private void setupToolbar(){

        // why not?
        // ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((ProfileActivity)getActivity()).setSupportActionBar(toolbar);


        profileMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to account settings.");
                Intent intent = new Intent(mContext, AccountSettingsActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

    }

    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationView);
        BottomNavigationViewHelper.enableNavigation(mContext,getActivity() ,bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

      /*
    ------------------------------------ Firebase ---------------------------------------------
     */

    /**
     * Setup the firebase auth object
     */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();


                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    textSend.setVisibility(View.INVISIBLE);
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };


        // retrieving user-related data
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //retrieve user information from the database
                setProfileWidgets(mFirebaseMethods.getUserSettings(dataSnapshot));

                //retrieve images for the user in question

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    // will set up the Firebase object when create this Fragment.
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}