package com.jeffnatiuk.spotifystreamer.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.io.Console;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;

/**
 * A placeholder fragment containing a simple view.
 */
public class TopSongsActivityFragment extends Fragment {

    public TopSongsActivityFragment() {
    }
    private ArrayAdapter<Track> mAdapter;
    private ArrayList<Track> mTracks;
    private ProgressBar mProgress;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_top_songs, container, false);

        if(mAdapter == null) {
            mAdapter = new TopTenArrayAdapter(getActivity(), new ArrayList<Track>());
        }
        mProgress = (ProgressBar)rootView.findViewById(R.id.progress_load_songs);

        Intent intent = getActivity().getIntent();
        String artistId = intent.getStringExtra(
                getResources().getString((R.string.intent_extra_ArtistId))
        );

        //Make calls to Spotify API to get top tracks by artist ID.
        if(mTracks == null) {
            mTracks = new ArrayList<>();
            FetchTopTenTask task = new FetchTopTenTask();
            task.execute(artistId);
        }

        ListView lv = (ListView)rootView.findViewById(R.id.list_view_artist_top10);
        lv.setAdapter(mAdapter);
        return rootView;
    }

    public class FetchTopTenTask extends AsyncTask<String, Void, Tracks> {

        private final String LOG_TAG = FetchTopTenTask.class.getSimpleName();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgress.setVisibility(View.VISIBLE);
        }

        protected Tracks doInBackground(String... params) {
            String artistId = params[0];
            Tracks results = null;
            try {
                SpotifyApi api = new SpotifyApi();
                SpotifyService spotify = api.getService();
                Map<String, Object> options = new HashMap<>();
                options.put("country", "US");
                results = spotify.getArtistTopTrack(artistId, options);
                int i = 0;
            }
            catch(Exception e){
                Log.e(LOG_TAG, "Retrofit error.");
            }
            return results;
        }

        @Override
        protected void onPostExecute(Tracks tracks) {
            if(tracks != null) {
                mAdapter.clear();
                for (Track t : tracks.tracks) {
                    mAdapter.add(t);
                    mTracks.add(t);
                }
            }
            mProgress.setVisibility(View.INVISIBLE);
        }
    }
}
