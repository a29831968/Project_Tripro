package tw.com.flag.tripro.Plan;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.EventLogTags;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import tw.com.flag.tripro.Profile.AccountSettingsActivity;
import tw.com.flag.tripro.Profile.ProfileActivity;
import tw.com.flag.tripro.R;
import tw.com.flag.tripro.Utils.BottomNavigationViewHelper;
import tw.com.flag.tripro.Utils.FirebaseMethods;
import tw.com.flag.tripro.models.Trip;

import static android.R.id.content;

/**
 * Created by Tony on 2018/4/19.
 */

public class TripActivity extends AppCompatActivity{

    // set "TAG" for using Fragment Name easily
    private static final String TAG = "TripActivity";
    //constants
    private static final int ACTIVITY_NUM = 1;

    private Context mContext=TripActivity.this;

    //firebase related declare
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;
    private FirebaseUser user;

    // widget declare
    private ImageView tripMenu;
    private Toolbar toolbar;
    private RecyclerView trip_list;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);
        Log.d(TAG, "onCreate");

        // get widgets from layout
        tripMenu=(ImageView)findViewById(R.id.tripMenu);
        toolbar=(Toolbar)findViewById(R.id.tripToolBar);

        // RecyclerView
        trip_list=(RecyclerView)findViewById(R.id.trip_list);
        trip_list.setHasFixedSize(true);
        trip_list.setLayoutManager(new LinearLayoutManager(this));

        // set up bottom navigation
        setupBottomNavigationView();


        //
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if(user!=null){
            mFirebaseDatabase = FirebaseDatabase.getInstance();
            myRef = mFirebaseDatabase.getReference();
        }



        // set up "Firebase object"  to check if the user login or not
        // after set up "Firebase object", it will check if the user is log in or not every time back to this fragment
        setupFirebaseAuth();

        //set up top tool bar
        setupToolbar();


    }


    /**
     * Responsible for setting up the trip toolbar
     */
    private void setupToolbar(){

        Log.d(TAG, "setupToolbar");
        TripActivity.this.setSupportActionBar(toolbar);


        tripMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to InfoActivity.");
                Intent intent = new Intent(mContext, InfoActivity.class);
                startActivity(intent);

                // make the change page animation smooth
                TripActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

    }


    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, this,bottomNavigationViewEx);

        // 使選擇到的 Bottom Navigation Item 變換顏色
        Menu menu = bottomNavigationViewEx.getMenu();
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
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();


                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());



                } else {
                    // User is signed out
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

        Log.d(TAG, "onStart: ");
        // FirebaseRecyclerAdapter
        DatabaseReference mData=FirebaseDatabase.getInstance().getReference().child("user_trips").child(user.getUid()).child("trips");

        FirebaseRecyclerAdapter<Trip,TripViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Trip, TripViewHolder>(Trip.class,
                R.layout.layout_trip_listitem,
                TripViewHolder.class,
                mData)
        {
            @Override
            protected void populateViewHolder(TripViewHolder viewHolder, Trip model, int position) {
                // get the key of that view
                Log.d(TAG, "FirebaseRecyclerView: setting up.");

                // get the key which is reference to the selected trip information
                final String trip_key=getRef(position).getKey();
                final String putExtraDay=model.getTrip_day();

                viewHolder.setName(model.getTrip_name());
                viewHolder.setDay(model.getTrip_day());
                viewHolder.setImage(model.getImage());
                viewHolder.setDate(model.getStart_date());

                // set onclick on individual imageview and button
                /**
                 * post/edit/delete
                 */
                viewHolder.btn_post.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Log.d(TAG, "FirebaseRecyclerView: name : "+trip_key);
                        Intent intent= new Intent(mContext, PlanActivity.class);
                        intent.putExtra(getString(R.string.trip_key), trip_key);
                        startActivity(intent);
                    }
                });
                viewHolder.img_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.v("MainActivity", "Some text"); //show message on monitor
                    }
                });

                viewHolder.img_edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.v("ItineraryActivity", "Some text"); //show message on monitor
                        NavigateItinerary(putExtraDay, trip_key);
                    }
                });
            }
        };
        trip_list.setAdapter(firebaseRecyclerAdapter);

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


    // navigate to Itinerary, pass different trip_day
    public void NavigateItinerary(String putExtraDay, String trip_key){

        Intent intent = new Intent(mContext,ItineraryActivity.class);
        intent.putExtra(getString(R.string.trip_day), putExtraDay);
        intent.putExtra(getString(R.string.trip_key),trip_key);
        startActivity(intent);

    }


    // view Holder for Firebase RecyclerView
    public static class TripViewHolder extends RecyclerView.ViewHolder{

        View mView;
        TextView trip_name, trip_day, start_date;
        Button btn_post;
        ImageView img_edit, img_delete, display_photo;

        DatabaseReference mDatabase;

        public TripViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            btn_post=(Button) mView.findViewById(R.id.btn_post);
            img_delete = (ImageView) mView.findViewById(R.id.img_delete);
            img_edit = (ImageView) mView.findViewById(R.id.img_edit);

        }

        public void setName(String name){
            trip_name = (TextView) mView.findViewById(R.id.trip_name);
            trip_name.setText(name);
        }
        public void setDay(String day){
            trip_day = (TextView) mView.findViewById(R.id.trip_day);
            trip_day.setText(day);
        }
        public void setImage(String image){
            display_photo=(ImageView)mView.findViewById(R.id.display_photo);
            // display photo
            ImageLoader imageLoader = ImageLoader.getInstance();

            imageLoader.displayImage("file:/"+image, display_photo, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                }
            });
        }
        public void setDate(String date){
            start_date=(TextView) mView.findViewById(R.id.start_date);
            start_date.setText(date);
        }
    }
}
