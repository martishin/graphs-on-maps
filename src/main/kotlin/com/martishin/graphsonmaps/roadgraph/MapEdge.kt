package com.martishin.graphsonmaps.roadgraph

import com.martishin.graphsonmaps.geography.GeographicPoint

/*
    The class MapEdge describes the relationship and information
    of the start point and end point of the streets. It not only contains
    these two points but also includes the length, name, and type
    of streets. All these pieces of information are encapsulated.
*/

class MapEdge(
    var from: GeographicPoint,
    var to: GeographicPoint,
    var roadName: String,
    var roadType: String,
    var distance: Double,
)
