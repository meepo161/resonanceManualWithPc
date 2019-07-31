package ru.avem.resonance.controllers

import javafx.fxml.FXML
import javafx.scene.layout.AnchorPane
import ru.avem.resonance.Main
import ru.avem.resonance.Main.Companion.css
import ru.avem.resonance.communication.CommunicationModel
import ru.avem.resonance.utils.Utils

class DeviceStateWindowController : DeviceState() {

    @FXML
    lateinit var root: AnchorPane

    var flag : Boolean = true

    @FXML
    private fun initialize() {
        if (css == "white") {
            root.getStylesheets().set(0, Main::class.java.getResource("styles/main_css.css").toURI().toString())
        } else {
            root.getStylesheets().set(0, Main::class.java.getResource("styles/main_css_black.css").toURI().toString())
        }
        val communicationModel = CommunicationModel.getInstance()
        communicationModel.addObserver(this)

        flag = true
        Thread {
            while (flag) {
                communicationModel.resetAllDevices()
                Utils.sleep(1000)
            }
            communicationModel.finalizeAllDevices()
            communicationModel.deleteObservers()
        }.start()
    }
}
