package in.net.codestar.popularmovies;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

import in.net.codestar.popularmovies.database.FavouriteMovieContract.MovieEntry;

/**
 * A placeholder fragment containing a simple view of grid of movie posters
 */

public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<String> {

    private GridView mMoviesGrid;
    //Arrays to store data about each movie loaded into grid
    private String[] mMovieName, mPosterLinks, mOverview, mRating, mRelease, mUrlArr, mMovieId;
    private static String mSortPref;
    private ImageAdapter mImageAdapter;
    private static int mSelectPosition = ListView.INVALID_POSITION;

    //Id for Loader
    private final static int GRID_LOADER_ID = 1;

    //Strings for building URL
    private final String SCHEME_HTTP = "http";
    private final String AUTHORITY_API = "api.themoviedb.org";
    private final String AUTHORITY_IMG = "image.tmdb.org";
    private final String PATH_3 = "3";
    private final String PATH_T = "t";
    private final String PATH_P = "p";
    private final String PATH_MOVIE = "movie";
    private final String POPULAR = "popular";
    private final String TOP_RATED = "top_rated";
    private final String API_KEY = "api_key";
    private final String SIZE_POSTER = "w342";
    private final String SIZE_BACKDROP = "w342";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mMoviesGrid = (GridView) view.findViewById(R.id.gridView);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        mSortPref = pref.getString(getString(R.string.sort_key), getString(R.string.pref_sort_default));

        mImageAdapter = new ImageAdapter(getActivity(), R.layout.image_style);
        mMoviesGrid.setAdapter(mImageAdapter);

        if (savedInstanceState != null) {
            mUrlArr = savedInstanceState.getStringArray("URL Array");
            mMovieName = savedInstanceState.getStringArray("Movie Name");
            mPosterLinks = savedInstanceState.getStringArray("Poster Links");
            mOverview = savedInstanceState.getStringArray("Overview");
            mRating = savedInstanceState.getStringArray("Rating");
            mRelease = savedInstanceState.getStringArray("Release");
            mMovieId = savedInstanceState.getStringArray("Movie Id");

            mImageAdapter.swapData(mUrlArr, mMovieName);
            mMoviesGrid.setOnItemClickListener(new onMovieClickListener());
        }
        else {
            //Load the favourites movies in offline mode
            if (mSortPref.equals(getString(R.string.pref_sort_favourites))) {
                loadFavourites();
            }
            else if (mSortPref.equals(getString(R.string.pref_sort_rating))
                    || mSortPref.equals(getString(R.string.pref_sort_popularity))) {
                loadPopularRatings();
            }
        }

        return view;
    }

    //Build a URL depending on sort criteria
    private URL getURL(String mSortPref) {
        URL listUrl = null;
        Uri.Builder builder = new Uri.Builder();

        if (mSortPref.equals(getString(R.string.pref_sort_popularity))) {
            builder.scheme(SCHEME_HTTP)
                    .authority(AUTHORITY_API)
                    .appendPath(PATH_3)
                    .appendPath(PATH_MOVIE)
                    .appendPath(POPULAR)
                    .appendQueryParameter(API_KEY, BuildConfig.TMDB_API_KEY);
        }
        else if (mSortPref.equals(getString(R.string.pref_sort_rating))) {
            builder.scheme(SCHEME_HTTP)
                    .authority(AUTHORITY_API)
                    .appendPath(PATH_3)
                    .appendPath(PATH_MOVIE)
                    .appendPath(TOP_RATED)
                    .appendQueryParameter(API_KEY, BuildConfig.TMDB_API_KEY);
        }

        try {
            listUrl = new URL(builder.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return listUrl;
    }


    @Override
    public void onResume() {
        super.onResume();

        if (mMoviesGrid.getAdapter() == null) {
            mMoviesGrid.setAdapter(mImageAdapter);
        }

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String str = pref.getString(getString(R.string.sort_key), getString(R.string.pref_sort_default));

        //Check if preference has changed
        //If sorting is done according to 'favourites' then there might be changes in data
        if (str.equals(getString(R.string.pref_sort_favourites))) {
            mSortPref = str;
            loadFavourites();
        }
        else if (!str.equals(mSortPref)) {
            mSortPref = str;
            loadPopularRatings();
        }

        if (mSelectPosition != ListView.INVALID_POSITION) {
            mMoviesGrid.smoothScrollToPosition(mSelectPosition);
        }
    }

    //If preference is to sort movies according to popularity or ratings
    private void loadPopularRatings() {
        getLoaderManager().restartLoader(GRID_LOADER_ID, null, this).forceLoad();
    }

    //If preference is to sort movies according to favourites
    public void loadFavourites() {
        //Get cursor to database
        Cursor c = getActivity().getContentResolver().query(
                MovieEntry.getContentUri(), null, null, null, null);

        //Initialize all arrays
        int count;
        if (c == null) {
            count = 0;
        }
        else {
            count = c.getCount();
        }

        if (count == 0) {
            Toast.makeText(getContext(), "No movies added to Favourites",
                    Toast.LENGTH_SHORT).show();

            //Clear the grid view as no entries in database
            mMoviesGrid.setAdapter(null);

            //If in two pane mode, replace the detail pane by a blank fragment
            if (getActivity().findViewById(R.id.frag_detail) != null) {
                DetailActivityFragment fragment = new DetailActivityFragment();

                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frag_detail, fragment, getString(R.string.detail_frag_tag))
                        .commit();
            }
        }
        else {
            mUrlArr = new String[count];
            mMovieName = new String[count];
            mPosterLinks = new String[count];
            mOverview = new String[count];
            mRating = new String[count];
            mRelease = new String[count];
            mMovieId = new String[count];

            c.moveToFirst();
            int i = 0;
            do {
                mUrlArr[i] = c.getString(c.getColumnIndex(MovieEntry.GRID_IMG_LINK));
                mMovieName[i] = c.getString(c.getColumnIndex(MovieEntry.MOVIE_NAME));
                mPosterLinks[i] = c.getString(c.getColumnIndex(MovieEntry.POSTER_LINK));
                mOverview[i] = c.getString(c.getColumnIndex(MovieEntry.OVERVIEW));
                mRating[i] = c.getString(c.getColumnIndex(MovieEntry.RATING));
                mRelease[i] = c.getString(c.getColumnIndex(MovieEntry.RELEASE_DATE));
                mMovieId[i] = c.getString(c.getColumnIndex(MovieEntry.MOVIE_ID));

                i++;
            } while (c.moveToNext());

            c.close();
            initFragment(true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putStringArray("URL Array", mUrlArr);
        outState.putStringArray("Movie Name", mMovieName);
        outState.putStringArray("Poster Links", mPosterLinks);
        outState.putStringArray("Overview", mOverview);
        outState.putStringArray("Rating", mRating);
        outState.putStringArray("Release", mRelease);
        outState.putStringArray("Movie Id", mMovieId);

        super.onSaveInstanceState(outState);
    }

    //Get the data of movie loaded into 1st grid cell for loading into DetailActivityFragment
    //while in two pane mode
    public MovieParcel getFirstData() {
        return new MovieParcel(mMovieName[0], mPosterLinks[0],
                mOverview[0], mRating[0], mRelease[0], mMovieId[0], mUrlArr[0]);
    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        return new BaseAsyncLoader(getContext(), getURL(mSortPref));
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        if (data != null && data.length() > 0) {
            //Parse JSON data
            parseJsonData(data);

            //Initialize fragment using fetched data
            initFragment(false);
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    //Parse the JSON string received
    private void parseJsonData(String jsonStr) {
        try {
            JSONObject resObj = new JSONObject(jsonStr);
            JSONArray movieArr = resObj.getJSONArray("results");

            mUrlArr = new String[movieArr.length()];
            mMovieName = new String[movieArr.length()];
            mPosterLinks = new String[movieArr.length()];
            mOverview = new String[movieArr.length()];
            mRating = new String[movieArr.length()];
            mRelease = new String[movieArr.length()];
            mMovieId = new String[movieArr.length()];

            JSONObject movieObj;

            //Compile a string array of image links
            for (int i = 0; i < movieArr.length(); i++) {
                movieObj = movieArr.getJSONObject(i);
                String imageLink = movieObj.getString("poster_path");
                imageLink = imageLink.substring(1);

                Uri.Builder builder = new Uri.Builder();
                builder.scheme(SCHEME_HTTP)
                        .authority(AUTHORITY_IMG)
                        .appendPath(PATH_T)
                        .appendPath(PATH_P)
                        .appendPath(SIZE_POSTER)
                        .appendPath(imageLink);
                mUrlArr[i] = builder.toString();

                //Save moviename, poster link, overview, vote_avg, release date, video trailer link
                mMovieName[i] = movieObj.getString("original_title");

                builder = new Uri.Builder();
                String pLink = movieObj.getString("backdrop_path");
                pLink = pLink.substring(1);

                builder.scheme(SCHEME_HTTP)
                        .authority(AUTHORITY_IMG)
                        .appendPath(PATH_T)
                        .appendPath(PATH_P)
                        .appendPath(SIZE_BACKDROP)
                        .appendPath(pLink);
                mPosterLinks[i] =  builder.toString();

                mOverview[i] = movieObj.getString("overview");
                mRating[i] = movieObj.getString("vote_average");
                mRelease[i] = movieObj.getString("release_date");
                mMovieId[i] = movieObj.getString("id");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //Initialize the fragment with downloaded data
    private void initFragment(boolean mode) {
        mImageAdapter.setMode(mode);
        mImageAdapter.swapData(mUrlArr, mMovieName);
        mMoviesGrid.setOnItemClickListener(new onMovieClickListener());

        //If two pane mode then initialize DetailActivityFragment with data from first grid
        if (getActivity().findViewById(R.id.frag_detail) != null) {
            DetailActivityFragment fragment = new DetailActivityFragment();

            MovieParcel parcel = getFirstData();
            Bundle args = new Bundle();
            args.putParcelable(getString(R.string.movie_parcel), parcel);

            fragment.setArguments(args);

            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frag_detail, fragment, getString(R.string.detail_frag_tag))
                    .commitAllowingStateLoss();
        }
    }

    class onMovieClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mSelectPosition = position;

            MovieParcel movieParcel = new MovieParcel(mMovieName[position],
                    mPosterLinks[position], mOverview[position], mRating[position],
                    mRelease[position], mMovieId[position], mUrlArr[position]);

            ((CallBack) getActivity()).onGridItemClicked(movieParcel);
        }
    }

    public interface CallBack {
        void onGridItemClicked(MovieParcel movieParcel);
    }
}
