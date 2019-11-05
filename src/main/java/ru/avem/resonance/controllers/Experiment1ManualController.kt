package ru.avem.resonance.controllers

import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.control.*
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Text
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
import ru.avem.resonance.db.model.TestItem
import ru.avem.resonance.model.Experiment1ManualModel
import ru.avem.resonance.model.MainModel
import ru.avem.resonance.model.Point
import ru.avem.resonance.utils.Log
import ru.avem.resonance.utils.Utils.sleep
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Experiment1ManualController : DeviceState(), ExperimentController {

    @FXML
    lateinit var tableViewExperiment1: TableView<Experiment1ManualModel>
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
    private var ткзДоТрансформатора: Boolean = false
    @Volatile
    private var ткзОИ: Boolean = false
    @Volatile
    private var ткзПослеТрансформатора: Boolean = false
    @Volatile
    private var контрольДверей: Boolean = false
    @Volatile
    private var контрольПуска: Boolean = false
    @Volatile
    private var ручнойРежим: Boolean = false
    @Volatile
    private var ручнойРежимСПК: Boolean = false
    @Volatile
    private var переменное: Boolean = false
    @Volatile
    private var постоянное: Boolean = false
    @Volatile
    private var резонанс: Boolean = false
    @Volatile
    private var старт: Boolean = false
    @Volatile
    private var стоп: Boolean = false
    @Volatile
    private var стопИспытания: Boolean = false
    @Volatile
    private var подъемНапряжения: Boolean = false
    @Volatile
    private var уменьшениеНапряжения: Boolean = false
    @Volatile
    private var statusEndsVFD: Short = 0
    @Volatile
    private var statusVFD: Short = 0

    private var coef: Double = 0.0

    private var voltageList: ArrayList<Double> = ArrayList()
    private var timeList: ArrayList<Double> = ArrayList()
    private var speedList: ArrayList<Double> = ArrayList()
    private var currentDot = XYChart.Series<Number, Number>()

    private val firstVoltage = 1500.0f

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
        //        get() = true
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
        experiment1ManualModel = mainModel.experiment1ManualModel
        experiment1Data.add(experiment1ManualModel)
        tableViewExperiment1.items = experiment1Data
        tableViewExperiment1.selectionModel = null
        communicationModel.addObserver(this)
        voltageList = currentProtocol.voltageResonance
        timeList = currentProtocol.timesResonance
        speedList = currentProtocol.speedResonance
        tableColumnU.setCellValueFactory { cellData -> cellData.value.voltageARNProperty() }
        tableColumnIB.setCellValueFactory { cellData -> cellData.value.currentBProperty() }
        tableColumnUOI.setCellValueFactory { cellData -> cellData.value.voltageProperty() }
        tableColumnIOI.setCellValueFactory { cellData -> cellData.value.currentOIProperty() }
        tableColumnResultExperiment1.setCellValueFactory { cellData -> cellData.value.resultProperty() }
        startExperiment()
    }

    private fun createLoadDiagram() {
//        Thread {
//            while (isExperimentRunning) {
//                sleep(1000)
//
//                Platform.runLater {
//                    realTime += 1
//                    lineChartExperiment1.data.clear()
//                    seriesTimesAndVoltage.data.add(XYChart.Data(realTime, measuringU))
//                    lineChartExperiment1.data.add(seriesTimesAndVoltage)
//                }
//            }
//        }.start()
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
        setCause("Отменено оператором")
        isExperimentRunning = false
    }


    private fun startExperiment() {
        points.clear()
        isNeedToRefresh = true
        isNeedCheckLatrStatus = false
        isExperimentRunning = true
        isExperimentEnded = false
        buttonNext.isDisable = true
        buttonCancelAll.isDisable = true
        experiment1ManualModel!!.clearProperties()
        isSchemeReady = false
        cause = ""
        lineChartExperiment1.data.clear()

        Thread {

            if (isExperimentRunning) {
                appendOneMessageToLog("Визуально осматривайте трансфоматор на наличие потеков масла перед каждым опытом")
                communicationModel.initOwenPrController()
                appendOneMessageToLog("Инициализация системы")
                communicationModel.initExperimentDevices()
                communicationModel.setKiloAvemShowValue(Constants.Avem.VOLTAGE_RMS.ordinal)
                sleep(1000)
                communicationModel.startUpLATRDown(0f, true)
            }

//            while (!isDevicesResponding) {
//                sleep(100)
//            }

            if (isExperimentRunning) {
                communicationModel.подсветкаНапряжения_On()  //TODO убрать
                communicationModel.разрешениеНаЗапуск_On()
                appendOneMessageToLog("ЖМИ ПУСК")
            }

            while (isExperimentRunning && !контрольПуска) {
                sleep(10)
            }

            if (isExperimentRunning) {
                appendOneMessageToLog("Начало испытания")
            }

//            while (isExperimentRunning && latrStatus != LATR_DOWN_END) {
//                sleep(100)
//            }

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


//            while (isPRDI6) {
//                appendOneMessageToLog("Включите рубильник")
//            }


            if (isExperimentRunning) {
                appendOneMessageToLog("Устанавливаем начальные точки для ЧП")
                communicationModel.setObjectParams(50 * 100, 380 * 10, 50 * 100)
                appendOneMessageToLog("Запускаем ЧП")
                resetOmik()
                communicationModel.параллельнаяСхема_On()
//                communicationModel.последовательнаяСхема_On()
                communicationModel.короткозамыкатель_On()
            }

//            if (isExperimentRunning) {
//                appendOneMessageToLog("Поднимаем напряжение на объекте испытания для поиска резонанса")
//                putUpLatr(1100f, 150)
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
                communicationModel.подсветкаТаймер_On()
                communicationModel.подсветкаНапряжения_On()
                createLoadDiagram()
                for (i in voltageList.indices) {
                    stackTriples[i].second.isDisable = true
                    timePassed = 0.0
                    if (isExperimentRunning && isDevicesResponding) {
                        appendOneMessageToLog("Началась регулировка")
//                        if (currentTestItem.speedResonance[i] > currentTestItem.speedResonance[i-1]) {
//                            selectDutyAndPulseForLatr(i)
//                        } else {
//                            duty = 48.0f
//                            pulse = 100.0f
//                        }
                        putUpLatr(voltageList[i].toFloat() * 1000, 150)
                        if (measuringULatr < measuringU * 0.5 && measuringULatr * 0.5 > measuringU) {
                            setCause("Коэфицент трансформации сильно отличается")
                        }

                        appendOneMessageToLog("Регулировка окончена")
                        Thread {
                            communicationModel.таймер_On()
                            communicationModel.таймер_Off()
                            communicationModel.звук_On()
                            sleep(3000)
                            communicationModel.звук_Off()
                        }.start()
                    }

                    time = currentTestItem.timesResonance[i] * MILLS_IN_SEC
                    while (isExperimentRunning && timePassed < time) {
                        sleep(100)
                        timePassed += 100.75 //потому что while занимает реально примерно 0.75 ms
                        if (time != stackTriples[i].second.text.toDouble() * MILLS_IN_SEC) {
                            time = currentTestItem.timesResonance[i] * MILLS_IN_SEC
                        }
                    }

                    Thread {
                        communicationModel.звук_On()
                        sleep(1500)
                        communicationModel.звук_Off()
                    }.start()

                    voltageList = currentTestItem.voltageResonance
                    timeSum += currentTestItem.timesResonance[i]
                    stackTriples[i].first.isDisable = true
                }
            }

            isNeedToRefresh = false
            communicationModel.startUpLATRDown(1f, true)
            while (measuringU > 1000) {
                sleep(10)
            }
            communicationModel.stopLATR()
            resetOmik()
//            communicationModel.stopObject()
            var timeToSleep = 300
            while (isExperimentRunning && (timeToSleep-- > 0)) {
                sleep(10)
            }
            communicationModel.offAllKms()

            timeToSleep = 200
            while (isExperimentRunning && (timeToSleep-- > 0)) {
                sleep(10)
            }

            communicationModel.finalizeAllDevices()

            if (cause != "") {
                appendMessageToLog(String.format("Испытание прервано по причине: %s", cause))
                experiment1ManualModel!!.result = "Неуспешно"
            } else if (!isDevicesResponding) {
                appendMessageToLog(getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"))
                experiment1ManualModel!!.result = "Неуспешно"
            } else {
                experiment1ManualModel!!.result = "Успешно"
                appendMessageToLog("Испытание завершено успешно")
            }
            appendMessageToLog("\n------------------------------------------------\n")

            Platform.runLater()
            {
                isExperimentEnded = true
                isExperimentRunning = false
                buttonNext.isDisable = false
                buttonCancelAll.isDisable = false
            }
        }.start()
    }

    private fun putUpLatr(voltage: Float, difference: Int) {
//        communicationModel.startUpLATRUp((voltage / coef).toFloat(), false)
//        while (measuringU < voltage - 1000 && measuringU < voltage + 1000 && isExperimentRunning) {
//            sleep(10)
//        }
//        sleep(1000)
//        waitingLatrCoarse(voltage)
//        fineLatr(voltage)
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
        var lowestI = measuringIB
        var step = 5
        appendOneMessageToLog("Идет поиск резонанса")
        while ((step-- > 0) && isExperimentRunning && isDevicesResponding) {
            if (measuringU > highestU) {
                highestU = measuringU
                step = 5
            }
            if (measuringIB < lowestI) {
                lowestI = measuringIB
                step = 5
            }
            sleep(500)
            Log.d("", "lowestI=$lowestI measuringIB=$measuringIB")
        }
        communicationModel.stopObject()
        sleep(3000)
        communicationModel.changeRotation()
        communicationModel.setObjectParams(25 * 100, 380 * 10, 25 * 100)
        communicationModel.startObject()
        while (measuringIB > lowestI && isExperimentRunning) { //Из-за инерции
            Log.d("", "lowestI=$lowestI measuringIB=$measuringIB")
            Log.d("", "measuringIB * 1.3 >= lowestI = ${measuringIB * 1.3 >= lowestI}")
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
        while (isExperimentRunning && isDevicesResponding && (measuringU <= voltage * 0.8 || measuringU > voltage * 1.2)) {
            if (measuringU * 1.2 > voltage && measuringU * 0.8 < voltage) {
                communicationModel.stopLATR()
                break
            }
            if (measuringU <= voltage * 0.8) {
                communicationModel.startUpLATRUp(440f, false)
            } else if (measuringU > voltage * 1.2) {
                communicationModel.startUpLATRDown(1f, false)
            }
        }
        communicationModel.stopLATR()
        appendOneMessageToLog("Грубая регулировка окончена")
    }

    private fun fineLatr(voltage: Float) {
        appendOneMessageToLog("Точная регулировка")
        communicationModel.stopLATR()
        while ((measuringU <= voltage * 0.9 || measuringU > voltage * 1.1) && isExperimentRunning) {
            if (measuringU * 1.1 > voltage && measuringU * 0.9 < voltage) {
                communicationModel.stopLATR()
                break
            }
            if (measuringU <= voltage * 0.9) {
                communicationModel.startUpLATRCharge(440f, false)
                if (measuringU + 1000 < voltage) {
                    sleep(3200)
                } else {
                    sleep(1600)
                }
                communicationModel.stopLATR()
            } else if (measuringU >= voltage * 1.1) {
                communicationModel.startUpLATRCharge(1f, false)
                if (measuringU - 1000 > voltage) {
                    sleep(3200)
                } else {
                    sleep(1600)
                }
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
            while (statusEndsVFD != OMIK_DOWN_END && isDevicesResponding && isExperimentRunning) {
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
        communicationModel.stopObject()
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
                OwenPRModel.ТКЗ_ДО_ТРАНСФОРМАТОРА -> {
                    ткзДоТрансформатора = value as Boolean
                    if (ткзДоТрансформатора) {
                        setCause("ткзДоТрансформатора")
                    }
                }
                OwenPRModel.ТКЗ_ОИ -> {
                    ткзОИ = value as Boolean
                    if (ткзОИ) {
                        setCause("ткзОИ")
                    }
                }
                OwenPRModel.ТКЗ_ПОСЛЕ_ТРАНСФОРМАТОРА -> {
                    ткзПослеТрансформатора = value as Boolean
                    if (ткзПослеТрансформатора) {
                        setCause("ткзПослеТрансформатора")
                    }
                }
                OwenPRModel.КОНТРОЛЬ_ДВЕРЕЙ_ШСО -> {
                    контрольДверей = value as Boolean
                    if (контрольДверей) {
                        setCause("контрольДверей")
                    }
                }
                OwenPRModel.КОНТРОЛЬ_ПУСКА -> {
                    контрольПуска = value as Boolean
                }
                OwenPRModel.РУЧНОЙ_РЕЖИМ -> {
                    ручнойРежим = value as Boolean
                }
                OwenPRModel.РУЧНОЙ_РЕЖИМ_С_ПК -> {
                    ручнойРежимСПК = value as Boolean
                }
                OwenPRModel.ПЕРЕМЕННОЕ -> {
                    переменное = value as Boolean
                    if (переменное) {
                        textExperiment.text = "ВИУ переменным напряжением"
                    }
                }
                OwenPRModel.ПЕРЕМЕННОЕ_С_РЕЗОНАНСОМ -> {
                    резонанс = value as Boolean
                    if (резонанс) {
                        textExperiment.text = "ВИУ резонансное переменным напряжением"
                    }
                }
                OwenPRModel.ПОСТОЯННОЕ -> {
                    постоянное = value as Boolean
                    if (постоянное) {
                        textExperiment.text = "ВИУ постоянным напряжением"
                    }
                }
                OwenPRModel.СТАРТ -> {
                    старт = value as Boolean
                }
                OwenPRModel.СТОП -> {
                    стоп = value as Boolean
                }
                OwenPRModel.СТОП_ИСПЫТАНИЯ -> {
                    стопИспытания = value as Boolean
                    if (стопИспытания) {
                        isExperimentRunning = false
                    }
                }
                OwenPRModel.ПОДЪЕМ_НАПРЯЖЕНИЯ -> {
                    подъемНапряжения = value as Boolean
                    if (подъемНапряжения) {
                        Platform.runLater {
                            while (подъемНапряжения) {
                                communicationModel.startUpLATRUp(440f, false)
                            }
                            communicationModel.stopLATR()
                        }
                    }
                }
                OwenPRModel.УМЕНЬШЕНИЕ_НАПРЯЖЕНИЯ -> {
                    уменьшениеНапряжения = value as Boolean
                    if (уменьшениеНапряжения) {
                        Platform.runLater {
                            while (уменьшениеНапряжения) {
                                communicationModel.startUpLATRDown(1f, false)
                            }
                            communicationModel.stopLATR()
                        }
                    }
                }
            }

            PM130_ID -> when (param) {
                PM130Model.RESPONDING_PARAM -> {
                    isParmaResponding = value as Boolean
                    Platform.runLater { deviceStateCirclePM130.fill = if (value) Color.LIME else Color.RED }
                }
                PM130Model.I2_PARAM -> {
                    measuringIB = value as Float * 20
                    val IB = String.format("%.4f", measuringIB)
                    experiment1ManualModel!!.currentB = IB
                    if (measuringIB > 45) {
                        appendMessageToLog("Ток B превышает 45А")
                    }
                }
                PM130Model.I3_PARAM -> {
                    measuringIC = value as Float * 2
                    val IC = String.format("%.4f", measuringIC)
                    experiment1ManualModel!!.currentOI = IC
                    if (measuringIC > 45) {
                        appendMessageToLog("Ток C превышает 45А")
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
            }

            KILOAVEM_ID -> when (param) {
                AvemVoltmeterModel.RESPONDING_PARAM -> {
                    isKiloAvemResponding = value as Boolean
                    Platform.runLater { deviceStateCircleKiloAvem.fill = if (value) Color.LIME else Color.RED }
                }
                AvemVoltmeterModel.U_RMS_PARAM -> {
                    measuringU = (value as Float) * 1000
                    coef = (measuringU / (measuringULatr / 102)).toDouble()
                    val kiloAvemU = String.format("%.2f", measuringU)
                    experiment1ManualModel!!.voltage = kiloAvemU
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
                    val uLatr = String.format("%.2f", measuringULatr / 102)
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
                Log.d("", "Замкнут верхний концевик ОМИКа.")
            }
            OMIK_DOWN_END -> {
                Log.d("", "Замкнут нижний концевик ОМИКа.")
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