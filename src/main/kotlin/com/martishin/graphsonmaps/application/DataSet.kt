package com.martishin.graphsonmaps.application

import com.martishin.graphsonmaps.geography.GeographicPoint
import com.martishin.graphsonmaps.geography.RoadSegment
import com.martishin.graphsonmaps.roadgraph.MapGraph
import com.martishin.graphsonmaps.util.GraphLoader

class DataSet(
    val filePath: String,
) {
    var graph: MapGraph? = null
        private set
    var roads: HashMap<GeographicPoint, MutableSet<RoadSegment>>? = null
        private set
    private var intersections: MutableSet<GeographicPoint>? = null
    var isDisplayed: Boolean = false
        private set

    init {
        graph = null
        roads = null
        isDisplayed = false
    }

    /**
     * Return the intersections in this graph.
     * In order to keep it consistent, if getVertices in the graph returns something
     * other than null (i.e. it's been implemented) we get the vertices from
     * the graph itself.  But if the graph hasn't been implemented, we return
     * the set of intersections we separately maintain specifically for this purpose.
     * @return The set of road intersections (vertices in the graph)
     */
    fun getIntersections(): Set<GeographicPoint>? = graph?.getVertices() ?: intersections

    fun initializeGraph() {
        graph = MapGraph()
        roads = HashMap()
        intersections = HashSet()
        GraphLoader.loadRoadMap(filePath, graph!!, roads, intersections!!)
    }

    fun getPoints(): Array<Any> = roads?.keys?.toTypedArray() ?: arrayOf()

    fun setDisplayed(value: Boolean) {
        isDisplayed = value
    }
}
