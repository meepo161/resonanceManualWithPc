package ru.avem.resonance.states.main

import ru.avem.resonance.utils.View

class WaitState(private val statable: Statable) : State {

    override fun toIdleState() {
//        View.showConfirmDialog("Подтвердите отмену", "Внимание! Все несохранённые результаты будут утеряны", { statable.toIdleState() }, {
//
//        })
    }

    override fun toWaitState() {
        statable.toWaitState()
    }

    override fun toResultState() {
        statable.toResultState()
    }
}
