package com.martishin.graphsonmaps.application

import com.dlsc.gmapsfx.javascript.event.UIEventType
import com.dlsc.gmapsfx.javascript.`object`.*
import com.martishin.graphsonmaps.geography.GeographicPoint
import javafx.scene.control.Button
import netscape.javascript.JSObject

class MarkerManager {
    companion object {
        private const val DEFAULT_Z = 2.0
        private const val SELECT_Z = 1.0
        private const val STRTDEST_Z = 3.0
        const val startURL = "http://maps.google.com/mapfiles/kml/pal3/icon40.png"
        const val destinationURL = "http://maps.google.com/mapfiles/kml/pal2/icon5.png"
        const val SELECTED_URL = "http://maps.google.com/mapfiles/kml/paddle/ltblu-circle.png"
        const val markerURL = "http://maps.google.com/mapfiles/kml/paddle/blu-diamond-lv.png"
        const val visURL = "http://maps.google.com/mapfiles/kml/paddle/red-diamond-lv.png"

        fun createDefaultOptions(coord: LatLong): MarkerOptions =
            MarkerOptions().apply {
                animation(null)
                icon(markerURL)
                position(coord)
                title(null)
                visible(true)
            }
    }

    private val markerMap = HashMap<GeographicPoint, Marker>()
    private var markerPositions: ArrayList<GeographicPoint>? = null
    private var map: GoogleMap? = null
    private var startMarker: Marker? = null
    private var destinationMarker: Marker? = null
    private var selectedMarker: Marker? = null
    private var dataSet: DataSet? = null
    private var bounds: LatLongBounds? = null
    private var selectManager: SelectManager? = null
    private var rv: RouteVisualization? = null
    private var vButton: Button? = null
    private var selectMode = true

    constructor()

    constructor(map: GoogleMap, selectManager: SelectManager) {
        this.map = map
        this.selectManager = selectManager
    }

    fun setVisButton(vButton: Button) {
        this.vButton = vButton
    }

    fun setSelect(value: Boolean) {
        selectMode = value
    }

    fun getVisualization(): RouteVisualization? = rv

    fun getMap(): GoogleMap? = map

    fun setMap(map: GoogleMap) {
        this.map = map
    }

    fun setSelectManager(selectManager: SelectManager) {
        this.selectManager = selectManager
    }

    fun putMarker(
        key: GeographicPoint,
        value: Marker,
    ) {
        markerMap[key] = value
    }

    fun initVisualization() {
        rv = RouteVisualization(this)
    }

    fun clearVisualization() {
        rv?.clearMarkers()
        rv = null
    }

    fun startVisualization() {
        rv?.startVisualization()
    }

    fun setStart(point: GeographicPoint) {
        startMarker?.let {
            changeIcon(it, markerURL)
        }
        startMarker = markerMap[point]
        changeIcon(startMarker!!, startURL)
    }

    fun setDestination(point: GeographicPoint) {
        destinationMarker = markerMap[point]
        changeIcon(destinationMarker!!, destinationURL)
    }

    fun changeIcon(
        marker: Marker,
        url: String,
    ) {
        marker.setVisible(false)
        marker.setVisible(true)
    }

    fun restoreMarkers() {
        markerMap.values.forEach { marker ->
            if (marker != startMarker) {
                marker.setVisible(false)
                marker.setVisible(true)
            }
        }
        selectManager?.resetSelect()
    }

    fun refreshMarkers() {
        markerMap.values.forEach { marker ->
            marker.setVisible(true)
        }
    }

    fun clearMarkers() {
        rv?.clearMarkers()
        rv = null
        markerMap.values.forEach { marker ->
            marker.setVisible(false)
        }
    }

    fun setSelectMode(value: Boolean) {
        if (!value) {
            selectManager?.clearSelected()
        }
        selectMode = value
    }

    fun getSelectMode(): Boolean = selectMode

    fun hideIntermediateMarkers() {
        markerMap.values.forEach { marker ->
            if (marker != startMarker && marker != destinationMarker) {
                marker.setVisible(false)
            }
        }
    }

    fun hideDestinationMarker() {
        destinationMarker?.setVisible(false)
    }

    fun displayMarker(point: GeographicPoint) {
        markerMap[point]?.let {
            it.setVisible(true)
        } ?: run {
            // println("No key found for MarkerManager::displayMarker")
        }
    }

    fun displayDataSet() {
        markerPositions = ArrayList()
        dataSet?.initializeGraph()
        val iterator = dataSet?.getIntersections()?.iterator()
        bounds = LatLongBounds()

        while (iterator?.hasNext() == true) {
            val point = iterator.next()
            val ll = LatLong(point.getX(), point.getY())
            val markerOptions = createDefaultOptions(ll)
            bounds!!.extend(ll)
            val marker = Marker(markerOptions)
            registerEvents(marker, point)
            map?.addMarker(marker)
            putMarker(point, marker)
            markerPositions?.add(point)
            // marker.zIndex = DEFAULT_Z
        }

        map?.fitBounds(bounds)
        // println("End of display Intersections")
    }

    private fun registerEvents(
        marker: Marker,
        point: GeographicPoint,
    ) {
        map?.addUIEventHandler(marker, UIEventType.click) { _: JSObject? ->
            if (selectMode) {
                selectManager?.setPoint(point, marker)
                selectedMarker = marker
            }
        }
    }

    fun disableVisButton(value: Boolean) {
        vButton?.setDisable(value)
    }

    fun setDataSet(dataSet: DataSet) {
        this.dataSet = dataSet
    }

    fun getDataSet(): DataSet? = dataSet
}
