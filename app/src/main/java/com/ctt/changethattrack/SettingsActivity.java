package com.ctt.changethattrack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

/**
 * Created by Acer on 2017-10-07.
 */

public class SettingsActivity extends AppCompatActivity {

    private EditText _serverIP;
    private EditText _port;
    private CheckBox _autoLogin;
    private Button _connect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        _serverIP = (EditText)findViewById(R.id.inServerIP);
        _port = (EditText)findViewById(R.id.inPort);
        _autoLogin = (CheckBox)findViewById(R.id.checkBox);

        _connect = (Button)findViewById(R.id.btnConnect);

        _connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences settings = getSharedPreferences("UserInfo", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("IP",_serverIP.getText().toString());
                editor.putString("Port",_port.getText().toString());
                editor.putBoolean("AutoLogin", _autoLogin.isChecked());
                editor.commit();

                Intent i = new Intent(SettingsActivity.this, MainActivity.class);
                startActivity(i);
            }
        });
    }
}
