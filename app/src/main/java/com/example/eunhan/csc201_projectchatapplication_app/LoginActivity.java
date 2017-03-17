package com.example.eunhan.csc201_projectchatapplication_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

/**
 * Created by EunHan on 2017-03-16.
 */

public class LoginActivity extends AppCompatActivity {
    MainActivity main;
    private EditText mUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUsername = (EditText) findViewById(R.id.usernameEdit);
    }

    public void joinChat(View view) {
        String username = mUsername.getText().toString();

        if (!isValid(username)) {
            return;
        }

        SharedPreferences sp = getSharedPreferences(main.DATASTREAM_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(main.DATASTREAM_UUID, username);
        edit.apply();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private static boolean isValid(String username) {
        return username.matches("^[a-zA-Z0-9_]+$");
    }
}
