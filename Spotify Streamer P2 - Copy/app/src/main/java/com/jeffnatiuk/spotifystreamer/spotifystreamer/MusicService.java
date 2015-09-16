package com.jeffnatiuk.spotifystreamer.spotifystreamer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import java.io.IOException;
import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Track;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener {

    private static final String ACTION_PLAY = "com.jeffnatiuk.spotifystreamer.action.PLAY";
    MediaPlayer mMediaPlayer = null;
    Track mCurrentTrack = null;
    private final IBinder musicBinder = new MusicBinder();
    private boolean isPaused = false;
    ArrayList<Track> fullTracklist = null;
    private int trackIndex = -1;
    private boolean beingRebouned = false;
    private int songEnd;

    public MusicService() {}

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    public void start(ArrayList<Track> tracks, int index){
        if(tracks != null){
            fullTracklist = tracks;
            trackIndex = index;
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(ACTION_PLAY)) {
            fullTracklist = intent.getParcelableArrayListExtra(getResources().getString(R.string.intent_extra_TopTenTracks));
            trackIndex = intent.getIntExtra(getResources().getString(R.string.intent_extra_selected_track), 0);
        }
        return 0;
    }


    public void play(){
        if(!beingRebouned) {
            if (isPaused) {
                mMediaPlayer.start();
            } else {
                mMediaPlayer.reset();
                setSong((Track) fullTracklist.get(trackIndex));
                try {
                    mMediaPlayer.setDataSource(mCurrentTrack.preview_url);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mMediaPlayer.setOnPreparedListener(this);
                mMediaPlayer.prepareAsync(); // prepare async to not block main thread
            }
            beingRebouned = false;
        }
    }

    public void next(){
        if(trackIndex < 9) {
            trackIndex++;
        }
        isPaused = false;
        play();
    }

    public void previous() {
        if(trackIndex > 0) {
            trackIndex--;
        }
        isPaused = false;
        play();
    }

    public int getTrackIndex(){
        return trackIndex;
    }

    /** Called when MediaPlayer is ready */
    public void onPrepared(MediaPlayer player) {
        player.start();
    }

    public void setSong(Track newTrack){
        mCurrentTrack = newTrack;
    }

    public void pause() {
        mMediaPlayer.pause();
        isPaused = true;
    }

    public boolean isPaused(){
        return isPaused;
    }

    public boolean isPlaying(){
        return mMediaPlayer.isPlaying();
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    public String getSongPosition() {
        int pos = mMediaPlayer.getCurrentPosition();
        int durMinutes = (pos/1000)/60;
        int durSeconds = (pos/1000)%60;
        String seconds;
        if(durSeconds < 10)
            seconds = '0' + String.valueOf(durSeconds);
        else
            seconds = String.valueOf(durSeconds);
        if (pos < 0)
            return null;
        else
            return String.valueOf(durMinutes) + ':' + seconds;
    }

    public int getSongPositionMilli(){
        return mMediaPlayer.getCurrentPosition();
    }

    public String getSongEnd() {
        int duration = mMediaPlayer.getDuration();
        int durMinutes = (duration/1000)/60;
        int durSeconds = (duration/1000)%60;

        String seconds;
        if(durSeconds < 10)
            seconds = '0' + String.valueOf(durSeconds);
        else
            seconds = String.valueOf(durSeconds);
        if (duration < 0)
            return null;
        else
            return String.valueOf(durMinutes) + ':' + seconds;

    }

    public void seek(int progress) {
        mMediaPlayer.seekTo(progress*1000);

    }

    public class MusicBinder extends Binder {
        MusicService getService(){
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        beingRebouned = true;
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMediaPlayer.stop();
        mMediaPlayer.release();
    }
}
