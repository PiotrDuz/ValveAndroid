package com.example.dzawor;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Response;

import java.io.IOException;
import java.util.function.Consumer;

import static com.example.dzawor.PreferenceKeys.*;

public class UserData extends AppCompatActivity {
    private final HttpRequestProducer httpClient = HttpRequestProducer.getInstance();
    private EditText login;
    private EditText key;
    private TextView calibrateResponse;
    private Button accept;
    private Button calibrate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userdata);
        setupControls();
        setButtonAcceptListener();
        setButtonCalibrateListener();
    }

    private void setupControls() {
        login = findViewById(R.id.setLogin);
        key = findViewById(R.id.setKey);
        accept = findViewById(R.id.acceptUserData);
        calibrate = findViewById(R.id.calibrate);
        calibrateResponse = findViewById(R.id.calibrateResponse);
    }

    private void setButtonAcceptListener() {
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Editable loginText = login.getText();
                Editable keyText = key.getText();
                String loginStr = loginText.toString();
                String keyStr = keyText.toString();
                if (loginStr.isEmpty() || keyStr.isEmpty()) {
                    return;
                }
                loginText.clear();
                keyText.clear();
                SharedPreferences.Editor editor = getSharedPreferences(ADAFRUIT_PREFERENCES, MODE_PRIVATE).edit();
                editor.putString(LOGIN, loginStr);
                editor.putString(KEY, keyStr);
                editor.apply();
            }
        });
    }

    private void setButtonCalibrateListener() {
        calibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCalibrateRequest();
            }
        });
    }


    private void sendCalibrateRequest() {
        Consumer<IOException> failureFunction = exc -> runOnUiThread(() ->
                calibrateResponse.setText(exc.getMessage()));
        Consumer<Response> responseFunction = resp -> runOnUiThread(() -> {
            try {
                calibrateResponse.setText(resp.body().string());
            } catch (IOException e) {
                calibrateResponse.setText("IO Error");
            }
        });
        httpClient.sendCalibrate(failureFunction, responseFunction);
    }
}