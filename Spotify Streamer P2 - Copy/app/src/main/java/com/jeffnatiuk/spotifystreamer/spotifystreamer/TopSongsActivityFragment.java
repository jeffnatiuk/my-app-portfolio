package com.jeffnatiuk.spotifystreamer.spotifystreamer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

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
    private Artist selectedArtist;
    private boolean isTwoPane = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_top_songs, container, false);
        Bundle arguments = getArguments();
        //Only the two pane layout uses Arguments to pass the selected artist.
        if(arguments != null){
            selectedArtist = arguments.getParcelable("selectedArtist");
            isTwoPane = true;
        }

        if(selectedArtist == null){
            selectedArtist = new Artist();
            Intent i = new Intent();
            i = getActivity().getIntent();
            selectedArtist.id = i.getStringExtra(getResources().getString(R.string.intent_extra_ArtistId));
        }

            if (mAdapter == null) {
                mAdapter = new TopTenArrayAdapter(getActivity(), new ArrayList<Track>());
            }
            mProgress = (ProgressBar) rootView.findViewById(R.id.progress_load_songs);

            Intent intent = getActivity().getIntent();
            String artistId = selectedArtist.id;

            //Make calls to Spotify API to get top tracks by artist ID.
            if (mTracks == null) {
                mTracks = new ArrayList<>();
                FetchTopTenTask task = new FetchTopTenTask();
                task.execute(artistId);
            }

            ListView lv = (ListView) rootView.findViewById(R.id.list_view_artist_top10);
            lv.setAdapter(mAdapter);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ListView lv = (ListView) parent;
                    Track selectedTrack = (Track) lv.getItemAtPosition(position);
                    Intent intent = new Intent(getActivity(), SongPlayer.class);

                    intent.putExtra(
                            getResources().getString((R.string.intent_extra_TopTenTracks)), mTracks
                    );
                    intent.putExtra(
                            getResources().getString((R.string.intent_extra_selected_track)), position
                    );

                    intent.putExtra(
                            getResources().getString((R.string.intent_extra_selected_track_name)), selectedTrack.name
                    );

                    intent.putExtra(
                            getResources().getString((R.string.intent_extra_ArtistName)), selectedTrack.artists.get(0).name
                    );

                    intent.putExtra(
                            getResources().getString((R.string.intent_extra_is_two_pane)), isTwoPane
                    );

                    if(isTwoPane)
                        showDialog(position, mTracks);
                    else
                        startActivity(intent);
                }
            });

        return rootView;
    }


    private void showDialog(int currentTrack, ArrayList<Track> top10Tracks){
        FragmentManager fm = getActivity().getSupportFragmentManager();
        SongPlayerDialog songPlayerDialog = new SongPlayerDialog();

        Bundle bundle = new Bundle();
        bundle.putInt(getResources().getString(R.string.intent_extra_selected_track), currentTrack);
        bundle.putParcelableArrayList(getResources().getString(R.string.intent_extra_TopTenTracks), top10Tracks);

        songPlayerDialog.setArguments(bundle);
        songPlayerDialog.show(fm, "fragment_music_player");
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
                results = spotify.getArtistTopTrack(artistId, "US");
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
                if(tracks.tracks.size() == 0)
                {
                    Toast.makeText(getActivity(), "No songs for given artist found.  Please refine search.", Toast.LENGTH_SHORT).show();
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
