package ru.avem.resonance.controllers;

import javafx.scene.shape.Circle;

import java.util.Observable;
import java.util.Observer;

public class DeviceState implements Observer {

    protected Circle deviceStateCirclePR200;


    @Override
    public void update(Observable o, Object values) {
        int modelId = (int) (((Object[]) values)[0]);
        int param = (int) (((Object[]) values)[1]);
        Object value = (((Object[]) values)[2]);

    }
}
