package com.thirdpresence.adsdk.sampleapp;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.thirdpresence.sampleapp.R;

public class MainActivity extends AppCompatActivity {

    private static boolean mUseStagingServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
        } else {
            View appBar = findViewById(R.id.appbar);
            appBar.setVisibility(View.GONE);
        }

        ListView listView = (ListView)findViewById(R.id.list_select_placement_type);

        ListAdapter adapter = new ListAdapter(getApplicationContext());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent;
                switch (position) {
                    case 1:
                        intent = new Intent(MainActivity.this, InterstitialActivity.class);
                        break;
                    case 2:
                        intent = new Intent(MainActivity.this, RewardedVideoActivity.class);
                        break;
                    case 3:
                        intent = new Intent(MainActivity.this, BannerActivity.class);
                        break;
                    default:
                        intent = null;
                }

                if (intent != null) {
                    intent.putExtra("use_staging_server", mUseStagingServer);
                    startActivity(intent);
                }
            }
        });
    }

    class ListAdapter extends BaseAdapter {

        private final LayoutInflater mInflater;
        private final String[] itemTexts;

        public ListAdapter(Context context) {
            mInflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            itemTexts = new String[]{
                getString(R.string.label_select_placement_type),
                getString(R.string.list_item_interstitial),
                getString(R.string.list_item_rewarded_video),
                getString(R.string.list_item_banner),
                getString(R.string.label_settings),
                getString(R.string.label_staging_switch)
            };
        }

        @Override
        public int getCount() {
            return itemTexts.length;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            if (position == 4 || position == 0) {
                // section headers
                return false;
            }
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            TextView textView = null;

            switch (position) {
                case 0:
                case 4:
                    view = mInflater.inflate(R.layout.header_item, null);
                    if (view != null) {
                        textView = (TextView) view.findViewById(R.id.header_item_text);
                    }
                    break;
                case 5:
                    view = mInflater.inflate(R.layout.settings_item, null);
                    if (view != null) {
                        textView = (TextView) view.findViewById(R.id.settings_item_text);
                        Switch sw = (Switch) view.findViewById(R.id.settings_item_switch);
                        sw.setChecked(mUseStagingServer);
                        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                mUseStagingServer = b;
                            }
                        });
                    }
                    break;
                default:
                    view = mInflater.inflate(android.R.layout.simple_list_item_1, null);
                    if (view != null) {
                        textView = (TextView) view.findViewById(android.R.id.text1);
                    }
            }


            if (textView != null) {
                textView.setText(itemTexts[position]);
            }
            return view;
        }
    }
}
