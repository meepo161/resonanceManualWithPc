package ru.avem.resonanceManual.model

import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty

class ResultModel(dimension: String, value: String) {

    private val dimension: StringProperty
    private val value: StringProperty

    init {
        this.dimension = SimpleStringProperty(dimension)
        this.value = SimpleStringProperty(value)
    }

    fun getDimension(): String {
        return dimension.get()
    }

    fun dimensionProperty(): StringProperty {
        return dimension
    }

    fun setDimension(dimension: String) {
        this.dimension.set(dimension)
    }

    fun getValue(): String {
        return value.get()
    }

    fun valueProperty(): StringProperty {
        return value
    }

    fun setValue(value: String) {
        this.value.set(value)
    }
}
