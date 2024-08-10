package com.martishin.graphsonmaps.application.services

import com.dlsc.gmapsfx.GoogleMapView
import com.dlsc.gmapsfx.javascript.`object`.GoogleMap
import com.dlsc.gmapsfx.javascript.`object`.LatLong
import com.dlsc.gmapsfx.javascript.`object`.LatLongBounds
import com.dlsc.gmapsfx.javascript.`object`.MVCArray
import com.dlsc.gmapsfx.shapes.Polyline
import com.martishin.graphsonmaps.application.MapApp
import com.martishin.graphsonmaps.application.MarkerManager
import com.martishin.graphsonmaps.application.RouteVisualization
import com.martishin.graphsonmaps.application.controllers.RouteController
import com.martishin.graphsonmaps.geography.GeographicPoint
import com.martishin.graphsonmaps.geography.RoadSegment
import java.util.function.Consumer

class RouteService(
    mapComponent: GoogleMapView,
    private val markerManager: MarkerManager,
) {
    private val map: GoogleMap = mapComponent.map
    private var routeLine: Polyline? = null
    private var rv: RouteVisualization? = null

    // Display route on Google Map
    private fun displayRoute(route: List<LatLong>): Boolean {
        routeLine?.let { removeRouteLine() }

        routeLine = Polyline()
        val path = MVCArray()
        val bounds = LatLongBounds()

        for (point in route) {
            path.push(point)
            bounds.extend(point)
        }
        routeLine!!.setPath(path)
        map.addMapShape(routeLine)

        markerManager.hideIntermediateMarkers()
        map.fitBounds(bounds)
        markerManager.disableVisButton(false)
        return true
    }

    fun hideRoute() {
        routeLine?.let {
            map.removeMapShape(it)
            markerManager.getVisualization()?.let { markerManager.clearVisualization() }
            markerManager.restoreMarkers()
            markerManager.disableVisButton(true)
            routeLine = null
        }
    }

    fun reset() {
        removeRouteLine()
    }

    fun isRouteDisplayed(): Boolean = routeLine != null

    fun displayRoute(
        start: GeographicPoint,
        end: GeographicPoint,
        toggle: Int,
    ): Boolean {
        if (routeLine == null) {
            markerManager.getVisualization()?.let { markerManager.clearVisualization() }

            if (toggle == RouteController.DIJ || toggle == RouteController.A_STAR || toggle == RouteController.BFS) {
                markerManager.initVisualization()
                val nodeAccepter = Consumer<GeographicPoint> { markerManager.getVisualization()!!.acceptPoint(it) }
                val graph = markerManager.getDataSet()?.graph
                val path =
                    when (toggle) {
                        RouteController.BFS -> graph?.bfs(start, end, nodeAccepter)
                        RouteController.DIJ -> graph?.dijkstra(start, end, nodeAccepter)
                        RouteController.A_STAR -> graph?.aStarSearch(start, end, nodeAccepter)
                        else -> null
                    }

                if (path == null) {
                    MapApp.showInfoAlert("Routing Error:", "No path found")
                    return false
                }

                val mapPath = constructMapPath(path)
                markerManager.setSelectMode(false)
                return displayRoute(mapPath)
            }
            return false
        }
        return false
    }

    // Construct path including road segments
    private fun constructMapPath(path: List<GeographicPoint>): List<LatLong> {
        val retVal = mutableListOf<LatLong>()
        for (i in 0 until path.size - 1) {
            val curr = path[i]
            val next = path[i + 1]
            val segments = markerManager.getDataSet()?.roads?.get(curr) ?: continue

            var chosenSegment: RoadSegment? = null
            var minLength = Double.MAX_VALUE

            for (currSegment in segments) {
                if (currSegment.getOtherPoint(curr) == next && currSegment.length < minLength) {
                    chosenSegment = currSegment
                    minLength = currSegment.length
                }
            }

            chosenSegment?.let {
                val segmentList = it.getPoints(curr, next)
                for (point in segmentList) {
                    retVal.add(LatLong(point.getX(), point.getY()))
                }
            } ?: run {
                System.err.println("ERROR in constructMapPath: chosenSegment was null")
            }
        }

        return retVal
    }

    private fun removeRouteLine() {
        routeLine?.let { map.removeMapShape(it) }
    }
}
