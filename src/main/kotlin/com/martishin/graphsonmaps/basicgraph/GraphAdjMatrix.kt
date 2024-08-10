package com.martishin.graphsonmaps.basicgraph

class GraphAdjMatrix : Graph() {
    private val defaultNumVertices = 5
    private var adjMatrix: Array<IntArray> = Array(defaultNumVertices) { IntArray(defaultNumVertices) }

    /**
     * Implement the abstract method for adding a vertex.
     * If need to increase dimensions of matrix, double them
     * to amortize cost.
     */
    override fun implementAddVertex() {
        val v = getNumVertices()
        if (v >= adjMatrix.size) {
            val newAdjMatrix = Array(v * 2) { IntArray(v * 2) }
            for (i in adjMatrix.indices) {
                for (j in adjMatrix[i].indices) {
                    newAdjMatrix[i][j] = adjMatrix[i][j]
                }
            }
            adjMatrix = newAdjMatrix
        }
    }

    /**
     * Implement the abstract method for adding an edge.
     * Allows for multiple edges between two points:
     * the entry at row v, column w stores the number of such edges.
     * @param v the index of the start point for the edge.
     * @param w the index of the end point for the edge.
     */
    override fun implementAddEdge(
        v: Int,
        w: Int,
    ) {
        adjMatrix[v][w] += 1
    }

    /**
     * Implement the abstract method for finding all
     * out-neighbors of a vertex.
     * If there are multiple edges between the vertex
     * and one of its out-neighbors, this neighbor
     * appears once in the list for each of these edges.
     *
     * @param v the index of vertex.
     * @return List<Integer> a list of indices of vertices.
     */
    override fun getNeighbors(v: Int): List<Int> {
        val neighbors = mutableListOf<Int>()
        for (i in 0 until getNumVertices()) {
            for (j in 0 until adjMatrix[v][i]) {
                neighbors.add(i)
            }
        }
        return neighbors
    }

    /**
     * Implement the abstract method for finding all
     * in-neighbors of a vertex.
     * If there are multiple edges from another vertex
     * to this one, the neighbor
     * appears once in the list for each of these edges.
     *
     * @param v the index of vertex.
     * @return List<Integer> a list of indices of vertices.
     */
    override fun getInNeighbors(v: Int): List<Int> {
        val inNeighbors = mutableListOf<Int>()
        for (i in 0 until getNumVertices()) {
            for (j in 0 until adjMatrix[i][v]) {
                inNeighbors.add(i)
            }
        }
        return inNeighbors
    }

    /**
     * Implement the abstract method for finding all
     * vertices reachable by two hops from v.
     * Use matrix multiplication to record length 2 paths.
     *
     * @param v the index of vertex.
     * @return List<Integer> a list of indices of vertices.
     */
    override fun getDistance2(v: Int): List<Int> {
        val twoHops = mutableListOf<Int>()
        for (u in getNeighbors(v)) {
            twoHops.addAll(getNeighbors(u))
        }
        return twoHops
    }

    /**
     * Generate string representation of adjacency matrix
     * @return the String
     */
    override fun adjacencyString(): String {
        val dim = getNumVertices()
        var s = "Adjacency matrix (size ${dim}x$dim = ${dim * dim} integers):"
        for (i in 0 until dim) {
            s += "\n\t$i: "
            for (j in 0 until dim) {
                s += "${adjMatrix[i][j]}, "
            }
        }
        return s
    }
}
