package in.net.codestar.popularmovies;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by salil on 12-06-2016.
 * Utility class with helper methods for fragments
 */
public class Utility {

    public static final int BASE_BUTTON_ID = 100;
    private static final String LOG_TAG = "popularmovies.Utility";

    //Add views to linear layout and set data for trailers
    public static void setTrailerData(Context context, View container, String[] trailerLink) {
        if (trailerLink == null) return;

        LinearLayout linearLayout = (LinearLayout) container
                .findViewById(R.id.trailer_linear_layout);

        int[] trailerId = new int[trailerLink.length];

        TextView tv = new TextView(context);
        tv.setText(Html.fromHtml("<br /><h4>Trailers:</h4>"));
        tv.setTextColor(Color.BLACK);
        linearLayout.addView(tv);

        Button button;

        for (int i = 0; i < trailerLink.length; i++) {
            button = new Button(context);
            trailerId[i] = BASE_BUTTON_ID + i;
            button.setId(trailerId[i]);

            String s = "Trailer ".concat(Integer.toString(i + 1));
            button.setText(s);
            button.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

            linearLayout.addView(button);
        }

        if (trailerLink.length == 0) {
            tv = new TextView(context);
            String review = "<h5>Sorry, no trailers available</h5>";
            tv.setText(Html.fromHtml(review));
            tv.setTextColor(Color.BLACK);
            tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            linearLayout.addView(tv);
        }
    }

    //Add views to linear layout and set data for reviews
    public static void setReviewData(Context context, View container,
                               String[] reviewContent, String[] reviewAuthor) {
        if (reviewContent == null) return;

        LinearLayout linearLayout = (LinearLayout) container
                .findViewById(R.id.review_linear_layout);

        TextView tv = new TextView(context);
        tv.setText(Html.fromHtml("<br /><h4>Reviews:</h4>"));
        tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tv.setTextColor(Color.BLACK);
        linearLayout.addView(tv);

        View view;

        for (int i = 0; i < reviewContent.length; i++) {
            tv = new TextView(context);
            String review = "<h5>" + reviewAuthor[i] + "</h5>" + reviewContent[i];
            tv.setText(Html.fromHtml(review));
            tv.setTextColor(Color.BLACK);
            tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            linearLayout.addView(tv);

            //Add line for all reviews except the last one
            if (i != reviewContent.length - 1) {
                view = new View(context);
                view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));
                view.setPadding(0, 10, 0, 30);
                view.setBackgroundColor(Color.BLACK);
                linearLayout.addView(view);
            }
        }

        if (reviewContent.length == 0) {
            tv = new TextView(context);
            String review = "<h5>Sorry, no reviews available</h5>";
            tv.setText(Html.fromHtml(review));
            tv.setTextColor(Color.BLACK);
            tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            linearLayout.addView(tv);
        }
    }

    //Get the url for either reviews or trailers
    public static URL getURL(String movieId, String params) {
        URL listUrl = null;

        String SCHEME_HTTP = "http";
        String AUTHORITY_API = "api.themoviedb.org";
        String PATH_3 = "3";
        String PATH_MOVIE = "movie";
        String TRAILER = "trailer";
        String REVIEW = "review";
        String VIDEOS_TAG = "videos";
        String REVIEWS_TAG = "reviews";
        String API_KEY = "api_key";

        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME_HTTP)
                .authority(AUTHORITY_API)
                .appendPath(PATH_3)
                .appendPath(PATH_MOVIE)
                .appendPath(movieId);

        if (params.equals(TRAILER)) {
            builder.appendPath(VIDEOS_TAG);
        }
        else if (params.equals(REVIEW)) {
            builder.appendPath(REVIEWS_TAG);
        }

        builder.appendQueryParameter(API_KEY, BuildConfig.TMDB_API_KEY);

        try {
            listUrl = new URL(builder.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return listUrl;
    }

    /*
     * type is the type of image, either grid or backdrop image
     */
    public static String storeImage(Context context, String url,
                                    final String absPath, final int movieId,
                                    final String type) {

        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                File file = new File(absPath + "/"
                        + Integer.toString(movieId)
                        + "_" + type + ".jpg");

                try {
                    if (file.createNewFile()) {
                        FileOutputStream opStream = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, opStream);
                        opStream.flush();
                        opStream.close();
                    }
                    else {
                        Log.w(LOG_TAG, "Unable to save file");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };

        Picasso.with(context).load(url).into(target);
        return absPath + "/" + Integer.toString(movieId) + "_" + type + ".jpg";
    }

    public static void deleteImage(String absPath, int movieId, String type) {
        File file = new File(absPath + "/"
                + Integer.toString(movieId) + "_" + type + ".jpg");

        if (file.exists() && file.delete()) {
            Log.i(LOG_TAG, "Image deleted successfully");
        }
    }
}
