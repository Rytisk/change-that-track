package com.ctt.changethattrack;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private ImageButton _play;
    private ImageButton _pause;
    private ImageButton _next;
    private ImageButton _previous;
    private ImageButton _shuffle;
    private ImageButton _repeat;
    private TextView _songInfo;

    private String _endpoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _play = (ImageButton)findViewById(R.id.img_play);
        _next = (ImageButton)findViewById(R.id.img_next);
        _previous = (ImageButton)findViewById(R.id.img_prev);
        _shuffle = (ImageButton)findViewById(R.id.img_shuffle);
        _repeat = (ImageButton)findViewById(R.id.img_repeat);
        _songInfo = (TextView)findViewById(R.id.txt_song_info);

        SharedPreferences settings = getSharedPreferences("UserInfo", 0);
        String ip = settings.getString("IP", "").toString();
        String port = settings.getString("Port", "").toString();

        _endpoint = "http://" + ip + ":" + port;

        updateSongInfo();

        _play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HTTP task = new HTTP();
                task.execute(_endpoint + "/?action=player_play");
            }
        });

        _next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HTTP task = new HTTP();
                task.execute(_endpoint + "/?action=player_next");
                updateSongInfo();
            }
        });

        _previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HTTP task = new HTTP();
                task.execute(_endpoint + "/?action=player_prevous");
                updateSongInfo();
            }
        });

        _shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HTTP task = new HTTP();
                task.execute(_endpoint + "/?action=set_player_status&statusType=shuffle&value=1");
            }
        });

        _repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HTTP task = new HTTP();
                task.execute(_endpoint + "/?action=set_player_status&statusType=repeat&value=1");
            }
        });
    }

    void updateSongInfo()
    {
        HTTP task = new HTTP();
        String songInfo = null;
        try {
            String response = task.execute(_endpoint + "/?action=get_song_current").get();
            JSONObject obj = new JSONObject(response);
            songInfo = obj.getString("PlayingFileName");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        _songInfo.setText(songInfo);
    }
}
