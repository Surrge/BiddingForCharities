package com.vie.biddingforcharities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.vie.biddingforcharities.logic.AuctionItem;
import com.vie.biddingforcharities.logic.GetBitmapTask;
import com.vie.biddingforcharities.logic.GetInfoTask;
import com.vie.biddingforcharities.logic.Utilities;
import com.vie.biddingforcharities.ui.AuctionGridAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class BidListActivity extends Activity {
    DrawerLayout NavLayout;
    ListView NavList;
    Button HomeButton;
    ImageButton NavDrawerButton;

    GridView ItemGrid;
    ProgressDialog spinner;

    ArrayList<GetInfoTask> jsonTasks = new ArrayList<GetInfoTask>();
    ArrayList<GetBitmapTask> downloadTasks = new ArrayList<GetBitmapTask>();
    ArrayList<AuctionItem> itemList = new ArrayList<AuctionItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bidlist);

        NavLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavList = (ListView) findViewById(R.id.navList);
        NavDrawerButton = (ImageButton) findViewById(R.id.nav_drawer_expand);
        ItemGrid = (GridView) findViewById(R.id.bid_item_grid);

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

    @Override
    protected void onResume() {
        super.onResume();

        //Show Spinner
        spinner = new ProgressDialog(this);
        spinner.setMessage("Loading Bids...");
        spinner.show();

        // Get Bids
        String queryStr = Utilities.BuildQueryParams(
                new String[][]{
                        new String[]{"user_guid", ((Global)getApplication()).getUser().getUserGuid()}
                });
        jsonTasks.add((GetInfoTask) new GetInfoTask(this).execute("getUserBids", queryStr));
    }

    @Override
    protected void onPause() {
        //Cancel threads while reference is valid
        for(GetInfoTask t: jsonTasks) {t.cancel(true);}
        for(GetBitmapTask t: downloadTasks) {t.cancel(true);}
        jsonTasks.clear();
        downloadTasks.clear();

        super.onPause();
    }

    public void onTaskFinish(GetInfoTask task ,String data) {
        jsonTasks.remove(task);

        try {
            //Deserialize
            JSONObject json = new JSONObject(data);
            JSONArray itemArray = (JSONArray) json.get("items");

            for(int i = 0; i < itemArray.length(); i++) {
                final JSONObject item = (JSONObject) itemArray.get(i);

                // All items on the Bids page will have a bidder user id, works as a null item check
                if(item.has("high_bidder_user_id") && !item.isNull("high_bidder_user_id")) {
                    int item_id = item.getInt("item_id");
                    String end_date_str = item.getString("end_date_time_stamp_utc");
                    Date end_date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(end_date_str);
                    double current_high_bid = item.getDouble("current_high_bid");
                    String title = item.getString("title");
                    int total_bids = item.getInt("total_bids");
                    int high_bidder_user_id = item.getInt("high_bidder_user_id");
                    int high_bidder_user_name_id = item.getInt("high_bidder_user_name_id");
                    final String image_url = item.getString("img");
                    end_date_str = android.text.Html.fromHtml(item.getString("end_date_pt")).toString();
                    int seller_id = item.getInt("seller_id");
                    int seller_user_name_id = item.getInt("seller_user_name_id");

                    itemList.add(new AuctionItem (image_url, title, end_date_str, current_high_bid, item_id));
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getResources().getString(R.string.generic_error), Toast.LENGTH_LONG).show();
        }

        //Dismiss Spinner if done
        if(jsonTasks.isEmpty() && spinner != null && spinner.isShowing()) {
            spinner.dismiss();

            // Display tiles
            ItemGrid.setAdapter(new AuctionGridAdapter(this, itemList));
        }
    }
}
