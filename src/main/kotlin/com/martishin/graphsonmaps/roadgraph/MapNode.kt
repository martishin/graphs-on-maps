package com.martishin.graphsonmaps.roadgraph

import com.martishin.graphsonmaps.geography.GeographicPoint

class MapNode(
    val location: GeographicPoint,
) : Comparable<MapNode> {
    var length: Double = 0.0
    var predictedLength: Double = 0.0
    val edgeList: HashSet<MapEdge> = HashSet()

    fun addEdge(newEdge: MapEdge) {
        edgeList.add(newEdge)
    }

    fun addEdge(
        toLoc: GeographicPoint,
        roadName: String,
        roadType: String,
        distance: Double,
    ) {
        val newEdge = MapEdge(location, toLoc, roadName, roadType, distance)
        edgeList.add(newEdge)
    }

    fun getNeighbors(): List<GeographicPoint> {
        val neighbors = ArrayList<GeographicPoint>()
        for (e in edgeList) {
            neighbors.add(e.to)
        }
        return neighbors
    }

    override fun compareTo(other: MapNode): Int = this.length.compareTo(other.length)
}
