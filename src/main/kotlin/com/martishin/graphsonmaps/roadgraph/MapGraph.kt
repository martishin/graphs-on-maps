package com.martishin.graphsonmaps.roadgraph

import com.martishin.graphsonmaps.geography.GeographicPoint
import com.martishin.graphsonmaps.util.GraphLoader
import java.util.*
import java.util.function.Consumer

/**
 * A class which represents a graph of geographic locations
 * Nodes in the graph are intersections between roads.
 */
class MapGraph {
    private val graph: HashMap<GeographicPoint, MapNode> = HashMap()
    private val edgeList: HashSet<MapEdge> = HashSet()

    /**
     * Get the number of vertices (road intersections) in the graph.
     * @return The number of vertices in the graph.
     */
    fun getNumVertices(): Int = graph.size

    /**
     * Return the intersections, which are the vertices in this graph.
     * @return The vertices in this graph as GeographicPoints.
     */
    fun getVertices(): Set<GeographicPoint> = graph.keys

    /**
     * Get the number of road segments in the graph.
     * @return The number of edges in the graph.
     */
    fun getNumEdges(): Int = edgeList.size

    /**
     * Add a node corresponding to an intersection at a Geographic Point.
     * If the location is already in the graph or null, this method does not change the graph.
     * @param location The location of the intersection.
     * @return true if a node was added, false if it was not (the node was already in the graph or the parameter is null).
     */
    fun addVertex(location: GeographicPoint?): Boolean =
        if (location != null && !graph.containsKey(location)) {
            graph[location] = MapNode(location)
            true
        } else {
            false
        }

    /**
     * Adds a directed edge to the graph from pt1 to pt2.
     * Precondition: Both GeographicPoints have already been added to the graph.
     * @param from The starting point of the edge.
     * @param to The ending point of the edge.
     * @param roadName The name of the road.
     * @param roadType The type of the road.
     * @param length The length of the road, in km.
     * @throws IllegalArgumentException If the points have not already been added as nodes to the graph, if any of the arguments are null, or if the length is less than 0.
     */
    @Throws(IllegalArgumentException::class)
    fun addEdge(
        from: GeographicPoint,
        to: GeographicPoint,
        roadName: String,
        roadType: String,
        length: Double,
    ) {
        require(length > 0) { "Road length must be positive." }
        requireNotNull(graph[from]) { "The starting point has not been added to the graph." }
        requireNotNull(graph[to]) { "The ending point has not been added to the graph." }
        graph[from]?.addEdge(to, roadName, roadType, length)
        edgeList.add(MapEdge(from, to, roadName, roadType, length))
    }

    /**
     * Find the path from start to goal using breadth-first search.
     * @param start The starting location.
     * @param goal The goal location.
     * @return The list of intersections that form the shortest (unweighted) path from start to goal (including both start and goal).
     */
    fun bfs(
        start: GeographicPoint,
        goal: GeographicPoint,
    ): List<GeographicPoint>? {
        val temp: Consumer<GeographicPoint> = Consumer { }
        return bfs(start, goal, temp)
    }

    /**
     * Find the path from start to goal using breadth-first search.
     * @param start The starting location.
     * @param goal The goal location.
     * @param nodeSearched A hook for visualization.
     * @return The list of intersections that form the shortest (unweighted) path from start to goal (including both start and goal).
     */
    fun bfs(
        start: GeographicPoint,
        goal: GeographicPoint,
        nodeSearched: Consumer<GeographicPoint>,
    ): List<GeographicPoint>? {
        requireNotNull(start) { "Start location cannot be null." }
        requireNotNull(goal) { "Goal location cannot be null." }

        val parentMap = HashMap<MapNode, MapNode>()
        val toBeVisited = PriorityQueue<MapNode>()
        val visited = HashSet<MapNode>()
        val startNode = graph[start]
        val goalNode = graph[goal]

        if (startNode == null || goalNode == null) {
            println("Null point for start or goal, please check!")
            return null
        }

        toBeVisited.add(startNode)
        nodeSearched.accept(startNode.location)
        var currNode: MapNode? = null

        while (toBeVisited.isNotEmpty()) {
            currNode = toBeVisited.remove()
            nodeSearched.accept(currNode.location)
            if (currNode == goalNode) {
                break
            }
            for (e in currNode.edgeList) {
                val tempNode = graph[e.to]
                if (tempNode != null && !visited.contains(tempNode)) {
                    visited.add(tempNode)
                    toBeVisited.add(tempNode)
                    parentMap[tempNode] = currNode
                }
            }
        }

        return if (currNode != goalNode) {
            null
        } else {
            buildPath(parentMap, goalNode, startNode)
        }
    }

    private fun buildPath(
        map: HashMap<MapNode, MapNode>,
        goal: MapNode,
        start: MapNode,
    ): List<GeographicPoint> {
        val route = LinkedList<GeographicPoint>()
        var currNode: MapNode? = goal
        while (currNode != start) {
            route.addFirst(currNode!!.location)
            currNode = map[currNode]
        }
        route.addFirst(start.location)
        return route
    }

    /**
     * Find the path from start to goal using Dijkstra's algorithm.
     * @param start The starting location.
     * @param goal The goal location.
     * @return The list of intersections that form the shortest path from start to goal (including both start and goal).
     */
    fun dijkstra(
        start: GeographicPoint,
        goal: GeographicPoint,
    ): List<GeographicPoint>? {
        val temp: Consumer<GeographicPoint> = Consumer { }
        return dijkstra(start, goal, temp)
    }

    /**
     * Find the path from start to goal using Dijkstra's algorithm.
     * @param start The starting location.
     * @param goal The goal location.
     * @param nodeSearched A hook for visualization.
     * @return The list of intersections that form the shortest path from start to goal (including both start and goal).
     */
    fun dijkstra(
        start: GeographicPoint,
        goal: GeographicPoint,
        nodeSearched: Consumer<GeographicPoint>,
    ): List<GeographicPoint>? {
        val priQueue = PriorityQueue<MapNode>()
        val visited = HashSet<MapNode>()
        val parentMap = HashMap<MapNode, MapNode>()
        initLengthInfinite()

        val startNode = graph[start] ?: return null
        val goalNode = graph[goal] ?: return null
        startNode.length = 0.0
        priQueue.add(startNode)

        while (priQueue.isNotEmpty()) {
            val current = priQueue.remove()
            if (!visited.contains(current)) {
                nodeSearched.accept(current.location)
                visited.add(current)
                if (current == goalNode) {
                    return buildPath(parentMap, goalNode, startNode)
                }
                for (n in current.edgeList) {
                    val temp = graph[n.to] ?: continue
                    if (!visited.contains(temp) && temp.length > n.distance + current.length) {
                        temp.length = n.distance + current.length
                        parentMap[temp] = current
                        priQueue.add(temp)
                    }
                }
            }
        }

        return null
    }

    private fun initLengthInfinite() {
        for (n in graph.values) {
            n.length = Double.MAX_VALUE
            n.predictedLength = Double.MAX_VALUE
        }
    }

    /**
     * Find the path from start to goal using A-Star search.
     * @param start The starting location.
     * @param goal The goal location.
     * @return The list of intersections that form the shortest path from start to goal (including both start and goal).
     */
    fun aStarSearch(
        start: GeographicPoint,
        goal: GeographicPoint,
    ): List<GeographicPoint>? {
        val temp: Consumer<GeographicPoint> = Consumer { }
        return aStarSearch(start, goal, temp)
    }

    /**
     * Find the path from start to goal using A-Star search.
     * @param start The starting location.
     * @param goal The goal location.
     * @param nodeSearched A hook for visualization.
     * @return The list of intersections that form the shortest path from start to goal (including both start and goal).
     */
    fun aStarSearch(
        start: GeographicPoint,
        goal: GeographicPoint,
        nodeSearched: Consumer<GeographicPoint>,
    ): List<GeographicPoint>? {
        val priQueue = PriorityQueue<MapNode>()
        val visited = HashSet<MapNode>()
        val parentMap = HashMap<MapNode, MapNode>()
        initLengthInfinite()

        val startNode = graph[start] ?: return null
        val goalNode = graph[goal] ?: return null
        startNode.length = 0.0
        priQueue.add(startNode)

        while (priQueue.isNotEmpty()) {
            val current = priQueue.remove()
            if (!visited.contains(current)) {
                nodeSearched.accept(current.location)
                visited.add(current)
                if (current == goalNode) {
                    return buildPath(parentMap, goalNode, startNode)
                }
                for (n in current.edgeList) {
                    val temp = graph[n.to] ?: continue
                    val distance = predictLength(temp, goalNode)
                    if (!visited.contains(temp) && temp.length > n.distance + current.length + distance) {
                        temp.length = n.distance + current.length + distance
                        parentMap[temp] = current
                        priQueue.add(temp)
                    }
                }
            }
        }

        return null
    }

    private fun predictLength(
        start: MapNode,
        goal: MapNode,
    ): Double = start.location.distance(goal.location)

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println("Making a new map...")
            val firstMap = MapGraph()
            println("DONE. \nLoading the map...")
            GraphLoader.loadRoadMap("data/testdata/simpletest.map", firstMap)
            println("DONE.")

            // You can use this method for testing.
            // Uncomment the below code to test
            /*
            val simpleTestMap = MapGraph()
            GraphLoader.loadRoadMap("data/testdata/simpletest.map", simpleTestMap)

            val testStart = GeographicPoint(1.0, 1.0)
            val testEnd = GeographicPoint(8.0, -1.0)

            println("Test 1 using simpletest: Dijkstra should be 9 and AStar should be 5")
            val testroute = simpleTestMap.dijkstra(testStart, testEnd)
            val testroute2 = simpleTestMap.aStarSearch(testStart, testEnd)

            val testMap = MapGraph()
            GraphLoader.loadRoadMap("data/maps/utc.map", testMap)

            // A very simple test using real data
            val testStart2 = GeographicPoint(32.869423, -117.220917)
            val testEnd2 = GeographicPoint(32.869255, -117.216927)
            println("Test 2 using utc: Dijkstra should be 13 and AStar should be 5")
            val testroute3 = testMap.dijkstra(testStart2, testEnd2)
            val testroute4 = testMap.aStarSearch(testStart2, testEnd2)

            // A slightly more complex test using real data
            val testStart3 = GeographicPoint(32.8674388, -117.2190213)
            val testEnd3 = GeographicPoint(32.8697828, -117.2244506)
            println("Test 3 using utc: Dijkstra should be 37 and AStar should be 10")
            val testroute5 = testMap.dijkstra(testStart3, testEnd3)
            val testroute6 = testMap.aStarSearch(testStart3, testEnd3)
             */
        }
    }
}
