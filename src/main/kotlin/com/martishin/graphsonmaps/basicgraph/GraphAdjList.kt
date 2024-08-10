package com.martishin.graphsonmaps.basicgraph

class GraphAdjList : Graph() {
    private val adjListsMap: MutableMap<Int, ArrayList<Int>> = HashMap()

    /**
     * Implement the abstract method for adding a vertex.
     */
    override fun implementAddVertex() {
        val v = getNumVertices()
        val neighbors = ArrayList<Int>()
        adjListsMap[v] = neighbors
    }

    /**
     * Implement the abstract method for adding an edge.
     * @param v the index of the start point for the edge.
     * @param w the index of the end point for the edge.
     */
    override fun implementAddEdge(
        v: Int,
        w: Int,
    ) {
        adjListsMap[v]?.add(w)
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
    override fun getNeighbors(v: Int): List<Int> = ArrayList(adjListsMap[v])

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
        for (u in adjListsMap.keys) {
            // iterate through all edges in u's adjacency list and
            // add u to the inNeighbor list of v whenever an edge
            // with startpoint u has endpoint v.
            for (w in adjListsMap[u]!!) {
                if (v == w) {
                    inNeighbors.add(u)
                }
            }
        }
        return inNeighbors
    }

    /**
     * Implement the abstract method for finding all
     * vertices reachable by two hops from v.
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
     * Generate string representation of adjacency list
     * @return the String
     */
    override fun adjacencyString(): String {
        var s = "Adjacency list (size ${getNumVertices()}+${getNumEdges()} integers):"

        for (v in adjListsMap.keys) {
            s += "\n\t$v: "
            for (w in adjListsMap[v]!!) {
                s += "$w, "
            }
        }
        return s
    }
}
