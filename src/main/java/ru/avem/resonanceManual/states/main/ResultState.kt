package ru.avem.resonanceManual.states.main

class ResultState(private val statable: Statable) : State {

    override fun toIdleState() {
        statable.toIdleState()
    }

    override fun toWaitState() {
        statable.toWaitState()
    }

    override fun toResultState() {}
}
