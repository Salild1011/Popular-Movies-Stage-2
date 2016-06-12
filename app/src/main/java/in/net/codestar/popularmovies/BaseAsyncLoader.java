package in.net.codestar.popularmovies;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by salil on 04-06-2016.
 * This Loader will return the string(JSON) received from the http calls
 */

public class BaseAsyncLoader extends AsyncTaskLoader<String> {

    private URL mUrl;
    private final String LOG_TAG = getClass().getSimpleName();

    public BaseAsyncLoader(Context context, URL url) {
        super(context);
        mUrl = url;
    }

    @Override
    public String loadInBackground() {

        //Fetch JSON Object, get JSON array inside, get Movie pictures
        StringBuilder strBuilder = new StringBuilder();
        try {
            HttpURLConnection connection = (HttpURLConnection) mUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            InputStream inputStream = connection.getInputStream();

            if (inputStream == null) {
                Log.w(LOG_TAG, "InputStream is null");
                return null;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String str;

            while ((str = br.readLine()) != null) {
                strBuilder.append(str);
            }

            if (strBuilder.length() == 0) {
                Log.w(LOG_TAG, "No String Received");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return strBuilder.toString();
    }
}
