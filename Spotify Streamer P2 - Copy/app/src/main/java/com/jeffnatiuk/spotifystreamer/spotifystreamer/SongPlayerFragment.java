package com.jeffnatiuk.spotifystreamer.spotifystreamer;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Track;

/**
 * A placeholder fragment containing a simple view.
 */
public class SongPlayerFragment extends Fragment {
    private ImageButton btnPlayPause;
    private MusicService musicService;
    public SongPlayerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_song_player, container, false);

        TextView tv_artistName = (TextView)rootView.findViewById(R.id.lbl_artistName);
        TextView tv_albumName = (TextView)rootView.findViewById(R.id.lbl_albumName);
        ImageView iv_albumImage = (ImageView)rootView.findViewById(R.id.img_album_art);
        TextView tv_songTitle = (TextView)rootView.findViewById(R.id.txt_song_title);
        TextView tv_songDuration = (TextView)rootView.findViewById(R.id.txt_song_end);
        TextView tv_songPosition = (TextView)rootView.findViewById(R.id.txt_song_position);

        Intent intent = getActivity().getIntent();
        ArrayList topTenTracks = intent.getParcelableArrayListExtra(
                getResources().getString((R.string.intent_extra_TopTenTracks))
        );
        int selectedTrack = intent.getIntExtra(
                getResources().getString(R.string.intent_extra_selected_track), 0
        );

        Track currentTrack = ((Track)topTenTracks.get(selectedTrack));

        String artistName = currentTrack.artists.get(0).name;
        tv_artistName.setText(artistName);

        String albumName = currentTrack.album.name;
        tv_albumName.setText(albumName);

        if(currentTrack.album.images.size() != 0) {
            Picasso.with(rootView.getContext()).load(currentTrack.album.images.get(0).url).into(iv_albumImage);
        }

        String songTitle = currentTrack.name;
        tv_songTitle.setText(songTitle);




        return rootView;
    }

}
