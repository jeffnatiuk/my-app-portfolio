package com.jeffnatiuk.spotifystreamer.spotifystreamer;

import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Fragment;

import com.squareup.okhttp.Call;

import kaaes.spotify.webapi.android.models.Artist;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callback {
    private boolean mTwoPane = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.fragment_top_songs_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_top_songs_container, new TopSongsActivityFragment())
                        .commit();
            }
        } else {
            mTwoPane = false;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Artist selectedArtist) {
        if(mTwoPane){
            Bundle args = new Bundle();
            args.putParcelable("selectedArtist", selectedArtist);
            TopSongsActivityFragment fragment = new TopSongsActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_top_songs_container, fragment, "DET")
                    .commit();
        }
        else{
            Intent intent = new Intent(this, TopSongsActivity.class);
            String artistID = selectedArtist.id;
            String artistName = selectedArtist.name;
            intent.putExtra(
                    getResources().getString((R.string.intent_extra_ArtistId)),
                    artistID
            );
            intent.putExtra(
                    getResources().getString((R.string.intent_extra_ArtistName))
                    , artistName);
            startActivity(intent);
        }
    }
}
