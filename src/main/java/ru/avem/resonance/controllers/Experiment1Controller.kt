package ru.avem.resonance.controllers

import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.chart.LineChart
import javafx.scene.chart.XYChart
import javafx.scene.control.*
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.Stage
import ru.avem.resonance.Constants
import ru.avem.resonance.Constants.Ends.*
import ru.avem.resonance.Constants.Time.MILLS_IN_SEC
import ru.avem.resonance.Constants.Vfd.VFD_FORWARD
import ru.avem.resonance.Constants.Vfd.VFD_REVERSE
import ru.avem.resonance.Main
import ru.avem.resonance.communication.CommunicationModel
import ru.avem.resonance.communication.devices.DeviceController.*
import ru.avem.resonance.communication.devices.avem_voltmeter.AvemVoltmeterModel
import ru.avem.resonance.communication.devices.deltaC2000.DeltaCP2000Model
import ru.avem.resonance.communication.devices.latr.LatrModel
import ru.avem.resonance.communication.devices.pm130.PM130Model
import ru.avem.resonance.communication.devices.pr200.OwenPRModel
import ru.avem.resonance.communication.modbus.utils.Utils
import ru.avem.resonance.db.TestItemRepository
import ru.avem.resonance.db.model.TestItem
import ru.avem.resonance.model.Experiment1Model
import ru.avem.resonance.model.MainModel
import ru.avem.resonance.model.Point
import ru.avem.resonance.utils.Log
import ru.avem.resonance.utils.Toast
import ru.avem.resonance.utils.Utils.sleep
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Experiment1Controller : DeviceState(), ExperimentController {

    @FXML
    lateinit var tableViewExperiment1: TableView<Experiment1Model>
    @FXML
    lateinit var tableColumnU: TableColumn<Experiment1Model, String>
    @FXML
    lateinit var tableColumnIB: TableColumn<Experiment1Model, String>
    @FXML
    lateinit var tableColumnUOI: TableColumn<Experiment1Model, String>
    @FXML
    lateinit var tableColumnIC: TableColumn<Experiment1Model, String>
    @FXML
    lateinit var tableColumnResultExperiment1: TableColumn<Experiment1Model, String>
    @FXML
    lateinit var textAreaExperiment1Log: TextArea
    @FXML
    lateinit var lineChartExperiment1: LineChart<Number, Number>
    @FXML
    lateinit var buttonStartStop: Button
    @FXML
    lateinit var buttonNext: Button
    @FXML
    lateinit var buttonCancelAll: Button
    @FXML
    lateinit var root: AnchorPane
    @FXML
    lateinit var gridPaneTimeTorque: GridPane
    @FXML
    lateinit var vBoxTime: VBox
    @FXML
    lateinit var vBoxVoltage: VBox
    @FXML
    lateinit var vBoxSpeed: VBox
    @FXML
    lateinit var anchorPaneTimeTorque: AnchorPane
    @FXML
    lateinit var scrollPaneTimeTorque: ScrollPane

    private lateinit var lastTriple: Triple<TextField, TextField, TextField>
    private val stackTriples: Stack<Triple<TextField, TextField, TextField>> = Stack()

    private val mainModel = MainModel.instance
    private val currentProtocol = mainModel.currentProtocol
    private val communicationModel = CommunicationModel.getInstance()
    private var experiment1Model: Experiment1Model? = null
    private val experiment1Data = FXCollections.observableArrayList<Experiment1Model>()

    private var dialogStage: Stage? = null
    private var isCanceled: Boolean = false

    @Volatile
    private var isNeedToRefresh: Boolean = false
    @Volatile
    private var isExperimentRunning: Boolean = false
    @Volatile
    private var isExperimentEnded = true
    @Volatile
    private var isNeedCheckLatrStatus: Boolean = false

    @Volatile
    private var isOwenPRResponding: Boolean = false
    @Volatile
    private var isDeltaResponding: Boolean = false
    @Volatile
    private var isDeltaReady50: Boolean = false
    @Volatile
    private var isDeltaReady0: Boolean = false
    @Volatile
    private var isParmaResponding: Boolean = false
    @Volatile
    private var isLatrResponding: Boolean = false
    @Volatile
    private var isAvemResponding: Boolean = false
    @Volatile
    private var isKiloAvemResponding: Boolean = false
    @Volatile
    private var latrStatus: String = ""

    private val sdf = SimpleDateFormat("HH:mm:ss-SSS")
    private var logBuffer: String? = null
    @Volatile
    private var cause: String? = null
    @Volatile
    private var measuringU: Float = 0.0f
    @Volatile
    private var measuringULatr: Float = 0.0f
    @Volatile
    private var measuringIAvem: Float = 0.0f
    @Volatile
    private var measuringIA: Float = 0.0f
    @Volatile
    private var measuringIB: Float = 0.0f
    @Volatile
    private var measuringIC: Float = 0.0f
    @Volatile
    private var isSchemeReady: Boolean = false
    @Volatile
    private var isStartButtonOn: Boolean = false
    @Volatile
    private var measuringF: Float = 0.0f

    @Volatile
    private var statusEndsVFD: Short = 0
    @Volatile
    private var statusVFD: Short = 0

    private var coef: Double = 102.0

    private var voltageList: ArrayList<Double> = ArrayList()
    private var timeList: ArrayList<Double> = ArrayList()
    private var speedList: ArrayList<Double> = ArrayList()

    private var currentDot = XYChart.Series<Number, Number>()

    private val firstVoltageLatr = 1.8.toFloat()

    private var currentTestItem: TestItem = mainModel.currentTestItem
    private var timePassed = 0.0
    private var time = 0.0
    private var timeSum = 0.0
    private var seriesTimesAndVoltage = XYChart.Series<Number, Number>()
    private var realTime = 0.0

    private var r: Random = Random()

    private var duty: Float = 0.0f
    private var pulse: Float = 0.0f

    private val isThereAreAccidents: Boolean
        get() {
            if (isCanceled) {
                isExperimentRunning = false
                isExperimentEnded = true
            }
            return !isCanceled
        }

    private val isDevicesResponding: Boolean
        get() = true
//        get() = isOwenPRResponding && isAvemResponding && isDeltaResponding && isLatrResponding
//                && isParmaResponding && isKiloAvemResponding

    private val points = ArrayList<Point>()

    @FXML
    private fun initialize() {
        if (Main.css == "white") {
            root.stylesheets[0] = Main::class.java.getResource("styles/main_css.css").toURI().toString()
        } else {
            root.stylesheets[0] = Main::class.java.getResource("styles/main_css_black.css").toURI().toString()
        }

        lineChartExperiment1.animated = false
        experiment1Model = mainModel.experiment1Model
        experiment1Data.add(experiment1Model)
        tableViewExperiment1.items = experiment1Data
        tableViewExperiment1.selectionModel = null
        communicationModel.addObserver(this)
        voltageList = currentProtocol.voltageResonance
        timeList = currentProtocol.timesResonance
        speedList = currentProtocol.speedResonance
        tableColumnU.setCellValueFactory { cellData -> cellData.value.voltageProperty() }
        tableColumnUOI.setCellValueFactory { cellData -> cellData.value.currentAProperty() }
        tableColumnIB.setCellValueFactory { cellData -> cellData.value.currentBProperty() }
        tableColumnIC.setCellValueFactory { cellData -> cellData.value.currentCProperty() }
//        tableColumnILeak.setCellValueFactory { cellData -> cellData.value.currentLeakProperty() }
//        tableColumnFrequency.setCellValueFactory { cellData -> cellData.value.frequencyProperty() }
        tableColumnResultExperiment1.setCellValueFactory { cellData -> cellData.value.resultProperty() }
        fillStackPairs()
    }

    private fun fillStackPairs() {
        for (i in 0 until currentTestItem.timesResonance.size) {
            addPair()
            lastTriple.first.text = currentTestItem.timesResonance[i].toString()
            lastTriple.second.text = currentTestItem.voltageResonance[i].toString()
            lastTriple.third.text = currentTestItem.speedResonance[i].toString()
        }
    }

    @FXML
    fun handleAddPair() {
        addPair()
    }

    private fun addPair() {
        lastTriple = newTextFieldsForChart()
        stackTriples.push(lastTriple)
        vBoxTime.children.add(lastTriple.first)
        vBoxVoltage.children.add(lastTriple.second)
        vBoxSpeed.children.add(lastTriple.third)
        anchorPaneTimeTorque.prefHeight += MainViewController.HEIGHT_VBOX
    }

    @FXML
    fun handleRemovePair() {
        if (stackTriples.isNotEmpty()) {
            removePair()
            saveTestItemPoints()
        } else {
            Toast.makeText("Нет полей для удаления").show(Toast.ToastType.ERROR)
        }
    }

    private fun removePair() {
        lastTriple = stackTriples.pop()
        vBoxTime.children.remove(lastTriple.first)
        vBoxVoltage.children.remove(lastTriple.second)
        vBoxSpeed.children.remove(lastTriple.third)
        anchorPaneTimeTorque.prefHeight -= MainViewController.HEIGHT_VBOX
    }

    private fun newTextFieldsForChart(): Triple<TextField, TextField, TextField> {
        val time = TextField()
        time.isEditable = true
        time.prefWidth = 72.0
        time.maxWidth = 72.0
        time.setOnAction {
            if (time.text.toDouble() * 1000 > timePassed) {
                saveTestItemPoints()
            } else {
                Toast.makeText("Введенное значение меньше пройденного значения времени").show(Toast.ToastType.ERROR)
            }
        }

        val voltage = TextField()
        voltage.isEditable = true
        voltage.prefWidth = 72.0
        voltage.maxWidth = 72.0
        voltage.setOnAction {
            saveTestItemPoints()
        }

        val speed = TextField()
        speed.isEditable = true
        speed.prefWidth = 72.0
        speed.maxWidth = 72.0
        speed.setOnAction {
            saveTestItemPoints()
        }
        return Triple(time, voltage, speed)
    }

    private fun saveTestItemPoints() {
        val times: ArrayList<Double> = ArrayList()
        val voltages: ArrayList<Double> = ArrayList()
        val speeds: ArrayList<Double> = ArrayList()

        stackTriples.forEach {
            if (!it.first.text.isNullOrEmpty() && !it.second.text.isNullOrEmpty() && !it.third.text.isNullOrEmpty() &&
                    it.first.text.toDoubleOrNull() != null && it.second.text.toDoubleOrNull() != null && it.third.text.toDoubleOrNull() != null) {
                times.add(it.first.text.toDouble())
                voltages.add(it.second.text.toDouble())
                speeds.add(it.third.text.toDouble())
            } else {
                Toast.makeText("Проверьте правильность введенных напряжений и времени проверки").show(Toast.ToastType.WARNING)
            }
        }
        currentTestItem.timesResonance = times
        currentTestItem.voltageResonance = voltages
        currentTestItem.speedResonance = speeds

        voltageList = currentProtocol.voltageResonance
        timeList = currentProtocol.timesResonance
        speedList = currentProtocol.speedResonance

        TestItemRepository.updateTestItem(currentTestItem)
    }

    private fun createLoadDiagram() {
        while (isExperimentRunning) {
            sleep(100)

            Platform.runLater {
                realTime += 0.1
                measuringU += 9
                lineChartExperiment1.data.clear()
                seriesTimesAndVoltage.data.add(XYChart.Data(realTime, measuringU))
                lineChartExperiment1.data.add(seriesTimesAndVoltage)
            }
        }
    }

    private fun fillProtocolExperimentFields() {
        val currentProtocol = mainModel.currentProtocol
    }

    @FXML
    private fun handleNextExperiment() {
        fillProtocolExperimentFields()
        dialogStage!!.close()
    }

    @FXML
    private fun handleRunStopExperiment() {
        if (isExperimentEnded) {
            startExperiment()
        } else {
            stopExperiment()
        }
    }

    @FXML
    private fun handleExperimentCancel() {
        isExperimentRunning = false
        dialogStage!!.close()
    }

    private fun stopExperiment() {
        isNeedToRefresh = false
        buttonStartStop.isDisable = true
        setCause("Отменено оператором")
        isExperimentRunning = false
    }


    private fun startExperiment() {
        points.clear()
        isNeedToRefresh = true
        isNeedCheckLatrStatus = false
        isExperimentRunning = true
        isExperimentEnded = false
        buttonStartStop.text = "Остановить"
        buttonNext.isDisable = true
        buttonCancelAll.isDisable = true
        experiment1Model!!.clearProperties()
        isSchemeReady = false
        cause = ""
        lineChartExperiment1.data.clear()

        Thread {

            if (isExperimentRunning) {
                appendOneMessageToLog("Визуально осматривайте трансфоматор на наличие потеков масла перед каждым опытом")
                communicationModel.initOwenPrController()
                appendOneMessageToLog("Начало испытания")
                communicationModel.initExperimentDevices()
//                sleep(5000)
                communicationModel.setKiloAvemShowValue(Constants.Avem.VOLTAGE_RMS.ordinal)
                sleep(5000)
                communicationModel.startUpLATRFast(1.0f, true)
            }

//            var startTime = 0L
//            duty = 40.0f
//            while (true) {
//                duty++
//                communicationModel.startUpLATRTest(400.0f, true, duty)
//                appendOneMessageToLog("duty = $duty")
//                startTime = System.currentTimeMillis()
//                while (measuringULatr <= 370) {
//                    sleep(10)
//                }
//                appendOneMessageToLog((System.currentTimeMillis() - startTime).toString())
//                appendOneMessageToLog((370 / ((System.currentTimeMillis() - startTime) / 1000)).toString())
//                sleep(6000)
//            }

//            while (latrStatus != LATR_DOWN_END) {
//                communicationModel.startUpLATRFast(0.0f, true)
//            }

//            while (isPRDI6) {
//                appendOneMessageToLog("Включите рубильник")
//            }

//            if (isExperimentRunning) {
//                communicationModel.onPRO1()
//            }

//            while (isPRMDI1) {
//                appendOneMessageToLog("Нажмите кнопку ПУСК")
//            }

            if (isExperimentRunning) {
                appendOneMessageToLog("Устанавливаем начальные точки для ЧП")
                communicationModel.setObjectParams(50 * 100, 380 * 10, 50 * 100)
                appendOneMessageToLog("Запускаем ЧП")
//                resetOmik()
//                communicationModel.onPRO6() //последовательное
//                communicationModel.onPRO7() //параллельно
            }


//            if (isExperimentRunning) {
//                appendOneMessageToLog("Поднимаем напряжение на объекте испытания для поиска резонанса")
//                communicationModel.startUpLATRFast(firstVoltageLatr, true)
//                waitingLatrCoarse(firstVoltageLatr)
//                if (measuringULatr < measuringU * 0.5 && measuringULatr * 0.5 > measuringU) {
//                    setCause("Коэфицент трансформации сильно отличается")
//                }
//            }
//
//            if (isExperimentRunning) {
//                findResonance()
//            }

            timeSum = 0.0

            if (isExperimentRunning && isDevicesResponding) {
                createLoadDiagram()
                for (i in voltageList.indices) {
                    stackTriples[i].second.isDisable = true
                    timePassed = 0.0
                    if (isExperimentRunning && isDevicesResponding) {
                        appendOneMessageToLog("Началась регулировка")
                        selectDutyAndPulseForLatr(i)
//                        communicationModel.startUpLATRFast((voltageList[i] / coef).toFloat(), false)
//                        waitingLatrCoarse(voltageList[i].toFloat())
//                        fineLatr(voltageList[i].toFloat())
//                        if (measuringULatr < measuringU * 0.5 && measuringULatr * 0.5 > measuringU) {
//                            setCause("Коэфицент трансформации сильно отличается")
//                        }
                        appendOneMessageToLog("Регулировка окончена")
//                        communicationModel.onPRlMDO6()
//                        sleep(3000)
//                        communicationModel.offPRMDO6()
//                        communicationModel.onPRMDO5()
//                        communicationModel.offPRMDO5()
                    }

                    time = currentTestItem.timesResonance[i] * MILLS_IN_SEC
                    while (isExperimentRunning && timePassed < time) {
                        sleep(100)
                        timePassed += 100.75 //потому что while занимает реально примерно 0.75 ms
                        if (time != stackTriples[i].second.text.toDouble() * MILLS_IN_SEC) {
                            time = currentTestItem.timesResonance[i] * MILLS_IN_SEC
                        }
                    }
//                    communicationModel.onPRMDO6()
//                    sleep(1500)
//                    communicationModel.offPRMDO6()
                    voltageList = currentTestItem.voltageResonance
                    timeSum += currentTestItem.timesResonance[i]
                    stackTriples[i].first.isDisable = true
                }
            }

            isNeedToRefresh = false
//            communicationModel.startUpLATRFast(1f, true)
//            while (measuringU > 800) {
//                sleep(10)
//            }
//            communicationModel.stopLATR()
////            resetOmik()
//            communicationModel.stopObject()
//            sleep(3000)
//            communicationModel.offAllKms()
//            sleep(50)
            communicationModel.finalizeAllDevices()

            if (cause != "") {
                appendMessageToLog(String.format("Испытание прервано по причине: %s", cause))
                experiment1Model!!.result = "Неуспешно"
            } else if (!isDevicesResponding) {
                appendMessageToLog(getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"))
                experiment1Model!!.result = "Неуспешно"
            } else {
                experiment1Model!!.result = "Успешно"
                appendMessageToLog("Испытание завершено успешно")
            }
            appendMessageToLog("\n------------------------------------------------\n")

            Platform.runLater()
            {
                buttonStartStop.text = "Запустить"
                isExperimentEnded = true
                isExperimentRunning = false
                buttonStartStop.isDisable = false
                buttonNext.isDisable = false
                buttonCancelAll.isDisable = false
            }

        }.start()
    }

    private fun selectDutyAndPulseForLatr(i: Int) {
        if (currentTestItem.speedResonance[i] < 0.4) {
            duty = 25.0f
            pulse = 50.0f
        } else if (currentTestItem.speedResonance[i] >= 0.4 && currentTestItem.speedResonance[i] < 0.5) {
            duty = 25.0f
            pulse = 56.0f
        } else if (currentTestItem.speedResonance[i] >= 0.5 && currentTestItem.speedResonance[i] < 0.6) {
            duty = 25.0f
            pulse = 64.0f
        } else if (currentTestItem.speedResonance[i] >= 0.6 && currentTestItem.speedResonance[i] < 0.7) {
            duty = 25.0f
            pulse = 70.0f
        } else if (currentTestItem.speedResonance[i] >= 0.7 && currentTestItem.speedResonance[i] < 0.8) {
            duty = 25.0f
            pulse = 74.0f
        } else if (currentTestItem.speedResonance[i] >= 0.8 && currentTestItem.speedResonance[i] < 0.9) {
            duty = 25.0f
            pulse = 79.0f
        } else if (currentTestItem.speedResonance[i] >= 0.9 && currentTestItem.speedResonance[i] < 1.0) {
            duty = 25.0f
            pulse = 85.0f
        } else if (currentTestItem.speedResonance[i] >= 1.0 && currentTestItem.speedResonance[i] < 1.1) {
            duty = 25.0f
            pulse = 89.0f
        } else if (currentTestItem.speedResonance[i] >= 1.1 && currentTestItem.speedResonance[i] < 1.2) {
            duty = 25.0f
            pulse = 94.0f
        } else if (currentTestItem.speedResonance[i] >= 1.2 && currentTestItem.speedResonance[i] < 1.3) {
            duty = 28.0f
            pulse = 86.0f
        } else if (currentTestItem.speedResonance[i] >= 1.3 && currentTestItem.speedResonance[i] < 1.4) {
            duty = 28.0f
            pulse = 88.0f
        } else if (currentTestItem.speedResonance[i] >= 1.4 && currentTestItem.speedResonance[i] < 1.5) {
            duty = 28.0f
            pulse = 90.0f
        } else if (currentTestItem.speedResonance[i] >= 1.5 && currentTestItem.speedResonance[i] < 1.6) {
            duty = 28.0f
            pulse = 92.0f
        } else if (currentTestItem.speedResonance[i] >= 1.6 && currentTestItem.speedResonance[i] < 1.7) {
            duty = 28.0f
            pulse = 95.0f
        } else if (currentTestItem.speedResonance[i] >= 1.7 && currentTestItem.speedResonance[i] < 1.8) {
            duty = 32.0f
            pulse = 90.0f
        } else if (currentTestItem.speedResonance[i] >= 1.8 && currentTestItem.speedResonance[i] < 1.9) {
            duty = 33.0f
            pulse = 91.0f
        } else if (currentTestItem.speedResonance[i] <= 2.0) {
            duty = 34.0f
            pulse = 91.0f
        }
    }

    private fun findResonance() {
        if (statusVFD == VFD_REVERSE) {
            communicationModel.changeRotation()
            sleep(2000)
        }
        communicationModel.startObject()
        sleep(3000)
        var highestU = measuringU
        var lowestI = measuringIC
        var step = 5
        appendOneMessageToLog("Идет поиск резонанса")
        while ((step-- > 0) && isExperimentRunning && isDevicesResponding) {
            if (measuringU > highestU) {
                highestU = measuringU
                step = 5
            }
            if (measuringIC < lowestI) {
                lowestI = measuringIC
                step = 5
            } //TODO попробовать через &&
            sleep(500)
        }
        communicationModel.stopObject()
        sleep(3000)
        communicationModel.changeRotation()
        communicationModel.setObjectParams(25 * 100, 380 * 10, 25 * 100)
        communicationModel.startObject()
        while (measuringU * 1.05 <= highestU || measuringIC >= lowestI * 1.05) { //Из-за инерции
            if (statusEndsVFD == OMIK_DOWN_END) {
                setCause("Не удалось подобрать резонанс")
            }
            sleep(10)
        }
        communicationModel.stopObject()
        sleep(1000)
        appendOneMessageToLog("Поиск завершен")
    }

    private fun waitingLatrCoarse(voltage: Float) {
        appendOneMessageToLog("Грубая регулировка")
        while (isExperimentRunning && isDevicesResponding && (measuringU <= voltage - 1000 || measuringU > voltage + 750)) {
            if (measuringU <= voltage - 1000) {
                communicationModel.startUpLATRFast(380f, false)
            } else if (measuringU > voltage + 750) {
                communicationModel.startUpLATRFast(1f, false)
            } else {
                break
            }
        }
        communicationModel.stopLATR()

        while (isExperimentRunning && isDevicesResponding && (measuringU <= voltage - 300 && measuringU > voltage + 300)) {
            if (measuringU <= voltage - 300) {
                communicationModel.startUpLATRSlow(380f, false)
            } else if (measuringU > voltage + 300) {
                communicationModel.startUpLATRSlow(1f, false)
            } else {
                break
            }
        }
        communicationModel.stopLATR()

        appendOneMessageToLog("Грубая регулировка окончена")
    }

    private fun fineLatr(voltage: Float) {
        appendOneMessageToLog("Точная регулировка")
        sleep(1000)
        while ((measuringU <= voltage - 150 || measuringU > voltage + 150) && isExperimentRunning) {
            if (measuringU <= voltage - 150) {
                communicationModel.startUpLATRFast(380f, false)
                sleep(1750)
                communicationModel.stopLATR()
            } else if (measuringU >= voltage + 150) {
                communicationModel.startUpLATRFast(1f, false)
                sleep(1750)
                communicationModel.stopLATR()
            }
        }
        appendOneMessageToLog("Точная регулировка закончена")

        communicationModel.stopLATR()
    }

    private fun resetOmik() {
        communicationModel.setObjectParams(50 * 100, 380 * 10, 50 * 100)
        if (statusEndsVFD != OMIK_DOWN_END && isDevicesResponding) {
            appendOneMessageToLog("Возвращаем магнитопровод в исходное состояние")
            if (statusVFD != VFD_REVERSE && isDevicesResponding) {
                communicationModel.changeRotation()
            }
            communicationModel.startObject()
            var waitingTime = 30
            while (isExperimentRunning && isDevicesResponding && (waitingTime-- > 0)) {
                sleep(100)
            }
            while (statusEndsVFD != OMIK_DOWN_END && isDevicesResponding) {
                sleep(10)
                if (statusEndsVFD == OMIK_UP_END && isDevicesResponding) {
                    setCause("Омик в верхнем положенении, двигаясь вниз")
                }
            }
            sleep(1000)
            communicationModel.stopObject()
        }
        if (statusEndsVFD == OMIK_DOWN_END) {
            appendOneMessageToLog("ОМИК в нижнем положении")
        }
    }

    private fun appendMessageToLog(message: String) {
        Platform.runLater {
            textAreaExperiment1Log.appendText(String.format("%s \t| %s\n", sdf.format(System.currentTimeMillis()), message))
        }
    }

    private fun appendOneMessageToLog(message: String) {
        if (logBuffer == null || logBuffer != message) {
            logBuffer = message
            appendMessageToLog(message)
        }
    }

    private fun getAccidentsString(mainText: String): String {
        return String.format("%s: %s",
                mainText,
//                if (is1DoorOn) "" else "открыта дверь, ",
//                if (is2OIOn) "" else "открыты концевики ОИ, ",
//                if (is6KM1_2_On) "" else "не замкнулся КМ1, ",
//                if (is7KM2_2_On) "" else "не замкнулся КМ2, ",
                if (isCanceled) "" else "нажата кнопка отмены, ")
    }

    private fun getNotRespondingDevicesString(mainText: String): String {
        return String.format("%s %s%s%s%s%s",
                mainText,
                if (isOwenPRResponding) "" else "Овен ПР ",
                if (isParmaResponding) "" else "Парма ",
                if (isDeltaResponding) "" else "Дельта ",
                if (isLatrResponding) "" else "Латр ",
                if (isAvemResponding) "" else "АВЭМ ")
    }

    private fun setCause(cause: String) {
        this.cause = cause
        if (cause.isNotEmpty()) {
            isExperimentRunning = false
        }
    }

    override fun update(o: Observable, values: Any) {
        val modelId = (values as Array<Any>)[0] as Int
        val param = values[1] as Int
        val value = values[2]

        when (modelId) {
            PR200_ID -> when (param) {
                OwenPRModel.RESPONDING_PARAM -> {
                    isOwenPRResponding = value as Boolean
                    Platform.runLater { deviceStateCirclePR200.fill = if (value) Color.LIME else Color.RED }
                }
                OwenPRModel.PRI1 -> {
                    isStartButtonOn = value as Boolean
                }
            }

            PM130_ID -> when (param) {
                PM130Model.RESPONDING_PARAM -> {
                    isParmaResponding = value as Boolean
                    Platform.runLater { deviceStateCirclePM130.fill = if (value) Color.LIME else Color.RED }
                }
                PM130Model.I1_PARAM -> {
                    measuringIA = value as Float * 16
                    val IA = String.format("%.4f", measuringIA)
                    experiment1Model!!.currentA = IA
                    if (measuringIA > 45) {
                        appendMessageToLog("Ток А превышает 45А")
                    }
                }
                PM130Model.I2_PARAM -> {
                    measuringIB = value as Float * 2
                    val IB = String.format("%.4f", measuringIB)
                    experiment1Model!!.currentB = IB
                    if (measuringIB > 45) {
                        appendMessageToLog("Ток B превышает 45А")
                    }
                }
                PM130Model.I3_PARAM -> {
                    measuringIC = value as Float * 16 //TODO ТОП УЗНАТЬ
                    val IC = String.format("%.4f", measuringIC)
                    experiment1Model!!.currentC = IC
                    if (measuringIC > 45) {
                        appendMessageToLog("Ток C превышает 45А")
                    }
                }
                PM130Model.F_PARAM -> {
                    measuringF = value as Float
                    val FParma = String.format("%.2f", measuringF)
                    experiment1Model!!.frequency = FParma
                }
            }

            DELTACP2000_ID -> when (param) {
                DeltaCP2000Model.RESPONDING_PARAM -> {
                    isDeltaResponding = value as Boolean
                    Platform.runLater { deviceStateCircleDelta.fill = if (value) Color.LIME else Color.RED }
                }
                DeltaCP2000Model.ENDS_STATUS_PARAM -> {
                    statusEndsVFD = value as Short
                    checkEndsVFDStatus()
                }
                DeltaCP2000Model.STATUS_VFD -> {
                    statusVFD = value as Short
                    checkVFDStatus()
//                    when {
//                        statusEndsVFD == OMIK_DOWN_END -> communicationModel.stopObject()
//                        statusVFD == OMIK_UP_END -> communicationModel.stopObject()
//                        statusVFD == OMIK_BOTH_END -> communicationModel.stopObject()
//                    }
                }
            }

            AVEM_ID -> when (param) {
                AvemVoltmeterModel.RESPONDING_PARAM -> {
                    isAvemResponding = value as Boolean
                    Platform.runLater { deviceStateCircleAvem.fill = if (value) Color.LIME else Color.RED }
                }
                AvemVoltmeterModel.U_AMP_PARAM -> {
                    measuringIAvem = value as Float
                    val IAvem = String.format("%.4f", measuringIAvem)
                    experiment1Model!!.currentLeak = IAvem
                }
            }

//            KILOAVEM_ID -> when (param) {
//                AvemVoltmeterModel.RESPONDING_PARAM -> {
//                    isKiloAvemResponding = value as Boolean
//                    Platform.runLater { deviceStateCircleKiloAvem.fill = if (value) Color.LIME else Color.RED }
//                }
//                AvemVoltmeterModel.U_RMS_PARAM -> {
//                    measuringU = (value as Float)
//                    val kiloAvemU = String.format("%.2f", measuringU)
//                    experiment1Model!!.voltage = kiloAvemU
//                }
//            }

            LATR_ID -> when (param) {
                LatrModel.RESPONDING_PARAM -> {
                    isLatrResponding = value as Boolean
                    Platform.runLater { deviceStateCircleLatr.fill = if (value) Color.LIME else Color.RED }
                }
                LatrModel.STATUS_PARAM -> {
                    latrStatus = Utils.toHexString(value as Byte)
                    checkLatrError()
                    checkLatrStatus()
                }
                LatrModel.U_PARAM -> {
                    measuringULatr = (value as Float)
                    experiment1Model!!.voltage = measuringULatr.toString()
                }
            }
        }
    }

    private fun checkLatrStatus() {
        when (latrStatus) {
            LATR_STARTED -> {
//                appendOneMessageToLog("Выход ЛАТРа на заданное напряжение")
            }
            LATR_WAITING -> {
                appendOneMessageToLog("Выдерживаем заданное напряжение на ЛАТРе")
            }
            LATR_CONFIG -> {
                appendOneMessageToLog("Режим кофигурации ЛАТР")
            }
            LATR_STOP_RESET -> {
//                appendOneMessageToLog("Стоп/Ресет ЛАТР")
            }
        }
    }

    private fun checkLatrError() {
        when (latrStatus) {
            LATR_UP_END -> {
                appendOneMessageToLog("Сработал верхний концевик ЛАТРа.")
            }
            LATR_DOWN_END -> {
                appendOneMessageToLog("Сработал нижний концевик ЛАТРа.")
            }
            LATR_BOTH_END -> {
                setCause("Сработали оба концевика ЛАТРа.")
            }
            LATR_TIME_ENDED -> {
                setCause("Время регулирования ЛАТРа превысило заданное.")
            }
            LATR_ZASTRYAL -> {
                setCause("Застревание ЛАТРа.")
            }
        }
    }

    private fun checkEndsVFDStatus() {
        when (statusEndsVFD) {
            OMIK_UP_END -> {
                appendOneMessageToLog("Замкнут верхний концевик ОМИКа.")
            }
            OMIK_DOWN_END -> {
                appendOneMessageToLog("Замкнут нижний концевик ОМИКа.")
            }
            OMIK_BOTH_END -> {
                setCause("Замкнуты оба концевика ОМИКа.")
            }
            OMIK_NOONE_END -> {
                Log.d("", "Оба концевика ОМИКа не замкнуты")
            }
        }
    }

    private fun checkVFDStatus() {
        when (statusVFD) {
            VFD_FORWARD -> {
                Log.d("", "FORWARD")
            }
            VFD_REVERSE -> {
                Log.d("", "REVERSE")
            }
        }
    }

    override fun setDialogStage(dialogStage: Stage) {
        this.dialogStage = dialogStage
    }

    override fun isCanceled(): Boolean {
        return isCanceled
    }
}