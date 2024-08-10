package com.martishin.graphsonmaps.application.services

import com.dlsc.gmapsfx.GoogleMapView
import com.dlsc.gmapsfx.javascript.`object`.GoogleMap
import com.martishin.graphsonmaps.application.DataSet
import com.martishin.graphsonmaps.application.MapApp
import com.martishin.graphsonmaps.application.MarkerManager
import com.martishin.graphsonmaps.application.SelectManager
import com.martishin.graphsonmaps.mapmaker.MapMaker
import javafx.concurrent.Task
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import java.util.regex.Pattern

class GeneralService(
    mapComponent: GoogleMapView,
    private val selectManager: SelectManager,
    private val markerManager: MarkerManager,
) {
    private var currentState: Int = 0
    private val map: GoogleMap = mapComponent.map
    private val filenames: MutableList<String> = mutableListOf()
    private var dataSet: DataSet? = null

    init {
        markerManager.setMap(map)
    }

    companion object {
        private const val DATA_FILE_PATTERN = "[\\w_]+\\.map"
        private const val DATA_FILE_DIR_STR = "data/maps/"

        fun getDataSetDirectory(): String = DATA_FILE_DIR_STR

        fun getFileRegex(): String = DATA_FILE_PATTERN
    }

    // Writes geographic data to a flat file
    fun writeDataToFile(
        filename: String,
        arr: FloatArray,
    ): Boolean {
        val mm = MapMaker(arr)

        // Parse data and write to filename
        return mm.parseData(filename)
    }

    // Gets current bounds of the map view
    fun getBoundsArray(): FloatArray {
        val bounds = map.bounds
        val sw = bounds.southWest
        val ne = bounds.northEast

        // [S, W, N, E]
        return floatArrayOf(
            sw.latitude.toFloat(),
            sw.longitude.toFloat(),
            ne.latitude.toFloat(),
            ne.longitude.toFloat(),
        )
    }

    fun addDataFile(filename: String) {
        filenames.add(filename)
    }

    fun displayIntersections(dataset: DataSet) {
        // Remove old dataset markers
        markerManager.getDataSet()?.let {
            markerManager.clearMarkers()
            it.setDisplayed(false)
        }

        // Display new dataset
        selectManager.setAndDisplayData(dataset)
        dataset.setDisplayed(true)
    }

    fun boundsSize(): Float {
        val bounds = getBoundsArray()
        return (bounds[2] - bounds[0]) * (bounds[3] - bounds[1])
    }

    fun checkBoundsSize(limit: Double): Boolean = boundsSize() <= limit

    /**
     * Check if file name matches pattern [filename].map
     *
     * @param str - path to check
     * @return string to use as path
     */
    fun checkDataFileName(str: String): String? =
        if (Pattern.matches(DATA_FILE_PATTERN, str)) {
            DATA_FILE_DIR_STR + str
        } else {
            null
        }

    fun runFetchTask(
        fName: String,
        cb: ComboBox<DataSet>,
        button: Button,
    ) {
        val arr = getBoundsArray()

        val task =
            object : Task<String>() {
                override fun call(): String =
                    if (writeDataToFile(fName, arr)) {
                        fName
                    } else {
                        "z$fName"
                    }
            }

        val fetchingAlert = MapApp.getInfoAlert("Loading: ", "Fetching data for current map area...")
        task.setOnSucceeded {
            if (task.value == fName) {
                addDataFile(fName)
                cb.items.add(DataSet(fName))
                if (fetchingAlert.isShowing) {
                    fetchingAlert.close()
                }
                MapApp.showInfoAlert("Fetch completed: ", "Data set: \"$fName\" written to file!")
            } else {
                // Handle the case where the file name returned differently
            }
            button.setDisable(false)
        }

        task.setOnFailed {
            // Handle task failure
        }

        task.setOnRunning {
            button.setDisable(true)
            fetchingAlert.showAndWait()
        }

        val fetchThread = Thread(task)
        fetchThread.start()
    }

    fun getDataFiles(): List<String> = filenames

    fun setState(state: Int) {
        currentState = state
    }

    fun getState(): Double = currentState.toDouble()
}
