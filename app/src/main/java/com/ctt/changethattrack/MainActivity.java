package com.ctt.changethattrack;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Console;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private ImageButton _btnPlayPause;
    private ImageButton _btnNext;
    private ImageButton _btnPrevious;
    private ImageButton _btnShuffle;
    private ImageButton _btnRepeat;
    private TextView _songInfo;
    private SeekBar _seekBar;

    private String _endpoint;
    private AudioManager _audioManager;

    private int _currentPlayPauseTag;
    private boolean _currentShuffleTag;
    private boolean _currentRepeatTag;

    private int _currentTrackLength;
    private int _maxVolume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _btnPlayPause = (ImageButton)findViewById(R.id.btnPlayPause);
        _btnNext = (ImageButton)findViewById(R.id.btnNext);
        _btnPrevious = (ImageButton)findViewById(R.id.btnPrev);
        _btnShuffle = (ImageButton)findViewById(R.id.btnShuffle);
        _btnRepeat = (ImageButton)findViewById(R.id.btnRepeat);
        _songInfo = (TextView)findViewById(R.id.txt_song_info);
        _seekBar = (SeekBar)findViewById(R.id.seek_bar);

        _audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        _maxVolume = _audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        SharedPreferences settings = getSharedPreferences("UserInfo", 0);
        String ip = settings.getString("IP", "10.0.2.2").toString();
        String port = settings.getString("Port", "38475").toString();
        boolean autoLogin   = settings.getBoolean("AutoLogin", false);


        if(autoLogin) {
            _endpoint = "http://" + ip + ":" + port;
            try {
                //ping the player to check if its working.
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Intent i = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(i);
            //should not check autoLogin after this step, and just proceed. TODO: FIX life cycles
        }

        updateSongInfo();
        updateStatuses();

        _btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HTTP task = new HTTP();
                if (_currentPlayPauseTag == R.drawable.ic_play){
                    task.execute(_endpoint + "/?action=player_play");
                    updatePlayPauseButton("1");
                } else {
                    task.execute(_endpoint + "/?action=player_pause");
                    updatePlayPauseButton("2");
                }
            }
        });

        _btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HTTP task = new HTTP();
                task.execute(_endpoint + "/?action=player_next");
                updateSongInfo();
            }
        });

        _btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HTTP task = new HTTP();
                task.execute(_endpoint + "/?action=player_prevous");
                updateSongInfo();
            }
        });

        _btnShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HTTP task = new HTTP();
                task.execute(_endpoint + "/?action=set_player_status&statusType=shuffle&value=" + (_currentShuffleTag ? "0" : "1"));
                _currentShuffleTag = !_currentShuffleTag;
            }
        });

        _btnRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HTTP task = new HTTP();
                task.execute(_endpoint + "/?action=set_player_status&statusType=repeat&value=" + (_currentRepeatTag ? "0" : "1"));
                _currentRepeatTag = !_currentRepeatTag;
            }
        });

        final Handler mHandler = new Handler();
        //Make sure you update Seek bar on UI thread
        MainActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                int mCurrentPosition = getTrackPosition();
                if(mCurrentPosition == 0){
                    updateSongInfo();
                }
                _seekBar.setProgress(mCurrentPosition);
                mHandler.postDelayed(this, 1000);
            }
        });

        _seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
    }

    void updateSongInfo() {
        HTTP task = new HTTP();
        String songInfo = null;
        try {
            String response = task.execute(_endpoint + "/?action=get_song_current").get();
            JSONObject obj = new JSONObject(response);
            songInfo = obj.getString("PlayingFileName");
            _currentTrackLength = Integer.parseInt(obj.getString("length"));
            _seekBar.setMax(_currentTrackLength);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        _songInfo.setText(songInfo);
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
                //TODO: set image
                _currentShuffleTag = false;
                break;
            case "1":
                //TODO: set image
                _currentShuffleTag = true;
                break;
        }
    }

    void updateRepeatStatus(String repeatStatus) {
        switch(repeatStatus){
            case "0":
                //TODO: set image
                _currentRepeatTag = false;
                break;
            case "1":
                //TODO: set image
                _currentRepeatTag = true;
                break;
        }
    }

    void updatePlayPauseButton(String playerStatus) {
        switch(playerStatus){
            case "0":
                _btnPlayPause.setImageResource(R.drawable.ic_play);
                _currentPlayPauseTag = R.drawable.ic_play;
                break;
            case "1":
                _btnPlayPause.setImageResource(R.drawable.ic_pause);
                _currentPlayPauseTag = R.drawable.ic_pause;
                break;
            case "2":
                _btnPlayPause.setImageResource(R.drawable.ic_play);
                _currentPlayPauseTag = R.drawable.ic_play;
                break;
        }
    }

    String getCustomStatus(String statusCode) {
        HTTP task = new HTTP();
        String response = "";
        try {
            response = task.execute(_endpoint + "/?action=get_custom_status&status=" + statusCode).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return response;
    }

    void setCustomStatus(String statusCode, String value){
        HTTP task = new HTTP();
        task.execute(_endpoint + "/?action=set_custom_status&status=" + statusCode + "&value=" + value);
    }

    void setTrackPosition(int position){
        HTTP task = new HTTP();
        task.execute(_endpoint + "/?action=set_track_position&position=" + Integer.toString(position));
    }

    int getTrackPosition(){
        HTTP task = new HTTP();
        int position = 0;
        try {
            String response = task.execute(_endpoint + "/?action=get_track_position").get();
            JSONObject obj = new JSONObject(response);
            position = obj.getInt("position");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return position;
    }

    void setVolume(int volume){
        HTTP task = new HTTP();
        System.out.println("VOLUME: " + volume);
        task.execute(_endpoint + "/?action=set_volume&volume=" + Integer.toString(volume));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                _audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE, 0);
                int currentVolume = _audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                setVolume(Math.round((currentVolume/((float)_maxVolume))*100));
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                _audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_LOWER, 0);
                int currentVolumeDown = _audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                setVolume(Math.round((currentVolumeDown/((float)_maxVolume))*100));
                return true;
            default:
                return false;
        }
    }

}
