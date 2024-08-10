package com.martishin.graphsonmaps.basicgraph

import com.martishin.graphsonmaps.util.GraphLoader

abstract class Graph {
    private var numVertices: Int = 0
    private var numEdges: Int = 0
    private var vertexLabels: MutableMap<Int, String>? = null

    /**
     * Create a new empty Graph
     */
    init {
        numVertices = 0
        numEdges = 0
        vertexLabels = null
    }

    /**
     * Report size of vertex set
     * @return The number of vertices in the graph.
     */
    fun getNumVertices(): Int = numVertices

    /**
     * Report size of edge set
     * @return The number of edges in the graph.
     */
    fun getNumEdges(): Int = numEdges

    /**
     * Add new vertex to the graph.  This vertex will
     * have as its index the next available integer.
     * Precondition: contiguous integers are used to
     * index vertices.
     * @return index of newly added vertex
     */
    fun addVertex(): Int {
        implementAddVertex()
        numVertices++
        return numVertices - 1
    }

    /**
     * Abstract method implementing adding a new
     * vertex to the representation of the graph.
     */
    abstract fun implementAddVertex()

    /**
     * Add new edge to the graph between given vertices,
     * @param v Index of the start point of the edge to be added.
     * @param w Index of the end point of the edge to be added.
     */
    fun addEdge(
        v: Int,
        w: Int,
    ) {
        numEdges++
        if (v < numVertices && w < numVertices) {
            implementAddEdge(v, w)
        } else {
            throw IndexOutOfBoundsException()
        }
    }

    /**
     * Abstract method implementing adding a new
     * edge to the representation of the graph.
     */
    abstract fun implementAddEdge(
        v: Int,
        w: Int,
    )

    /**
     * Get all (out-)neighbors of a given vertex.
     * @param v Index of vertex in question.
     * @return List of indices of all vertices that are adjacent to v
     *  via outgoing edges from v.
     */
    abstract fun getNeighbors(v: Int): List<Int>

    /**
     * Get all in-neighbors of a given vertex.
     * @param v Index of vertex in question.
     * @return List of indices of all vertices that are adjacent to v
     *  via incoming edges to v.
     */
    abstract fun getInNeighbors(v: Int): List<Int>

    /**
     * The degree sequence of a graph is a sorted (organized in numerical order
     * from largest to smallest, possibly with repetitions) list of the degrees
     * of the vertices in the graph.
     *
     * @return The degree sequence of this graph.
     */
    fun degreeSequence(): List<Int> {
        val degreeSeq = mutableListOf<Int>()
        for (v in 0 until getNumVertices()) {
            degreeSeq.add(getInNeighbors(v).size + getNeighbors(v).size)
        }
        return degreeSeq.sortedDescending() // largest to smallest
    }

    /**
     * Get all the vertices that are 2 away from the vertex in question.
     * @param v The starting vertex
     * @return A list of the vertices that can be reached in exactly two hops (by
     * following two edges) from vertex v.
     */
    abstract fun getDistance2(v: Int): List<Int>

    /**
     * Return a String representation of the graph
     * @return A string representation of the graph
     */
    override fun toString(): String {
        var s = "\nGraph with $numVertices vertices and $numEdges edges.\n"
        s += "Degree sequence: ${degreeSequence()}.\n"
        if (numVertices <= 20) s += adjacencyString()
        return s
    }

    /**
     * Generate string representation of adjacency list
     * @return the String
     */
    abstract fun adjacencyString(): String

    // The next methods implement labeled vertices.
    // Basic graphs may or may not have labeled vertices.

    /**
     * Create a new map of vertex indices to string labels
     * (Optional: only if using labeled vertices.)
     */
    fun initializeLabels() {
        vertexLabels = mutableMapOf()
    }

    /**
     * Test whether some vertex in the graph is labeled
     * with a given index.
     * @param v The index being checked
     * @return True if there's a vertex in the graph with this index; false otherwise.
     */
    fun hasVertex(v: Int): Boolean = v < getNumVertices()

    /**
     * Test whether some vertex in the graph is labeled
     * with a given String label
     * @param s The String label being checked
     * @return True if there's a vertex in the graph with this label; false otherwise.
     */
    fun hasVertex(s: String): Boolean = vertexLabels?.containsValue(s) ?: false

    /**
     * Add label to an unlabeled vertex in the graph.
     * @param v The index of the vertex to be labeled.
     * @param s The label to be assigned to this vertex.
     */
    fun addLabel(
        v: Int,
        s: String,
    ) {
        if (v < getNumVertices() && !(vertexLabels?.containsKey(v) ?: false)) {
            vertexLabels?.put(v, s)
        } else {
            println("ERROR: tried to label a vertex that is out of range or already labeled")
        }
    }

    /**
     * Report label of vertex with given index
     * @param v The integer index of the vertex
     * @return The String label of this vertex
     */
    fun getLabel(v: Int): String? = vertexLabels?.get(v)

    /**
     * Report index of vertex with given label.
     * (Assume distinct labels for vertices.)
     * @param s The String label of the vertex
     * @return The integer index of this vertex
     */
    fun getIndex(s: String): Int? {
        for ((key, value) in vertexLabels!!) {
            if (value == s) {
                return key
            }
        }
        println("ERROR: No vertex with this label")
        return null
    }

    /** Main method provided with some basic tests.  */
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            GraphLoader.createIntersectionsFile("data/maps/ucsd.map", "data/intersections/ucsd.intersections")

            // For testing of Part 1 functionality
            println("Loading graphs based on real data...")
            println("Goal: use degree sequence to analyze graphs.")

            println("****")
            println("Roads / intersections:")
            val graphFromFile = GraphAdjList()
            GraphLoader.loadRoadMap("data/testdata/simpletest.map", graphFromFile)
            println(graphFromFile)

            println("Observe all degrees are <= 12.")
            println("****")

            println("\n****")

            println("Flight data:")
            val airportGraph = GraphAdjList()
            GraphLoader.loadRoutes("data/airports/routesUA.dat", airportGraph)
            println(airportGraph)
            println("Observe most degrees are small (1-30), eight are over 100.")
            println("****")

            // For testing Part 2 functionality
            println("Testing distance-two methods on sample graphs...")
            println("Goal: implement method using two approaches.")
        }
    }
}
