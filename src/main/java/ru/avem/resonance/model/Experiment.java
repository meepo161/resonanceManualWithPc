package ru.avem.resonance.model;

public class Experiment {
    private String resourceLocation;
    private String title;

    Experiment(String resourceLocation, String title) {
        this.resourceLocation = resourceLocation;
        this.title = title;
    }

    public String getResourceLocation() {
        return resourceLocation;
    }

    public void setResourceLocation(String resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
