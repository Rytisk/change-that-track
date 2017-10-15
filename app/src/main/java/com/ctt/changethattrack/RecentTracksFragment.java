package com.ctt.changethattrack;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Rytis on 2017-10-16.
 */

public class RecentTracksFragment extends Fragment {

    private String mEndpoint;
    private List<Track> mRecentTracks;
    private ListView mListView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recent_tracks, container, false);

        mRecentTracks = ((MainActivity)getActivity()).getRecentTracks();

        SharedPreferences settings = getActivity().getSharedPreferences("UserInfo", 0);
        String ip = settings.getString("IP", "10.0.2.2");
        String port = settings.getString("Port", "38475");

        mEndpoint = "http://" + ip + ":" + port;
        mListView = (ListView) rootView.findViewById(R.id.recentTracks);

        final ArrayAdapter<Track> gridViewArrayAdapter = new ArrayAdapter<Track>
                (getActivity(), android.R.layout.simple_list_item_1, mRecentTracks);

        mListView.setAdapter(gridViewArrayAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Track track = (Track)parent.getItemAtPosition(position);
                HTTP task = new HTTP();
                task.execute(mEndpoint + "/?action=set_song_play&playlist=" + track.getPlaylistId() + "&song=" + track.getPosition());
            }
        });

        return rootView;
    }


}
