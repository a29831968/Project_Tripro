package tw.com.flag.tripro.Plan;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;
import java.util.Arrays;

import tw.com.flag.tripro.R;
import tw.com.flag.tripro.models.Itinerary;
import tw.com.flag.tripro.models.Trip;

/**
 * Created by Tony on 2018/4/23.
 */

public class ItineraryActivity extends AppCompatActivity {

    private static final String TAG = "ItineraryActivity";

    private String [] day;


    private Context mContext=ItineraryActivity.this;


    // Firebase related

    //declare Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    //vars
    private String trip_day;
    private String trip_key;
    private String append="Day";
    private String chosen_day;


    // widgets
    private Button newRoutie;
    private Spinner spinner;
    private RecyclerView itinerary_list;
    private ImageView page_photo;
    private TextView name, date, dayNum;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itinerary);
        Log.d(TAG, "onCreate... ");



        // get widget from layout
        itinerary_list=(RecyclerView) findViewById(R.id.itinerary_list);
        //itinerary_list.setHasFixedSize(true);
        itinerary_list.setLayoutManager(new LinearLayoutManager(this));
        page_photo=(ImageView)findViewById(R.id.page_photo);
        name=(TextView)findViewById(R.id.name);
        dayNum=(TextView)findViewById(R.id.day);
        date=(TextView)findViewById(R.id.date);
        spinner=(Spinner) findViewById(R.id.spinner);
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart... ");

        getExtra();
        day=new String[Integer.parseInt(trip_day)];
        // set day button for the drawer
        for(int x=1;x<=Integer.parseInt(trip_day);x++){
            String tmp=append+String.valueOf(x);
            Log.d(TAG, tmp);
            day[x-1]=tmp;
        }
        setSpinner();
        setFirebaseUtils();
        setTop();

    }

    // set the top snippet
    void setTop(){
        DatabaseReference data = FirebaseDatabase.getInstance().getReference().child("user_trips")
                .child(mAuth.getCurrentUser().getUid()).child("trips").child(trip_key);
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dayNum.setText(dataSnapshot.getValue(Trip.class).getTrip_day().toString());
                date.setText(dataSnapshot.getValue(Trip.class).getStart_date().toString());
                name.setText(dataSnapshot.getValue(Trip.class).getTrip_name().toString());

                ImageLoader imageLoader = ImageLoader.getInstance();

                imageLoader.displayImage("file:/"+dataSnapshot.getValue(Trip.class).getImage(), page_photo, new ImageLoadingListener() {
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

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    // put extra for next activity
    void putExtra(String chosen_day, String routieOption){
        Intent intent = new Intent(mContext,EditRoutieFragment.class);
        intent.putExtra(getString(R.string.trip_day),trip_day);
        intent.putExtra(getString(R.string.trip_key),trip_key);
        intent.putExtra(getString(R.string.chosen_day),chosen_day);
        intent.putExtra(getString(R.string.routieOption),routieOption);
        Log.d(TAG, "routieOption: "+routieOption );
        Log.d(TAG, "putExtra... trip_key: "+trip_key+" chosen_day: "+chosen_day);
        startActivity(intent);
    }
    void putExtra(String chosen_day, String routieOption, String key){
        Intent intent = new Intent(mContext,EditRoutieFragment.class);
        intent.putExtra(getString(R.string.trip_day),trip_day);
        intent.putExtra(getString(R.string.trip_key),trip_key);
        intent.putExtra(getString(R.string.chosen_day),chosen_day);
        Log.d(TAG, "routieOption: "+routieOption );
        intent.putExtra(getString(R.string.routieOption),routieOption);
        intent.putExtra("key",key);
        Log.d(TAG, "putExtra... trip_key: "+trip_key+" chosen_day: "+chosen_day);
        startActivity(intent);
    }



    // getExtra from previous activity
    void getExtra(){
        // get day from the TripActivity
        trip_day=getIntent().getStringExtra(getString(R.string.trip_day));
        // get trip_key(which trip you selected)
        trip_key=getIntent().getStringExtra(getString(R.string.trip_key));
        Log.d(TAG, "getExtra... trip_day: "+trip_day+" trip_key: "+trip_key);
    }

    /**
     * RecyclerView Setup
     * @param chosen
     */

    public void firebaseRecycler(String chosen){

        Log.d(TAG, "firebaseRecycler...");
        Log.d(TAG, "firebaseRecycler..."+mDatabase.toString());
        Log.d(TAG, "firebaseRecycler..."+chosen);
        if(mAuth != null)
            Log.d(TAG, mAuth.getCurrentUser().getUid());
        DatabaseReference viewHolderDatabase=mDatabase.child(chosen);
        FirebaseRecyclerAdapter<Itinerary, detailViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Itinerary, detailViewHolder>(Itinerary.class
                ,R.layout.layout_routie_listitem
                ,detailViewHolder.class
                ,viewHolderDatabase) {
            @Override
            protected void populateViewHolder(detailViewHolder viewHolder, Itinerary model, final int position) {
                Log.d(TAG, "Firebase Adapter: succeed");
                // this is the key for recognize which itinerary(origin and destination)
                // you select
                final String key=getRef(position).getKey();

                viewHolder.setOrigin(model.getOrigin());
                viewHolder.setDestination(model.getDestination());
                viewHolder.setTime(model.getDuration());
                viewHolder.btn_edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        putExtra(chosen_day, getString(R.string.edit_routie),key);
                    }
                });
                viewHolder.btn_routie.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext,MapActivity.class);
                        intent.putExtra(getString(R.string.trip_day),trip_day);
                        intent.putExtra(getString(R.string.trip_key),trip_key);
                        intent.putExtra(getString(R.string.chosen_day),chosen_day);
                        intent.putExtra("key",key);
                        Log.d(TAG, "putExtra... trip_key: "+trip_key+" chosen_day: "+chosen_day);
                        startActivity(intent);
                    }
                });
            }
        };
        itinerary_list.setAdapter(firebaseRecyclerAdapter);
    }


    // viewHolder

    public static class detailViewHolder extends RecyclerView.ViewHolder{

        View mView;
        TextView txv_origin;
        TextView txv_destination;
        TextView txv_time;
        Button btn_routie;
        Button btn_edit;

        public detailViewHolder(View itemView){
            super(itemView);
            mView=itemView;
            txv_origin=(TextView) mView.findViewById(R.id.txv_origin);
            txv_destination=(TextView) mView.findViewById(R.id.txv_destination);
            txv_time=(TextView) mView.findViewById(R.id.txv_time);
            btn_edit=(Button) mView.findViewById(R.id.btn_edit);
            btn_routie=(Button) mView.findViewById(R.id.btn_routie);
        }

        public void setOrigin(String origin){
            txv_origin.setText(origin);
        }
        public void setDestination(String destination){
            txv_destination.setText(destination);
        }
        public void setTime(String time){
            txv_time.setText(time);
        }
    }


    // everytime reenter this activity, it will re set the database ref again
    void setFirebaseUtils(){
        Log.d(TAG, "setFirebaseUtils... ");
        mAuth= FirebaseAuth.getInstance();
        mDatabase= FirebaseDatabase.getInstance().getReference().child("user_trips").child(mAuth.getCurrentUser().getUid()).child("trips")
                .child(trip_key);

    }



    // everytime reenter this activity, it will setupSipnner again
    void setSpinner(){
        Log.d(TAG, "setSpinner");
        final ArrayAdapter<String> adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,day);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "setSpinner: the day you choose is " + day[position]);
                chosen_day=day[position];
                firebaseRecycler(chosen_day);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        newRoutie=(Button) findViewById(R.id.newRoutie);
        // set Onclick listener
        newRoutie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                putExtra(chosen_day, getString(R.string.new_routie));
            }
        });
    }
}
