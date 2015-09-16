package com.jeffnatiuk.spotifystreamer.spotifystreamer;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private ArrayAdapter<Artist> mAdapter;
    private ProgressBar mProgress;
    private ListView mListView;
    private ArrayList<Artist> mArtists;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        if(mArtists == null || mAdapter == null) {
            mAdapter = new ArtistArrayAdapter(getActivity(), new ArrayList());
        }

        final SearchView sv = (SearchView)rootView.findViewById(R.id.sv_artist_search);
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                FetchArtistsTask task = new FetchArtistsTask();
                task.execute(query);
                sv.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        mListView = (ListView)rootView.findViewById(R.id.lv_artist_search_results);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView lv = (ListView)parent;
                Artist selectedArtist = (Artist)lv.getItemAtPosition(position);
                String artistID = selectedArtist.id;
                String artistName = selectedArtist.name;

                ((Callback) getActivity())
                        .onItemSelected(selectedArtist);
            }
        });

        mProgress = (ProgressBar)rootView.findViewById(R.id.progress_load_artists);

        return rootView;
    }

    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Artist selectedArtist);
    }

    public class FetchArtistsTask extends AsyncTask<String, Void, List<Artist>> {

        private final String LOG_TAG = FetchArtistsTask.class.getSimpleName();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgress.setVisibility(View.VISIBLE);
        }

        protected List<Artist> doInBackground(String... params) {
            String artistQuery = params[0];
            ArtistsPager results = null;
            try {
                SpotifyApi api = new SpotifyApi();
                SpotifyService spotify = api.getService();
                results = spotify.searchArtists(artistQuery);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Retrofit error");
            }
            if (results != null && results.artists.items.size() > 0) {
                return results.artists.items;
            }
            else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Artist> artists) {
            if(artists != null) {
                mAdapter.clear();
                mArtists = new ArrayList<Artist>();
                for (Artist a : artists) {
                    mAdapter.add(a);
                    mArtists.add(a);
                }
                if(artists.size() == 0)
                {
                    Toast.makeText(getActivity(), "No artists found.  Please refine search.", Toast.LENGTH_SHORT).show();
                }
                mProgress.setVisibility(View.INVISIBLE);
            }
            else
            {
                Toast.makeText(getActivity(), "No internet connection.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}