package com.martishin.graphsonmaps.application

import com.dlsc.gmapsfx.javascript.`object`.Marker
import com.martishin.graphsonmaps.geography.GeographicPoint

class SelectManager {
    private var pointLabel: CLabel<GeographicPoint>? = null
    private var startLabel: CLabel<GeographicPoint>? = null
    private var destinationLabel: CLabel<GeographicPoint>? = null
    private var startMarker: Marker? = null
    private var destinationMarker: Marker? = null
    private var selectedMarker: Marker? = null
    private var markerManager: MarkerManager? = null
    private var dataSet: DataSet? = null

    fun resetSelect() {
        markerManager?.setSelectMode(true)
    }

    fun clearSelected() {
        selectedMarker = null
        pointLabel?.item?.set(null)
    }

    fun setAndDisplayData(data: DataSet) {
        setDataSet(data)
        markerManager?.displayDataSet() ?: System.err.println("Error: Marker Manager is null.")
    }

    fun setMarkerManager(manager: MarkerManager) {
        this.markerManager = manager
    }

    fun setPoint(
        point: GeographicPoint,
        marker: Marker,
    ) {
        pointLabel?.setItem(point)
        selectedMarker = marker
    }

    fun setDataSet(dataSet: DataSet) {
        this.dataSet = dataSet
        markerManager?.setDataSet(dataSet)
    }

    fun setPointLabel(label: CLabel<GeographicPoint>) {
        this.pointLabel = label
    }

    fun setStartLabel(label: CLabel<GeographicPoint>) {
        this.startLabel = label
    }

    fun setDestinationLabel(label: CLabel<GeographicPoint>) {
        this.destinationLabel = label
    }

    fun getPoint(): GeographicPoint? = pointLabel?.item?.get()

    fun getStart(): GeographicPoint? = startLabel?.item?.get()

    fun getDestination(): GeographicPoint? = destinationLabel?.item?.get()

    fun setStart() {
        pointLabel?.getItem()?.let { point ->
            startLabel?.setItem(point)
            markerManager?.setStart(point)
        }
    }

    fun setDestination() {
        pointLabel?.getItem()?.let { point ->
            destinationLabel?.setItem(point)
            markerManager?.setDestination(point)
        }
    }
}
