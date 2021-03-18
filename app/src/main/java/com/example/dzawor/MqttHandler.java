package com.example.dzawor;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class MqttHandler {
    static final String telemetryTopicSuffix = "/feeds/telemetry";
    static final String commandTopicSuffix = "/feeds/command";
    static final String getSuffix = "/get";
    private static final MqttHandler instance = new MqttHandler();
    private final MqttClient client;

    private MqttHandler() {
        MqttClient client = null;
        try {
            client = new MqttClient(
                    "ssl://io.adafruit.com:8883", //URI
                    MqttClient.generateClientId(), //ClientId
                    new MemoryPersistence()); //Persistence
        } catch (MqttException e) {
            e.printStackTrace();
        } finally {
            this.client = client;
        }
    }

    public static MqttHandler getInstance() {
        return instance;
    }

    private static MqttConnectOptions setUpConnectionOptions(String username, char[] pas) {
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setKeepAliveInterval(80);
        connOpts.setAutomaticReconnect(true);
        connOpts.setCleanSession(true);
        connOpts.setUserName(username);
        connOpts.setPassword(pas);
        return connOpts;
    }

    public void publish(String name, Double value, ZonedDateTime date){
        String messageText = value.toString() + ";" + date.format(DateTimeFormatter.ISO_INSTANT);
        MqttMessage message = new MqttMessage(messageText.getBytes(StandardCharsets.UTF_8));
        try {
            client.publish(getCommandTopic(name), message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void initTelemetryData(String name){
        MqttMessage emptyMessage = new MqttMessage("none".getBytes(StandardCharsets.UTF_8));
        try {
            client.publish(getTelemetryTopic(name) + getSuffix, emptyMessage);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void connect(MqttCallback callback, String username, char[] pas) {
        client.setCallback(callback);
        try {
            client.connect(setUpConnectionOptions(username, pas));
            client.subscribe(getTelemetryTopic(username));
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private String getCommandTopic(String name){
        return name + commandTopicSuffix;
    }

    private String getTelemetryTopic(String name){
        return name + telemetryTopicSuffix;
    }
}
