package tw.com.flag.tripro.Plan;

// onCreate
// getlocationPermission 獲得地圖授權
// initmap 與Map layout 做連結
// onMapReady
// getdevicelocation

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import tw.com.flag.tripro.R;
import tw.com.flag.tripro.Utils.DirectionFinder;
import tw.com.flag.tripro.Utils.DirectionFinderListener;
import tw.com.flag.tripro.Utils.PlaceAutocompleteAdapter;
import tw.com.flag.tripro.models.Itinerary;
import tw.com.flag.tripro.models.PlaceInfo;
import tw.com.flag.tripro.models.Route;

import static tw.com.flag.tripro.R.string.chosen_day;
import static tw.com.flag.tripro.R.string.trip_key;

public class EditRoutieFragment extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, LocationListener, DirectionFinderListener {

    private static final String TAG = "EditRouteFragment";
    private Context mContext=EditRoutieFragment.this;

    // interface -> GoogleApiClient.OnConnectionFailedListener
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: onMapready");
        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();
        mMap = googleMap;

        if(mLocationPermissionsGranted){
            Log.d(TAG, "onMapReady: mLocationpermissionGranted: true");
            getDeviceLocation();

            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                return;
            }
            // the dot will show device location
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
        }
    }

    // constant
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM=15f;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40, -168), new LatLng(71, 136));


    //widget
    private AutoCompleteTextView mOrigin;
    private AutoCompleteTextView mDestination;
    private ImageView mInfoOrigin, mInfoDestination;
    private Button mFindPath;
    private Button mSavepath;

    private TextView mDistance;
    private TextView mDuration;

    //vars
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleApiClient mGoogleApiClient;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private Location currentLocation;

    private double stlat, stlong;
    private double endlat, endlong;
    private String durationDatabase;
    private String distanceDatabase;
    private String startaddress;
    private String endaddress;
    private String originName;
    private String destinationName;


    private LatLng origin, destination;

    // for extra
    private String chosen_day;
    private String trip_key;
    private String trip_day;
    private String routieOption;
    private String key;

    // for bundle
    private String plan_id;
    private String select_day;
    private String day;


    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;

    private double distance;

    private Marker mMarker, mMarkerDestination;
    private PlaceInfo mPlace, mPlaceDestination;

    private DatabaseReference mDatabase;
    private DatabaseReference mItineraryRef;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_editroutie);




        // get widget from layout
        mOrigin = (AutoCompleteTextView) findViewById(R.id.mOrigin);
        mDestination = (AutoCompleteTextView) findViewById(R.id.mDestination);
        mInfoOrigin = (ImageView) findViewById(R.id.place_info_origin);
        mInfoDestination = (ImageView) findViewById(R.id.place_info_destination);

        mInfoOrigin.setVisibility(View.INVISIBLE);
        mInfoDestination.setVisibility(View.INVISIBLE);

        mDistance =(TextView) findViewById(R.id.txv_showDistance);
        mDuration =(TextView) findViewById(R.id.txv_showDuration);

        mSavepath= (Button) findViewById(R.id.btn_savePath);
        mFindPath= (Button) findViewById(R.id.btn_path);
        mFindPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                findPath();

            }
        });


        getlocationPermission();
        init();
    }


    public void onStart(){
        super.onStart();
        getExtra();
        setFirebaseUtils();

        mSavepath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, " Savepath: chosen_day: "+chosen_day+"routieOption: "+ routieOption);
                Log.d(TAG,"getString(R.string.new_routie): "+getString(R.string.new_routie));

                Itinerary itinerary = new Itinerary(originName, destinationName
                        , stlat, stlong, endlat, endlong, durationDatabase, distanceDatabase);

                if(routieOption.equals(getString(R.string.new_routie)) ){
                    mItineraryRef=mDatabase.push();
                    mItineraryRef.setValue(itinerary);
                    putExtra(chosen_day);
                }else{
                    Log.d("TAG", " Edit routie: "+ key);
                    mDatabase.child(key).setValue(itinerary);
                    putExtra(chosen_day);
                }


            }
        });
    }

    // everytime reenter this activity, it will re set the database ref again
    void setFirebaseUtils(){
        Log.d(TAG, "setFirebaseUtils... ");

        mAuth= FirebaseAuth.getInstance();

        try{
            if(mAuth.getCurrentUser().getUid() != null){
                mDatabase= FirebaseDatabase.getInstance().getReference().child("user_trips")
                        .child(mAuth.getCurrentUser().getUid())
                        .child("trips").child(trip_key).child(chosen_day);
            }
        }catch (NullPointerException e){
            Log.d(TAG, "current_user: NullPointerException: " + e.getMessage() );
        }


    }

    // put extra for next activity
    void putExtra(String chosen_day){
        Intent intent = new Intent(mContext,ItineraryActivity.class);
        intent.putExtra(getString(R.string.trip_day),trip_day);
        intent.putExtra(getString(R.string.trip_key),trip_key);
        intent.putExtra(getString(R.string.chosen_day),chosen_day);
        Log.d(TAG, "putExtra... trip_key: "+trip_key+" chosen_day: "+chosen_day);
        startActivity(intent);
        finish();

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
        routieOption= getIntent().getStringExtra(getString(R.string.routieOption));
        //
        if(getIntent().getStringExtra("key")!=null)
            key=getIntent().getStringExtra("key");
        Log.d(TAG, "getExtra... trip_day: "+chosen_day+" trip_key: "+trip_key);
        Log.d(TAG, "routieOption: "+routieOption );
    }

    private void findPath(){

        if(origin == null){
            Toast.makeText(this,"Please choose the Origin!!!",Toast.LENGTH_SHORT).show();
            return;
        }
        if(destination == null){
            Toast.makeText(this,"Please choose the Destination!!!",Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            new DirectionFinder(this, stlat, stlong,endlat,endlong).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    // 設定origin and destination 的輸入 可用enter表示輸入
    private void init(){
        Log.d(TAG, "init: initializing");

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        mOrigin.setOnItemClickListener(mAutocompleteClickListener);
        mDestination.setOnItemClickListener(mAutocompleteClickListenerDestination);

        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient,
                LAT_LNG_BOUNDS, null);


        mOrigin.setAdapter(mPlaceAutocompleteAdapter);
        mDestination.setAdapter(mPlaceAutocompleteAdapter);

        mOrigin.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                    //execute our method for searching
                    geoLocate();
                }

                return false;
            }
        });

        mDestination.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                    //execute our method for searching
                    geoLocateDestination();
                }

                return false;
            }
        });

        hideSoftKeyboard();

    }

    // 定位所輸入的位置
    private void geoLocate(){
        //Log.d(TAG, "geoLocate: geolocating");

        String searchString = mOrigin.getText().toString();
        //String searchString = mOrigin.getText().toString();

        Geocoder geocoder = new Geocoder(mContext);
        List<android.location.Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(searchString, 1);
        }catch (IOException e){
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage() );
        }

        if(list.size() > 0){
            android.location.Address address = list.get(0);

            Log.d(TAG, "geoLocate: found a location: " + address.toString());
            //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();

            stlat=address.getLatitude();
            stlong=address.getLongitude();
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM,
                    address.getAddressLine(0));

        }
    }

    private void geoLocateDestination(){
        //Log.d(TAG, "geoLocate: geolocating");

        String searchString = mDestination.getText().toString();
        //String searchString = mOrigin.getText().toString();

        Geocoder geocoder = new Geocoder(mContext);
        List<android.location.Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(searchString, 1);
        }catch (IOException e){
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage() );
        }

        if(list.size() > 0){
            android.location.Address address = list.get(0);

            Log.d(TAG, "geoLocate: found a location: " + address.toString());
            //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();

            endlat=address.getLatitude();
            endlong=address.getLongitude();
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM,
                    address.getAddressLine(0));

        }
    }



    // getDeviceLocation method 取得現在裝置的定位
    private void getDeviceLocation(){
        Log.d(TAG,"getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(mLocationPermissionsGranted){

                final Task location = mFusedLocationProviderClient.getLastLocation();

                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful() && task != null){
                            Log.d("ShowRoute","onComplete: found location !");
                            // move camera
                            currentLocation = (Location) task.getResult();
                            try{
                                if(currentLocation != null){
                                    moveCamera(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()),DEFAULT_ZOOM, "My location");
                                    mMap.addMarker(new MarkerOptions().position(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude())).title("My Location"));
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()), DEFAULT_ZOOM));
                                }
                            }catch(SecurityException e){
                                Log.d("ShowRoute","getDeviceLocation: currentlocation is null: " + e.getMessage());
                            }
                        }else{
                            Log.d("ShowRoute","onCOmplete: current location is null");
                            Toast.makeText(mContext,"unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch(SecurityException e){
            Log.d(TAG,"getDeviceLocation: SecurityException: " + e.getMessage());

        }

    }

    private void moveCamera(LatLng latLng, float zoom, PlaceInfo placeInfo){
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        //mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(mContext));
        //mMap.clear();
        if(mMarker != null){
            mMarker.remove();
        }

        if(placeInfo != null){
            try{
                String snippet = "Address: " + placeInfo.getAddress() + "\n" +
                        "Phone Number: " + placeInfo.getPhoneNumber() + "\n" +
                        "Website: " + placeInfo.getWebsiteUri() + "\n" +
                        "Price Rating: " + placeInfo.getRating() + "\n";

                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .title(placeInfo.getName())
                        .snippet(snippet);
                mMarker = mMap.addMarker(options);

            }catch (NullPointerException e){
                Log.e(TAG, "moveCamera: NullPointerException: " + e.getMessage() );
            }
        }else{
            mMap.addMarker(new MarkerOptions().position(latLng));
        }

        hideSoftKeyboard();
    }

    private void moveCameraDestination(LatLng latLng, float zoom, PlaceInfo placeInfo){
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        //mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(mContext));
        //mMap.clear();
        if(mMarkerDestination != null){
            mMarkerDestination.remove();
        }

        if(placeInfo != null){
            try{
                String snippet = "Address: " + placeInfo.getAddress() + "\n" +
                        "Phone Number: " + placeInfo.getPhoneNumber() + "\n" +
                        "Website: " + placeInfo.getWebsiteUri() + "\n" +
                        "Price Rating: " + placeInfo.getRating() + "\n";

                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .title(placeInfo.getName())
                        .snippet(snippet);
                mMarkerDestination = mMap.addMarker(options);

            }catch (NullPointerException e){
                Log.e(TAG, "moveCamera: NullPointerException: " + e.getMessage() );
            }
        }else{
            mMap.addMarker(new MarkerOptions().position(latLng));
        }

        hideSoftKeyboard();
    }

    // moveCamera method 移動視窗
    private void moveCamera(LatLng latlng, float zoom, String title){
        Log.d(TAG,"moveCamera: moving the camera to...: lat:" + latlng.latitude + "lng:" + latlng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom));


        if(!title.equals("My Location")){
            MarkerOptions options = new MarkerOptions()
                    .position(latlng)
                    .title(title);
            mMap.addMarker(options);
        }
        hideSoftKeyboard();

    }

    // 得到授權後打開地圖
    private void initMap(){
        Log.d(TAG, "initMap : init Map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        // -> onMapReady
        mapFragment.getMapAsync(EditRoutieFragment.this);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionsGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0 ){
                    for (int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionsGranted = true;
                    // init our map
                }
            }
        }
    }

    // 隱藏鍵盤
    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mOrigin.getWindowToken(), 0);
    }

    /*
        --------------------------- google places API autocomplete suggestions -----------------
     */

    private AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            hideSoftKeyboard();

            final AutocompletePrediction item = mPlaceAutocompleteAdapter.getItem(i);
            final String placeId = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };

    // get place object information
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if(!places.getStatus().isSuccess()){
                Log.d(TAG, "onResult: Place query did not complete successfully: " + places.getStatus().toString());
                // always remember to release place buffer object when no longer use it
                places.release();
                return;
            }
            final Place place = places.get(0);

            try{
                mPlace = new PlaceInfo();
                mPlace.setName(place.getName().toString());
                Log.d(TAG, "onResult: name: " + place.getName());
                mPlace.setAddress(place.getAddress().toString());
                Log.d(TAG, "onResult: address: " + place.getAddress());
//                mPlace.setAttributions(place.getAttributions().toString());
//                Log.d(TAG, "onResult: attributions: " + place.getAttributions());
                mPlace.setId(place.getId());
                Log.d(TAG, "onResult: id:" + place.getId());
                mPlace.setLatlng(place.getLatLng());
                Log.d(TAG, "onResult: latlng: " + place.getLatLng());
                mPlace.setRating(place.getRating());
                Log.d(TAG, "onResult: rating: " + place.getRating());
                mPlace.setPhoneNumber(place.getPhoneNumber().toString());
                Log.d(TAG, "onResult: phone number: " + place.getPhoneNumber());
                mPlace.setWebsiteUri(place.getWebsiteUri());
                Log.d(TAG, "onResult: website uri: " + place.getWebsiteUri());

                Log.d(TAG, "onResult: place: " + mPlace.toString());
            }catch (NullPointerException e){
                Log.e(TAG, "onResult: NullPointerException: " + e.getMessage() );
            }

            originName=place.getName().toString();
            stlat=place.getViewport().getCenter().latitude;
            stlong=place.getViewport().getCenter().longitude;
            origin=new LatLng(stlat,stlong);

            moveCamera(new LatLng(place.getViewport().getCenter().latitude,
                    place.getViewport().getCenter().longitude), DEFAULT_ZOOM, mPlace);

            places.release();
        }
    };

    //
    private AdapterView.OnItemClickListener mAutocompleteClickListenerDestination = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            hideSoftKeyboard();

            final AutocompletePrediction item = mPlaceAutocompleteAdapter.getItem(i);
            final String placeId = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallbackDestination);
        }
    };

    // get place object information
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallbackDestination = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if(!places.getStatus().isSuccess()){
                Log.d(TAG, "onResult: Place query did not complete successfully: " + places.getStatus().toString());
                // always remember to release place buffer object when no longer use it
                places.release();
                return;
            }
            final Place place = places.get(0);

            try{
                mPlaceDestination = new PlaceInfo();
                mPlaceDestination.setName(place.getName().toString());
                Log.d(TAG, "onResult: name: " + place.getName());
                mPlaceDestination.setAddress(place.getAddress().toString());
                Log.d(TAG, "onResult: address: " + place.getAddress());
//                mPlaceDestination.setAttributions(place.getAttributions().toString());
//                Log.d(TAG, "onResult: attributions: " + place.getAttributions());
                mPlaceDestination.setId(place.getId());
                Log.d(TAG, "onResult: id:" + place.getId());
                mPlaceDestination.setLatlng(place.getLatLng());
                Log.d(TAG, "onResult: latlng: " + place.getLatLng());
                mPlaceDestination.setRating(place.getRating());
                Log.d(TAG, "onResult: rating: " + place.getRating());
                mPlaceDestination.setPhoneNumber(place.getPhoneNumber().toString());
                Log.d(TAG, "onResult: phone number: " + place.getPhoneNumber());
                mPlaceDestination.setWebsiteUri(place.getWebsiteUri());
                Log.d(TAG, "onResult: website uri: " + place.getWebsiteUri());

                Log.d(TAG, "onResult: place: " + mPlaceDestination.toString());
            }catch (NullPointerException e){
                Log.e(TAG, "onResult: NullPointerException: " + e.getMessage() );
            }

            destinationName=place.getName().toString();
            endlat=place.getViewport().getCenter().latitude;
            endlong=place.getViewport().getCenter().longitude;

            destination=new LatLng(endlat,endlong);

            moveCameraDestination(new LatLng(place.getViewport().getCenter().latitude,
                    place.getViewport().getCenter().longitude), DEFAULT_ZOOM, mPlaceDestination);

            places.release();
        }
    };


    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Finding direction..!", true);

        /*
        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }
        */

        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));


            durationDatabase=route.duration.text;
            distanceDatabase=route.distance.text;
            mDistance.setText(route.distance.text);
            mDuration.setText(route.duration.text);
            startaddress=route.startAddress;
            endaddress=route.endAddress;


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

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
    }
}
