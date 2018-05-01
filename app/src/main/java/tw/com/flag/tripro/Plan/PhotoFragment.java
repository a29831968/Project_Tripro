package tw.com.flag.tripro.Plan;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import tw.com.flag.tripro.Profile.AccountSettingsActivity;
import tw.com.flag.tripro.R;
import tw.com.flag.tripro.Utils.Permissions;

/**
 * Created by Tony on 2018/2/13.
 */

public class PhotoFragment extends Fragment {
    private static final String TAG = "PhotoFragment";

    //constant
    private static final int PHOTO_FRAGMENT_NUM = 1;
    private static final int GALLERY_FRAGMENT_NUM = 2;
    private static final int  CAMERA_REQUEST_CODE = 5;

    //for extra
    private String trip_key;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo, container, false);
        Log.d(TAG, "onCreateView: started.");

        // get trip_key first
        trip_key=getActivity().getIntent().getStringExtra(getString(R.string.trip_key));
        Log.d(TAG, "onCreateView: trip_key: "+trip_key);

        Button btnLaunchCamera = (Button) view.findViewById(R.id.btnLaunchCamera);
        btnLaunchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: launching camera.");

                if(((PlanActivity)getActivity()).getCurrentTabNumber() == PHOTO_FRAGMENT_NUM){
                    if(((PlanActivity)getActivity()).checkPermissions(Permissions.CAMERA_PERMISSION[0])){
                        Log.d(TAG, "onClick: starting camera");
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                    }else{
                        Intent intent = new Intent(getActivity(), PlanActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                }
            }
        });

        return view;
    }

    private boolean isRootTask(){
        if(((PlanActivity)getActivity()).getTask() == 0){
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CAMERA_REQUEST_CODE){
            Log.d(TAG, "onActivityResult: done taking a photo.");
            Log.d(TAG, "onActivityResult: attempting to navigate to final share screen.");

            Bitmap bitmap;
            // 我有更改過
            try{

                bitmap = (Bitmap) data.getExtras().get("data");
                if(isRootTask()){
                    try{
                        Log.d(TAG, "onActivityResult: received new bitmap from camera: " + bitmap);
                        Intent intent = new Intent(getActivity(), NextActivity.class);
                        intent.putExtra(getString(R.string.trip_key),trip_key);
                        intent.putExtra(getString(R.string.selected_bitmap), bitmap);
                        startActivity(intent);
                    }catch (NullPointerException e){
                        Log.d(TAG, "onActivityResult: NullPointerException: " + e.getMessage());
                    }
                }else{
                    try{
                        Log.d(TAG, "onActivityResult: received new bitmap from camera: " + bitmap);
                        Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                        intent.putExtra(getString(R.string.selected_bitmap), bitmap);
                        intent.putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile_fragment));
                        startActivity(intent);
                        getActivity().finish();
                    }catch (NullPointerException e){
                        Log.d(TAG, "onActivityResult: NullPointerException: " + e.getMessage());
                    }
                }
            }catch(NullPointerException e){
                Log.d(TAG, "onActivityResult: NullPointerException: " + e.getMessage());
            }


        }
    }
}

