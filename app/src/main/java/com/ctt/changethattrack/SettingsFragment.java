package com.ctt.changethattrack;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Acer on 2017-10-07.
 */

public class SettingsFragment extends Fragment {

    private EditText mTxtServerIP;
    private EditText mTxtPort;
    private CheckBox mChbAutoLogin;
    private Button mBtnSave;

    public SettingsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        mTxtServerIP = (EditText)rootView.findViewById(R.id.inServerIP);
        mTxtPort = (EditText)rootView.findViewById(R.id.inPort);
        mChbAutoLogin = (CheckBox)rootView.findViewById(R.id.checkBox);
        mBtnSave = (Button)rootView.findViewById(R.id.btnSave);

        SharedPreferences settings = getActivity().getSharedPreferences(getResourcesString(R.string.setting_user_info), 0);
        String ip = settings.getString(getResourcesString(R.string.setting_ip), getResourcesString(R.string.default_ip));
        String port = settings.getString(getResourcesString(R.string.setting_port), getResourcesString(R.string.default_port));

        boolean autoLogin = settings.getBoolean(getResourcesString(R.string.setting_auto_login), false);

        mTxtServerIP.setText(ip);
        mTxtPort.setText(port);
        mChbAutoLogin.setChecked(autoLogin);


        mBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences settings = getActivity().getSharedPreferences("UserInfo", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("IP", mTxtServerIP.getText().toString());
                editor.putString("Port", mTxtPort.getText().toString());
                editor.putBoolean("AutoLogin", mChbAutoLogin.isChecked());
                editor.commit();

                getActivity().getFragmentManager().beginTransaction()
                        .replace(R.id.content_frame,new PlayerFragment()).commit();
            }
        });
        return rootView;
    }

    String getResourcesString(int id){
        return ((MainActivity)getActivity()).getResourcesString(id);
    }
}
