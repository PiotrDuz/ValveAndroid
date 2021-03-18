package com.example.dzawor;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.Consumer;

public class MqttMessageHandler implements MqttCallback {
    private final Consumer<Telemetry> telemetryConsumer;


    public MqttMessageHandler(Consumer<Telemetry> telemetryConsumer) {
        this.telemetryConsumer = telemetryConsumer;
    }

    @Override
    public void connectionLost(Throwable cause) {
        cause.printStackTrace();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        if (topic.contains(MqttHandler.telemetryTopicSuffix)) {
            Telemetry telemetry = new Telemetry(message.getPayload());
            telemetryConsumer.accept(telemetry);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    public static class Telemetry {
        private double temp;
        private double position;
        private ZonedDateTime date;

        public Telemetry(byte[] payloadRaw) {
            String payload = new String(payloadRaw);
            String temp = payload.split(";")[0];
            String position = payload.split(";")[1];
            String date = payload.split(";")[2];
            this.temp = Double.parseDouble(temp);
            this.position = Double.parseDouble(position);
            this.date = ZonedDateTime.ofInstant(Instant.parse(date), ZoneId.systemDefault());
        }

        public Double getTemp() {
            return temp;
        }

        public Double getPosition() {
            return position;
        }

        public ZonedDateTime getDate() {
            return date;
        }
    }
}
