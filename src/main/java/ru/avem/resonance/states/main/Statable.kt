package ru.avem.resonance.states.main


interface Statable {
    fun toIdleState()

    fun toWaitState()

    fun toResultState()
}
