package com.martishin.graphsonmaps.basicgraph

import com.martishin.graphsonmaps.util.GraphLoader
import java.io.BufferedReader
import java.io.FileReader

class GraphGrader {
    private var feedback: String = ""
    private var correct: Int = 0

    companion object {
        private const val TESTS = 16

        /**
         * Turn a list into a readable and printable string
         * @param lst  The list to process
         * @return The list items formatted as a printable string
         */
        fun printList(lst: List<Int>): String = lst.joinToString("-")

        /**
         * Format readable feedback
         * @param score  The score received
         * @param feedback  The feedback message
         * @return A string where the feedback and score are formatted nicely
         */
        fun printOutput(
            score: Double,
            feedback: String,
        ): String = "Score: $score\nFeedback: $feedback"

        /**
         * Format test number and description
         * @param num  The test number
         * @param test The test description
         * @return A String with the test number and description neatly formatted.
         */
        fun appendFeedback(
            num: Int,
            test: String,
        ): String = "\n** Test #$num: $test..."

        /**
         * Run the grader
         * @param args Doesn't use command line parameters
         */
        @JvmStatic
        fun main(args: Array<String>) {
            val grader = GraphGrader()
            grader.run()
        }
    }

    /** Run a test case on an adjacency list and adjacency matrix.
     * @param i The graph number
     * @param desc A description of the graph
     * @param start The node to start from
     * @param corr A list containing the correct answer
     */
    fun runTest(
        i: Int,
        desc: String,
        start: Int,
        corr: List<Int>,
    ) {
        val lst = GraphAdjList()
        val mat = GraphAdjMatrix()

        feedback += "\n\nGRAPH: $desc"
        feedback += appendFeedback(i * 2 - 1, "Testing adjacency list")

        // Load the graph, get the user's answer, and compare with the right answer
        GraphLoader.loadGraph("data/graders/mod1/graph$i.txt", lst)
        var result = lst.getDistance2(start)
        judge(result, corr)

        feedback += appendFeedback(i * 2, "Testing adjacency matrix")
        GraphLoader.loadGraph("data/graders/mod1/graph$i.txt", mat)
        result = mat.getDistance2(start)
        judge(result, corr)
    }

    /** Run a road map/airplane route test case.
     * @param i The graph number
     * @param file The file to read the correct answer from
     * @param desc A description of the graph
     * @param start The node to start from
     * @param corr A list containing the correct answer
     * @param type The type of graph to use
     */
    fun runSpecialTest(
        i: Int,
        file: String,
        desc: String,
        start: Int,
        corr: List<Int>,
        type: String,
    ) {
        val lst = GraphAdjList()
        val mat = GraphAdjMatrix()

        val prefix = "data/graders/mod1/"

        feedback += "\n\n$desc"
        feedback += appendFeedback(i * 2 - 1, "Testing adjacency list")

        // Different method calls for different graph types
        when (type) {
            "road" -> {
                GraphLoader.loadRoadMap(prefix + file, lst)
                GraphLoader.loadRoadMap(prefix + file, mat)
            }
            "air" -> {
                GraphLoader.loadRoutes(prefix + file, lst)
                GraphLoader.loadRoutes(prefix + file, mat)
            }
        }

        var result = lst.getDistance2(start)
        judge(result, corr)

        feedback += appendFeedback(i * 2, "Testing adjacency matrix")
        result = mat.getDistance2(start)
        judge(result, corr)
    }

    /** Compare the user's result with the right answer.
     * @param result The list with the user's result
     * @param corr The list with the correct answer
     */
    fun judge(
        result: List<Int>?,
        corr: List<Int>,
    ) {
        // Correct answer if both lists contain the same elements
        when {
            result == null -> feedback += "FAILED. Result returned was NULL. "
            result.size != corr.size || !result.containsAll(corr) -> {
                feedback += "FAILED. Expected ${printList(corr)}, got ${printList(result)}. "
                if (result.size > corr.size) {
                    feedback += "Make sure you aren't including vertices of distance 1. "
                }
                if (result.size < corr.size) {
                    feedback += "Make sure you're exploring all possible paths. "
                }
            }
            else -> {
                feedback += "PASSED."
                correct++
            }
        }
    }

    /** Read a correct answer from a file.
     * @param file The file to read from
     * @return A list containing the correct answer
     */
    fun readCorrect(file: String): ArrayList<Int> {
        val ret = ArrayList<Int>()
        try {
            BufferedReader(FileReader("data/graders/mod1/$file")).use { br ->
                var next: String?
                while (br.readLine().also { next = it } != null) {
                    ret.add(next!!.toInt())
                }
            }
        } catch (e: Exception) {
            feedback += "\nCould not open answer file! Please submit a bug report."
        }
        return ret
    }

    /** Run the grader */
    fun run() {
        feedback = ""
        correct = 0

        try {
            var correctAns = arrayListOf(7)
            runTest(1, "Straight line (0->1->2->3->...)", 5, correctAns)

            correctAns = arrayListOf(4, 6, 6, 8)
            runTest(2, "Undirected straight line (0<->1<->2<->3<->...)", 6, correctAns)

            correctAns = ArrayList<Int>().apply { repeat(9) { add(0) } }
            runTest(3, "Star graph - 0 is connected in both directions to all nodes except itself (starting at 0)", 0, correctAns)

            correctAns = arrayListOf<Int>().apply { for (i in 1 until 10) add(i) }
            runTest(4, "Star graph (starting at 5)", 5, correctAns)

            correctAns = arrayListOf<Int>().apply { for (i in 6 until 11) add(i) }
            runTest(5, "Star graph - Each 'arm' consists of two undirected edges leading away from 0 (starting at 0)", 0, correctAns)

            correctAns = ArrayList()
            runTest(6, "Same graph as before (starting at 5)", 5, correctAns)

            correctAns = readCorrect("ucsd.map.twoaway")
            runSpecialTest(7, "ucsd.map", "UCSD MAP: Intersections around UCSD", 3, correctAns, "road")

            correctAns = readCorrect("routesUA.dat.twoaway")
            runSpecialTest(8, "routesUA.dat", "AIRLINE MAP: Airplane routes around the world", 6, correctAns, "air")

            feedback =
                if (correct == TESTS) {
                    "All tests passed. Great job!$feedback"
                } else {
                    "Some tests failed. Check your code for errors, then try again:$feedback"
                }
        } catch (e: Exception) {
            feedback += "\nError during runtime: $e"
            e.printStackTrace()
        }

        println(printOutput(correct.toDouble() / TESTS, feedback))
    }
}
