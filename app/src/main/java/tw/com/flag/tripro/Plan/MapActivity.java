package tw.com.flag.tripro.Plan;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import tw.com.flag.tripro.R;
import tw.com.flag.tripro.Utils.DirectionFinder;
import tw.com.flag.tripro.Utils.DirectionFinderListener;
import tw.com.flag.tripro.models.Itinerary;
import tw.com.flag.tripro.models.Route;

/**
 * Created by Tony on 2018/4/25.
 */

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, DirectionFinderListener{

    private static final String TAG = "MapActivity";


    // constant
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM=15f;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40, -168), new LatLng(71, 136));
    private Boolean mLocationPermissionsGranted = false;

    // map
    private GoogleApiClient mGoogleApiClient;
    private Marker origin, destination;
    private double olat, olong, elat, elong;
    private LatLng olatlng, dlatlng;
    private String otitle, dtitle;
    private GoogleMap mMap;
    // draw path
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();

    //for extra
    private String chosen_day;
    private String trip_key;
    private String trip_day;
    private String key;

    // Firebase
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);


    }


    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        getExtra();
        mAuth=FirebaseAuth.getInstance();
        mDatabase= FirebaseDatabase.getInstance().getReference().child("user_trips")
                .child(mAuth.getCurrentUser().getUid())
                .child("trips").child(trip_key).child(chosen_day)
                .child(key);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                olat=dataSnapshot.getValue(Itinerary.class).getOrigin_lat();
                olong=dataSnapshot.getValue(Itinerary.class).getOrigin_long();
                elat=dataSnapshot.getValue(Itinerary.class).getDestination_lat();
                elong=dataSnapshot.getValue(Itinerary.class).getDestination_long();
                otitle=dataSnapshot.getValue(Itinerary.class).getOrigin();
                dtitle=dataSnapshot.getValue(Itinerary.class).getDestination();
                Log.d(TAG, "all the info: "+otitle+": "+olat+" , "+olong+" / "+ dtitle+": "+elat+" , "+elong);
                olatlng=new LatLng(olat, olong);
                dlatlng=new LatLng(elat,elong);
                Log.d(TAG,"New olatlng: lat:" + olatlng.latitude + "lng:" + olatlng.longitude);
                Log.d(TAG,"New dlatlng: lat:" + dlatlng.latitude + "lng:" + dlatlng.longitude);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        getlocationPermission();
    }

    // getExtra from previous activity
    void getExtra(){
        // get day from the TripActivity
        chosen_day=getIntent().getStringExtra(getString(R.string.chosen_day));
        // get trip_key(which trip you selected)
        trip_key=getIntent().getStringExtra(getString(R.string.trip_key));
        // get trip_day(which trip you selected)
        trip_day=getIntent().getStringExtra(getString(R.string.trip_day));
        // get option from previous in order to differentiate which routie option(new / edit)
        //routieOption= getIntent().getStringExtra("routieOption");
        //
        if(getIntent().getStringExtra("key")!=null)
            key=getIntent().getStringExtra("key");
        Log.d(TAG, "getExtra... trip_day: "+chosen_day+" trip_key: "+trip_key);
    }


    // moveCamera method 移動視窗
    private void moveCamera(LatLng latlng, float zoom, String title){
        Log.d(TAG,"moveCamera: moving the camera to...: lat:" + latlng.latitude + "lng:" + latlng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom));
        MarkerOptions options = new MarkerOptions()
                .position(latlng)
                .title(title);
        mMap.addMarker(options);

        MarkerOptions optionsD = new MarkerOptions()
                .position(dlatlng)
                .title(dtitle);
        mMap.addMarker(optionsD);
        try{

            new DirectionFinder(this, olat, olong,elat,elong).execute();
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: onMapready");
        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();
        mMap = googleMap;

        if(mLocationPermissionsGranted){
            Log.d(TAG, "onMapReady: mLocationpermissionGranted: true");
            moveCamera(olatlng, DEFAULT_ZOOM, otitle);

            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                return;
            }
            // the dot will show device location
            /*
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    getDeviceLocation();
                    //mMap.addMarker(new MarkerOptions().position(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude())).title("My Location"));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()), DEFAULT_ZOOM));
                    return false;
                }
            });
            */
        }
    }

    // 得到授權後打開地圖
    private void initMap(){
        Log.d(TAG, "initMap : init Map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        // -> onMapReady
        mapFragment.getMapAsync(MapActivity.this);
    }

    // 查看是否取得授權使用地圖 -> 獲得授權 initMap
    private  void getlocationPermission(){
        Log.d(TAG, "getlocationPermission : get permission");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),FINE_LOCATION)
                ==PackageManager.PERMISSION_GRANTED) {
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),COURSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;

                initMap();
            }else{
                ActivityCompat.requestPermissions(this,permissions,LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            //
            ActivityCompat.requestPermissions(this,permissions,LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onDirectionFinderStart() {

    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Route routie : routes) {




            /*
            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
                    .title(route.startAddress)
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green))
                    .title(route.endAddress)
                    .position(route.endLocation)));
            */

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.RED).
                    width(15);

            for (int i = 0; i < routie.points.size(); i++)
                polylineOptions.add(routie.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
    }
}
