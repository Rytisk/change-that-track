package com.ctt.changethattrack;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PlayerFragment extends Fragment {

    private ImageButton mBtnPlayPause;
    private ImageButton mBtnNext;
    private ImageButton mBtnPrevious;
    private ImageButton mBtnShuffle;
    private ImageButton mBtnRepeat;
    private TextView mSongInfo;
    private TextView mTxtMaxPosition;
    private TextView mTxtCurrentPosition;
    private SeekBar mSeekBar;

    private String mEndpoint;

    private int mCurrentPlayPauseTag;
    private boolean mCurrentShuffleTag;
    private boolean mCurrentRepeatTag;

    private PlaylistFragment.OnDataPass dataPasser;

    public PlayerFragment() {

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player, container, false);

        mBtnNext = (ImageButton)rootView.findViewById(R.id.btnNext);
        mBtnPlayPause = (ImageButton)rootView.findViewById(R.id.btnPlayPause);
        mBtnPrevious = (ImageButton)rootView.findViewById(R.id.btnPrev);
        mBtnShuffle = (ImageButton)rootView.findViewById(R.id.btnShuffle);
        mBtnRepeat = (ImageButton)rootView.findViewById(R.id.btnRepeat);
        mSongInfo = (TextView)rootView.findViewById(R.id.txt_song_info);
        mSeekBar = (SeekBar)rootView.findViewById(R.id.seek_bar);
        mTxtMaxPosition = (TextView)rootView.findViewById(R.id.txtMaxPosition);
        mTxtCurrentPosition = (TextView)rootView.findViewById(R.id.txtCurrentPosition);

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

        updateSongInfo();
        updateStatuses();

        mBtnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    if (mCurrentPlayPauseTag == R.drawable.ic_play){
                        AIMPHandler.play(mEndpoint);
                        updatePlayPauseButton("1");
                    } else {
                        AIMPHandler.pause(mEndpoint);
                        updatePlayPauseButton("2");
                    }
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    e.printStackTrace();
                    showError();
                }
            }
        });

        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    AIMPHandler.playNext(mEndpoint);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    e.printStackTrace();
                    showError();
                }
            }
        });

        mBtnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    AIMPHandler.playPrevious(mEndpoint);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    e.printStackTrace();
                    showError();
                }
            }
        });

        mBtnShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    AIMPHandler.toggleShuffle(mEndpoint, mCurrentShuffleTag);
                    mCurrentShuffleTag = !mCurrentShuffleTag;
                    ((MainActivity)getActivity()).showMessage(mCurrentShuffleTag ? "On" : "Off", Toast.LENGTH_SHORT);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    e.printStackTrace();
                    showError();
                }
            }
        });

        mBtnRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    AIMPHandler.toggleRepeat(mEndpoint, mCurrentRepeatTag);
                    mCurrentRepeatTag = !mCurrentRepeatTag;
                    ((MainActivity)getActivity()).showMessage(mCurrentRepeatTag ? "On" : "Off", Toast.LENGTH_SHORT);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    e.printStackTrace();
                    showError();
                }
            }
        });

        setupTrackSeekBar();

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    try {
                        AIMPHandler.setTrackPosition(mEndpoint, progress);
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        e.printStackTrace();
                        showError();
                    }
                }
            }
        });
        return rootView;
    }

    void setupTrackSeekBar(){
        final Handler mHandler = new Handler();

        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                int currentPosition = 0;
                try {
                    currentPosition = AIMPHandler.getTrackPosition(mEndpoint);
                    if(currentPosition == 0){
                        updateSongInfo();
                    }
                    mSeekBar.setProgress(currentPosition);
                    mHandler.postDelayed(this, 1000);
                    mTxtCurrentPosition.setText(getTime(currentPosition));
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    e.printStackTrace();
                    showError();
                }

            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dataPasser = (PlaylistFragment.OnDataPass) context;
    }

    public void passData(Track data) {
        dataPasser.onDataPass(data);
    }

    void updateSongInfo() {
        String songInfo = null;
        try {
            JSONObject obj = new JSONObject(AIMPHandler.getSongInfo(mEndpoint));
            songInfo = obj.getString("PlayingFileName");
            int length = Integer.parseInt(obj.getString("length"));
            mSeekBar.setMax(length);
            mSeekBar.setProgress(0);
            mTxtMaxPosition.setText(getTime(length));

            passData(new Track(obj.getInt("PlayingFile"), songInfo, obj.getInt("PlayingList")));
        } catch (JSONException | InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            showError();
        }

        mSongInfo.setText(songInfo);
    }

    String getTime(int position){
        String minutes = position/60 < 10 ? "0"+Integer.toString(position/60) : Integer.toString(position/60);
        String seconds = position%60 < 10 ? "0"+Integer.toString(position%60) : Integer.toString(position%60);
        return minutes + ":" + seconds;
    }

    void updateStatuses() {
        try {
            String playerStatus = AIMPHandler.getCustomStatus(mEndpoint, "4");
            String shuffleStatus = AIMPHandler.getCustomStatus(mEndpoint, "41");
            String repeatStatus = AIMPHandler.getCustomStatus(mEndpoint, "29");

            updatePlayPauseButton(playerStatus);
            updateShuffleStatus(shuffleStatus);
            updateRepeatStatus(repeatStatus);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            showError();
        }

    }

    void updateShuffleStatus(String shuffleStatus) {
        switch(shuffleStatus){
            case "0":
                mCurrentShuffleTag = false;
                break;
            case "1":
                mCurrentShuffleTag = true;
                break;
        }
    }

    void updateRepeatStatus(String repeatStatus) {
        switch(repeatStatus){
            case "0":
                mCurrentRepeatTag = false;
                break;
            case "1":
                mCurrentRepeatTag = true;
                break;
        }
    }

    void updatePlayPauseButton(String playerStatus) {
        switch(playerStatus){
            case "0":
                mBtnPlayPause.setImageResource(R.drawable.ic_play);
                mCurrentPlayPauseTag = R.drawable.ic_play;
                break;
            case "1":
                mBtnPlayPause.setImageResource(R.drawable.ic_pause);
                mCurrentPlayPauseTag = R.drawable.ic_pause;
                break;
            case "2":
                mBtnPlayPause.setImageResource(R.drawable.ic_play);
                mCurrentPlayPauseTag = R.drawable.ic_play;
                break;
        }
    }

    void showError(){
        ((MainActivity)getActivity()).showError();
    }

    String getResourcesString(int id){
        return ((MainActivity)getActivity()).getResourcesString(id);
    }
}
