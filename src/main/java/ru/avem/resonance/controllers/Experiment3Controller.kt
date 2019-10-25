package ru.avem.resonance.controllers

import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.chart.LineChart
import javafx.scene.chart.XYChart
import javafx.scene.control.Button
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TextArea
import javafx.scene.layout.AnchorPane
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
import ru.avem.resonance.model.Experiment3Model
import ru.avem.resonance.model.MainModel
import ru.avem.resonance.model.Point
import ru.avem.resonance.utils.Log
import ru.avem.resonance.utils.Utils.sleep
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

class Experiment3Controller : DeviceState(), ExperimentController {

    @FXML
    lateinit var tableViewExperiment3: TableView<Experiment3Model>
    @FXML
    lateinit var tableColumnU: TableColumn<Experiment3Model, String>
    @FXML
    lateinit var tableColumnIA: TableColumn<Experiment3Model, String>
    @FXML
    lateinit var tableColumnIB: TableColumn<Experiment3Model, String>
    @FXML
    lateinit var tableColumnIC: TableColumn<Experiment3Model, String>
    @FXML
    lateinit var tableColumnILeak: TableColumn<Experiment3Model, String>
    @FXML
    lateinit var tableColumnFrequency: TableColumn<Experiment3Model, String>
    @FXML
    lateinit var tableColumnResultExperiment3: TableColumn<Experiment3Model, String>
    @FXML
    lateinit var textAreaExperiment3Log: TextArea
    @FXML
    lateinit var lineChartExperiment3: LineChart<Number, Number>
    @FXML
    lateinit var buttonStartStop: Button
    @FXML
    lateinit var buttonNext: Button
    @FXML
    lateinit var buttonCancelAll: Button
    @FXML
    lateinit var root: AnchorPane

    private val mainModel = MainModel.instance
    private val currentProtocol = mainModel.currentProtocol
    private val communicationModel = CommunicationModel.getInstance()
    private var experiment3Model: Experiment3Model? = null
    private val experiment3Data = FXCollections.observableArrayList<Experiment3Model>()

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

    private var coef: Double = 100.0

    private var voltageList: ArrayList<Double> = ArrayList()
    private var timeList: ArrayList<Double> = ArrayList()
    private var currentDot = XYChart.Series<Number, Number>()

    private val isThereAreAccidents: Boolean
        get() {
            if (isCanceled) {
                isExperimentRunning = false
                isExperimentEnded = true
            }
            return !isCanceled
        }

    private val isDevicesResponding: Boolean
        get() = isOwenPRResponding && isAvemResponding && isDeltaResponding && isLatrResponding
                && isParmaResponding && isKiloAvemResponding

    private val points = ArrayList<Point>()

    @FXML
    private fun initialize() {
        if (Main.css == "white") {
            root.stylesheets[0] = Main::class.java.getResource("styles/main_css.css").toURI().toString()
        } else {
            root.stylesheets[0] = Main::class.java.getResource("styles/main_css_black.css").toURI().toString()
        }

        experiment3Model = mainModel.experiment3Model
        experiment3Data.add(experiment3Model)
        tableViewExperiment3.items = experiment3Data
        tableViewExperiment3.selectionModel = null
        communicationModel.addObserver(this)
        voltageList = currentProtocol.voltageViuDC
        timeList = currentProtocol.timesViuDC

        tableColumnU.setCellValueFactory { cellData -> cellData.value.voltageProperty() }
        tableColumnIA.setCellValueFactory { cellData -> cellData.value.currentAProperty() }
        tableColumnIB.setCellValueFactory { cellData -> cellData.value.currentBProperty() }
        tableColumnIC.setCellValueFactory { cellData -> cellData.value.currentCProperty() }
        tableColumnILeak.setCellValueFactory { cellData -> cellData.value.currentLeakProperty() }
        tableColumnFrequency.setCellValueFactory { cellData -> cellData.value.frequencyProperty() }
        tableColumnResultExperiment3.setCellValueFactory { cellData -> cellData.value.resultProperty() }

        val createLoadDiagram = createLoadDiagram()
        lineChartExperiment3.data.addAll(createLoadDiagram)
    }

    private fun createLoadDiagram(): XYChart.Series<Number, Number> {
        val seriesTimesAndTorques = XYChart.Series<Number, Number>()

        var desperateDot = 0.0

        seriesTimesAndTorques.data.add(XYChart.Data(desperateDot, currentProtocol.voltageViuDC[0]))

        for (i in 0 until currentProtocol.timesViuDC.size) {
            seriesTimesAndTorques.data.add(XYChart.Data(desperateDot + currentProtocol.timesViuDC[i], currentProtocol.voltageViuDC[i]))
            if (i != currentProtocol.timesViuDC.size - 1) {
                seriesTimesAndTorques.data.add(XYChart.Data(desperateDot + currentProtocol.timesViuDC[i], currentProtocol.voltageViuDC[i + 1]))
            }
            desperateDot += currentProtocol.timesViuDC[i]
        }

        seriesTimesAndTorques.data.add(XYChart.Data(desperateDot, 0))

        return seriesTimesAndTorques
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
        experiment3Model!!.clearProperties()
        isSchemeReady = false
        cause = ""

        Thread {
            if (isExperimentRunning) {
                appendOneMessageToLog("Визуально осматривайте трансфоматор на наличие потеков масла перед каждым опытом")
                communicationModel.initOwenPrController()
                appendOneMessageToLog("Начало испытания")
                communicationModel.initExperimentDevices()
                sleep(4000)
            }

            while (isExperimentRunning && !isStartButtonOn) {
                appendOneMessageToLog("Включите кнопочный пост")
                sleep(10)
            }

            if (isExperimentRunning) {
                communicationModel.setKiloAvemShowValue(Constants.Avem.VOLTAGE_AMP.ordinal)
                appendOneMessageToLog("Устанавливаем начальные точки для ЧП")
                communicationModel.setObjectParams(50 * 100, 380 * 10, 50 * 100)
                appendOneMessageToLog("Запускаем ЧП")
//                communicationModel.прием_команды_on()
                resetOmik()
            }

            if (isExperimentRunning) {
//                appendOneMessageToLog("Поднимаем напряжение на объекте испытания до $firstVoltageLatr")
//                communicationModel.startUpLATR(firstVoltageLatr, true)
//                waitingLatrCoarse(firstVoltageLatr)
//                sleep(2000)
//                fineLatrCoarse(firstVoltageLatr)
            }

            if (isExperimentRunning) {
                communicationModel.startUpLATRFast((voltageList[0] / coef).toFloat(), true)
            }

            var timeSum = 0.0

            if (isExperimentRunning && isDevicesResponding) {
                for (i in voltageList.indices) {
                    var timePassed = 0.0
                    if (isExperimentRunning && isDevicesResponding) {
                        appendOneMessageToLog("Началась регулировка")
                        communicationModel.startUpLATRFast((voltageList[i] / coef).toFloat(), false)
                        waitingLatrCoarse(voltageList[i].toFloat())
                        fineLatrCoarse(voltageList[i].toFloat())
                        if (measuringULatr < measuringU * 0.5 && measuringULatr * 0.5 > measuringU) {
                            setCause("Коэфицент трансформации сильно отличается")
                        }
                        appendOneMessageToLog("Регулировка окончена")
                    }
                    val time = timeList[i] * MILLS_IN_SEC
                    while (isExperimentRunning && timePassed < time) {
                        sleep(100)
                        timePassed += 100.0
                        drawDot(timeSum, timeSum + currentProtocol.timesViuDC[i], currentProtocol.voltageViuDC[i], timePassed / time)
                    }
                    timeSum += currentProtocol.timesViuDC[i]
                }
            }

            isNeedToRefresh = false
            communicationModel.startUpLATRFast(1f, true)
            while (measuringU > 800) {
                sleep(10)
            }
            communicationModel.stopLATR()
            resetOmik()
            communicationModel.stopObject()
            sleep(3000)
            communicationModel.offAllKms()
            sleep(50)
            communicationModel.finalizeAllDevices()

            if (cause != "") {
                appendMessageToLog(String.format("Испытание прервано по причине: %s", cause))
                experiment3Model!!.result = "Неуспешно"
            } else if (!isDevicesResponding) {
                appendMessageToLog(getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"))
                experiment3Model!!.result = "Неуспешно"
            } else {
                experiment3Model!!.result = "Успешно"
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

    private fun drawDot(x1: Double, x2: Double, y: Double, percent: Double) {
        Platform.runLater {
            lineChartExperiment3.data.removeAll(currentDot)
            currentDot = createCurrentDot(x1, x2, y, percent)
            lineChartExperiment3.data.addAll(currentDot)
        }
    }

    private fun createCurrentDot(x1: Double, x2: Double, y: Double, percent: Double): XYChart.Series<Number, Number> {
        val currentDot = XYChart.Series<Number, Number>()
        currentDot.data.add(XYChart.Data(x1 + (x2 - x1) * percent, y))
        return currentDot
    }

    private fun waitingLatrCoarse(voltage: Float) {
        appendOneMessageToLog("Грубая регулировка")
        while (isExperimentRunning && isDevicesResponding && (measuringU <= voltage - 2000 || measuringU > voltage + 750)) {
            if (measuringU <= voltage - 2000) {
                communicationModel.startUpLATRFast(380f, false)
            } else if (measuringU > voltage + 750) {
                communicationModel.startUpLATRFast(1f, false)
            } else {
                break
            }
        }
        communicationModel.stopLATR()

        while (isExperimentRunning && isDevicesResponding && (measuringU <= voltage - 600 && measuringU > voltage + 300)) {
            if (measuringU <= voltage - 600) {
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

    private fun fineLatrCoarse(voltage: Float) {
        appendOneMessageToLog("Точная регулировка")
        sleep(2000)
        while ((measuringU <= voltage - 150 || measuringU >= voltage + 150) && isExperimentRunning) {
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
//        while ((measuringU <= voltage * 0.99 || measuringU >= voltage * 1.01) && isExperimentRunning) {
//            appendOneMessageToLog("Точная регулировка")
//            sleep(1000)
//            if (measuringU <= voltage * 0.99) {
//                appendMessageToLog("Accurate UP")
//                communicationModel.startUpLATR(380f, false)
//                sleep(1500)
//                communicationModel.stopLATR()
//            } else if (measuringU >= voltage * 1.01) {
//                appendMessageToLog("Accurate DOWN")
//                communicationModel.startUpLATR(1f, false)
//                sleep(1500)
//                communicationModel.stopLATR()
//            }
//        }
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
            communicationModel.stopObject()
        }
        if (statusEndsVFD == OMIK_DOWN_END) {
            appendOneMessageToLog("ОМИК в нижнем положении")
        }
    }

    private fun appendMessageToLog(message: String) {
        Platform.runLater {
            textAreaExperiment3Log.appendText(String.format("%s \t| %s\n", sdf.format(System.currentTimeMillis()), message))
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
                    experiment3Model!!.currentA = IA
                    if (measuringIA > 45) {
                        appendMessageToLog("Ток А превышает 45А")
                    }
                }
                PM130Model.I2_PARAM -> {
                    measuringIB = value as Float * 2
                    val IB = String.format("%.4f", measuringIB)
                    experiment3Model!!.currentB = IB
                    if (measuringIB > 45) {
                        appendMessageToLog("Ток B превышает 45А")
                    }
                }
                PM130Model.I3_PARAM -> {
                    measuringIC = value as Float * 16
                    val IC = String.format("%.4f", measuringIC)
                    experiment3Model!!.currentC = IC
                    if (measuringIC > 45) {
                        appendMessageToLog("Ток C превышает 45А")
                    }
                }
                PM130Model.F_PARAM -> {
                    measuringF = value as Float
                    val FParma = String.format("%.2f", measuringF)
                    experiment3Model!!.frequency = FParma
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
                    experiment3Model!!.currentLeak = IAvem
                }
            }
            KILOAVEM_ID -> when (param) {
                AvemVoltmeterModel.RESPONDING_PARAM -> {
                    isKiloAvemResponding = value as Boolean
                    Platform.runLater { deviceStateCircleKiloAvem.fill = if (value) Color.LIME else Color.RED }
                }
                AvemVoltmeterModel.U_RMS_PARAM -> {
                    measuringU = abs((value as Float) * 1000)
                    val kiloAvemU = String.format("-%.2f", measuringU)
                    experiment3Model!!.voltage = kiloAvemU
                }
            }

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
                    measuringULatr = (value as Float) * 102
                }
            }
        }
    }

    private fun checkLatrStatus() {
        when (latrStatus) {
            LATR_STARTED -> {
                Log.i("", "Выход ЛАТРа на заданное напряжение")
            }
            LATR_WAITING -> {
                Log.i("", "Выдерживаем заданное напряжение на ЛАТРе")
            }
            LATR_CONFIG -> {
                Log.i("", "Режим кофигурации ЛАТР")
            }
            LATR_STOP_RESET -> {
                Log.i("", "Стоп/Ресет ЛАТР")
            }
        }
    }

    private fun checkLatrError() {
        when (latrStatus) {
            LATR_UP_END -> {
                Log.i("", "Сработал верхний концевик ЛАТРа.")
            }
            LATR_DOWN_END -> {
                Log.i("", "Сработал нижний концевик ЛАТРа.")
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
                Log.i("", "Замкнут верхний концевик ОМИКа.")
            }
            OMIK_DOWN_END -> {
                Log.i("", "Замкнут нижний концевик ОМИКа.")
            }
            OMIK_BOTH_END -> {
                setCause("Замкнуты оба концевика ОМИКа.")
            }
            OMIK_NOONE_END -> {
                Log.i("", "Оба концевика ОМИКа не замкнуты")
            }
        }
    }

    private fun checkVFDStatus() {
        when (statusVFD) {
            VFD_FORWARD -> {
                Log.i("", "FORWARD")
            }
            VFD_REVERSE -> {
                Log.i("", "REVERSE")
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