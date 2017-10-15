package com.ctt.changethattrack;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.View;
import android.app.Fragment;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Rytis on 2017-10-15.
 */

public class MainActivity extends Activity implements PlaylistFragment.OnDataPass{
    private String[] mFragments;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    private String mEndpoint;

    private List<Track> mRecentTracks = new ArrayList<Track>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFragments = getResources().getStringArray(R.array.fragments_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mFragments));

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        SharedPreferences settings = getSharedPreferences("UserInfo", 0);
        boolean autoLogin = settings.getBoolean("AutoLogin", false);
        if(autoLogin) {
            String ip = settings.getString("IP", "10.0.2.2");
            String port = settings.getString("Port", "38475");

            mEndpoint = "http://" + ip + ":" + port;
            selectItem(0);
        }
        else {
            selectItem(1);
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        Fragment fragment = null;

        switch(position){
            case 0:
                fragment = new PlayerFragment();
                break;
            case 1:
                fragment = new SettingsFragment();
                break;
            case 2:
                fragment = new PlaylistFragment();
                break;
            case 3:
                fragment = new RecentTracksFragment();
                break;
        }

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment).addToBackStack(null)
                .commit();

        mDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void onBackPressed() {

        int count = getFragmentManager().getBackStackEntryCount();

        if (count == 1) {
            getFragmentManager().popBackStack();
            super.onBackPressed();
            //additional code
        } else {
            getFragmentManager().popBackStack();
        }

    }

    void setVolume(int volume){
        HTTP task = new HTTP();
        task.execute(mEndpoint + "/?action=set_volume&volume=" + Integer.toString(volume));
    }

    int getVolume(){
        HTTP task = new HTTP();
        int volume = 0;
        try {
            volume = Integer.parseInt(task.execute(mEndpoint + "/?action=get_volume").get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return volume;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                int volUp = getVolume();
                if (volUp + 10 <= 100)
                    setVolume(volUp + 10);
                else if (volUp > 90)
                    setVolume(100);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                int volDown = getVolume();
                if (volDown - 10 >= 0)
                    setVolume(volDown - 10);
                else if (volDown < 10)
                    setVolume(0);
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onDataPass(Track data) {
        if(mRecentTracks.contains(data)){
            int i = mRecentTracks.indexOf(data);
            mRecentTracks.remove(i);
        } else if(mRecentTracks.size() == 25){
            mRecentTracks.remove(0);
        }
        mRecentTracks.add(data);
    }

    public List<Track> getRecentTracks(){
        return mRecentTracks;
    }


}