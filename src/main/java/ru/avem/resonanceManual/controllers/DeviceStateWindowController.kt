package ru.avem.resonanceManual.controllers

import javafx.fxml.FXML
import javafx.scene.layout.AnchorPane
import ru.avem.resonanceManual.Main
import ru.avem.resonanceManual.Main.Companion.css
import ru.avem.resonanceManual.communication.CommunicationModel

class DeviceStateWindowController : DeviceState() {

    @FXML
    lateinit var root: AnchorPane

    @FXML
    private fun initialize() {
        if (css == "white") {
            root.getStylesheets().set(0, Main::class.java.getResource("styles/main_css.css").toURI().toString())
        } else {
            root.getStylesheets().set(0, Main::class.java.getResource("styles/main_css_black.css").toURI().toString())
        }
        val model = CommunicationModel.getInstance()
        model.addObserver(this)
        model.setDeviceStateOn(true)
        model.setNeedToReadAllDevices(true)
    }
}
