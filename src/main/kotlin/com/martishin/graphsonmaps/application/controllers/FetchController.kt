package com.martishin.graphsonmaps.application.controllers

import com.martishin.graphsonmaps.application.DataSet
import com.martishin.graphsonmaps.application.services.GeneralService
import com.martishin.graphsonmaps.application.services.RouteService
import javafx.scene.control.*
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException

class FetchController(
    private val generalService: GeneralService,
    private val routeService: RouteService,
    private val writeFile: TextField,
    private val fetchButton: Button,
    cb: ComboBox<DataSet>,
    private val displayButton: Button,
) {
    private val dataChoices: ComboBox<DataSet> = cb
    private val filename = "data.map"
    private val persistPath = "data/maps/mapfiles.list"

    companion object {
        private const val ROW_COUNT = 5
    }

    init {
        setupComboCells()
        setupFetchButton()
        setupDisplayButton()
        loadDataSets()
    }

    private fun loadDataSets() {
        try {
            BufferedReader(FileReader(persistPath)).use { reader ->
                var line = reader.readLine()
                while (line != null) {
                    dataChoices.items.add(DataSet(GeneralService.getDataSetDirectory() + line))
                    line = reader.readLine()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun setupComboCells() {
        dataChoices.setCellFactory { _ ->
            object : ListCell<DataSet>() {
                init {
                    prefWidth = 100.0
                }

                override fun updateItem(
                    item: DataSet?,
                    empty: Boolean,
                ) {
                    super.updateItem(item, empty)
                    text =
                        if (empty || item == null) {
                            "None."
                        } else {
                            item.filePath.substring(GeneralService.getDataSetDirectory().length)
                        }
                }
            }
        }

        dataChoices.buttonCell =
            object : ListCell<DataSet>() {
                override fun updateItem(
                    t: DataSet?,
                    bln: Boolean,
                ) {
                    super.updateItem(t, bln)
                    text =
                        if (t != null) {
                            t.filePath.substring(GeneralService.getDataSetDirectory().length)
                        } else {
                            "Choose..."
                        }
                }
            }
    }

    private fun setupFetchButton() {
        fetchButton.setOnAction {
            val fName = writeFile.text
            val fileName = generalService.checkDataFileName(fName) // Store the result in a local variable

            if (fileName != null) {
                if (!generalService.checkBoundsSize(0.1)) {
                    val alert = Alert(Alert.AlertType.ERROR)
                    alert.title = "Size Error"
                    alert.headerText = "Map Size Error"
                    alert.contentText = "Map boundaries are too large."
                    alert.showAndWait()
                } else if (!generalService.checkBoundsSize(0.02)) {
                    val warning = Alert(Alert.AlertType.CONFIRMATION)
                    warning.title = "Size Warning"
                    warning.headerText = "Map Size Warning"
                    warning.contentText =
                        "Your map file may take a long time to download,\nand your computer may crash when you try to\nload the intersections. Continue?"
                    warning.showAndWait().ifPresent { response ->
                        if (response == ButtonType.OK) {
                            generalService.runFetchTask(fileName, dataChoices, fetchButton)
                        }
                    }
                } else {
                    generalService.runFetchTask(fileName, dataChoices, fetchButton)
                }
            } else {
                val alert = Alert(Alert.AlertType.ERROR)
                alert.title = "Filename Error"
                alert.headerText = "Input Error"
                alert.contentText = "Check filename input. \n\n\n" +
                    "Filename must match format : [filename].map." +
                    "\n\nUse only uppercase and lowercase letters,\nnumbers, and underscores in [filename]."
                alert.showAndWait()
            }
        }
    }

    private fun setupDisplayButton() {
        displayButton.setOnAction {
            val dataSet = dataChoices.value

            if (dataSet == null) {
                val alert = Alert(Alert.AlertType.ERROR)
                alert.title = "Display Error"
                alert.headerText = "Invalid Action :"
                alert.contentText = "No map file has been selected for display."
                alert.showAndWait()
            } else if (!dataSet.isDisplayed) {
                if (routeService.isRouteDisplayed()) {
                    routeService.hideRoute()
                }
                generalService.displayIntersections(dataSet)
            } else {
                val alert = Alert(Alert.AlertType.INFORMATION)
                alert.title = "Display Info"
                alert.headerText = "Intersections Already Displayed"
                alert.contentText = "Data set : ${dataSet.filePath} has already been loaded."
                alert.showAndWait()
            }
        }
    }
}
