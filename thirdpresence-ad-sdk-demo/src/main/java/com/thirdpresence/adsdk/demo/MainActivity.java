package com.thirdpresence.adsdk.demo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.thirdpresence.adsdk.sdk.VideoAd;
import com.thirdpresence.adsdk.sdk.VideoAdManager;
import com.thirdpresence.adsdk.sdk.internal.TLog;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ImageViewerFragment.OnFragmentInteractionListener {

    private static final int REQUEST_SETTINGS = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private String mCurrentPhotoPath;
    private Uri mCurrentImageUri;

    private ImagePagerAdapter mPagerAdapter;
    private ViewPager mViewPager;

    private List<String> mImages;
    private Timer mSlideShowTimer;

    private boolean mExternalStorageReadPermission;
    private boolean mExternalStorageWritePermission;
    private boolean mCameraPermission;

    private static final int REQUEST_CODE_PERMISSIONS = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TLog.enabled = true;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final VideoAd ad = VideoAdManager.getInstance().get(Ads.PLACEMENT_ID_1);
                if (ad != null && ad.isAdLoaded()) {
                    ad.displayAd(new Runnable() {
                        @Override
                        public void run() {
                            ad.resetAndLoadAd();
                            shareCurrentImage();
                        }
                    });
                } else {
                    shareCurrentImage();
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        boolean requiresPermissions = false;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requiresPermissions = true;
        }
        
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requiresPermissions = true;
        } else {
            mExternalStorageReadPermission = true;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requiresPermissions = true;
        } else {
            mExternalStorageWritePermission = true;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requiresPermissions = true;
        } else {
            mCameraPermission = true;
        }

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE);

        mPagerAdapter = new ImagePagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);

        if (requiresPermissions) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA},
                    REQUEST_CODE_PERMISSIONS);
        } else {
            updateImages();
        }

        Ads.initInterstitial(this, Ads.PLACEMENT_ID_1);
        Ads.initInterstitial(this, Ads.PLACEMENT_ID_2);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrDisplayAd(Ads.PLACEMENT_ID_1);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent i = new Intent();
            i.setClass(this, SettingsActivity.class);
            startActivityForResult(i, REQUEST_SETTINGS);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        final VideoAd ad = VideoAdManager.getInstance().get(Ads.PLACEMENT_ID_1);

        if (id == R.id.nav_camera) {
            if (!mExternalStorageWritePermission) {
                Toast.makeText(MainActivity.this, R.string.error_no_ext_storage_write_permission, Toast.LENGTH_SHORT).show();
            } else if (!mCameraPermission) {
                Toast.makeText(MainActivity.this, R.string.error_no_camera_permission, Toast.LENGTH_SHORT).show();
            } else {
                takePicture();
            }
        } else if (id == R.id.nav_gallery) {
            if (ad != null && ad.isAdLoaded()) {
                ad.displayAd(null, new Runnable() {
                    @Override
                    public void run() {
                        ad.resetAndLoadAd();
                        launchGallery();
                    }
                });
            } else {
                launchGallery();
            }
        } else if (id == R.id.nav_slideshow) {
            if (ad != null && ad.isAdLoaded()) {
                ad.displayAd(null, new Runnable() {
                    @Override
                    public void run() {
                        ad.resetAndLoadAd();
                        startSlideShow();
                    }
                });
            } else {
                startSlideShow();
            }

        } else if (id == R.id.nav_share) {
            shareCurrentImage();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            sendMediaStoreScanIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE_PERMISSIONS) {

            for (int i = 0; i < permissions.length; i++) {

                if (permissions[i] != null) {

                    if (permissions[i].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            mExternalStorageReadPermission = true;
                            updateImages();
                        } else {
                            Toast.makeText(MainActivity.this, R.string.error_no_ext_storage_read_permission, Toast.LENGTH_LONG).show();
                        }
                    } else if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            mExternalStorageWritePermission = true;
                        }
                    }
                    else if (permissions[i].equals(Manifest.permission.CAMERA)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            mCameraPermission = true;
                        }
                    }
                }
            }
        }
    }

    public class ImagePagerAdapter extends FragmentStatePagerAdapter {
        ImagePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new ImageViewerFragment();
            Bundle args = new Bundle();
            Uri uri = Uri.parse(mImages.get(i));
            args.putParcelable(ImageViewerFragment.URI, uri);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            if (mImages != null) {
                return mImages.size();
            }
            return 0;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (mImages != null && mImages.size() > position) {
                String path = mImages.get(position);
                Uri uri = Uri.parse(path);
                return uri.getLastPathSegment();
            }
            return "";
        }
    }

    public void onFragmentLoadedImage(Uri uri) {
        mCurrentImageUri = uri;
    }

    public void onFragmentToggleFullscreen() {
        if (getSupportActionBar().isShowing()) {
            setFullscreen(true);
        } else {
            stopSlideShow();
        }
    }

    private void setFullscreen(boolean fullscreen) {
        if (fullscreen) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE);

            ActionBar actionBar = getSupportActionBar();
            actionBar.hide();
            View titleStrip = findViewById(R.id.pager_title_strip);
            titleStrip.setVisibility(View.GONE);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE);
            ActionBar actionBar = getSupportActionBar();
            actionBar.show();
            View titleStrip = findViewById(R.id.pager_title_strip);
            titleStrip.setVisibility(View.VISIBLE);
        }
    }

    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
            }

            if (photoFile != null) {
                Uri photoURI = Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "Photo_" + timeStamp ;
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void sendMediaStoreScanIntent() {
        MediaScannerConnection.scanFile(getApplicationContext(),
            new String[]{mCurrentPhotoPath},
            new String[]{"image/jpeg"}, new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(String s, Uri uri) {
                    updateImages();
                }
            });
    }

    private void launchGallery() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setType("image/*");
        startActivity(intent);
    }

    private void shareCurrentImage() {
        if (mCurrentImageUri != null && mViewPager.getChildCount() > 0) {
            Snackbar.make(mViewPager, R.string.snackbar_opening_email, Snackbar.LENGTH_SHORT).show();

            try {
                Uri fileUri = FileProvider.getUriForFile(getApplicationContext(), "com.thirdpresence.demo.provider", new File(mCurrentImageUri.toString()));

                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("image/*");
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                i.putExtra(Intent.EXTRA_STREAM, fileUri);

                startActivity(Intent.createChooser(i, getString(R.string.share_chooser_title)));
            } catch (Exception e) {
                Snackbar.make(mViewPager.getRootView(), R.string.snackbar_failed_to_share, Snackbar.LENGTH_SHORT)
                        .show();
            }
        }
        else {
            Toast.makeText(MainActivity.this, R.string.error_no_image_loaded, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateImages() {
        new GetImagesTask().execute();
    }

    private void startSlideShow() {
        setFullscreen(true);

        if (mSlideShowTimer != null) {
            mSlideShowTimer.cancel();
        }

        mViewPager.setCurrentItem(0, true);

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        String displayTimeStr = prefs.getString("slideshow_display_time", "2");
        int displayTime;
        try {
            displayTime = Integer.parseInt(displayTimeStr);
        } catch (Exception e) {
            displayTime = 2;
        }

        mSlideShowTimer = new Timer();
        mSlideShowTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mViewPager != null && mImages != null) {
                            int cur = mViewPager.getCurrentItem();
                            if (cur < mImages.size() - 1) {
                                mViewPager.setCurrentItem(cur + 1);
                            } else {
                                stopSlideShow();
                            }
                        } else {
                            stopSlideShow();
                        }
                    }
                });
            }
        }, displayTime * 1000, displayTime * 1000);
    }

    private void stopSlideShow() {
        setFullscreen(false);

        if (mSlideShowTimer != null) {
            mSlideShowTimer.cancel();
        }
    }

    private void onImageListReady(List<String> images) {
        mImages = images;
        mPagerAdapter.notifyDataSetChanged();
    }

    private static List<String> getImages(Context context) {
        ArrayList<String> result = new ArrayList<>(10);
        final String[] projection = { MediaStore.Images.Media.DATA };
        String orderBy = android.provider.MediaStore.Images.Media.DATE_TAKEN;
        final Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                orderBy + " DESC" );
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                final int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                do {
                    final String data = cursor.getString(dataColumn);
                    result.add(data);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return result;
    }

    private class GetImagesTask extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... params) {

            final List<String> imageList = getImages(getApplication());

            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onImageListReady(imageList);
                }
            });

            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Long result) {
        }
    }

    private boolean loadOrDisplayAd(String placementId) {
        boolean displaysAd = false;
        final VideoAd ad = VideoAdManager.getInstance().get(placementId);
        if (ad != null) {
            if (!ad.isPlayerReady()) {
                Ads.initInterstitial(this, placementId);
            } else if (ad.isAdLoaded()) {
                ad.displayAd(new Runnable() {
                    @Override
                    public void run() {
                        ad.resetAndLoadAd();
                    }
                });
                displaysAd = true;

            } else {
                ad.resetAndLoadAd();
            }
        }
        return displaysAd;
    }
}