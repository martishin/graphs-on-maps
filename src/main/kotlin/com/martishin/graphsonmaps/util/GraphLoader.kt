package com.martishin.graphsonmaps.util

import com.martishin.graphsonmaps.basicgraph.Graph
import com.martishin.graphsonmaps.geography.GeographicPoint
import com.martishin.graphsonmaps.geography.RoadSegment
import com.martishin.graphsonmaps.roadgraph.MapGraph
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.io.PrintWriter
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.HashMap

object GraphLoader {
    /**
     * The file contains data lines as follows:
     * lat1 lon1 lat2 lon2 roadName roadType
     *
     * where each line is a segment of a road
     * These road segments are assumed to be ONE WAY.
     *
     * This method will collapse the points so that only intersections
     * are represented as nodes in the graph.
     *
     * @param roadDataFile The file containing the road data, in the format
     *   described.
     * @param intersectionsFile The output file containing the intersections.
     */
    @JvmStatic
    fun createIntersectionsFile(
        roadDataFile: String,
        intersectionsFile: String,
    ) {
        val nodes = HashSet<GeographicPoint>()
        val pointMap = buildPointMapOneWay(roadDataFile)

        // Print the intersections to the file
        val intersections = findIntersections(pointMap)
        nodes.addAll(intersections)

        try {
            val writer = PrintWriter(intersectionsFile, "UTF-8")

            // Now we need to add the edges
            // This is the tricky part
            for (pt in nodes) {
                // Trace the node to its next node, building up the points
                // on the edge as you go.
                val inAndOut = pointMap[pt] ?: continue
                val outgoing = inAndOut[0]
                for (info in outgoing) {
                    val pointsOnEdge = findPointsOnEdge(pointMap, info, nodes)
                    val end = pointsOnEdge.removeAt(pointsOnEdge.size - 1)
                    writer.println("$pt $end")
                }
            }
            writer.flush()
            writer.close()
        } catch (e: Exception) {
            println("Exception opening intersections file $e")
        }
    }

    /**
     * Read in a file specifying a map.
     *
     * The file contains data lines as follows:
     * lat1 lon1 lat2 lon2 roadName roadType
     *
     * where each line is a segment of a road
     * These road segments are assumed to be ONE WAY.
     *
     * This method will collapse the points so that only intersections
     * are represented as nodes in the graph.
     *
     * @param filename The file containing the road data, in the format
     *   described.
     * @param map The graph to load the map into.  The graph is
     *   assumed to be directed.
     */
    @JvmStatic
    fun loadRoadMap(
        filename: String,
        map: MapGraph,
    ) {
        loadRoadMap(filename, map, null, null)
    }

    @JvmStatic
    fun loadRoadMap(
        filename: String,
        map: MapGraph,
        segments: HashMap<GeographicPoint, MutableSet<RoadSegment>>?,
        intersectionsToLoad: MutableSet<GeographicPoint>?,
    ) {
        val nodes = HashSet<GeographicPoint>()
        val pointMap = buildPointMapOneWay(filename)

        // Add the nodes to the graph
        val intersections = findIntersections(pointMap)
        for (pt in intersections) {
            map.addVertex(pt)
            intersectionsToLoad?.add(pt)
            nodes.add(pt)
        }

        addEdgesAndSegments(nodes, pointMap, map, segments)
    }

    @JvmStatic
    fun loadRoadMap(
        filename: String,
        theGraph: Graph,
    ) {
        val pointMap = buildPointMapOneWay(filename)

        val vertexMap = HashMap<Int, GeographicPoint>()
        val reverseMap = HashMap<GeographicPoint, Int>()

        // Add the nodes to the graph
        val intersections = findIntersections(pointMap)

        var index = 0
        for (pt in intersections) {
            theGraph.addVertex()
            vertexMap[index] = pt
            reverseMap[pt] = index
            index++
        }

        // Now add the edges
        for (nodeNum in vertexMap.keys) {
            // Trace the node to its next node, building up the points
            // on the edge as you go.
            val pt = vertexMap[nodeNum] ?: continue
            val inAndOut = pointMap[pt] ?: continue
            val infoList = inAndOut[0]
            for (info in infoList) {
                val end = findEndOfEdge(pointMap, info, theGraph, reverseMap)
                val endNum = reverseMap[end] ?: continue
                theGraph.addEdge(nodeNum, endNum)
            }
        }
    }

    @JvmStatic
    fun loadRoutes(
        filename: String,
        graph: Graph,
    ) {
        var lineCount = 0 // for debugging

        // Initialize vertex label HashMap in graph
        graph.initializeLabels()

        // Read in flights from file
        try {
            BufferedReader(FileReader(filename)).use { reader ->
                var nextLine: String?
                while (reader.readLine().also { nextLine = it } != null) {
                    val flightInfo = nextLine?.split(",") ?: continue
                    val source = flightInfo[2]
                    val destination = flightInfo[4]

                    // Add edge for this flight, if both source & destination are already vertices.
                    // If one of these airports is missing, add vertex for it and then place edge.
                    val sourceIndex = graph.getIndex(source) ?: graph.addVertex().also { graph.addLabel(it, source) }
                    val destinationIndex =
                        graph.getIndex(destination) ?: graph.addVertex().also { graph.addLabel(it, destination) }
                    graph.addEdge(sourceIndex, destinationIndex)
                    lineCount++
                }
            }
        } catch (e: IOException) {
            System.err.println("Problem loading route file: $filename")
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun loadGraph(
        filename: String,
        theGraph: Graph,
    ) {
        try {
            BufferedReader(FileReader(filename)).use { reader ->
                var nextLine = reader.readLine() ?: throw IOException("Graph file is empty!")
                val numVertices = nextLine.toInt()
                for (i in 0 until numVertices) {
                    theGraph.addVertex()
                }

                while (reader.readLine().also { nextLine = it } != null) {
                    val verts = nextLine.split(" ") ?: continue
                    val start = verts[0].toInt()
                    val end = verts[1].toInt()
                    theGraph.addEdge(start, end)
                }
            }
        } catch (e: IOException) {
            System.err.println("Problem loading graph file: $filename")
            e.printStackTrace()
        }
    }

    private fun addEdgesAndSegments(
        nodes: Collection<GeographicPoint>,
        pointMap: HashMap<GeographicPoint, List<LinkedList<RoadLineInfo>>>,
        map: MapGraph,
        segments: HashMap<GeographicPoint, MutableSet<RoadSegment>>?,
    ) {
        for (pt in nodes) {
            val inAndOut = pointMap[pt] ?: continue
            val outgoing = inAndOut[0]
            for (info in outgoing) {
                val pointsOnEdge = findPointsOnEdge(pointMap, info, nodes)
                val end = pointsOnEdge.removeAt(pointsOnEdge.size - 1)
                val length = getRoadLength(pt, end, pointsOnEdge)
                map.addEdge(pt, end, info.roadName, info.roadType, length)

                if (segments != null) {
                    var segs = segments[pt]
                    if (segs == null) {
                        segs = HashSet()
                        segments[pt] = segs
                    }
                    val seg = RoadSegment(pt, end, pointsOnEdge, info.roadName, info.roadType, length)
                    segs.add(seg)
                    segs = segments[end]
                    if (segs == null) {
                        segs = HashSet()
                        segments[end] = segs
                    }
                    segs.add(seg)
                }
            }
        }
    }

    private fun getRoadLength(
        start: GeographicPoint,
        end: GeographicPoint,
        path: List<GeographicPoint>,
    ): Double {
        var dist = 0.0
        var curr = start
        for (next in path) {
            dist += curr.distance(next)
            curr = next
        }
        dist += curr.distance(end)
        return dist
    }

    private fun findPointsOnEdge(
        pointMap: HashMap<GeographicPoint, List<LinkedList<RoadLineInfo>>>,
        info: RoadLineInfo,
        nodes: Collection<GeographicPoint>,
    ): MutableList<GeographicPoint> {
        val toReturn = LinkedList<GeographicPoint>()
        var pt = info.point1
        var end = info.point2
        var nextInAndOut = pointMap[end] ?: return toReturn
        var nextLines = nextInAndOut[0]
        while (!nodes.contains(end)) {
            toReturn.add(end)
            var nextInfo = nextLines[0]
            if (nextLines.size == 2) {
                if (nextInfo.point2 == pt) {
                    nextInfo = nextLines[1]
                }
            } else if (nextLines.size != 1) {
                println("Something went wrong building edges")
            }
            pt = end
            end = nextInfo.point2
            nextInAndOut = pointMap[end] ?: return toReturn
            nextLines = nextInAndOut[0]
        }
        toReturn.add(end)

        return toReturn
    }

    private fun findEndOfEdge(
        pointMap: HashMap<GeographicPoint, List<LinkedList<RoadLineInfo>>>,
        info: RoadLineInfo,
        graph: Graph,
        reverseMap: HashMap<GeographicPoint, Int>,
    ): GeographicPoint {
        var pt = info.point1
        var end = info.point2
        var endNum = reverseMap[end]
        while (endNum == null) {
            val inAndOut = pointMap[end] ?: return end
            var nextLines = inAndOut[0]
            var nextInfo = nextLines[0]
            if (nextLines.size == 2) {
                if (nextInfo.point2 == pt) {
                    nextInfo = nextLines[1]
                }
            } else if (nextLines.size != 1) {
                println("Something went wrong building edges")
            }
            pt = end
            end = nextInfo.point2
            endNum = reverseMap[end]
        }

        return end
    }

    private fun findIntersections(pointMap: HashMap<GeographicPoint, List<LinkedList<RoadLineInfo>>>): List<GeographicPoint> {
        val intersections = LinkedList<GeographicPoint>()
        for (pt in pointMap.keys) {
            val roadsInAndOut = pointMap[pt] ?: continue
            val roadsOut = roadsInAndOut[0]
            val roadsIn = roadsInAndOut[1]

            var isNode = true

            if (roadsIn.size == 1 && roadsOut.size == 1) {
                if (!(roadsIn[0].point1 == roadsOut[0].point2 && roadsIn[0].point2 == roadsOut[0].point1) &&
                    roadsIn[0].roadName == roadsOut[0].roadName
                ) {
                    isNode = false
                }
            }
            if (roadsIn.size == 2 && roadsOut.size == 2) {
                val name = roadsIn[0].roadName
                var sameName = true
                for (info in roadsIn) {
                    if (info.roadName != name) {
                        sameName = false
                    }
                }
                for (info in roadsOut) {
                    if (info.roadName != name) {
                        sameName = false
                    }
                }

                val in1 = roadsIn[0]
                val in2 = roadsIn[1]
                val out1 = roadsOut[0]
                val out2 = roadsOut[1]

                val passThrough =
                    (in1.isReverse(out1) && in2.isReverse(out2)) ||
                        (in1.isReverse(out2) && in2.isReverse(out1))

                if (sameName && passThrough) {
                    isNode = false
                }
            }
            if (isNode) {
                intersections.add(pt)
            }
        }
        return intersections
    }

    private fun buildPointMapOneWay(filename: String): HashMap<GeographicPoint, List<LinkedList<RoadLineInfo>>> {
        val pointMap = HashMap<GeographicPoint, List<LinkedList<RoadLineInfo>>>()
        try {
            BufferedReader(FileReader(filename)).use { reader ->
                var nextLine: String?
                while (reader.readLine().also { nextLine = it } != null) {
                    val line = splitInputString(nextLine ?: "")
                    addToPointsMapOneWay(line, pointMap)
                }
            }
        } catch (e: IOException) {
            System.err.println("Problem loading dictionary file: $filename")
            e.printStackTrace()
        }

        return pointMap
    }

    private fun addToPointsMapOneWay(
        line: RoadLineInfo,
        map: HashMap<GeographicPoint, List<LinkedList<RoadLineInfo>>>,
    ) {
        val pt1Infos = map.getOrPut(line.point1) { listOf(LinkedList(), LinkedList()) }
        pt1Infos[0].add(line)

        val pt2Infos = map.getOrPut(line.point2) { listOf(LinkedList(), LinkedList()) }
        pt2Infos[1].add(line)
    }

    private fun splitInputString(input: String): RoadLineInfo {
        val tokens = ArrayList<String>()
        val tokSplitter = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"")
        val m = tokSplitter.matcher(input)

        while (m.find()) {
            tokens.add(m.group(1) ?: m.group())
        }

        val lat1 = tokens[0].toDouble()
        val lon1 = tokens[1].toDouble()
        val lat2 = tokens[2].toDouble()
        val lon2 = tokens[3].toDouble()
        val p1 = GeographicPoint(lat1, lon1)
        val p2 = GeographicPoint(lat2, lon2)

        return RoadLineInfo(p1, p2, tokens[4], tokens[5])
    }

    @JvmStatic
    fun main(args: Array<String>) {
        createIntersectionsFile("data/maps/hollywood_small.map", "data/intersections/hollywood_small.intersections")
        createIntersectionsFile("data/maps/new_york.map", "data/intersections/new_york.intersections")
        createIntersectionsFile("data/maps/san_diego.map", "data/intersections/san_diego.intersections")
        createIntersectionsFile("data/maps/ucsd.map", "data/intersections/ucsd.intersections")

        // To use this method to convert your custom map files to custom intersections files
        // just change YOURFILE in the strings below to be the name of the file you saved.
        // You can comment out the other method calls above to save time.
        createIntersectionsFile("data/maps/YOURFILE.map", "data/intersections/YOURFILE.intersections")
    }
}

// A class to store information about the lines in the road files.
data class RoadLineInfo(
    val point1: GeographicPoint,
    val point2: GeographicPoint,
    val roadName: String,
    val roadType: String,
) {
    fun getOtherPoint(pt: GeographicPoint?): GeographicPoint =
        when {
            pt == point1 -> point2
            pt == point2 -> point1
            else -> throw IllegalArgumentException()
        }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is RoadLineInfo) {
            return false
        }
        return other.point1 == this.point1 &&
            other.point2 == this.point2 &&
            other.roadType == this.roadType &&
            other.roadName == this.roadName
    }

    override fun hashCode(): Int = point1.hashCode() + point2.hashCode()

    fun sameRoad(info: RoadLineInfo): Boolean = info.roadName == this.roadName && info.roadType == this.roadType

    fun getReverseCopy(): RoadLineInfo = RoadLineInfo(this.point2, this.point1, this.roadName, this.roadType)

    fun isReverse(other: RoadLineInfo): Boolean =
        this.point1 == other.point2 &&
            this.point2 == other.point1 &&
            this.roadName == other.roadName &&
            this.roadType == other.roadType

    override fun toString(): String = "$point1 $point2 $roadName $roadType"
}
