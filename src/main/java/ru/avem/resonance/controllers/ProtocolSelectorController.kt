package ru.avem.resonance.controllers

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.scene.control.Alert
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.AnchorPane
import javafx.stage.Stage
import ru.avem.resonance.Main
import ru.avem.resonance.Main.Companion.css
import ru.avem.resonance.db.ProtocolRepository
import ru.avem.resonance.db.model.Protocol
import ru.avem.resonance.model.MainModel

class ProtocolSelectorController : ExperimentController {

    private var mainModel: MainModel? = null

    private var protocols: ObservableList<Protocol>? = null

    @FXML
    lateinit var root: AnchorPane

    @FXML
    lateinit var tableProtocols: TableView<Protocol>

    @FXML
    lateinit var columnProtocolID: TableColumn<Protocol, String>

    @FXML
    lateinit var columnProtocolSerialNumber: TableColumn<Protocol, Double>

    @FXML
    lateinit var columnProtocolDate: TableColumn<Protocol, String>

    @FXML
    lateinit var columnProtocolFullName1: TableColumn<Protocol, Double>

    @FXML
    lateinit var columnProtocolFullName2: TableColumn<Protocol, Double>

    private var dialogStage: Stage? = null
    private var isCanceled = true

    @FXML
    private fun initialize() {
        if (css == "white") {
            root.getStylesheets().set(0, Main::class.java.getResource("styles/main_css.css").toURI().toString())
        } else {
            root.getStylesheets().set(0, Main::class.java.getResource("styles/main_css_black.css").toURI().toString())
        }
        mainModel = MainModel.instance
        initData()

        columnProtocolID.cellValueFactory = PropertyValueFactory("id")
        columnProtocolSerialNumber.cellValueFactory = PropertyValueFactory("serialNumber")
        columnProtocolDate.cellValueFactory = PropertyValueFactory("date")
        columnProtocolFullName1.cellValueFactory = PropertyValueFactory("position1FullName")
        columnProtocolFullName2.cellValueFactory = PropertyValueFactory("position2FullName")

        // заполняем таблицу данными
        tableProtocols.items = protocols
    }

    private fun initData() {
        val allProtocols = ProtocolRepository.getAllProtocols()
        protocols = FXCollections.observableArrayList(allProtocols)
    }

    @FXML
    private fun handleProtocolSelect() {
        val selectedIndex = tableProtocols.selectionModel.selectedIndex
        if (selectedIndex >= 0) {
            mainModel!!.setIntermediateProtocol(tableProtocols.selectionModel.selectedItem)
            isCanceled = false
            dialogStage!!.close()
        } else {
            val alert = Alert(Alert.AlertType.WARNING)
            alert.title = "Не выбрано"
            alert.headerText = "Протокол не выбран"
            alert.contentText = "Пожалуйста выберите протокол в таблице"

            alert.showAndWait()
        }
    }

    override fun setDialogStage(dialogStage: Stage) {
        this.dialogStage = dialogStage
    }

    override fun isCanceled(): Boolean {
        return isCanceled
    }
}
