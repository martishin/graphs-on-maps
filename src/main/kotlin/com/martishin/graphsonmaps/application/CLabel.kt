package com.martishin.graphsonmaps.application

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.paint.Color
import javafx.scene.paint.Paint

/**
 * Label which has an object property associated with it
 *
 * @param T Type of object associated with label
 */
class CLabel<T> : Label {
    var item: ObjectProperty<T> = SimpleObjectProperty(this, "item")

    companion object {
        private val RED: Paint = Color.web("#9E092F")
        private val GREEN: Paint = Color.web("#099E78")
    }

    constructor(text: String, item: T?) : super(text) {
        setItem(item)
    }

    constructor(text: String, graphic: Node, item: T?) : super(text, graphic) {
        setItem(item)
    }

    /**
     * Used to update item when new item is set.
     */
    private fun updateView(
        item: T?,
        empty: Boolean,
    ) {
        if (item != null) {
            text = item.toString()
            textFill = GREEN
        } else {
            text = "Choose Point"
            textFill = RED
        }
    }

    fun itemProperty(): ObjectProperty<T> = item

    fun getItem(): T? = item.get()

    fun setItem(newItem: T?) {
        item.set(newItem)
        updateView(item.get(), true)
    }
}
