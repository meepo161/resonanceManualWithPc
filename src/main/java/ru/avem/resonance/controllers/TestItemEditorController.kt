package ru.avem.resonance.controllers

import javafx.fxml.FXML
import javafx.scene.control.TextField
import javafx.scene.layout.AnchorPane
import ru.avem.resonance.Main
import ru.avem.resonance.db.TestItemRepository
import ru.avem.resonance.utils.Toast

class TestItemEditorController {
    //region FXML
    @FXML
    lateinit var root: AnchorPane
    @FXML
    lateinit var textFieldType: TextField
    //endregion
    var mainViewController = MainViewController()

    @FXML
    private fun initialize() {
        if (Main.css == "white") {
            root.stylesheets[0] = Main::class.java.getResource("styles/main_css.css").toURI().toString()
        } else {
            root.stylesheets[0] = Main::class.java.getResource("styles/main_css_black.css").toURI().toString()
        }
        fillParameters()
    }


    //    private fun initData() {
//        val allTestItems = TestItemRepository.getAllTestItems()
//        comboBoxTestItem.items.clear()
//        if (allTestItems.isNotEmpty()) {
//            comboBoxTestItem.items.setAll(allTestItems)
//            comboBoxTestItem.selectionModel.selectFirst()
//            fillParameters()
//        } else {
//            clearParameters()
//        }
//    }
//
    private fun fillParameters() {
        if (mainViewController.currentTestItem != null) {
            textFieldType.text = mainViewController.currentTestItem!!.type
        }
    }
//
//    private fun clearParameters() {
//        textFieldType.text = ""
//        comboBoxTestItem.selectionModel.clearSelection()
//    }

    @FXML
    fun handleSave() {
        if (mainViewController.currentTestItem != null) {
            mainViewController.currentTestItem!!.type = textFieldType.text
            TestItemRepository.updateTestItem(mainViewController.currentTestItem)
//            textFieldType.text = currentTestItem!!.type
        } else {
            Toast.makeText("Выберите ОИ для сохранения").show(Toast.ToastType.INFORMATION)
        }
    }
}
