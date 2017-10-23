package com.ctt.changethattrack;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TabHost;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Created by Rytis on 2017-10-15.
 */

public class PlaylistFragment extends Fragment {
    private String mEndpoint;
    private TabHost mTabHost;

    public interface OnDataPass {
        void onDataPass(Track data);
    }

    private OnDataPass dataPasser;

    class Playlist{
        private String mName;
        private String mID;

        public String getName() {
            return mName;
        }

        public String getID() {
            return mID;
        }

        public Playlist(String name, String ID) {
            this.mName = name;
            this.mID = ID;
        }
    }

    public PlaylistFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_playlist, container, false);

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

        mTabHost = (TabHost)rootView.findViewById(R.id.th_set_menu_tabhost);
        mTabHost.setup();

        setupPlaylist();

        return rootView;
    }

    void setupPlaylist(){
        for(final Playlist playlist : getPlaylistList()){
            final ListView list = new ListView(getActivity());
            list.setId(Integer.parseInt(playlist.getID()));
            TabHost.TabSpec ts1 = mTabHost.newTabSpec(playlist.getID());
            ts1.setIndicator(playlist.getName());
            ts1.setContent(new TabHost.TabContentFactory(){
                public View createTabContent(String tag)
                {
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1, getPlaylistTracks(playlist.getID()));
                    list.setAdapter(adapter);
                    return list;
                }
            });
            list.setOnItemClickListener(
                    new AdapterView.OnItemClickListener()
                    {
                        @Override
                        public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
                            try {
                                AIMPHandler.setPlayedSong(mEndpoint, arg0.getId(), position);
                                passData(new Track(position, getTrackName(), arg0.getId()));
                            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                                e.printStackTrace();
                                showError();
                            }

                        }
                    }
            );
            mTabHost.addTab(ts1);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dataPasser = (OnDataPass) context;
    }

    public void passData(Track data) {
        dataPasser.onDataPass(data);
    }

    private String getTrackName(){

        HTTP task = new HTTP();
        String name = "";
        try {
            JSONObject obj = new JSONObject(AIMPHandler.getCurrentSong(mEndpoint));
            name = obj.getString("PlayingFileName");

        } catch (InterruptedException | JSONException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            showError();
        }
        return name;
    }

    List<Playlist> getPlaylistList() {
        HTTP task = new HTTP();
        List<Playlist> playlists = new ArrayList<Playlist>();
        try {
            JSONArray arr = new JSONArray(AIMPHandler.getPlaylistList(mEndpoint));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject ob = arr.getJSONObject(i);
                playlists.add(new Playlist(ob.getString("name"), ob.getString("id")));
            }
        } catch (InterruptedException | JSONException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            showError();
        }
        return playlists;
    }

    String[] getPlaylistTracks(String playlistId) {
        HTTP task = new HTTP();
        List<String> tracksList = new ArrayList<String>();
        try {
            JSONObject obj = new JSONObject(AIMPHandler.getPlaylistSongs(mEndpoint, playlistId));
            JSONArray arr = obj.getJSONArray("songs");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject ob = null;
                ob = arr.getJSONObject(i);
                tracksList.add(ob.getString("name"));
            }

        } catch (InterruptedException | JSONException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            showError();
        }
        String[] tracks = new String[tracksList.size()];
        tracks = tracksList.toArray(tracks);

        return tracks;
    }

    void showError(){
        ((MainActivity)getActivity()).showError();
    }

    String getResourcesString(int id){
        return ((MainActivity)getActivity()).getResourcesString(id);
    }
}