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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Created by Rytis on 2017-10-15.
 */

public class MainActivity extends Activity implements PlaylistFragment.OnDataPass{
    private String[] mFragments;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private Toast mToast;
    private String mEndpoint;
    private List<Track> mRecentTracks = new ArrayList<Track>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFragments = getResources().getStringArray(R.array.fragments_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new ArrayAdapter<>(this,
                R.layout.drawer_list_item, mFragments));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        SharedPreferences settings = getSharedPreferences(getResourcesString(R.string.setting_user_info), 0);
        String ip = settings.getString(getResourcesString(R.string.setting_ip), getResourcesString(R.string.default_ip));
        String port = settings.getString(getResourcesString(R.string.setting_port), getResourcesString(R.string.default_port));
        mEndpoint = "http://" + ip + ":" + port;

        boolean autoLogin = settings.getBoolean(getResourcesString(R.string.setting_auto_login), false);
        if(autoLogin) {
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
        } else {
            getFragmentManager().popBackStack();
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                int volUp = 0;
                try {
                    volUp = AIMPHandler.getVolume(mEndpoint);
                    if (volUp + 10 <= 100)
                        volUp += 10;
                    else if (volUp > 90)
                        volUp = 100;
                    AIMPHandler.setVolume(mEndpoint, volUp);
                    showMessage("Volume: " + volUp, Toast.LENGTH_SHORT);
                } catch (TimeoutException | ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                    showError();
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                try {
                    int volDown = AIMPHandler.getVolume(mEndpoint);
                    if (volDown - 10 >= 0)
                        volDown -= 10;
                    else if (volDown < 10)
                        volDown = 0;
                    AIMPHandler.setVolume(mEndpoint, volDown);
                    showMessage("Volume: " + volDown, Toast.LENGTH_SHORT);
                } catch (TimeoutException | ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                    showError();
                }
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

    public void showError(){
        showMessage(getResources().getString(R.string.error), Toast.LENGTH_SHORT);
    }

    public void showMessage(String message, int duration){
        if(mToast != null) mToast.cancel();
        mToast = Toast.makeText(MainActivity.this, message, duration);
        mToast.show();
    }

    public String getResourcesString(int id){
        return getResources().getString(id);
    }

}