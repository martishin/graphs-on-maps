package com.martishin.graphsonmaps.application

import com.dlsc.gmapsfx.javascript.IJavascriptRuntime
import com.dlsc.gmapsfx.javascript.JavascriptArray
import com.dlsc.gmapsfx.javascript.JavascriptRuntime
import com.dlsc.gmapsfx.javascript.`object`.*
import com.martishin.graphsonmaps.geography.GeographicPoint

class RouteVisualization(
    private val manager: MarkerManager,
) {
    private val points: MutableList<GeographicPoint> = ArrayList()
    private val markerList: MutableList<Marker> = ArrayList()
    private var markers: JavascriptArray? = null
    private var runtime: IJavascriptRuntime? = null

    fun acceptPoint(point: GeographicPoint) {
        points.add(point)
        // println("accepted point : $point")
    }

    fun startVisualization() {
        val bounds = LatLongBounds()
        val jsArray = JavascriptArray()

        manager.hideIntermediateMarkers()
        manager.hideDestinationMarker()

        // create JavascriptArray of points
        points.forEach { point ->
            val ll = LatLong(point.getX(), point.getY())
            val options = MarkerManager.createDefaultOptions(ll)
            val newMarker = Marker(options)
            jsArray.push(newMarker)
            markerList.add(newMarker)
            bounds.extend(ll)
        }

        // fit map bounds to visualization
        manager.getMap()?.fitBounds(bounds)

        // get javascript runtime and execute animation
        runtime = JavascriptRuntime.getInstance()
        val command = runtime?.getFunction("visualizeSearch", manager.getMap(), jsArray)
        // println(command)

        command?.let { runtime?.execute(it) }

        manager.disableVisButton(true)
    }

    fun clearMarkers() {
        markerList.forEach { marker ->
            marker.setVisible(false)
        }
    }
}
