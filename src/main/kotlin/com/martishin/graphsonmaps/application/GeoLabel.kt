package com.martishin.graphsonmaps.application

import com.martishin.graphsonmaps.geography.GeographicPoint
import javafx.scene.paint.Color
import javafx.scene.paint.Paint

class GeoLabel(
    val point: GeographicPoint,
) {
    companion object {
        val RED: Paint = Color.web("#9E092F")
        val GREEN: Paint = Color.web("#099E78")
    }
}
