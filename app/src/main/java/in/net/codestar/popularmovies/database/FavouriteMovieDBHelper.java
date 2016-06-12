package in.net.codestar.popularmovies.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import in.net.codestar.popularmovies.database.FavouriteMovieContract.MovieEntry;

/**
 * Created by salil on 05-06-2016.
 * Defines and Creates Table(structure)
 */
public class FavouriteMovieDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "favourite.db";

    public FavouriteMovieDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_FAV_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + "(" +
                MovieEntry._ID + " INTEGER PRIMARY KEY, " +
                MovieEntry.MOVIE_ID + " INTEGER UNIQUE, " +
                MovieEntry.MOVIE_NAME + " TEXT NOT NULL, " +
                MovieEntry.GRID_IMG_LINK + " TEXT NOT NULL, " +
                MovieEntry.POSTER_LINK + " TEXT NOT NULL, " +
                MovieEntry.OVERVIEW + " TEXT NOT NULL, " +
                MovieEntry.RATING + " REAL NOT NULL, " +
                MovieEntry.RELEASE_DATE + " TEXT NOT NULL" +
                " );";

        db.execSQL(SQL_CREATE_FAV_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        onCreate(db);
    }
}
