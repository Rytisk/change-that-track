package com.ctt.changethattrack;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private ImageView _play;
    private ImageView _pause;
    private ImageView _next;
    private ImageView _previous;
    private ImageView _shuffle;
    private ImageView _repeat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _play = (ImageView)findViewById(R.id.img_play);
        _next = (ImageView)findViewById(R.id.img_next);
        _previous = (ImageView)findViewById(R.id.img_prev);
        _shuffle = (ImageView)findViewById(R.id.img_shuffle);
        _repeat = (ImageView)findViewById(R.id.img_repeat);

        _play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HTTP task = new HTTP();
                task.execute("http://10.0.2.2:38475/?action=player_play");
            }
        });

        _next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HTTP task = new HTTP();
                task.execute("http://10.0.2.2:38475/?action=player_next");
            }
        });

        _previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HTTP task = new HTTP();
                task.execute("http://10.0.2.2:38475/?action=player_prevous");
            }
        });

        _shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HTTP task = new HTTP();
                task.execute("http://10.0.2.2:38475/?action=set_player_status&statusType=shuffle&value=1");
            }
        });

        _repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HTTP task = new HTTP();
                task.execute("http://10.0.2.2:38475/?action=set_player_status&statusType=repeat&value=1");
            }
        });

    }
}
