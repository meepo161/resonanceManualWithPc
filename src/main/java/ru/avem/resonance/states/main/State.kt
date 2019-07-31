package ru.avem.resonance.states.main

interface State {
    fun toIdleState()

    fun toWaitState()

    fun toResultState()
}
