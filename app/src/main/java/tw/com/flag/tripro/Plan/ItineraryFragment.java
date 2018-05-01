package tw.com.flag.tripro.Plan;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import tw.com.flag.tripro.R;

/**
 * Created by Tony on 2018/4/23.
 */

public class ItineraryFragment extends Fragment{

    // widgets
    private Button newRoutie;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_itinerary,container,false);

        LinearLayout myRoot = (LinearLayout) view.findViewById(R.id.my_root);
        LinearLayout a = new LinearLayout(getActivity());
        a.setOrientation(LinearLayout.VERTICAL);

        //
        newRoutie=(Button) view.findViewById(R.id.newRoutie);
        /*
        */

        EditText view1 = new EditText(getActivity());
        EditText view2 = new EditText(getActivity());
        EditText view3 = new EditText(getActivity());
        view1.setText("1");
        view2.setText("2");
        view3.setText("3");

        a.addView(view1);
        a.addView(view2);
        a.addView(view3);
        myRoot.addView(a);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

    }
}


