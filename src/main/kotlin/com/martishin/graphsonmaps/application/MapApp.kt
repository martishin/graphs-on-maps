package com.martishin.graphsonmaps.application

import com.dlsc.gmapsfx.GoogleMapView
import com.dlsc.gmapsfx.MapComponentInitializedListener
import com.dlsc.gmapsfx.javascript.`object`.GoogleMap
import com.dlsc.gmapsfx.javascript.`object`.LatLong
import com.dlsc.gmapsfx.javascript.`object`.MapOptions
import com.dlsc.gmapsfx.javascript.`object`.MapTypeIdEnum
import com.martishin.graphsonmaps.application.controllers.FetchController
import com.martishin.graphsonmaps.application.controllers.RouteController
import com.martishin.graphsonmaps.application.services.GeneralService
import com.martishin.graphsonmaps.application.services.RouteService
import com.martishin.graphsonmaps.geography.GeographicPoint
import javafx.application.Application
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.scene.web.WebView
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.util.*

class MapApp :
    Application(),
    MapComponentInitializedListener {
    private lateinit var mapComponent: GoogleMapView
    private lateinit var map: GoogleMap
    private lateinit var bp: BorderPane
    private lateinit var primaryStage: Stage

    // CONSTANTS
    companion object {
        private const val MARGIN_VAL = 10.0
        private const val FETCH_COMPONENT_WIDTH = 160.0

        @JvmStatic
        fun main(args: Array<String>) {
            launch(MapApp::class.java, *args)
        }

        fun showInfoAlert(
            header: String,
            content: String,
        ) {
            val alert = getInfoAlert(header, content)
            alert.showAndWait()
        }

        fun getInfoAlert(
            header: String,
            content: String,
        ): Alert {
            val alert = Alert(Alert.AlertType.INFORMATION)
            alert.title = "Information"
            alert.headerText = header
            alert.contentText = content
            return alert
        }

        fun showErrorAlert(
            header: String,
            content: String,
        ) {
            val alert = Alert(Alert.AlertType.ERROR)
            alert.title = "File Name Error"
            alert.headerText = header
            alert.contentText = content
            alert.showAndWait()
        }
    }

    /**
     * Application entry point
     */
    @Throws(Exception::class)
    override fun start(primaryStage: Stage) {
        this.primaryStage = primaryStage

        // MAIN CONTAINER
        bp = BorderPane()

        // set up map
        mapComponent = GoogleMapView()
        mapComponent.addMapInitializedListener(this)

        // initialize tabs for data fetching and route controls
        val routeTab = Tab("Routing")

        // create components for fetch tab
        val fetchButton = Button("Fetch Data")
        val displayButton = Button("Show Intersections")
        val tf = TextField()
        val cb = ComboBox<DataSet>()

        // set on mouse pressed, this fixes Windows 10 / Surface bug
        cb.setOnMousePressed {
            cb.requestFocus()
        }

        val fetchControls = getBottomBox(tf, fetchButton)
        val fetchBox = getFetchBox(displayButton, cb)

        // create components for route tab
        val routeButton = Button("Show Route")
        val hideRouteButton = Button("Hide Route")
        val resetButton = Button("Reset")
        val visualizationButton = Button("Start Visualization")
        val sImage = Image(MarkerManager.startURL)
        val dImage = Image(MarkerManager.destinationURL)
        val startLabel = CLabel<GeographicPoint>("Empty.", ImageView(sImage), null)
        val endLabel = CLabel<GeographicPoint>("Empty.", ImageView(dImage), null)
        // TODO -- hot fix
        startLabel.minWidth = 180.0
        endLabel.minWidth = 180.0

        val startButton = Button("Start")
        val destinationButton = Button("Dest")

        // Radio buttons for selecting search algorithm
        val group = ToggleGroup()
        val searchOptions = setupToggle(group)

        // Select and marker managers for route choosing and marker display/visuals
        val manager = SelectManager()
        val markerManager = MarkerManager()
        markerManager.setSelectManager(manager)
        manager.setMarkerManager(markerManager)
        markerManager.setVisButton(visualizationButton)

        // create components for route tab
        val pointLabel = CLabel<GeographicPoint>("No point Selected.", null)
        manager.setPointLabel(pointLabel)
        manager.setStartLabel(startLabel)
        manager.setDestinationLabel(endLabel)

        setupRouteTab(
            routeTab,
            fetchBox,
            startLabel,
            endLabel,
            pointLabel,
            routeButton,
            hideRouteButton,
            resetButton,
            visualizationButton,
            startButton,
            destinationButton,
            searchOptions,
        )

        // add tabs to pane, give no option to close
        val tp = TabPane(routeTab)
        tp.tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

        // initialize Services and controllers after map is loaded
        mapComponent.addMapReadyListener {
            val gs = GeneralService(mapComponent, manager, markerManager)
            val rs = RouteService(mapComponent, markerManager)
            // initialize controllers
            RouteController(
                rs,
                routeButton,
                hideRouteButton,
                resetButton,
                startButton,
                destinationButton,
                group,
                searchOptions,
                visualizationButton,
                startLabel,
                endLabel,
                pointLabel,
                manager,
                markerManager,
            )
            FetchController(gs, rs, tf, fetchButton, cb, displayButton)
        }

        // add components to border pane
        bp.right = tp
        bp.bottom = fetchControls
        bp.center = mapComponent

        val scene = Scene(bp)
        scene.stylesheets.add(MapApp::class.java.getResource("/html/routing.css").toExternalForm())
        primaryStage.scene = scene
        primaryStage.show()
    }

    override fun mapInitialized() {
        val center =
            LatLong(32.8810, -117.2380)

        // set map options
        val options = MapOptions()
        options
            .center(center)
            .mapMarker(false)
            .mapType(MapTypeIdEnum.SATELLITE)
            .mapTypeControl(true)
            .overviewMapControl(false)
            .panControl(true)
            .rotateControl(false)
            .scaleControl(false)
            .streetViewControl(false)
            .zoom(14.0)
            .zoomControl(true)

        // create map;
        map = mapComponent.createMap(options)
        setupJSAlerts(mapComponent.webview)

        val jsCode = """
            var delay = 10;
            var map, markers;
            var markerURL = "http://maps.google.com/mapfiles/kml/paddle/red-diamond-lv.png";
            var destURL = "http://maps.google.com/mapfiles/kml/pal2/icon5.png";
            
            function visualizeSearch(mapParam, markersParam) {
                markers = markersParam;
                map = mapParam;	
                drop();
            }
            
            function displayMarker(marker, timeout, URL) {
                window.setTimeout(function() {
                    marker.setIcon(URL);
                    marker.setMap(map);
                }, timeout);
            }
            
            function drop() {
                var i;
                for(i = 1; i < markers.length - 1; ++i) {
                    displayMarker(markers[i], i*delay, markerURL);
                }
                displayMarker(markers[markers.length - 1], i*delay, destURL);
                i++;
                displayAlert(markers.length, i*delay);
            }
            
            function displayAlert(length, delay) {
                ////window.setTimeout(function() {
                    alert(length + " nodes visited in search.");
                //}, delay);
            }
        """
        mapComponent.webview.engine.executeScript(jsCode)
    }

    // SETTING UP THE VIEW

    private fun getBottomBox(
        tf: TextField,
        fetchButton: Button,
    ): HBox {
        val box = HBox()
        tf.prefWidth = FETCH_COMPONENT_WIDTH
        box.children.add(tf)
        fetchButton.prefWidth = FETCH_COMPONENT_WIDTH
        box.children.add(fetchButton)
        return box
    }

    /**
     * Setup layout and controls for Fetch tab
     */
    private fun getFetchBox(
        displayButton: Button,
        cb: ComboBox<DataSet>,
    ): VBox {
        // add button to tab, rethink design and add V/HBox for content
        val v = VBox()
        val h = HBox()

        val intersectionControls = HBox()
        cb.prefWidth = FETCH_COMPONENT_WIDTH
        intersectionControls.children.add(cb)
        displayButton.prefWidth = FETCH_COMPONENT_WIDTH
        intersectionControls.children.add(displayButton)

        h.children.add(v)
        v.children.add(Label("Choose map file : "))
        v.children.add(intersectionControls)

        return v
    }

    /**
     * Setup layout of route tab and controls
     */
    private fun setupRouteTab(
        routeTab: Tab,
        fetchBox: VBox,
        startLabel: Label,
        endLabel: Label,
        pointLabel: Label,
        showButton: Button,
        hideButton: Button,
        resetButton: Button,
        vButton: Button,
        startButton: Button,
        destButton: Button,
        searchOptions: List<RadioButton>,
    ) {
        // set up tab layout
        val h = HBox()
        val v = VBox()
        h.children.add(v)

        val selectLeft = VBox()
        selectLeft.children.add(startLabel)

        val startBox = HBox()
        startBox.children.add(startLabel)
        startBox.children.add(startButton)
        startBox.spacing = 20.0

        val destinationBox = HBox()
        destinationBox.children.add(endLabel)
        destinationBox.children.add(destButton)
        destinationBox.spacing = 20.0

        val markerBox = VBox()
        val markerLabel = Label("Selected Marker : ")

        markerBox.children.add(markerLabel)
        markerBox.children.add(pointLabel)

        VBox.setMargin(markerLabel, Insets(MARGIN_VAL, MARGIN_VAL, MARGIN_VAL, MARGIN_VAL))
        VBox.setMargin(pointLabel, Insets(0.0, MARGIN_VAL, MARGIN_VAL, MARGIN_VAL))
        VBox.setMargin(fetchBox, Insets(0.0, 0.0, MARGIN_VAL * 2, 0.0))

        val showHideBox = HBox()
        showHideBox.children.add(showButton)
        showHideBox.children.add(hideButton)
        showHideBox.spacing = 2 * MARGIN_VAL

        v.children.add(fetchBox)
        v.children.add(Label("Start Position : "))
        v.children.add(startBox)
        v.children.add(Label("Goal : "))
        v.children.add(destinationBox)
        v.children.add(showHideBox)
        for (rb in searchOptions) {
            v.children.add(rb)
        }
        v.children.add(vButton)
        VBox.setMargin(showHideBox, Insets(MARGIN_VAL, MARGIN_VAL, MARGIN_VAL, MARGIN_VAL))
        VBox.setMargin(vButton, Insets(MARGIN_VAL, MARGIN_VAL, MARGIN_VAL, MARGIN_VAL))
        vButton.setDisable(true)
        v.children.add(markerBox)

        routeTab.content = h
    }

    private fun setupJSAlerts(webView: WebView) {
        webView.engine.onAlert =
            EventHandler { e ->
                val popup = Stage()
                popup.initOwner(primaryStage)
                popup.initStyle(StageStyle.UTILITY)
                popup.initModality(Modality.WINDOW_MODAL)

                val content =
                    StackPane().apply {
                        children.setAll(Label(e.data))
                        prefWidth = 200.0
                        prefHeight = 100.0
                    }

                popup.scene = Scene(content)
                popup.showAndWait()
            }
    }

    private fun setupToggle(group: ToggleGroup): LinkedList<RadioButton> {
        // Use Dijkstra as default
        val rbD =
            RadioButton("Dijkstra").apply {
                userData = "Dijkstra"
                isSelected = true
            }

        val rbA =
            RadioButton("A*").apply {
                userData = "A*"
            }

        val rbB =
            RadioButton("BFS").apply {
                userData = "BFS"
            }

        rbB.toggleGroup = group
        rbD.toggleGroup = group
        rbA.toggleGroup = group

        return LinkedList(listOf(rbB, rbD, rbA))
    }

    /*
     * METHODS FOR SHOWING DIALOGS/ALERTS
     */

    fun showLoadStage(
        loadStage: Stage,
        text: String,
    ) {
        loadStage.initModality(Modality.APPLICATION_MODAL)
        loadStage.initOwner(primaryStage)
        val loadVBox =
            VBox(20.0).apply {
                alignment = Pos.CENTER
                children.addAll(
                    HBox(),
                    Text(text).apply { font = Font(16.0) },
                    HBox(),
                )
            }

        val loadScene = Scene(loadVBox, 300.0, 200.0)
        loadStage.scene = loadScene
        loadStage.show()
    }
}
