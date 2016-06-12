package in.net.codestar.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * Created by salil on 22-04-2016.
 * Custom ImageAdapter to recycle views and load images into grid
 */

public class ImageAdapter extends ArrayAdapter<String> {
    private Context mContext;
    private String[] mUrlArr, mMovieName;
    private boolean mOffline = false;

    public ImageAdapter(Context context, int resource) {
        super(context, resource);
        mContext = context;
    }

    public ImageAdapter(Context context, int resource, boolean offline) {
        super(context, resource);
        mContext = context;
        mOffline = offline;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.image_style, null);
        }

        imageView = (ImageView) convertView;
        if (mUrlArr != null) {
            if (mOffline) {
                Picasso.with(mContext).load(new File(mUrlArr[position])).into(imageView);
            }
            else {
                Picasso.with(mContext).load(Uri.parse(mUrlArr[position])).into(imageView);
            }
            imageView.setContentDescription(mMovieName[position]);
        }

        return imageView;
    }

    @Override
    public int getCount() {
        if (mUrlArr == null) {
            return 0;
        }
        return mUrlArr.length;
    }

    public void setMode(boolean mode) {
        mOffline = mode;
    }

    public void swapData(String[] url_arr, String[] movie_name) {
        this.mUrlArr = url_arr;
        this.mMovieName = movie_name;
        notifyDataSetChanged();
    }
}
