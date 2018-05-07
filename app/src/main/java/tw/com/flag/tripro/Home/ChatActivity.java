package tw.com.flag.tripro.Home;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.HashMap;
import java.util.Map;

import tw.com.flag.tripro.R;
import tw.com.flag.tripro.models.UserAccountSettings;

/**
 * Created by Tony on 2018/5/6.
 */

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    private Context mContext=ChatActivity.this;

    private LinearLayout layout;
    private RelativeLayout layout_2;
    private ImageView sendButton;
    private EditText messageArea;
    private ScrollView scrollView;

    //
    FirebaseAuth mAuth;
    DatabaseReference mDatabase1;
    DatabaseReference mDatabase2;

    // var
    String display_name;
    String chatWith_uid;
    String current_display_name;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        layout = (LinearLayout) findViewById(R.id.layout1);
        layout_2 = (RelativeLayout)findViewById(R.id.layout2);
        sendButton = (ImageView)findViewById(R.id.sendButton);
        messageArea = (EditText)findViewById(R.id.messageArea);
        scrollView = (ScrollView)findViewById(R.id.scrollView);

        display_name = getIntent().getStringExtra(getString(R.string.field_display_name));
        mAuth=FirebaseAuth.getInstance();
        ChatDetails.current_uid=mAuth.getCurrentUser().getUid();
        //

    }

    @Override
    protected void onStart() {
        super.onStart();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_display_name))
                .equalTo(display_name);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found user: " +
                            singleSnapshot.getValue(UserAccountSettings.class).getUser_id().toString());
                    chatWith_uid=singleSnapshot.getValue(UserAccountSettings.class).getUser_id().toString();
                    Log.d(TAG, "chat_uid: "+chatWith_uid);
                    ChatDetails.chat_uid=chatWith_uid;
                    found_current_dispaly_name();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabase1= FirebaseDatabase.getInstance().getReference()
                .child("messengers")
                .child(mAuth.getCurrentUser().getUid())
                .child(display_name);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageArea.getText().toString();

                if(!messageText.equals("")){
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("message", messageText);
                    map.put("user", current_display_name);
                    mDatabase1.push().setValue(map);
                    mDatabase2.push().setValue(map);
                    messageArea.setText("");
                }
            }
        });

        mDatabase1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map map = (Map<String, String>) dataSnapshot.getValue();
                String message = map.get("message").toString();
                String userName = map.get("user").toString();

                if(userName.equals(current_display_name)){
                    addMessageBox("You: \n" + message, userName);
                }
                else{
                    addMessageBox(display_name + ": \n" + message, userName);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void addMessageBox(String message, String name){
        TextView textView = new TextView(ChatActivity.this);
        textView.setText(message);

        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.weight = 1.0f;
        lp2.leftMargin=10;
        lp2.rightMargin=10;

        Log.d(TAG, "show name:"+ name);
        Log.d(TAG, "display name:"+ current_display_name);
        if(name.equals(ChatDetails.current_display_name)) {
            lp2.gravity = Gravity.LEFT;
            textView.setBackgroundResource(R.drawable.leftin);
        }
        else{
            lp2.gravity = Gravity.RIGHT;
            textView.setBackgroundResource(R.drawable.rightin);
        }
        textView.setLayoutParams(lp2);
        layout.addView(textView);
        scrollView.fullScroll(View.FOCUS_DOWN);
    }

    public void found_current_dispaly_name(){
        Query ref = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbname_user_account_settings))
                .child(mAuth.getCurrentUser().getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                current_display_name=dataSnapshot.getValue(UserAccountSettings.class).getDisplay_name();
                Log.d(TAG, "current_display_name: "+current_display_name);
                mDatabase2=FirebaseDatabase.getInstance().getReference()
                        .child("messengers")
                        .child(chatWith_uid)
                        .child(current_display_name);
                ChatDetails.current_display_name=current_display_name;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
