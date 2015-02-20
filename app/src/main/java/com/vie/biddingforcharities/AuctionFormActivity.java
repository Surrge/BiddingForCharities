package com.vie.biddingforcharities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.vie.biddingforcharities.netcode.GetInfoTask;

import org.json.JSONObject;

public class AuctionFormActivity extends Activity {
    DrawerLayout NavLayout;
    ListView NavList;
    Button HomeButton;
    ImageButton NavDrawerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auctionform);

        NavLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavList = (ListView) findViewById(R.id.navList);
        NavDrawerButton = (ImageButton) findViewById(R.id.nav_drawer_expand);

        // Build Side Nav Menu
        ((Global) getApplication()).BuildNavigationMenu(NavList);
        NavDrawerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavLayout.openDrawer(Gravity.LEFT);
            }
        });

        // Build Top Nav Menu
        HomeButton = (Button) findViewById(R.id.home_button);
        HomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
    }

    public void onTaskFinish(String data) {
        try {
            //Deserialize
            JSONObject json = new JSONObject(data);
            int UserId = json.getInt("UserId");
            if(UserId > 0) {
                //Navigate to Home
                startActivity(new Intent(AuctionFormActivity.this, HomeActivity.class));
                finish();
            }
            else {
                Toast.makeText(this, "Invalid Login", Toast.LENGTH_LONG);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Problem retrieving data", Toast.LENGTH_LONG).show();
        }
    }
}
