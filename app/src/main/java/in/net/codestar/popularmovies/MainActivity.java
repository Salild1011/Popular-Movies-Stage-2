package in.net.codestar.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.CallBack,
        DetailActivityFragment.CallBack {

    private boolean mTwoPane, mIsConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        mIsConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (!mIsConnected){
            Toast.makeText(MainActivity.this,
                    "No Internet Connection, Favourite movies only available",
                    Toast.LENGTH_SHORT).show();
        }
        mTwoPane = findViewById(R.id.frag_detail) != null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Callback implementation for grid item clicked
    @Override
    public void onGridItemClicked(MovieParcel movieParcel) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putParcelable(getString(R.string.movie_parcel), movieParcel);

            DetailActivityFragment fragment = new DetailActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frag_detail, fragment).commit();
        }
        else {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(getString(R.string.movie_parcel), movieParcel);

            startActivity(intent);
        }
    }

    @Override
    public void onFavouriteClicked() {
        String s = PreferenceManager.getDefaultSharedPreferences(getBaseContext())
                .getString(getString(R.string.sort_key), getString(R.string.pref_sort_default));

        if (mTwoPane) {
            if (s.equals(getString(R.string.pref_sort_favourites))) {
                MainActivityFragment fragment = (MainActivityFragment) getSupportFragmentManager()
                        .findFragmentByTag(getString(R.string.main_frag_tag));
                fragment.loadFavourites();
            }
        }
    }
}
