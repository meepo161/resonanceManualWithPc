package ru.avem.resonanceManual.controllers;

import javafx.stage.Stage;

public interface ExperimentController {
    void setDialogStage(Stage dialogStage);

    boolean isCanceled();

}