package tw.com.flag.tripro.Home;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import tw.com.flag.tripro.R;
import tw.com.flag.tripro.models.UserAccountSettings;

/**
 * Created by Tony on 2018/2/7.
 */

public class MessagesFragment extends Fragment{
    private static final String TAG = "MessagesFragment";

    private ListView usersList;
    private TextView noUsersText;
    private ArrayList<String> users;
    private int totalUsers = 0;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        usersList=(ListView)view.findViewById(R.id.usersList) ;
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // set the userself's display_name
        mAuth=FirebaseAuth.getInstance();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("user_account_settings");
        Query query=mDatabase.orderByChild("user_id").equalTo(mAuth.getCurrentUser().getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    Log.d(TAG, "addValueEvent"+singleSnapshot);
                    ChatDetails.current_display_name =singleSnapshot.getValue(UserAccountSettings.class).getDisplay_name();

                    Log.d(TAG, "displayname:"+ChatDetails.current_display_name);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //
        users= new ArrayList<>();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("user_account_settings");
        Query qquery=mDatabase.orderByChild(getActivity().getString(R.string.field_display_name));
        qquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    Log.d(TAG, "addValueEvent"+singleSnapshot);
                    String username =singleSnapshot.getValue(UserAccountSettings.class).getDisplay_name();

                    Log.d(TAG, "username:"+username);
                    if(!username.equals(ChatDetails.current_display_name)){
                        users.add(username);
                    }
                }
                Log.d(TAG, "users:"+users);
                ArrayAdapter adapter=new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, users);
                usersList.setAdapter(adapter);
                setListClick();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    void setListClick(){
        usersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG,"username:"+users.get(position));
                Intent intent=new Intent(getActivity(), ChatActivity.class);
                ChatDetails.display_name=users.get(position);
                intent.putExtra(getActivity().getString(R.string.field_display_name),users.get(position));
                startActivity(intent);
            }
        });


    }
}
