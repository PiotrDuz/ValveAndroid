package com.example.dzawor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static com.example.dzawor.PreferenceKeys.*;

public class MainActivity extends AppCompatActivity {
    private static final int MAX_VALVE_CLICKS = 10;
    private final MqttHandler mqttHandler = MqttHandler.getInstance();

    private EditText tempText;
    private EditText positionText;
    private EditText timeText;
    private EditText valve;
    private Button valveButton;
    private Button settingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupControls();
        connectToMqtt();
        setButtonValveListener();
        setSettingsButtonListener();
    }

    private void connectToMqtt() {
        MqttMessageHandler telemetryHandler = new MqttMessageHandler(this::drawTelemetry);
        SharedPreferences prefs = getSharedPreferences(ADAFRUIT_PREFERENCES, MODE_PRIVATE);
        String name = prefs.getString(LOGIN, "NoLogin");
        mqttHandler.connect(telemetryHandler,
                name,
                prefs.getString(KEY, "NoKey").toCharArray());
        mqttHandler.initTelemetryData(name);
    }

    private void setupControls() {
        tempText = (EditText) findViewById(R.id.getTemp);
        positionText = (EditText) findViewById(R.id.getValve);
        timeText = (EditText) findViewById(R.id.getTime);
        valve = (EditText) findViewById(R.id.setValve);
        valveButton = (Button) findViewById(R.id.acceptSetValve);
        settingsButton = (Button) findViewById(R.id.configure);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void startSettingsActivity() {
        Intent intent = new Intent(this, UserData.class);
        startActivity(intent);
    }

    private void drawTelemetry(MqttMessageHandler.Telemetry telemetry) {
        String temp = String.format("%3.0f", telemetry.getTemp());
        String position = String.format("%3.0f", telemetry.getPosition());
        String date = telemetry.getDate().format(DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss",
                Locale.getDefault()));
        tempText.setText(temp);
        positionText.setText(position);
        timeText.setText(date);
    }

    private void setSettingsButtonListener() {
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSettingsActivity();
            }
        });
    }

    private void setButtonValveListener() {
        SharedPreferences sharedPreferences = getSharedPreferences(ADAFRUIT_PREFERENCES, MODE_PRIVATE);
        String login = sharedPreferences.getString(LOGIN, "NoLogin");
        ClickCounterPerMinute counter = new ClickCounterPerMinute(MAX_VALVE_CLICKS);
        valveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Editable textEdit = valve.getText();
                String text = textEdit.toString();
                if (text.isEmpty() || !counter.clickNextAndValidate()) {
                    return;
                }
                textEdit.clear();
                Double inputNumber = Double.parseDouble(text);
                mqttHandler.publish(login, narrowNumber(inputNumber), ZonedDateTime.now());
            }
        });
    }

    private double narrowNumber(double number) {
        double narrowedNumber = number;
        if (number < 0) {
            narrowedNumber = 0;
        }
        if (number > 100) {
            narrowedNumber = 100;
        }
        return narrowedNumber;
    }
}