package ru.avem.resonanceManual.controllers

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.AnchorPane
import ru.avem.resonanceManual.Main
import ru.avem.resonanceManual.Main.Companion.css
import ru.avem.resonanceManual.db.AccountRepository
import ru.avem.resonanceManual.db.model.Account
import ru.avem.resonanceManual.utils.Toast

class RegisterWindowController {
    private lateinit var profilesData: ObservableList<Account>

    @FXML
    lateinit var root: AnchorPane

    @FXML
    lateinit var tableProfiles: TableView<Account>
    @FXML
    lateinit var columnProfilesLogin: TableColumn<Account, String>
    @FXML
    lateinit var columnProfilesFullName: TableColumn<Account, String>


    @FXML
    lateinit var textProfilesLogin: TextField
    @FXML
    lateinit var textProfilesFullName: TextField
    @FXML
    lateinit var textProfilesPosition: TextField
    @FXML
    lateinit var textProfilesPositionNumber: TextField
    @FXML
    lateinit var textProfilesPassword1: PasswordField
    @FXML
    lateinit var textProfilesPassword2: PasswordField

    private val isInputValid: Boolean
        get() {
            val errorMessage = StringBuilder()

            if (textProfilesLogin.text == null || textProfilesLogin.text.isEmpty()) {
                errorMessage.append("Неверный логин\n")
            }
            if (textProfilesPosition.text == null || textProfilesPosition.text.isEmpty()) {
                errorMessage.append("Неверная должность\n")
            }
            if (textProfilesPositionNumber.text == null || textProfilesPositionNumber.text.isEmpty()) {
                errorMessage.append("Неверный табельный номер\n")
            }
            if (textProfilesFullName.text == null || textProfilesFullName.text.isEmpty()) {
                errorMessage.append("Неверные ФИО\n")
            }
            if (textProfilesPassword1.text == null || textProfilesPassword1.text.isEmpty()) {
                errorMessage.append("Неверный пароль\n")
            }

            if (textProfilesPassword2.text == null || textProfilesPassword2.text.isEmpty()) {
                errorMessage.append("Неверный второй пароль\n")
            } else {
                if (textProfilesPassword1.text != textProfilesPassword2.text) {
                    errorMessage.append("Пароли не совпадают\n")
                }
            }

            return if (errorMessage.isEmpty()) {
                true
            } else {
                Toast.makeText(errorMessage.toString()).show(Toast.ToastType.ERROR)
                false
            }
        }
    //endregion

    @FXML
    private fun initialize() {
        initData()

        columnProfilesLogin.cellValueFactory = PropertyValueFactory("name")
        columnProfilesFullName.cellValueFactory = PropertyValueFactory("fullName")

        // заполняем таблицу данными
        tableProfiles.items = profilesData

        if (css == "white") {
            root.getStylesheets().set(0, Main::class.java.getResource("styles/main_css.css").toURI().toString())
        } else {
            root.getStylesheets().set(0, Main::class.java.getResource("styles/main_css_black.css").toURI().toString())
        }
    }

    private fun initData() {
        val allAccounts = AccountRepository.getAllAccounts()
        profilesData = FXCollections.observableArrayList(allAccounts)
    }

    @FXML
    private fun handleProfilesAddProfile() {
        if (isInputValid) {
            val account = Account(textProfilesLogin.text, textProfilesPassword1.text,
                    textProfilesPosition.text, textProfilesPositionNumber.text, textProfilesFullName.text)
            AccountRepository.insertAccount(account)
            profilesData.add(account)
        }
    }

    @FXML
    private fun handleProfilesDeleteProfile() {
        val selectedIndex = tableProfiles.selectionModel.selectedIndex
        val account = tableProfiles.selectionModel.selectedItem
        if (selectedIndex >= 0) {
            tableProfiles.items.removeAt(selectedIndex)
            AccountRepository.deleteAccount(account)
        } else {
            val alert = Alert(Alert.AlertType.WARNING)
            alert.initOwner(Main.PRIMARY_STAGE)
            alert.title = "Не выбрано"
            alert.headerText = "Профиль не выбран"
            alert.contentText = "Пожалуйста выберите профиль в таблице"

            alert.showAndWait()
        }
    }
}
