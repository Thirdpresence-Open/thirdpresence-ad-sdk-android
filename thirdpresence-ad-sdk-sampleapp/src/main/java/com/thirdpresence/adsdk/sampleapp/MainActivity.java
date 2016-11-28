package com.thirdpresence.adsdk.sampleapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.thirdpresence.sampleapp.R;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ArrayList listValues = new ArrayList<String>();
        listValues.add(getString(R.string.list_item_interstitial));
        listValues.add(getString(R.string.list_item_rewarded_video));
        listValues.add(getString(R.string.list_item_banner));

        ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter <String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, listValues);

        ListView listView = (ListView)findViewById(R.id.list_select_placement_type);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                switch (position) {
                    case 0:
                        Intent interstitialIntent = new Intent(MainActivity.this, InterstitialActivity.class);
                        startActivity(interstitialIntent);
                        break;
                    case 1:
                        Intent rewardedVideoIntent = new Intent(MainActivity.this, RewardedVideoActivity.class);
                        startActivity(rewardedVideoIntent);
                        break;
                    case 2:
                        Intent bannerIntent = new Intent(MainActivity.this, BannerActivity.class);
                        startActivity(bannerIntent);
                        break;
                }
            }
        });
    }
}
