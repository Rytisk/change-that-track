package com.ctt.changethattrack;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutionException;

/**
 * Created by Rytis on 2017-10-15.
 */

public class PlaylistActivity extends AppCompatActivity {
    private String _endpoint;
    private TabHost mTabHost;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        _endpoint = "http://192.168.1.2:38475";     //TODO: FIX settings retrieval

        mTabHost = (TabHost)this.findViewById(R.id.th_set_menu_tabhost);
        mTabHost.setup();

        for(final Playlist playlist : getPlaylistList()){
            final ListView list = new ListView(PlaylistActivity.this);
            list.setId(Integer.parseInt(playlist.getID()));
            TabHost.TabSpec ts1 = mTabHost.newTabSpec(playlist.getID());
            ts1.setIndicator(playlist.getName());
            ts1.setContent(new TabHost.TabContentFactory(){
                public View createTabContent(String tag)
                {
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(PlaylistActivity.this,android.R.layout.simple_list_item_1, getPlaylistTracks(playlist.getID()));
                    list.setAdapter(adapter);
                    return list;
                }
            });
            list.setOnItemClickListener(
                    new AdapterView.OnItemClickListener()
                    {
                        @Override
                        public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
                            HTTP task = new HTTP();
                            task.execute(_endpoint + "/?action=set_song_play&playlist=" + arg0.getId() + "&song=" + position);
                        }
                    }
            );
            mTabHost.addTab(ts1);
        }
    }

    List<Playlist> getPlaylistList() {
        HTTP task = new HTTP();
        List<Playlist> playlists = new ArrayList<Playlist>();
        try {
            String response = task.execute(_endpoint + "/?action=get_playlist_list").get();
            JSONArray arr = new JSONArray(response);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject ob = arr.getJSONObject(i);
                playlists.add(new Playlist(ob.getString("name"), ob.getString("id")));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return playlists;
    }

    String[] getPlaylistTracks(String playlistId) {
        HTTP task = new HTTP();
        List<String> tracksList = new ArrayList<String>();
        try {
            String response = task.execute(_endpoint + "/?action=get_playlist_songs&id=" + playlistId).get();
            JSONObject obj = new JSONObject(response);
            JSONArray arr = obj.getJSONArray("songs");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject ob = null;
                ob = arr.getJSONObject(i);
                tracksList.add(ob.getString("name"));
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String[] tracks = new String[tracksList.size()];
        tracks = tracksList.toArray(tracks);

        return tracks;
    }
}