package ru.avem.resonanceManual.states.main


interface Statable {
    fun toIdleState()

    fun toWaitState()

    fun toResultState()
}
