package ru.avem.resonance.controllers

import javafx.fxml.FXML
import javafx.scene.control.ComboBox
import javafx.scene.control.PasswordField
import javafx.scene.control.TextField
import javafx.scene.layout.AnchorPane
import ru.avem.resonance.Main
import ru.avem.resonance.Main.Companion.css
import ru.avem.resonance.db.AccountRepository
import ru.avem.resonance.db.model.Account
import ru.avem.resonance.model.MainModel
import ru.avem.resonance.utils.Toast

class LoginController {

    @FXML
    lateinit var root: AnchorPane

    @FXML
    lateinit var textLogin: TextField
    @FXML
    lateinit var textPassword: PasswordField
    @FXML
    lateinit var secondTester: ComboBox<Account>

    private var main: Main? = null
    private val mainModel = MainModel.instance


    @FXML
    private fun initialize() {
        css = "white"
        if (css == "white") {
            root.getStylesheets().set(0, Main::class.java.getResource("styles/main_css.css").toURI().toString())
        } else {
            root.getStylesheets().set(0, Main::class.java.getResource("styles/main_css_black.css").toURI().toString())
        }
    }
    @FXML
    private fun handleLogIn() {
        val allAccounts = AccountRepository.getAllAccounts()
        val login = textLogin.text
        val password = textPassword.text

        if (allAccounts.size == 0) {
            if (login == "Administrator" && password == "Administrator") {
                Toast.makeText("Вы зашли в DEBUG режим").show(Toast.ToastType.INFORMATION)
                main!!.showMainView()
                return
            } else {
                Toast.makeText("В базе данных нет пользователей, обратитесь к поставщику").show(Toast.ToastType.ERROR)
                return
            }
        }

        var foundAccount: Account? = null
        for (account in allAccounts) {
            if (login == account.name) {
                foundAccount = account
                if (password == account.password) {
                    val secondTesterAccount = secondTester.selectionModel.selectedItem
                    if (secondTesterAccount == null) {
                        Toast.makeText("Выберите второго испытателя").show(Toast.ToastType.WARNING)
                        return
                    }
                    if (account == secondTesterAccount && allAccounts.size > 1) {
                        Toast.makeText("Первый и второй испытатель не могут быть одним и тем же лицом").show(Toast.ToastType.WARNING)
                        return
                    }
                    mainModel.setTesters(account, secondTesterAccount)
                    main!!.showMainView()
                    break
                } else {
                    Toast.makeText("Введенные вами данные неверные").show(Toast.ToastType.ERROR)
                }
            }
        }

        if (foundAccount == null) {
            Toast.makeText("Введенные вами данные неверные").show(Toast.ToastType.ERROR)
        }
    }

    fun setMainApp(main: Main) {
        this.main = main
    }

    fun fillFields() {
        secondTester.selectionModel.clearSelection()
        val allAccounts = AccountRepository.getAllAccounts()
        secondTester.items.setAll(allAccounts)
    }

    fun clearFields() {
        textLogin.clear()
        textPassword.clear()
        textLogin.requestFocus()
    }
}
