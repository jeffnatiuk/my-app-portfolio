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

/**
 * Created by jeffn_000 on 7/6/2015.
 */
public class ArtistArrayAdapter extends ArrayAdapter {
    public ArtistArrayAdapter(FragmentActivity activity, List artists) {
        super(activity, 0, artists);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        Artist artist = (Artist) getItem(position);
        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_artist, parent, false);

        ImageView artistImage = (ImageView) rootView.findViewById(R.id.list_item_artist_image);
        if(artist.images.size() != 0) {
            Picasso.with(getContext()).load(artist.images.get(0).url).into(artistImage);
        }

        TextView artistName = (TextView) rootView.findViewById(R.id.list_item_artist_name);
        artistName.setText(artist.name);

        return rootView;
    }
}
