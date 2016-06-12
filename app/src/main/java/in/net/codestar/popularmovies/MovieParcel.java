package in.net.codestar.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

/**
 * Created by salil on 03-05-2016.
 * MovieParcel Parcelable for transferring data in-between fragments
 */

public class MovieParcel implements Parcelable {
    private String name, backdrop_link, synopsis, avg, rel_date, movieId, grid_link;

    public MovieParcel(String name, String backdrop_link, String synopsis, String avg,
                       String rel_date, String movieId, String grid_link) {
        this.name = name;
        this.backdrop_link = backdrop_link;
        this.synopsis = synopsis;
        this.avg = avg;
        this.rel_date = rel_date;
        this.movieId = movieId;
        this.grid_link = grid_link;
    }

    public MovieParcel(Parcel source) {
        name = source.readString();
        backdrop_link = source.readString();
        synopsis = source.readString();
        avg = source.readString();
        rel_date = source.readString();
        movieId = source.readString();
        grid_link = source.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(backdrop_link);
        dest.writeString(synopsis);
        dest.writeString(avg);
        dest.writeString(rel_date);
        dest.writeString(movieId);
        dest.writeString(grid_link);
    }

    public static final Parcelable.Creator<MovieParcel> CREATOR = new Parcelable.Creator<MovieParcel>() {
        @Override
        public MovieParcel createFromParcel(Parcel source) {
            return new MovieParcel(source);
        }

        @Override
        public MovieParcel[] newArray(int size) {
            return new MovieParcel[size];
        }
    };

    public HashMap<String, String> getHash() {
        HashMap<String, String> details = new HashMap<>();
        details.put("Movie Name", name);
        details.put("Backdrop Link", backdrop_link);
        details.put("Synopsis", synopsis);
        details.put("Rating", avg);
        details.put("Release Date", rel_date);
        details.put("Movie Id", movieId);
        details.put("Grid Link", grid_link);

        return details;
    }
}