package com.ctt.changethattrack;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class PlayerFragment extends Fragment {

    private ImageButton mBtnPlayPause;
    private ImageButton mBtnNext;
    private ImageButton mBtnPrevious;
    private ImageButton mBtnShuffle;
    private ImageButton mBtnRepeat;
    private TextView mSongInfo;
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

        SharedPreferences settings = getActivity().getSharedPreferences("UserInfo", 0);
        String ip = settings.getString("IP", "10.0.2.2");
        String port = settings.getString("Port", "38475");

        mEndpoint = "http://" + ip + ":" + port;

        updateSongInfo();
        updateStatuses();

        mBtnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HTTP task = new HTTP();
                if (mCurrentPlayPauseTag == R.drawable.ic_play){
                    task.execute(mEndpoint + "/?action=player_play");
                    updatePlayPauseButton("1");
                } else {
                    task.execute(mEndpoint + "/?action=player_pause");
                    updatePlayPauseButton("2");
                }
            }
        });

        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HTTP task = new HTTP();
                task.execute(mEndpoint + "/?action=player_next");
            }
        });

        mBtnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HTTP task = new HTTP();
                task.execute(mEndpoint + "/?action=player_prevous");
            }
        });

        mBtnShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HTTP task = new HTTP();
                task.execute(mEndpoint + "/?action=set_player_status&statusType=shuffle&value=" + (mCurrentShuffleTag ? "0" : "1"));
                mCurrentShuffleTag = !mCurrentShuffleTag;
            }
        });

        mBtnRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HTTP task = new HTTP();
                task.execute(mEndpoint + "/?action=set_player_status&statusType=repeat&value=" + (mCurrentRepeatTag ? "0" : "1"));
                mCurrentRepeatTag = !mCurrentRepeatTag;
            }
        });

        final Handler mHandler = new Handler();

        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                int mCurrentPosition = getTrackPosition();
                if(mCurrentPosition == 0){
                    updateSongInfo();
                }
                mSeekBar.setProgress(mCurrentPosition);
                mHandler.postDelayed(this, 1000);
            }
        });

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
                    setTrackPosition(progress);
                }
            }
        });
        return rootView;
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
        HTTP task = new HTTP();
        String songInfo = null;
        try {
            String response = task.execute(mEndpoint + "/?action=get_song_current").get();
            JSONObject obj = new JSONObject(response);
            songInfo = obj.getString("PlayingFileName");
            mSeekBar.setMax(Integer.parseInt(obj.getString("length")));
            mSeekBar.setProgress(0);
            passData(new Track(obj.getInt("PlayingFile"), songInfo, obj.getInt("PlayingList")));
        } catch (InterruptedException | JSONException | ExecutionException e) {
            e.printStackTrace();
        }

        mSongInfo.setText(songInfo);
    }

    void updateStatuses() {
        String playerStatus = getCustomStatus("4");
        String shuffleStatus = getCustomStatus("41");
        String repeatStatus = getCustomStatus("29");

        updatePlayPauseButton(playerStatus);
        updateShuffleStatus(shuffleStatus);
        updateRepeatStatus(repeatStatus);
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

    String getCustomStatus(String statusCode) {
        HTTP task = new HTTP();
        String response = "";
        try {
            response = task.execute(mEndpoint + "/?action=get_custom_status&status=" + statusCode).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return response;
    }

    void setCustomStatus(String statusCode, String value){
        HTTP task = new HTTP();
        task.execute(mEndpoint + "/?action=set_custom_status&status=" + statusCode + "&value=" + value);
    }

    void setTrackPosition(int position){
        HTTP task = new HTTP();
        task.execute(mEndpoint + "/?action=set_track_position&position=" + Integer.toString(position));
    }

    int getTrackPosition(){
        HTTP task = new HTTP();
        int position = 0;
        try {
            String response = task.execute(mEndpoint + "/?action=get_track_position").get();
            JSONObject obj = new JSONObject(response);
            position = obj.getInt("position");
        } catch (InterruptedException | JSONException | ExecutionException e) {
            e.printStackTrace();
        }
        return position;
    }


}
