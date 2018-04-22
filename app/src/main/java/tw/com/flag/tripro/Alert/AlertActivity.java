package tw.com.flag.tripro.Alert;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import tw.com.flag.tripro.R;
import tw.com.flag.tripro.Utils.BottomNavigationViewHelper;
import tw.com.flag.tripro.dialogs.ConfirmPasswordDialog;

/**
 * Created by Tony on 2018/2/7.
 */

public class AlertActivity extends AppCompatActivity{




    private static final String TAG = "AlertActivity";
    private static final int ACTIVITY_NUM = 3;
    private Context mContext = AlertActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        setupBottomNavigationView();
    }

    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext,this, bottomNavigationViewEx);

        // 使選擇到的 Bottom Navigation Item 變換顏色
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}
