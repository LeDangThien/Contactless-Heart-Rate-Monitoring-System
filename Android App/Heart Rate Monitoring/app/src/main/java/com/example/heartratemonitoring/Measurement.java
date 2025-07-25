package com.example.heartratemonitoring;

public class Measurement {
    String radar_heart_rate, respiratory_rate, body_signal, distance, real_heart_rate;

    public Measurement() {
        this.radar_heart_rate = "";
        this.respiratory_rate = "";
        this.body_signal = "";
        this.distance = "";
        this.real_heart_rate = "";
    }

    public Measurement(String radar_heart_rate, String respiratory_rate, String body_signal, String distance, String real_heart_rate) {
        this.radar_heart_rate = radar_heart_rate;
        this.respiratory_rate = respiratory_rate;
        this.body_signal = body_signal;
        this.distance = distance;
        this.real_heart_rate = real_heart_rate;
    }

    public String getRadar_heart_rate() {
        return radar_heart_rate;
    }

    public void setRadar_heart_rate(String radar_heart_rate) {
        this.radar_heart_rate = radar_heart_rate;
    }

    public String getRespiratory_rate() {
        return respiratory_rate;
    }

    public void setRespiratory_rate(String respiratory_rate) {
        this.respiratory_rate = respiratory_rate;
    }

    public String getBody_signal() {
        return body_signal;
    }

    public void setBody_signal(String body_signal) {
        this.body_signal = body_signal;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getReal_heart_rate() {
        return real_heart_rate;
    }

    public void setReal_heart_rate(String real_heart_rate) {
        this.real_heart_rate = real_heart_rate;
    }
}
