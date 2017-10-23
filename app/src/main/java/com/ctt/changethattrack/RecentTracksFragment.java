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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

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

        SharedPreferences settings = getActivity().getSharedPreferences(getResourcesString(R.string.setting_user_info), 0);
        String ip = settings.getString(getResourcesString(R.string.setting_ip), getResourcesString(R.string.default_ip));
        String port = settings.getString(getResourcesString(R.string.setting_port), getResourcesString(R.string.default_port));
        mEndpoint = "http://" + ip + ":" + port;

        if(!AIMPHandler.ping(mEndpoint)){
            showError();
            getActivity().getFragmentManager().beginTransaction()
                    .replace(R.id.content_frame,new SettingsFragment()).commit();
            return rootView;
        }
        mListView = (ListView) rootView.findViewById(R.id.recentTracks);

        final ArrayAdapter<Track> gridViewArrayAdapter = new ArrayAdapter<Track>
                (getActivity(), android.R.layout.simple_list_item_1, mRecentTracks);

        mListView.setAdapter(gridViewArrayAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Track track = (Track)parent.getItemAtPosition(position);
                try {
                    AIMPHandler.setPlayedSong(mEndpoint, track.getPlaylistId(), track.getPosition());
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    e.printStackTrace();
                    showError();
                }
            }
        });

        return rootView;
    }

    void showError(){
        ((MainActivity)getActivity()).showError();
    }

    String getResourcesString(int id){
        return ((MainActivity)getActivity()).getResourcesString(id);
    }
}
