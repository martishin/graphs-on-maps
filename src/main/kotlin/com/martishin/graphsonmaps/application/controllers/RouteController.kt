package com.martishin.graphsonmaps.application.controllers

import com.martishin.graphsonmaps.application.CLabel
import com.martishin.graphsonmaps.application.MapApp
import com.martishin.graphsonmaps.application.MarkerManager
import com.martishin.graphsonmaps.application.SelectManager
import com.martishin.graphsonmaps.application.services.RouteService
import com.martishin.graphsonmaps.geography.GeographicPoint
import javafx.scene.control.Button
import javafx.scene.control.RadioButton
import javafx.scene.control.ToggleGroup

class RouteController(
    private val routeService: RouteService,
    private val displayButton: Button,
    private val hideButton: Button,
    private val resetButton: Button,
    private val startButton: Button,
    private val destinationButton: Button,
    private val group: ToggleGroup,
    searchOptions: List<RadioButton>,
    private val visualizationButton: Button,
    private val startLabel: CLabel<GeographicPoint>,
    private val endLabel: CLabel<GeographicPoint>,
    private val pointLabel: CLabel<GeographicPoint>,
    private val selectManager: SelectManager,
    private val markerManager: MarkerManager,
) {
    private var selectedToggle = DIJ

    companion object {
        const val BFS = 3
        const val A_STAR = 2
        const val DIJ = 1
        const val DISABLE = 0
        const val START = 1
        const val DESTINATION = 2
    }

    init {
        setupDisplayButtons()
        setupRouteButtons()
        setupVisualizationButton()
        setupLabels()
        setupToggle()
        // routeService.displayRoute("data/sampleroute.map")
    }

    private fun setupDisplayButtons() {
        displayButton.setOnAction {
            val start = startLabel.getItem()
            val end = endLabel.getItem()

            if (start != null && end != null) {
                routeService.displayRoute(start, end, selectedToggle)
            } else {
                MapApp.showErrorAlert(
                    "Route Display Error",
                    "Make sure to choose points for both start and destination.",
                )
            }
        }

        hideButton.setOnAction {
            routeService.hideRoute()
        }

        // TODO -- implement
        resetButton.setOnAction {
            routeService.reset()
        }
    }

    private fun setupVisualizationButton() {
        visualizationButton.setOnAction {
            markerManager.startVisualization()
        }
    }

    private fun setupRouteButtons() {
        startButton.setOnAction {
            selectManager.setStart()
        }

        destinationButton.setOnAction {
            selectManager.setDestination()
        }
    }

    private fun setupLabels() {
        // Currently empty, can be implemented if necessary
    }

    private fun setupToggle() {
        group.selectedToggleProperty().addListener { _ ->
            selectedToggle =
                when (group.selectedToggle.userData) {
                    "Dijkstra" -> DIJ
                    "A*" -> A_STAR
                    "BFS" -> BFS
                    else -> {
                        System.err.println("Invalid radio button selection")
                        selectedToggle
                    }
                }
        }
    }
}
