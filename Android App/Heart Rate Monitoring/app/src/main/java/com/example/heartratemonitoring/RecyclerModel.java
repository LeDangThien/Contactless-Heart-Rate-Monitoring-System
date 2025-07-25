package com.example.heartratemonitoring;

import java.util.Map;

public class RecyclerModel {
    private Object bodySignal, distance, radarHR, realHR, respRate;

    public RecyclerModel() {
    }

    public RecyclerModel(Object bodySignal, Object distance, Object radarHR, Object realHR, Object respRate) {
        this.bodySignal = bodySignal;
        this.distance = distance;
        this.radarHR = radarHR;
        this.realHR = realHR;
        this.respRate = respRate;
    }

    public Object getBodySignal() {
        return bodySignal;
    }

    public void setBodySignal(Object bodySignal) {
        this.bodySignal = bodySignal;
    }

    public Object getDistance() {
        return distance;
    }

    public void setDistance(Object distance) {
        this.distance = distance;
    }

    public Object getRadarHR() {
        return radarHR;
    }

    public void setRadarHR(Object radarHR) {
        this.radarHR = radarHR;
    }

    public Object getRealHR() {
        return realHR;
    }

    public void setRealHR(Object realHR) {
        this.realHR = realHR;
    }

    public Object getRespRate() {
        return respRate;
    }

    public void setRespRate(Object respRate) {
        this.respRate = respRate;
    }
}
