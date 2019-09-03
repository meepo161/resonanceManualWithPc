package ru.avem.resonance.model

import ru.avem.resonance.db.model.Account
import ru.avem.resonance.db.model.Protocol
import ru.avem.resonance.db.model.TestItem

class MainModel private constructor() {
    private var firstTester = Account("ADMIN", "ADMIN", "ADMIN", "ADMIN", "ADMIN")
    private var secondTester = Account("ADMIN", "ADMIN", "ADMIN", "ADMIN", "ADMIN")

    var isNeedRefresh = true
    var currentProtocol: Protocol = Protocol()
    var currentTestItem: TestItem = TestItem()

    private lateinit var intermediateProtocol: Protocol

    var experiment1Model = Experiment1Model()
    var experiment2Model = Experiment2Model()
    var experiment3Model = Experiment3Model()

    fun setTesters(tester1: Account, tester2: Account) {
        this.firstTester = tester1
        this.secondTester = tester2
    }

    fun createNewProtocol(serialNumber: String, selectedTestItem: TestItem) {
        currentProtocol = Protocol(serialNumber, selectedTestItem, firstTester, secondTester, System.currentTimeMillis())
    }

    fun setIntermediateProtocol(intermediateProtocol: Protocol) {
        this.intermediateProtocol = intermediateProtocol
    }

    fun applyIntermediateProtocol() {
        currentProtocol = intermediateProtocol
        intermediateProtocol = Protocol()
    }

    companion object {
        val instance = MainModel()
    }
}
