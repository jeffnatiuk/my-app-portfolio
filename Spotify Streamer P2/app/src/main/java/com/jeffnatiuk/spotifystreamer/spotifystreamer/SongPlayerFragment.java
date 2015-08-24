package com.jeffnatiuk.spotifystreamer.spotifystreamer;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Track;

/**
 * A placeholder fragment containing a simple view.
 */
public class SongPlayerFragment extends Fragment {

    public SongPlayerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_song_player, container, false);


        Intent intent = getActivity().getIntent();
        ArrayList artistId = intent.getParcelableArrayListExtra(
                getResources().getString((R.string.intent_extra_TopTenTracks))
        );

        int i = 0;

        return rootView;
    }
}
