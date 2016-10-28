package com.thirdpresence.adsdk.demo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

public class ImageViewerFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private GestureDetectorCompat mDetector;
    private View.OnTouchListener mGestureListener;

    private BitmapWorkerTask mWorker;
    public static final String URI = "URI";

    private class GestureListener implements GestureDetector.OnGestureListener {

        @Override
        public boolean onDown(MotionEvent motionEvent) {
            return true;
        }

        @Override
        public void onShowPress(MotionEvent motionEvent) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            if (mListener != null) {
                mListener.onFragmentToggleFullscreen();
                return true;
            }
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent motionEvent) {
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentLoadedImage(Uri uri);
        void onFragmentToggleFullscreen();
    }

    public ImageViewerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_image_viewer, container, false);

        mDetector = new GestureDetectorCompat(getActivity(), new GestureListener() );

        mGestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return mDetector.onTouchEvent(event);
            }
        };
        ImageView imageView = (ImageView) v.findViewById(R.id.image_view);
        imageView.setOnTouchListener(mGestureListener);
        Bundle args = getArguments();
        Uri uri = args.getParcelable(URI);

        mWorker = new BitmapWorkerTask(imageView);
        mWorker.execute(uri);
        onImageLoaded(uri);
        return v;
    }

    public void onImageLoaded(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentLoadedImage(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mWorker != null && !mWorker.isCancelled()) {
            mWorker.cancel(true);
        }
        mWorker = null;
        mListener = null;
    }

    public void onDestroyView() {
        super.onDestroyView();
        ImageView imageView = (ImageView) getView().findViewById(R.id.image_view);
        imageView.setOnTouchListener(null);
        imageView.setImageURI(null);
        if (mWorker != null && !mWorker.isCancelled()) {
            mWorker.cancel(true);
        }
        mWorker = null;
    }

    class BitmapWorkerTask extends AsyncTask<Uri, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private Uri uri;
        private int width;
        private int height;

        public BitmapWorkerTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
            DisplayMetrics metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
            width = metrics.widthPixels;
            height = metrics.heightPixels;
        }

        @Override
        protected Bitmap doInBackground(Uri... params) {
            uri = params[0];
            if (!isCancelled()) {
                return decodeSampledBitmap(uri, width, height);
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewReference != null && bitmap != null && !isCancelled()) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmap(Uri uri, int reqWidth, int reqHeight) {
        Bitmap bm = null;
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(uri.toString(), options);
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;
            bm = BitmapFactory.decodeFile(uri.toString(), options);
        }
        catch (Exception e) {
        }
        return bm;
    }
}
