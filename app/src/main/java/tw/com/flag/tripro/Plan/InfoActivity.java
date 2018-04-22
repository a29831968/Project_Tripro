package tw.com.flag.tripro.Plan;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.icu.util.Calendar;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.w3c.dom.Text;

import tw.com.flag.tripro.Profile.AccountSettingsActivity;
import tw.com.flag.tripro.R;
import tw.com.flag.tripro.Utils.FirebaseMethods;

/**
 * Created by Tony on 2018/4/19.
 */

public class InfoActivity extends AppCompatActivity{

    private static final String TAG = "InfoActivity";
    private Context mContext=InfoActivity.this;

    //firebase related declare
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private FirebaseUser user;

    private EditText trip_name, trip_day;
    private TextView update;
    private EditText start_date;
    private ImageView back_arrow, save_changes,trip_photo;

    private int mYear, mMonth, mDay;
    private String name, day, format;
    private String mAppend = "file:/";
    private String imgUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        Log.d(TAG, "onCreate: InfoActivity");

        // get widgets from layout
        start_date=(EditText) findViewById(R.id.start_date);
        trip_day=(EditText) findViewById(R.id.trip_day);
        trip_name=(EditText) findViewById(R.id.trip_name);
        back_arrow=(ImageView) findViewById(R.id.backArrow);
        save_changes=(ImageView) findViewById(R.id.saveChanges);
        trip_photo=(ImageView) findViewById(R.id.trip_photo);

        update=(TextView)findViewById(R.id.tx_update);

        // navigate to the Gallery
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigate to the Gallery");
                Intent Gallery=new Intent(mContext,GalleryActivity.class);
                startActivity(Gallery);
            }
        });


        // back to the TripActivity and clean the tasks(so that the record of filling information of trip will disappear
        back_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back to TripActivity");
                finish();
            }
        });

        // DatePickerDialog
        start_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                mYear=c.get(Calendar.YEAR);
                mMonth=c.get(Calendar.MONTH);
                mDay=c.get(Calendar.DAY_OF_MONTH);
                // change the theme of DatePickerDialog
                new DatePickerDialog(InfoActivity.this,DatePickerDialog.THEME_HOLO_DARK, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        format = year+"/"+(month+1)+"/"+day;
                        start_date.setText(format);
                    }
                }, mYear,mMonth, mDay).show();
            }
        });


        Intent intent = getIntent();

        if(intent.hasExtra(getString(R.string.selected_image))){
            //if there is an imageUrl attached as an extra, then it was chosen from the gallery/photo fragment
            Log.d(TAG, "getIncomingIntent: New incoming imgUrl");
            imgUrl=intent.getStringExtra(getString(R.string.selected_image));
            setImage(imgUrl,trip_photo,mAppend);
        }else{
            imgUrl=null;
        }

        // save the information and put into the Firebase
        save_changes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Save the information");
                name = trip_name.getText().toString();
                day = trip_day.getText().toString();

                if(name!=null && day!=null && format!=null){

                    saveInformation();
                    Log.d(TAG, "onClick: navigating back to TripActivity");
                    Intent intent = new Intent(mContext, TripActivity.class);
                    startActivity(intent);
                    finish();

                }else{
                    Toast.makeText(mContext,"Please fill the information!",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    // save the information of the trip
    private void saveInformation(){

        Log.d(TAG, "saveInformation: Saving to Database");
        user = mAuth.getInstance().getCurrentUser();
        myRef=FirebaseDatabase.getInstance().getReference().child("user_trips").child(user.getUid()).child("trips")
                .push();
        myRef.child("trip_name").setValue(name);
        myRef.child("trip_day").setValue(day);
        myRef.child("start_date").setValue(format);
        if(imgUrl==null)
            myRef.child("image").setValue("default");
        else
            myRef.child("image").setValue(imgUrl);
    }

    // set image
    private void setImage(String imgURL, ImageView image, String append){
        Log.d(TAG, "setImage: setting image");

        ImageLoader imageLoader = ImageLoader.getInstance();

        imageLoader.displayImage(append + imgURL, image, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                Log.d(TAG, "ImageLoader: Loading");
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                Log.d(TAG, "ImageLoader: Fail");
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                Log.d(TAG, "ImageLoader: Complete");
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                Log.d(TAG, "ImageLoader: Cancel");
            }
        });
    }
}
