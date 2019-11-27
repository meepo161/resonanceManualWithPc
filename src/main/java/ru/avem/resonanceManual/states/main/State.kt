package ru.avem.resonanceManual.states.main

interface State {
    fun toIdleState()

    fun toWaitState()

    fun toResultState()
}
