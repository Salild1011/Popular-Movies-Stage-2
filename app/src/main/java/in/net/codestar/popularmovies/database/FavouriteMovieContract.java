package in.net.codestar.popularmovies.database;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by salil on 05-06-2016.
 * Defines all column names and table name
 */
public class FavouriteMovieContract {

    public static final String CONTENT_AUTHORITY = "in.net.codestar.popularmovies";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String FAVOURITES_MOVIES = "favourites";

    public FavouriteMovieContract() {}

    public static final class MovieEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(FAVOURITES_MOVIES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + FAVOURITES_MOVIES;

        public static final String TABLE_NAME = "favourites";

        public static final String MOVIE_ID = "movie_id";
        public static final String MOVIE_NAME = "movie_name";
        public static final String GRID_IMG_LINK = "grid_img_link";
        public static final String POSTER_LINK = "poster_link";
        public static final String OVERVIEW = "overview";
        public static final String RATING = "rating";
        public static final String RELEASE_DATE = "release_date";

        public static Uri buildFavouritesUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri getContentUri() {
            return CONTENT_URI;
        }
    }
}
