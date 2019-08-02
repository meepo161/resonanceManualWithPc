package ru.avem.resonance.states.main

import ru.avem.resonance.utils.View

class WaitState(private val statable: Statable) : State {

    override fun toIdleState() {
    }

    override fun toWaitState() {
        statable.toWaitState()
    }

    override fun toResultState() {
        statable.toResultState()
    }
}
