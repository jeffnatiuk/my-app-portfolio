package com.jeffnatiuk.spotifystreamer.spotifystreamer;

/**
 * Created by jeffn_000 on 9/16/2015.
 */
public interface DialogInterface {
    public void startService();
    public void stopService();
    public void playPause();
    public void previousSong();
    public void nextSong();
}
