package com.jeffnatiuk.spotifystreamer.spotifystreamer;

import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by jeffn_000 on 7/6/2015.
 */
public class TopTenArrayAdapter extends ArrayAdapter {
    public TopTenArrayAdapter(FragmentActivity activity, List tracks) {
        super(activity, 0, tracks);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        Track track = (Track) getItem(position);
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_top10, parent, false);
        }

        ImageView albumImage = (ImageView) convertView.findViewById(R.id.list_item_album_image);
        if(track.album.images.size() != 0) {
            Picasso.with(getContext()).load(track.album.images.get(0).url).into(albumImage);
        }

        TextView songName = (TextView) convertView.findViewById(R.id.list_item_song_name);
        songName.setText(track.name);

        TextView albumName = (TextView) convertView.findViewById(R.id.list_item_album_name);
        albumName.setText(track.album.name);

        return convertView;
    }
}
