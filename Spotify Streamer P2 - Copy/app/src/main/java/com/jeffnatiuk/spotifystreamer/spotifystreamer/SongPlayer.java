package com.jeffnatiuk.spotifystreamer.spotifystreamer;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Timer;

import kaaes.spotify.webapi.android.models.Track;

public class SongPlayer extends AppCompatActivity {
    private static final String ACTION_PLAY = "com.jeffnatiuk.spotifystreamer.action.PLAY";
    private MusicService musicService;
    private boolean isBound = false;
    private Intent svcIntent;
    private ArrayList<Track> topTenTracks;
    private int selectedTrack;
    private Timer songPositionUpdate;
    private Handler handler;
    private Runnable runnable;
    private boolean isTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        isTwoPane = intent.getBooleanExtra(getString(R.string.intent_extra_is_two_pane), false);

        if(!isTwoPane) {
            setContentView(R.layout.activity_song_player);
            final TextView tv_artistName = (TextView) findViewById(R.id.lbl_artistName);
            final TextView tv_albumName = (TextView) findViewById(R.id.lbl_albumName);
            final ImageView iv_albumImage = (ImageView) findViewById(R.id.img_album_art);
            final TextView tv_songTitle = (TextView) findViewById(R.id.txt_song_title);
            final TextView tv_songPosition = (TextView) findViewById(R.id.txt_song_position);
            final SeekBar songPosition = (SeekBar) findViewById(R.id.seek_song_position);
            final TextView tv_songDuration = (TextView) findViewById(R.id.txt_song_end);
            final ImageButton playPause = (ImageButton) findViewById(R.id.btn_media_play_pause);

            topTenTracks = intent.getParcelableArrayListExtra(
                    getResources().getString((R.string.intent_extra_TopTenTracks))
            );
            selectedTrack = intent.getIntExtra(
                    getResources().getString(R.string.intent_extra_selected_track), 0
            );

            svcIntent = new Intent(getApplicationContext(), MusicService.class);
            svcIntent.putParcelableArrayListExtra(
                    getResources().getString((R.string.intent_extra_TopTenTracks)),
                    topTenTracks
            );

            svcIntent.putExtra(
                    getResources().getString(R.string.intent_extra_selected_track),
                    selectedTrack
            );

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
                bindService(svcIntent, musicConnection, Context.BIND_AUTO_CREATE);
                startService(svcIntent);
                playPause.setImageResource(android.R.drawable.ic_media_pause);
            }

            runnable = new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
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

            ImageButton nextSong = (ImageButton) findViewById(R.id.btn_media_next);
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
                            Picasso.with(getApplicationContext()).load(currentTrack.album.images.get(0).url).into(iv_albumImage);
                        }

                        String songTitle = currentTrack.name;
                        tv_songTitle.setText(songTitle);
                    }
                }
            });

            ImageButton prevSong = (ImageButton) findViewById(R.id.btn_media_previous);
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
                            Picasso.with(getApplicationContext()).load(currentTrack.album.images.get(0).url).into(iv_albumImage);
                        }

                        String songTitle = currentTrack.name;
                        tv_songTitle.setText(songTitle);
                    }
                }
            });
        }
        else{
            showDialog(selectedTrack, topTenTracks);
        }
    }

    private void showDialog(int currentTrack, ArrayList<Track> top10Tracks){
        FragmentManager fm = getSupportFragmentManager();
        SongPlayerDialog songPlayerDialog = new SongPlayerDialog();

        Bundle bundle = new Bundle();
        bundle.putInt(
                getResources().getString(R.string.intent_extra_selected_track)
                , currentTrack);
        bundle.putParcelableArrayList(
                getResources().getString(R.string.intent_extra_TopTenTracks)
                , top10Tracks);

        songPlayerDialog.setArguments(bundle);
        songPlayerDialog.show(fm, "fragment_music_player");
    }

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            musicService = binder.getService();

            //musicService.start(topTenTracks, selectedTrack);
            musicService.play();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }

    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_song_player, menu);
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
    protected void onDestroy() {
        super.onDestroy();
            //stopService(svcIntent);
            unbindService(musicConnection);
            musicService = null;
            handler.removeCallbacks(runnable);
    }
}
