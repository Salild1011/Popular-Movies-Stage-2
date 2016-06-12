package in.net.codestar.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

import in.net.codestar.popularmovies.database.FavouriteMovieContract.MovieEntry;

/**
 * Created by salil on 30-05-2016.
 * DetailActivityFragment displays the details of selected movie, on receiving the "MovieParcel"
 * from previous MainActivityFragment
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<String> {

    private final String LOG_TAG = getClass().getSimpleName();
    private MovieParcel mParcel = null;
    private String[] mTrailerLink = null, mReviewContent = null, mReviewAuthor = null;
    private String mMovieName, mBackdropLink, mOverview, mRelDate, mGridLink;
    private int mMovieId;
    private float mRating;
    private static boolean mFavourite;
    private ShareActionProvider mShareActionProvider;

    private final int TRAILER_LOADER_ID = 1;
    private final int REVIEW_LOADER_ID = 2;

    public DetailActivityFragment() { setHasOptionsMenu(true); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        setRetainInstance(true);

        Intent intent = getActivity().getIntent();
        final HashMap<String, String> details;
        Bundle args = getArguments();

        if (savedInstanceState != null) {
            mParcel = savedInstanceState.getParcelable(getString(R.string.movie_parcel));
        }
        else if (intent == null & args == null) {
            Log.w(LOG_TAG, "Null initiation");
        }
        else if (intent != null && intent.hasExtra(getString(R.string.movie_parcel))) {
            mParcel = intent.getExtras().getParcelable(getString(R.string.movie_parcel));
        }
        else if (args != null) {
            mParcel = args.getParcelable(getString(R.string.movie_parcel));
        }

        if (mParcel != null) {
            //Get data from MovieParcel
            details = mParcel.getHash();

            mMovieName = details.get("Movie Name");
            mBackdropLink = details.get("Backdrop Link");
            mOverview = details.get("Synopsis");
            mRating = Float.parseFloat(details.get("Rating"));
            mRelDate = details.get("Release Date");
            mMovieId = Integer.parseInt(details.get("Movie Id"));
            mGridLink = details.get("Grid Link");

            String[] columns = {MovieEntry.MOVIE_ID};
            String selection = MovieEntry.MOVIE_ID + " =?";
            String[] selectionArgs = {Integer.toString(mMovieId)};
            Cursor cursor = getActivity().getContentResolver().query(
                    MovieEntry.getContentUri(),
                    columns,
                    selection,
                    selectionArgs,
                    null);

            if (cursor != null) {
                mFavourite = cursor.getCount() > 0;
                cursor.close();
            }

            ImageView poster = (ImageView) view.findViewById(R.id.imageid);
            TextView movieNameRate = (TextView) view.findViewById(R.id.movieNameRate);
            ToggleButton button = (ToggleButton) view.findViewById(R.id.favourites_button);
            TextView movieDetails = (TextView) view.findViewById(R.id.movieText);

            if (mFavourite) {
                Picasso.with(getContext()).load(new File(getActivity().getFilesDir().getAbsolutePath() + "/"
                        + Integer.toString(mMovieId) + "_Backdrop" + ".jpg")).into(poster);
            }
            else {
                Picasso.with(getContext()).load(details.get("Backdrop Link")).into(poster);
            }
            button.setVisibility(View.VISIBLE);
            button.setChecked(mFavourite);
            button.setOnCheckedChangeListener(new MyCheckedChangeListener());

            String s = mMovieName + " (" + mRating + " / 10)";
            movieNameRate.setText(s);
            movieDetails.setText(Html.fromHtml(
                    "<h4>Release: " + mRelDate + "</h4>"
                            + "<h4>Synopsis:</h4>" + mOverview));

            if (savedInstanceState == null) {
                //Get and Set trailer and reviews data
                getLoaderManager().initLoader(TRAILER_LOADER_ID, null, this).forceLoad();
                getLoaderManager().initLoader(REVIEW_LOADER_ID, null, this).forceLoad();
            } else {
                //Data is present in saveInstanceState, just update the views
                Utility.setTrailerData(getActivity(), view, mTrailerLink);
                Utility.setReviewData(getActivity(), getView(), mReviewContent, mReviewAuthor);
            }
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_frag_detail, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        mShareActionProvider.setShareIntent(null);

        if (mTrailerLink != null && mTrailerLink[0] != null) {
            mShareActionProvider.setShareIntent(createShareYTLinkIntent());
        }
    }

    private Intent createShareYTLinkIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "http://www.youtube.com/watch?v=" + mTrailerLink[0]);
        return shareIntent;
    }

    private class MyCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            String gridStr = "Grid";
            String backdropStr = "Backdrop";

            if (mFavourite != isChecked && isChecked) {
                mFavourite = true;

                mGridLink = Utility.storeImage(getActivity(), mGridLink,
                        getActivity().getFilesDir().getAbsolutePath(), mMovieId,
                        gridStr);
                mBackdropLink = Utility.storeImage(getActivity(), mBackdropLink,
                        getActivity().getFilesDir().getAbsolutePath(), mMovieId,
                        backdropStr);

                ContentValues values = new ContentValues();
                values.put(MovieEntry.MOVIE_ID, mMovieId );
                values.put(MovieEntry.MOVIE_NAME, mMovieName);
                values.put(MovieEntry.GRID_IMG_LINK, mGridLink);
                values.put(MovieEntry.POSTER_LINK, mBackdropLink);
                values.put(MovieEntry.OVERVIEW, mOverview);
                values.put(MovieEntry.RATING, mRating);
                values.put(MovieEntry.RELEASE_DATE, mRelDate);
                getActivity().getContentResolver().insert(MovieEntry.getContentUri(), values);

                Toast.makeText(getActivity(),mMovieName + " added to favourites",
                        Toast.LENGTH_SHORT).show();
            }
            else {
                mFavourite = false;

                Utility.deleteImage(getActivity().getFilesDir().getAbsolutePath(),
                        mMovieId, gridStr);
                Utility.deleteImage(getActivity().getFilesDir().getAbsolutePath(),
                        mMovieId, backdropStr);

                String[] whereArgs = { Integer.toString(mMovieId) };
                getActivity().getContentResolver().delete(
                        MovieEntry.getContentUri(), MovieEntry.MOVIE_ID + "=?", whereArgs);

                Toast.makeText(getActivity(),mMovieName + " removed from favourites",
                        Toast.LENGTH_SHORT).show();
            }

            ((CallBack) getActivity()).onFavouriteClicked();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mParcel != null) {
            outState.putParcelable(getString(R.string.movie_parcel), mParcel);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        //Strings for getting corresponding urls
        if (id == TRAILER_LOADER_ID) {
            return new BaseAsyncLoader(getContext(), Utility.getURL(Integer.toString(mMovieId), "trailer"));
        }
        else if (id == REVIEW_LOADER_ID){
            return new BaseAsyncLoader(getContext(), Utility.getURL(Integer.toString(mMovieId), "review"));
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        parseJsonData(loader.getId(), data);

        if (loader.getId() == TRAILER_LOADER_ID) {
            Utility.setTrailerData(getActivity(), getView(), mTrailerLink);

            if (mTrailerLink != null && mTrailerLink.length > 0){
                for (int i = 0; i < mTrailerLink.length; i++) {
                    Button b = (Button) getActivity().findViewById(Utility.BASE_BUTTON_ID + i);
                    if (b != null) {
                        b.setOnClickListener(new TrailerLinkClickListener());
                    }
                }

                if (mShareActionProvider != null) {
                    mShareActionProvider.setShareIntent(createShareYTLinkIntent());
                }
            }
        }
        else if (loader.getId() == REVIEW_LOADER_ID) {
            Utility.setReviewData(getActivity(), getView(), mReviewContent, mReviewAuthor);
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) { }

    public class TrailerLinkClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int pos = v.getId() - Utility.BASE_BUTTON_ID;
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.youtube.com/watch?v=" + mTrailerLink[pos]));
            startActivity(intent);
        }
    }

    private void parseJsonData(int id, String jsonStr) {

        try {
            JSONObject resObj = new JSONObject(jsonStr);
            JSONArray movieArr = resObj.getJSONArray("results");

            if (id == TRAILER_LOADER_ID) {
                mTrailerLink = new String[movieArr.length()];

                JSONObject trailerObj;

                //Compile a string array of image links
                for (int i = 0; i < movieArr.length(); i++) {
                    trailerObj = movieArr.getJSONObject(i);
                    mTrailerLink[i] = trailerObj.getString("key");
                }
            }
            else if (id == REVIEW_LOADER_ID) {
                mReviewContent = new String[movieArr.length()];
                mReviewAuthor = new String[movieArr.length()];

                JSONObject reviewObj;

                //Compile a string array of image links
                for (int i = 0; i < movieArr.length(); i++) {
                    reviewObj = movieArr.getJSONObject(i);
                    mReviewContent[i] = reviewObj.getString("content");
                    mReviewAuthor[i] = reviewObj.getString("author");
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public interface CallBack {
        void onFavouriteClicked();
    }
}
