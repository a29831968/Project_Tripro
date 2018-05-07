package tw.com.flag.tripro.Google;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import tw.com.flag.tripro.Profile.ProfileActivity;
import tw.com.flag.tripro.R;
import tw.com.flag.tripro.Utils.BottomNavigationViewHelper;
import tw.com.flag.tripro.Utils.UserListAdapter;
import tw.com.flag.tripro.models.Trip;
import tw.com.flag.tripro.models.User;

/**
 * Created by Tony on 2018/2/7.
 */

public class GoogleActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{

    private static final String TAG = "GoogleActivity";
    private static final int ACTIVITY_NUM = 2;
    private Context mContext=GoogleActivity.this;


    private GoogleMap mMap;
    private static final int MY_PERMISSION_REQUEST_CODE=7000;
    private static final int PLAY_SERVICE_RES_REQUEST=7001;

    // play service
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Marker mUserMarker;

    // declare init view
    private ImageButton imgbtn_home;
    private ImageButton imgbtn_newplan;
    private ImageButton imgbtn_profile;

    // declare firebase and geofire
    private DatabaseReference mDatabase;
    private GeoFire geoFire;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        setupBottomNavigationView();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // 建立Google API用戶端物件
        //應用程式啟動以後，就會建立好需要的Google API用戶端物件。
        // 在後續執行連線與運作的時候，應用程式會執行ConnectionCallbacks與OnConnectionFailedListener介面對應的方法
        //buildGoogleApiClient();

        // 建立Location請求物件
        //應用程式需要接收最新的位置資訊，需要依照應用程式的需求，建立與啟動LocationRequest服務
        //createLocationRequest();


        //init geofire and firebase
        mDatabase= FirebaseDatabase.getInstance().getReference().child("MapUsers");
        geoFire=new GeoFire(mDatabase);
        setUpLocation();
    }

    private void setUpLocation() {
        // copy code from driver app
        //在程式中若想存取屬於危險權限的資源之前，需先檢查是否已經取得使用者的授權
        //未取得權限，向使用者要求允許權限
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            // request runtime permission
            // ActivityCompat類別提供向使用者請求權限的類別方法「requestPermissions」

            //第一個參數傳入Context物件，第二個字串陣列則是欲要求的權限，第三個int是本次請求的辨識編號，
            //當使用者決定後返回onRequestPermissionsResult方法時的辨認號碼，
            // 因此，應在類別中定義符合其功能的常數名稱，以提高程式可讀性，例如 MY_PERMISSION_REQUEST_CODE
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            },MY_PERMISSION_REQUEST_CODE);
            // 已獲得權限
        }else{
            if(checkPlayServices()){
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();
            }
        }

    }
    // 建立Google API用戶端物件
    private void buildGoogleApiClient() {
        // construct Google Api object
        // and add the location servecies API
        mGoogleApiClient= new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    // 建立Location請求物件
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // 設定讀取位置資訊的間隔時間為一秒（1000ms）
        mLocationRequest.setInterval(1000);

        // 設定讀取位置資訊最快的間隔時間為一秒（3000ms）
        mLocationRequest.setFastestInterval(3000);

        // 設定優先讀取高精確度的位置資訊（GPS）
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }





    private boolean checkPlayServices() {
        // GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)
        // Verifies that Google Play services is installed and enabled on this device,
        // and that the version installed on this device is no older than the one required by this client

        // ConnectionResult: SUCCESS, SERVICE_MISSING, SERVICE_VERSION_UPDATE_REQUIRED, SERVICE_DISABLED, SERVICE_INVALID
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS)
        {

            //Determines whether an error is user-recoverable.
            // If true, proceed by calling getErrorDialog(int, Activity, int) and showing the dialog
            // If false, show ->This device is not supported
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode ,this, PLAY_SERVICE_RES_REQUEST).show();
            else{
                Toast.makeText(this, "This device is not supported",Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void displayLocation() {
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        // 開始更新位置 Location mLastlocation
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation != null){

            final double latitude = mLastLocation.getLatitude();
            final double longitude= mLastLocation.getLongitude();


            // set user in to the firebase : MapUsers
            // remember setting the remove while logging out.
            geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {

                    // add marker
                    // mCurrent 是一個marker , mMap 是一個Map object
                    if(mUserMarker != null)
                        mUserMarker.remove(); // remove already marker
                    mUserMarker = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitude,longitude))
                            .title("You")
                    );
                    // move camera to this position
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude), 15.0f));
                    loadAroundUsers();
                }
            });

        }
        else{
            Log.d("Error", "Can not get your location!");
        }
    }


    private void loadAroundUsers() {

        // find users around 3 km
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()),3);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(final String key, final GeoLocation location) {
                if(!key.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                    FirebaseDatabase.getInstance().getReference().child("MapUsers").child(key)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    DatabaseReference DA=FirebaseDatabase.getInstance().getReference().child("users").child(key);
                                    DA.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Log.d(TAG, "dataSnapshot: "+dataSnapshot);
                                            mMap.addMarker(new MarkerOptions()
                                                    .position(new LatLng(location.latitude,location.longitude))
                                                    .flat(true)
                                                    .title(dataSnapshot.getValue(User.class).getUsername().toString())
                                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location)));
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(final String key, final GeoLocation location) {
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

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
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
    // ConnectionCallbacks
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // 已經連線到Google Services
        displayLocation();
        startLocationUpdates();
    }
    private void startLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
    }

    // ConnectionCallbacks
    @Override
    public void onConnectionSuspended(int i) {
        // Google Services連線中斷
        // int參數是連線中斷的代號
        mGoogleApiClient.connect();
    }

    // OnConnectionFailedListener
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Google Services連線失敗
        // ConnectionResult參數是連線失敗的資訊

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onLocationChanged(Location location) {
        // 位置改變
        // Location參數是目前的位置
        mLastLocation=location;
        displayLocation();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);

    }
}


/*



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setupBottomNavigationView();

    }




 */