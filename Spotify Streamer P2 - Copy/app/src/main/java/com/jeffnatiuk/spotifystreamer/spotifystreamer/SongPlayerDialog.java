package com.jeffnatiuk.spotifystreamer.spotifystreamer;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Timer;

import kaaes.spotify.webapi.android.models.Track;


/**
 * A simple {@link Fragment} subclass.
 */
public class SongPlayerDialog extends DialogFragment {

    private static final String ACTION_PLAY = "com.jeffnatiuk.spotifystreamer.action.PLAY";
    private MusicService musicService;
    private boolean isBound = false;
    private Intent svcIntent;
    private ArrayList<Track> topTenTracks;
    private int selectedTrack;
    private Timer songPositionUpdate;
    private Handler handler;
    private Runnable runnable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    Activity activity;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity=activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_song_player, container, false);

        Bundle bundle = getArguments();

        selectedTrack = bundle.getInt(getResources().getString(R.string.intent_extra_selected_track));
        topTenTracks = bundle.getParcelableArrayList(getResources().getString(R.string.intent_extra_TopTenTracks));

        final TextView tv_artistName = (TextView) rootView.findViewById(R.id.lbl_artistName);
        final TextView tv_albumName = (TextView) rootView.findViewById(R.id.lbl_albumName);
        final ImageView iv_albumImage = (ImageView) rootView.findViewById(R.id.img_album_art);
        final TextView tv_songTitle = (TextView) rootView.findViewById(R.id.txt_song_title);
        final TextView tv_songPosition = (TextView) rootView.findViewById(R.id.txt_song_position);
        final SeekBar songPosition = (SeekBar) rootView.findViewById(R.id.seek_song_position);
        final TextView tv_songDuration = (TextView) rootView.findViewById(R.id.txt_song_end);
        final ImageButton playPause = (ImageButton) rootView.findViewById(R.id.btn_media_play_pause);

        svcIntent = new Intent(getActivity(), MusicService.class);
        svcIntent.putParcelableArrayListExtra(
                getResources().getString((R.string.intent_extra_TopTenTracks)),
                topTenTracks
        );

        svcIntent.putExtra(
                getResources().getString(R.string.intent_extra_selected_track),
                selectedTrack
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

        songPosition.setMax(0);
        songPosition.setMax(30);

        songPosition.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            int prog = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prog = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                musicService.seek(prog);
            }
        });

        svcIntent.setAction(ACTION_PLAY);


        if (!isBound ) {
            getActivity().bindService(svcIntent, musicConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(svcIntent);
            playPause.setImageResource(android.R.drawable.ic_media_pause);
        }

        runnable = new Runnable() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (musicService != null) {
                            if (musicService.getSongPosition() != null && musicService.getSongEnd() != null) {
                                tv_songPosition.setText(musicService.getSongPosition());
                                tv_songDuration.setText(musicService.getSongEnd());
                                songPosition.setProgress(musicService.getSongPositionMilli() / 1000);
                            }
                        }
                    }
                });
                handler.postDelayed(runnable, 1000);
            }
        };

        handler = new Handler();
        handler.postDelayed(runnable, 1000);

        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicService.isPlaying()) {
                    musicService.pause();
                    playPause.setImageResource(android.R.drawable.ic_media_play);
                } else {
                    musicService.play();
                    playPause.setImageResource(android.R.drawable.ic_media_pause);
                }
            }
        });

        ImageButton nextSong = (ImageButton) rootView.findViewById(R.id.btn_media_next);
        nextSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicService.getTrackIndex() < 9) {
                    playPause.setImageResource(android.R.drawable.ic_media_pause);
                    musicService.next();

                    Track currentTrack = topTenTracks.get(musicService.getTrackIndex());
                    String artistName = currentTrack.artists.get(0).name;
                    tv_artistName.setText(artistName);

                    String albumName = currentTrack.album.name;
                    tv_albumName.setText(albumName);

                    if (currentTrack.album.images.size() != 0) {
                        Picasso.with(getActivity().getApplicationContext()).load(currentTrack.album.images.get(0).url).into(iv_albumImage);
                    }

                    String songTitle = currentTrack.name;
                    tv_songTitle.setText(songTitle);
                }
            }
        });

        ImageButton prevSong = (ImageButton) rootView.findViewById(R.id.btn_media_previous);
        prevSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicService.getTrackIndex() > 0) {
                    playPause.setImageResource(android.R.drawable.ic_media_pause);
                    musicService.previous();

                    Track currentTrack = topTenTracks.get(musicService.getTrackIndex());
                    String artistName = currentTrack.artists.get(0).name;
                    tv_artistName.setText(artistName);

                    String albumName = currentTrack.album.name;
                    tv_albumName.setText(albumName);

                    if (currentTrack.album.images.size() != 0) {
                        Picasso.with(getActivity().getApplicationContext()).load(currentTrack.album.images.get(0).url).into(iv_albumImage);
                    }

                    String songTitle = currentTrack.name;
                    tv_songTitle.setText(songTitle);
                }
            }
        });

        return rootView;
    }

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            musicService = binder.getService();

            musicService.start(topTenTracks, selectedTrack);
            musicService.play();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }

    };

    @Override
    public void onStop() {
        super.onStop();
        //getActivity().unbindService(musicConnection);

    }

    @Override
    public void onDestroy() {
        musicService=null;
        handler.removeCallbacks(runnable);
        super.onDestroy();
    }

    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setOnDismissListener(null);
        super.onDestroyView();
    }

}
