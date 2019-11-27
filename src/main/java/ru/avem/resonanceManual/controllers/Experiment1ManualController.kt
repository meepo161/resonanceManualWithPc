package ru.avem.resonanceManual.controllers

import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.control.Button
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TextArea
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.text.Text
import javafx.stage.Stage
import ru.avem.resonanceManual.Constants
import ru.avem.resonanceManual.Constants.Ends.*
import ru.avem.resonanceManual.Constants.Vfd.VFD_FORWARD
import ru.avem.resonanceManual.Constants.Vfd.VFD_REVERSE
import ru.avem.resonanceManual.Main
import ru.avem.resonanceManual.communication.CommunicationModel
import ru.avem.resonanceManual.communication.devices.avem_voltmeter.AvemVoltmeterModel
import ru.avem.resonanceManual.communication.devices.deltaC2000.DeltaCP2000Model
import ru.avem.resonanceManual.communication.devices.ipp120.OwenIPP120Controller.*
import ru.avem.resonanceManual.communication.devices.latr.LatrModel
import ru.avem.resonanceManual.communication.devices.pm130.PM130Model
import ru.avem.resonanceManual.communication.devices.pr200.OwenPRModel
import ru.avem.resonanceManual.communication.modbus.utils.Utils
import ru.avem.resonanceManual.model.Experiment1ManualModel
import ru.avem.resonanceManual.model.MainModel
import ru.avem.resonanceManual.model.Point
import ru.avem.resonanceManual.utils.Toast
import ru.avem.resonanceManual.utils.Utils.sleep
import java.lang.Math.abs
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Experiment1ManualController : DeviceState(), ExperimentController {

    @FXML
    lateinit var tableViewExperiment1: TableView<Experiment1ManualModel>
    @FXML
    lateinit var tableViewExperiment2: TableView<Experiment1ManualModel>
    @FXML
    lateinit var tableColumnU: TableColumn<Experiment1ManualModel, String>
    @FXML
    lateinit var tableColumnIB: TableColumn<Experiment1ManualModel, String>
    @FXML
    lateinit var tableColumnUOI: TableColumn<Experiment1ManualModel, String>
    @FXML
    lateinit var tableColumnIOI: TableColumn<Experiment1ManualModel, String>
    @FXML
    lateinit var tableColumnResultExperiment1: TableColumn<Experiment1ManualModel, String>
    @FXML
    lateinit var tableColumnF: TableColumn<Experiment1ManualModel, String>
    @FXML
    lateinit var tableColumnCoefAMP: TableColumn<Experiment1ManualModel, String>
    @FXML
    lateinit var tableColumnI1: TableColumn<Experiment1ManualModel, String>
    @FXML
    lateinit var textAreaExperiment1Log: TextArea
    @FXML
    lateinit var lineChartExperiment1: LineChart<Number, Number>
    @FXML
    lateinit var textExperiment: Text
    @FXML
    lateinit var xAxis: NumberAxis
    @FXML
    lateinit var yAxis: NumberAxis
    @FXML
    lateinit var buttonCancelAll: Button
    @FXML
    lateinit var buttonStop: Button
    @FXML
    lateinit var buttonSaveDot: Button
    @FXML
    lateinit var root: AnchorPane
    @FXML
    lateinit var hBoxTable2: HBox

    private val mainModel = MainModel.instance
    private val currentProtocol = mainModel.currentProtocol
    private val communicationModel = CommunicationModel.getInstance()
    private var experiment1ManualModel: Experiment1ManualModel? = null
    private val experiment1Data = FXCollections.observableArrayList<Experiment1ManualModel>()

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
    private var isPM130Responding: Boolean = false
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
    private var measuringUAMP: Float = 0.0f
    @Volatile
    private var measuringURMS: Float = 0.0f
    @Volatile
    private var measuringUAMPMinute: Float = 0.0f
    @Volatile
    private var measuringURMSMinute: Float = 0.0f
    @Volatile
    private var measuringULatr: Float = 0.0f
    @Volatile
    private var measuringIA: Float = 0.0f
    @Volatile
    private var measuringIB: Float = 0.0f
    @Volatile
    private var measuringIC: Float = 0.0f
    @Volatile
    private var isControlRubilNeed: Boolean = false
    @Volatile
    private var isManualNeed: Boolean = false
    @Volatile
    private var isCoefNeed: Boolean = false
    @Volatile
    private var isTimerNeed: Boolean = false

    @Volatile
    private var currentProtectionToTransformer: Boolean = false
    @Volatile
    private var currentProtectionTestObject: Boolean = false
    @Volatile
    private var currentProtectionAfterTransformer: Boolean = false
    @Volatile
    private var doorControl: Boolean = false
    @Volatile
    private var startControl: Boolean = false
    @Volatile
    private var breakerControl: Boolean = false
    @Volatile
    private var manualModeWithPC: Boolean = false
    @Volatile
    private var acVoltage: Boolean = false
    @Volatile
    private var dcVoltage: Boolean = false
    @Volatile
    private var resonance: Boolean = false
    @Volatile
    private var startTimer: Boolean = false
    @Volatile
    private var stopTimer: Boolean = false
    @Volatile
    private var stopExperiment: Boolean = false
    @Volatile
    private var upVoltage: Boolean = false
    @Volatile
    private var downVoltage: Boolean = false
    @Volatile
    private var statusEndsVFD: Short = 0
    @Volatile
    private var statusVFD: Short = 0
    @Volatile
    private var modeOperating: Int = 0
    private var modeOperatingLast: Int = 0
    private var isModeOperatingSelected: Boolean = true

    private var coef: Double = 0.0

    private var voltageList: ArrayList<Double> = ArrayList()
    private var timeList: ArrayList<Double> = ArrayList()
    private var speedList: ArrayList<Double> = ArrayList()
    private var timeSum = 0.0
    private var seriesTimesAndVoltage = XYChart.Series<Number, Number>()
    private var realTime = 0.0
    private var isDiagramNeed = false

    private val isDevicesResponding: Boolean
        get() = isOwenPRResponding && isAvemResponding && isDeltaResponding && isLatrResponding
                && isPM130Responding && isKiloAvemResponding

    private val points = ArrayList<Point>()

    @FXML
    private fun initialize() {
        if (Main.css == "white") {
            root.stylesheets[0] = Main::class.java.getResource("styles/main_css.css").toURI().toString()
        } else {
            root.stylesheets[0] = Main::class.java.getResource("styles/main_css_black.css").toURI().toString()
        }
        cause = ""
        experiment1ManualModel = mainModel.experiment1ManualModel
        experiment1Data.add(experiment1ManualModel)
        tableViewExperiment1.items = experiment1Data
        tableViewExperiment1.selectionModel = null
        tableViewExperiment2.items = experiment1Data
        tableViewExperiment2.selectionModel = null
        communicationModel.addObserver(this)
        voltageList = currentProtocol.voltageResonance
        timeList = currentProtocol.timesResonance
        speedList = currentProtocol.speedResonance
        tableColumnU.setCellValueFactory { cellData -> cellData.value.voltageARNProperty() }
        tableColumnIB.setCellValueFactory { cellData -> cellData.value.currentBProperty() }
        tableColumnUOI.setCellValueFactory { cellData -> cellData.value.voltageProperty() }
        tableColumnIOI.setCellValueFactory { cellData -> cellData.value.currentOIProperty() }
        tableColumnF.setCellValueFactory { cellData -> cellData.value.frequencyProperty() }
        tableColumnCoefAMP.setCellValueFactory { cellData -> cellData.value.coefAmpProperty() }
        tableColumnI1.setCellValueFactory { cellData -> cellData.value.currentAProperty() }
        tableColumnResultExperiment1.setCellValueFactory { cellData -> cellData.value.resultProperty() }
        startExperiment()
        lineChartExperiment1.data.add(seriesTimesAndVoltage)
    }

    private fun createLoadDiagram() {
        isDiagramNeed = true
        Thread {
            while (isDiagramNeed) {
                if (realTime < 400) {
                    Platform.runLater {
                        if (measuringU < 100000) {
                            seriesTimesAndVoltage.data.add(XYChart.Data<Number, Number>(realTime, measuringU))
                        }
                    }
                } else {
                    Platform.runLater {
                        seriesTimesAndVoltage.data.clear()
                    }
                    realTime = 0.0
                }
                sleep(1000)
                realTime += 1
            }
        }.start()
    }

    @FXML
    private fun handleExperimentCancel() {
        isExperimentRunning = false
        buttonStop.isDisable = true
    }

    @FXML
    private fun handleExperimentClose() {
        dialogStage!!.close()
    }

    private fun startExperiment() {
        points.clear()
        setCause("")

        currentProtectionToTransformer = false
        currentProtectionTestObject = false
        currentProtectionAfterTransformer = false
        doorControl = false
        startControl = false
        breakerControl = false
        manualModeWithPC = false
        acVoltage = false
        dcVoltage = false
        resonance = false
        startTimer = false
        stopTimer = false
        stopExperiment = false
        upVoltage = false
        downVoltage = false

        points.clear()
        isNeedToRefresh = true
        isNeedCheckLatrStatus = false
        isExperimentRunning = true
        isExperimentEnded = false
//        buttonCancelAll.isDisable = true
        experiment1ManualModel!!.clearProperties()
        isControlRubilNeed = false
        modeOperatingLast = 0
        modeOperating = 0
        isModeOperatingSelected = false
        isManualNeed = false
        isTimerNeed = false
        buttonStop.isDisable = false
        buttonSaveDot.isDisable = false

        Thread {

            if (isExperimentRunning) {
                communicationModel.showStrings(ИНИЦИАЛИЗАЦИЯ_УСТРОЙСТВ)
                appendOneMessageToLog("Инициализация системы")
                communicationModel.initOwenPrController()
                communicationModel.initExperimentDevices()
            }

            while (!isDevicesResponding) {
                sleep(10)
            }

            if (isExperimentRunning && isDevicesResponding) {
                communicationModel.resetLATR()
                communicationModel.onWaitCommand()
            }

            while (modeOperating == 0 && isExperimentRunning && isDevicesResponding) {
                communicationModel.showStrings(РЕЖИМ_ИСПЫТАНИЯ)
                appendOneMessageToLog("Выберите режим испытания")
            }

            if (isExperimentRunning && isDevicesResponding) {
                if (dcVoltage) {
                    tableColumnCoefAMP.isVisible = false
                    tableColumnF.isVisible = false
                } else {
                    tableColumnCoefAMP.isVisible = true
                    tableColumnF.isVisible = true
                }
            }

            if (!breakerControl && isExperimentRunning && isDevicesResponding) {
                sleep(1000)
                communicationModel.launchPermissionOff()
                communicationModel.showStrings(ВКЛЮЧИТЕ_РУБИЛЬНИК)
                appendOneMessageToLog("Поднимите рубильник силового питания")
            }

            while (!breakerControl && isExperimentRunning && isDevicesResponding) {
                sleep(10)
            }

            if (isExperimentRunning && isDevicesResponding) {
                communicationModel.onLaunchPermission()
                communicationModel.showStrings(НАЖМИТЕ_ПУСК)
                appendOneMessageToLog("Нажмите кнопку ПУСК")
            }

            while (!startControl && isExperimentRunning && isDevicesResponding) {
                sleep(10)
            }

            if (isExperimentRunning && isDevicesResponding) {
                modeOperatingLast = modeOperating
            }

            if (isExperimentRunning && isDevicesResponding) {
                currentProtocol.typeExperiment = textExperiment.text
                communicationModel.showStrings(ПОИСК_РЕЗОНАНСА)
                appendOneMessageToLog("Начало испытания")
                isModeOperatingSelected = true
                isControlRubilNeed = true
            }

            if (isExperimentRunning && isDevicesResponding) {
                appendOneMessageToLog("Устанавливаем начальные точки для ЧП")
                communicationModel.setObjectParams(50 * 100, 380 * 10, 50 * 100)
                appendOneMessageToLog("Запускаем ЧП")
                resetOmik()
                when {
                    dcVoltage -> {
                        communicationModel.setKiloAvemShowValue(Constants.Avem.VOLTAGE_AMP.ordinal)
                        communicationModel.onParallelConnection()
                        communicationModel.onAvem()
                    }
                    resonance -> {
                        communicationModel.onSeriesConnection()
                        communicationModel.setKiloAvemShowValue(Constants.Avem.VOLTAGE_RMS.ordinal)
                    }
                    acVoltage -> {
                        communicationModel.onParallelConnection()
                        communicationModel.setKiloAvemShowValue(Constants.Avem.VOLTAGE_RMS.ordinal)
                    }
                }
                communicationModel.onShortCircuiter()
            }


            if (isExperimentRunning && isDevicesResponding) {
                if (!dcVoltage) {
                    communicationModel.offWaitCommand()
                    appendOneMessageToLog("Поднимаем напряжение на объекте испытания для поиска резонанса")
                    communicationModel.resetLATR()
                    putUpLatrForResonance()
                    findResonance()
                }
            }

            timeSum = 0.0

            if (isExperimentRunning && isDevicesResponding) {
                isManualNeed = true
                isTimerNeed = true
                communicationModel.onWaitCommand()
                communicationModel.onLightTimer()
                communicationModel.onLightVoltage()
                createLoadDiagram()

                var time = 0.0
                while (isExperimentRunning && isDevicesResponding) {
                    communicationModel.showCurrents(measuringIA, measuringIB, measuringIC)
                    sleep(100)
                    if (isCoefNeed) {
                        measuringUAMPMinute += measuringUAMP
                        measuringURMSMinute += measuringURMS
                        time += 0.1
                    } else {
                        measuringUAMPMinute = 0f
                        measuringURMSMinute = 0f
                        time = 0.0
                    }
                    if (time >= 15) {
                        val coefAmp = String.format("%.4f", measuringUAMPMinute / measuringURMSMinute)
                        experiment1ManualModel!!.coefAmp = coefAmp
                        measuringUAMPMinute = 0f
                        measuringURMSMinute = 0f
                        time = 0.0
                    }
                }
            }

            if (isExperimentRunning) {
                communicationModel.showStrings(ЗАВЕРШЕНИЕ_ОПЫТА)
            }

            communicationModel.offTimer()
            communicationModel.offWaitCommand()
            communicationModel.offLightVoltage()
            communicationModel.offLightTimer()
            isTimerNeed = false
            isManualNeed = false
            isNeedToRefresh = false
            communicationModel.startUpLATRUp(0f, true)

            while (measuringU > 1300) {
                sleep(10)
            }

            communicationModel.stopLATR()
            resetOmik()
            var timeToSleep = 300
            while (isExperimentRunning && (timeToSleep-- > 0) && isDevicesResponding) {
                sleep(10)
            }
            isControlRubilNeed = false

            communicationModel.offAllKms()

            if (breakerControl && isDevicesResponding) {
                appendOneMessageToLog("Отключите рубильник")
                communicationModel.onAttention()
                communicationModel.showStrings(ОТКЛЮЧИТЕ_РУБИЛЬНИК)
            }

            while (breakerControl && isDevicesResponding) {
                sleep(10)
            }

            if (isExperimentRunning && isDevicesResponding) {
                communicationModel.offAttention()
            }

            if (isExperimentRunning) {
                communicationModel.showStrings(ОЖИДАНИЕ_ДЕЙСТВИЙ)
            }
            isDiagramNeed = false
            communicationModel.finalizeAllDevices()

            if (cause != "") {
                appendMessageToLog(String.format("Испытание прервано по причине: %s", cause))
                experiment1ManualModel!!.result = "Завершено"
            } else if (!isDevicesResponding) {
                appendMessageToLog(getNotRespondingDevicesString())
                experiment1ManualModel!!.result = "Завершено"
            } else {
                experiment1ManualModel!!.result = "Завершено"
                appendMessageToLog("Испытание завершено успешно")
            }
            appendMessageToLog("\n------------------------------------------------\n")

            isExperimentEnded = true
            isExperimentRunning = false
            Platform.runLater {
                buttonCancelAll.isDisable = false
                buttonSaveDot.isDisable = true
            }
        }.start()
    }

    @FXML
    fun handleSaveDot() {
        fillPointData()
        Toast.makeText("Точка сохранена").show(Toast.ToastType.CONFIRM)
    }

    private fun fillPointData() {
        points.add(
                Point(
                        String.format("%.2f", measuringU),
                        String.format("%.2f", measuringIC),
                        String.format("%s", sdf.format(System.currentTimeMillis()))
                )
        )
        currentProtocol.points = points
    }

    private fun findResonance() {
        appendOneMessageToLog("Идет поиск резонанса")
        if (statusVFD == VFD_REVERSE && isExperimentRunning && isDevicesResponding) {
            communicationModel.changeRotation()
            sleep(2000)
        }
        communicationModel.startObject()
        var highestU = measuringU
        var lowestI = measuringIB
        var step = 5
        while ((step-- > 0) && isExperimentRunning && isDevicesResponding) {
            if (measuringU > highestU) {
                highestU = measuringU
                step = 5
            }
            if (measuringIB < lowestI) {
                lowestI = measuringIB
                step = 5
            }
            if (statusEndsVFD == OMIK_UP_END) {
                appendOneMessageToLog("ОМИК достиг верхнего концевика")
                break
            }
            sleep(500)
        }
        communicationModel.stopObject()
        sleep(3000)
        communicationModel.changeRotation()
        communicationModel.setObjectParams(25 * 100, 380 * 10, 25 * 100)
        communicationModel.startObject()
        while (measuringU < highestU && measuringIB > lowestI && isExperimentRunning && isDevicesResponding) { //Из-за инерции
            if (statusEndsVFD == OMIK_DOWN_END) {
                appendOneMessageToLog("ОМИК достиг нижнего концевика")
                break
            }
            sleep(10)
        }
        communicationModel.stopObject()
        appendOneMessageToLog("Поиск завершен")
        sleep(1000)
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
                    break
                }
            }
            communicationModel.stopObject()
        }
        if (statusEndsVFD == OMIK_DOWN_END) {
            appendOneMessageToLog("ОМИК в нижнем положении")
        }
        communicationModel.stopObject()
    }

    private fun putUpLatrForResonance() {
        communicationModel.startUpLATRUp(7f, true)
        waitingLatrCoarseForResonance()
    }

    private fun waitingLatrCoarseForResonance() {
        while (isExperimentRunning && isDevicesResponding && (measuringU <= 1000 || measuringU > 1500)) {

            if (measuringU <= 1000) {
                communicationModel.startUpLATRWithRegulationSpeed(440f, false, 50f, 60f)
                while (isExperimentRunning && isDevicesResponding && measuringU <= 1000) {
                    sleep(1)
                }
            } else if (measuringU > 1500) {
                communicationModel.startUpLATRWithRegulationSpeed(1f, false, 50f, 60f)
                while (isExperimentRunning && isDevicesResponding && measuringU > 1500) {
                    sleep(1)
                }
            }
        }
        communicationModel.stopLATR()
    }

    private fun appendMessageToLog(message: String) {
        Platform.runLater {
            textAreaExperiment1Log.appendText(
                    String.format(
                            "%s \t| %s\n",
                            sdf.format(System.currentTimeMillis()),
                            message
                    )
            )
        }
    }

    private fun appendOneMessageToLog(message: String) {
        if (logBuffer == null || logBuffer != message) {
            logBuffer = message
            appendMessageToLog(message)
        }
    }

    private fun getNotRespondingDevicesString(): String {
        return String.format(
                "%s %s%s%s%s%s%s",
                "Испытание прервано по причине: потеряна связь с устройствами",
                if (isOwenPRResponding) "" else "Овен ПР ",
                if (isPM130Responding) "" else "ПМ130",
                if (isDeltaResponding) "" else "Дельта ",
                if (isLatrResponding) "" else "Латр ",
                if (isAvemResponding) "" else "АВЭМ ",
                if (isKiloAvemResponding) "" else "КилоАВЭМ "
        )
    }

    private fun setCause(cause: String) {
        this.cause = cause
        if (cause.isNotEmpty()) {
            isExperimentRunning = false
        }
    }

    override fun update(o: Observable, values: Any) {
        val modelId = (values as Array<*>)[0] as Int
        val param = values[1] as Int
        val value = values[2]

        when (modelId) {
            PR200_ID -> when (param) {
                OwenPRModel.RESPONDING_PARAM -> {
                    isOwenPRResponding = value as Boolean
                    Platform.runLater { deviceStateCirclePR200.fill = if (value) Color.LIME else Color.RED }
                }
                OwenPRModel.ТКЗ_ДО_ТРАНСФОРМАТОРА -> {
                    currentProtectionToTransformer = value as Boolean
                    if (currentProtectionToTransformer) {
                        communicationModel.offAllKms()
                        setCause("ткзДоТрансформатора")
                        communicationModel.showStrings(ТОКОВАЯ_ЗАЩИТА_ДО_ТР)
                    }
                }
                OwenPRModel.ТКЗ_ОИ -> {
                    currentProtectionTestObject = value as Boolean
                    if (currentProtectionTestObject) {
                        communicationModel.offAllKms()
                        setCause("ткзОИ")
                        communicationModel.showStrings(ТОКОВАЯ_ЗАЩИТА_ОИ)
                    }
                }
                OwenPRModel.ТКЗ_ПОСЛЕ_ТРАНСФОРМАТОРА -> {
                    currentProtectionAfterTransformer = value as Boolean
                    if (currentProtectionAfterTransformer) {
                        communicationModel.offAllKms()
                        setCause("ткзПослеТрансформатора")
                        communicationModel.showStrings(ТОКОВАЯ_ЗАЩИТА_ЗА_ТР)
                    }
                }
                OwenPRModel.КОНТРОЛЬ_ДВЕРЕЙ_ШСО -> {
                    doorControl = value as Boolean
                    if (doorControl) {
                        communicationModel.offAllKms()
                        setCause("контрольДверей")
                        communicationModel.showStrings(ДВЕРИ_ШКАФА)
                    }
                }
                OwenPRModel.КОНТРОЛЬ_ПУСКА -> {
                    startControl = value as Boolean
                    if (!startControl && isControlRubilNeed) {
                        communicationModel.offAllKms()
                        setCause("Сработала защита")
                        communicationModel.showStrings(ОШИБКА)
                    }
                }
                OwenPRModel.КОНТРОЛЬ_РУБИЛЬНИКА -> {
                    breakerControl = value as Boolean
                    if (!breakerControl && isControlRubilNeed) {
                        setCause("Во время испытания отключен рубильник силового питания")
                    }
                }
                OwenPRModel.РУЧНОЙ_РЕЖИМ_С_ПК -> {
                    manualModeWithPC = value as Boolean
                    if (!manualModeWithPC) {
                        setCause("Для продолжения переключите на Ручной режим с ПК")
                    }
                }
                OwenPRModel.ПЕРЕМЕННОЕ -> {
                    acVoltage = value as Boolean
                    if (acVoltage) {
                        textExperiment.text = "ВИУ переменным напряжением"
                        modeOperating = 1
                    }
                }
                OwenPRModel.ПЕРЕМЕННОЕ_С_РЕЗОНАНСОМ -> {
                    resonance = value as Boolean
                    if (resonance) {
                        textExperiment.text = "ВИУ резонансное переменным напряжением"
                        modeOperating = 2
                    }
                }
                OwenPRModel.ПОСТОЯННОЕ -> {
                    dcVoltage = value as Boolean
                    if (dcVoltage) {
                        textExperiment.text = "ВИУ постоянным напряжением"
                        modeOperating = 3
                    }
                    if (!acVoltage && !resonance && !dcVoltage) {
                        modeOperating = 0
                        textExperiment.text = "ВНИМАНИЕ! ВЫБЕРИТЕ РЕЖИМ ИСПЫТАНИЯ!"
                        communicationModel.onWaitCommand()
                    }
                    if (isModeOperatingSelected && modeOperatingLast != modeOperating) {
                        setCause("Во время испытания был изменен режим испытания")
                    }
                }
                OwenPRModel.СТАРТ_ТАЙМЕР -> {
                    startTimer = value as Boolean
                    if (startTimer && isTimerNeed) {
                        communicationModel.onTimer()
                        communicationModel.offWaitCommand()
                        communicationModel.offLightVoltage()
                        isManualNeed = false
                        isCoefNeed = true
                    }
                }
                OwenPRModel.СТОП_ТАЙМЕР -> {
                    stopTimer = value as Boolean
                    if (stopTimer && isTimerNeed) {
                        communicationModel.offTimer()
                        communicationModel.onWaitCommand()
                        communicationModel.onLightVoltage()
                        isManualNeed = true
                        isCoefNeed = false
                    }
                }
                OwenPRModel.СТОП_ИСПЫТАНИЯ -> {
                    stopExperiment = value as Boolean
                    if (stopExperiment) {
                        communicationModel.offTimer()
                        communicationModel.offWaitCommand()
                        communicationModel.offLightVoltage()
                        isExperimentRunning = false
                    }
                }
                OwenPRModel.ПОДЪЕМ_НАПРЯЖЕНИЯ -> {
                    upVoltage = value as Boolean
                    if (upVoltage && !downVoltage && isManualNeed) {
                        communicationModel.startUpLATRWithRegulationSpeed(500f, false, 70f, 30f)
                    }
                    if (!upVoltage && !downVoltage && isManualNeed) {
                        communicationModel.stopLATR()
                    }
                }
                OwenPRModel.УМЕНЬШЕНИЕ_НАПРЯЖЕНИЯ -> {
                    downVoltage = value as Boolean
                    if (downVoltage && !upVoltage && isManualNeed) {
                        communicationModel.startUpLATRWithRegulationSpeed(1f, false, 70f, 30f)
                    }
                    if (!downVoltage && !upVoltage && isManualNeed) {
                        communicationModel.stopLATR()
                    }
                }
            }

            PM130_ID -> when (param) {
                PM130Model.RESPONDING_PARAM -> {
                    isPM130Responding = value as Boolean
                    Platform.runLater { deviceStateCirclePM130.fill = if (value) Color.LIME else Color.RED }
                }
                PM130Model.I1_PARAM -> {
                    measuringIA = value as Float * 20
                    val iA = String.format("%.2f", measuringIA)
                    experiment1ManualModel!!.currentA = iA
                }
                PM130Model.I2_PARAM -> {
                    measuringIB = value as Float * 20
                    val iB = String.format("%.2f", measuringIB)
                    experiment1ManualModel!!.currentB = iB
                    if (measuringIB > 45) {
                        appendMessageToLog("Ток B превышает 45А")
                    }
                }
                PM130Model.I3_PARAM -> {
                    if (!dcVoltage) {
                        measuringIC = value as Float
                        val iC = String.format("%.2f", measuringIC)
                        experiment1ManualModel!!.currentOI = iC
                        if (measuringIC > 45) {
                            appendMessageToLog("Ток C превышает 45А")
                        }
                    }
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
                AvemVoltmeterModel.U_RMS_PARAM -> {
                    if (dcVoltage) {
                        measuringIC = value as Float
                        val iC = String.format("%.4f", measuringIC)
                        experiment1ManualModel!!.currentOI = iC
                        if (measuringIC > 45) {
                            appendMessageToLog("Ток C превышает 45А")
                        }
                    }
                }
            }

            KILOAVEM_ID -> when (param) {
                AvemVoltmeterModel.RESPONDING_PARAM -> {
                    isKiloAvemResponding = value as Boolean
                    Platform.runLater { deviceStateCircleKiloAvem.fill = if (value) Color.LIME else Color.RED }
                }
                AvemVoltmeterModel.U_RMS_PARAM -> {
                    measuringURMS = (value as Float) * 1000
                    if (!dcVoltage) {
                        measuringU = value * 1000
                        coef = (measuringU / (measuringULatr / 140)).toDouble()
                        val kiloAvemU = String.format("%.2f", measuringU)
                        experiment1ManualModel!!.voltage = kiloAvemU
                    }
                }
                AvemVoltmeterModel.U_AMP_PARAM -> {
                    measuringUAMP = abs((value as Float) * 1000)
                    if (dcVoltage) {
                        measuringU = abs(value * 1000)
                        coef = (measuringU / (measuringULatr / 140)).toDouble()
                        val kiloAvemU = String.format("%.2f", measuringU)
                        experiment1ManualModel!!.voltage = kiloAvemU
                    }
                }
                AvemVoltmeterModel.F_PARAM -> {
                    val measuringF = (value as Float)
                    val kiloAvemF = String.format("%.2f", measuringF)
                    experiment1ManualModel!!.frequency = kiloAvemF
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
                    measuringULatr = (value as Float) * 140
                    val uLatr = String.format("%.2f", measuringULatr / 140)
                    experiment1ManualModel!!.voltageARN = uLatr
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
//                Log.d("", "Замкнут верхний концевик ОМИКа.")
            }
            OMIK_DOWN_END -> {
//                Log.d("", "Замкнут нижний концевик ОМИКа.")
            }
            OMIK_BOTH_END -> {
                setCause("Замкнуты оба концевика ОМИКа.")
            }
            OMIK_NOONE_END -> {
//                Log.d("", "Оба концевика ОМИКа не замкнуты")
            }
        }
    }

    private fun checkVFDStatus() {
        when (statusVFD) {
            VFD_FORWARD -> {
//                Log.d("", "FORWARD")
            }
            VFD_REVERSE -> {
//                Log.d("", "REVERSE")
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